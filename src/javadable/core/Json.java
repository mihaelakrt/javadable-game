package javadable.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A very small JSON reader and writer, just enough to keep the save file
 * readable by a human. Numbers come back as Double, objects as LinkedHashMap.
 */
public final class Json {

    private Json() {
    }

    // ----------------------------------------------------------------- write

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, 0);
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(Object value, StringBuilder out, int depth) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (map.isEmpty()) {
                out.append("{}");
                return;
            }
            out.append("{\n");
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                indent(out, depth + 1);
                quote(entry.getKey(), out);
                out.append(": ");
                write(entry.getValue(), out, depth + 1);
                if (++i < map.size()) out.append(',');
                out.append('\n');
            }
            indent(out, depth);
            out.append('}');
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            out.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) out.append(", ");
                write(list.get(i), out, depth);
            }
            out.append(']');
        } else if (value instanceof String) {
            quote((String) value, out);
        } else if (value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d)) out.append((long) d);
            else out.append(d);
        } else {
            out.append("null");
        }
    }

    private static void indent(StringBuilder out, int depth) {
        for (int i = 0; i < depth; i++) out.append("  ");
    }

    private static void quote(String text, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') out.append('\\').append(c);
            else if (c == '\n') out.append("\\n");
            else if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
            else out.append(c);
        }
        out.append('"');
    }

    // ----------------------------------------------------------------- parse

    public static Object parse(String text) {
        Parser parser = new Parser(text);
        parser.skipSpace();
        return parser.value();
    }

    private static final class Parser {
        private final String text;
        private int at;

        Parser(String text) {
            this.text = text;
        }

        void skipSpace() {
            while (at < text.length() && Character.isWhitespace(text.charAt(at))) at++;
        }

        Object value() {
            skipSpace();
            if (at >= text.length()) return null;
            char c = text.charAt(at);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (text.startsWith("true", at)) { at += 4; return Boolean.TRUE; }
            if (text.startsWith("false", at)) { at += 5; return Boolean.FALSE; }
            if (text.startsWith("null", at)) { at += 4; return null; }
            return number();
        }

        Map<String, Object> object() {
            Map<String, Object> map = new LinkedHashMap<>();
            at++;
            skipSpace();
            if (at < text.length() && text.charAt(at) == '}') { at++; return map; }
            while (at < text.length()) {
                skipSpace();
                String key = string();
                skipSpace();
                if (at < text.length() && text.charAt(at) == ':') at++;
                map.put(key, value());
                skipSpace();
                if (at < text.length() && text.charAt(at) == ',') { at++; continue; }
                if (at < text.length() && text.charAt(at) == '}') { at++; break; }
                break;
            }
            return map;
        }

        List<Object> array() {
            List<Object> list = new ArrayList<>();
            at++;
            skipSpace();
            if (at < text.length() && text.charAt(at) == ']') { at++; return list; }
            while (at < text.length()) {
                list.add(value());
                skipSpace();
                if (at < text.length() && text.charAt(at) == ',') { at++; continue; }
                if (at < text.length() && text.charAt(at) == ']') { at++; break; }
                break;
            }
            return list;
        }

        String string() {
            StringBuilder out = new StringBuilder();
            at++;
            while (at < text.length()) {
                char c = text.charAt(at++);
                if (c == '"') break;
                if (c == '\\' && at < text.length()) {
                    char escaped = text.charAt(at++);
                    if (escaped == 'n') out.append('\n');
                    else if (escaped == 't') out.append('\t');
                    else if (escaped == 'u') {
                        out.append((char) Integer.parseInt(text.substring(at, at + 4), 16));
                        at += 4;
                    } else out.append(escaped);
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }

        Double number() {
            int start = at;
            while (at < text.length() && "+-0123456789.eE".indexOf(text.charAt(at)) >= 0) at++;
            try {
                return Double.valueOf(text.substring(start, at));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    // --------------------------------------------------------------- helpers

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object value) {
        return value instanceof List ? (List<Object>) value : new ArrayList<Object>();
    }

    public static int getInt(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    public static String getString(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    public static boolean getBoolean(Map<String, Object> map, String key, boolean fallback) {
        Object value = map.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }
}
