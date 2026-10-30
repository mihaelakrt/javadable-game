package javadable.core;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Takes what the player typed, wraps it in a class, compiles it in memory with
 * the real Java compiler and runs it against a fresh canvas.
 */
public class Runner {

    public static final String CLASS_NAME = "JavadableSketch";

    /** Lines of boilerplate added above the player code. */
    private static final int HEADER_LINES = 2;
    private static final long TIME_LIMIT_MS = 2500;

    public static class Result {
        public Canvas canvas;
        public String console = "";
        public String error;
        public boolean compiled;
        public long millis;

        public boolean ok() {
            return error == null;
        }
    }

    public static boolean available() {
        return ToolProvider.getSystemJavaCompiler() != null;
    }

    /**
     * Characters that count towards the budget. Whitespace is free, and so are
     * comments, so the code can be laid out and explained without a penalty.
     */
    public static int characters(String code) {
        if (code == null) return 0;
        int count = 0;
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);

            if (c == '/' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
                int end = code.indexOf('\n', i);
                i = end < 0 ? code.length() : end;
                continue;
            }
            if (c == '/' && i + 1 < code.length() && code.charAt(i + 1) == '*') {
                int end = code.indexOf("*/", i + 2);
                i = end < 0 ? code.length() : end + 2;
                continue;
            }
            if (c == '"' || c == (char) 39) {
                int end = i + 1;
                while (end < code.length() && code.charAt(end) != c) {
                    if (code.charAt(end) == (char) 92) end++;
                    end++;
                }
                end = Math.min(code.length(), end + 1);
                for (int j = i; j < end; j++) {
                    if (!Character.isWhitespace(code.charAt(j))) count++;
                }
                i = end;
                continue;
            }
            if (!Character.isWhitespace(c)) count++;
            i++;
        }
        return count;
    }

    public Result run(String code, int width, int height) {
        Result result = new Result();
        result.canvas = new Canvas(width, height);
        long started = System.currentTimeMillis();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            result.error = "No Java compiler in this runtime. Start the game with a JDK.";
            return result;
        }

        String source = "public final class " + CLASS_NAME + " extends javadable.core.Sketch {\n"
                + "public void draw() {\n"
                + code + "\n"
                + "}\n}\n";

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final Map<String, MemoryClass> classes = new HashMap<>();

        try (StandardJavaFileManager standard = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavaFileManager manager = new ForwardingJavaFileManager<StandardJavaFileManager>(standard) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                           JavaFileObject.Kind kind, FileObject sibling) {
                    MemoryClass file = new MemoryClass(className);
                    classes.put(className, file);
                    return file;
                }
            };

            List<String> options = Arrays.asList(
                    "-classpath", System.getProperty("java.class.path"), "-nowarn", "-proc:none");
            boolean compiled = compiler.getTask(null, manager, diagnostics, options, null,
                    Collections.singletonList(new MemorySource(source))).call();
            result.compiled = compiled;

            if (!compiled) {
                result.error = describe(diagnostics);
                result.millis = System.currentTimeMillis() - started;
                return result;
            }
        } catch (Exception e) {
            result.error = "The compiler gave up: " + e;
            return result;
        }

        try {
            ClassLoader loader = new ClassLoader(Runner.class.getClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    MemoryClass file = classes.get(name);
                    if (file == null) throw new ClassNotFoundException(name);
                    byte[] bytes = file.bytes();
                    return defineClass(name, bytes, 0, bytes.length);
                }
            };
            Class<?> type = loader.loadClass(CLASS_NAME);
            final Sketch sketch = (Sketch) type.getDeclaredConstructor().newInstance();
            sketch.begin(result.canvas);

            final Throwable[] failure = new Throwable[1];
            Thread thread = new Thread(() -> {
                try {
                    sketch.draw();
                } catch (Throwable t) {
                    failure[0] = t;
                }
            }, "javadable-sketch");
            thread.setDaemon(true);
            thread.start();
            thread.join(TIME_LIMIT_MS);

            if (thread.isAlive()) {
                sketch.cancel();
                thread.join(400);
                result.error = "The sketch was still running after 2.5 seconds, so it was stopped.";
            } else if (failure[0] != null) {
                result.error = describe(failure[0]);
            }
            result.console = sketch.console();
        } catch (Throwable t) {
            result.error = "Could not run the sketch: " + t;
        }

        result.millis = System.currentTimeMillis() - started;
        return result;
    }

    private String describe(DiagnosticCollector<JavaFileObject> diagnostics) {
        List<String> lines = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() != Diagnostic.Kind.ERROR) continue;
            long line = diagnostic.getLineNumber() - HEADER_LINES;
            String message = diagnostic.getMessage(null);
            int cut = message.indexOf('\n');
            if (cut > 0) message = message.substring(0, cut);
            lines.add(line > 0 ? "line " + line + ": " + message : message);
            if (lines.size() >= 6) break;
        }
        return lines.isEmpty() ? "The code did not compile." : String.join("\n", lines);
    }

    private String describe(Throwable failure) {
        if (failure instanceof Sketch.Stopped) {
            return "Stopped: " + failure.getMessage();
        }
        int line = -1;
        for (StackTraceElement element : failure.getStackTrace()) {
            if (element.getClassName().startsWith(CLASS_NAME)) {
                line = element.getLineNumber() - HEADER_LINES;
                break;
            }
        }
        String name = failure.getClass().getSimpleName();
        String message = failure.getMessage() == null ? "" : ": " + failure.getMessage();
        return (line > 0 ? "line " + line + ", " : "") + name + message;
    }

    private static final class MemorySource extends SimpleJavaFileObject {
        private final String code;

        MemorySource(String code) {
            super(URI.create("string:///" + CLASS_NAME + ".java"), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private static final class MemoryClass extends SimpleJavaFileObject {
        private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        MemoryClass(String className) {
            super(URI.create("mem:///" + className.replace('.', '/') + ".class"), Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() {
            return stream;
        }

        byte[] bytes() {
            return stream.toByteArray();
        }
    }
}
