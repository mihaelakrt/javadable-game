package javadable.core;

/**
 * The base class every player sketch extends. Whatever is typed in the editor
 * becomes the body of draw(), so these few methods are the whole language the
 * puzzles are solved with.
 */
public abstract class Sketch {

    /** Width and height of the grid, handy for loops. */
    public int W;
    public int H;

    private Canvas canvas;
    private final StringBuilder console = new StringBuilder();
    private volatile boolean cancelled;
    private int steps;

    public final void begin(Canvas canvas) {
        this.canvas = canvas;
        this.W = canvas.width;
        this.H = canvas.height;
    }

    public final void cancel() {
        cancelled = true;
    }

    public final String console() {
        return console.toString();
    }

    /** The body written in the editor. */
    public abstract void draw();

    // ------------------------------------------------------------------- api

    /** Paints one cell. */
    protected final void p(int x, int y, String color) {
        guard();
        canvas.set(x, y, color, null);
    }

    /** Paints one cell and writes a small mark on it. */
    protected final void p(int x, int y, String color, String glyph) {
        guard();
        canvas.set(x, y, color, glyph);
    }

    /** True when the cell is on the grid. */
    protected final boolean in(int x, int y) {
        return canvas.inside(x, y);
    }

    /** Writes a line to the console tab. */
    protected final void print(Object value) {
        guard();
        if (console.length() < 12000) {
            console.append(value).append('\n');
        }
    }

    private void guard() {
        if (cancelled) throw new Stopped("stopped");
        if (++steps > 400000) {
            cancelled = true;
            throw new Stopped("that is a lot of painting, the sketch was stopped");
        }
    }

    /** Thrown when a sketch is cut short. */
    public static final class Stopped extends RuntimeException {
        public Stopped(String message) {
            super(message);
        }
    }
}
