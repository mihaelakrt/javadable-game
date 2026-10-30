package javadable.core;

import java.util.Arrays;

/** The grid a sketch paints on, and the thing a puzzle is checked against. */
public class Canvas {

    public final int width;
    public final int height;
    private final String[] colors;
    private final String[] glyphs;

    public Canvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.colors = new String[width * height];
        this.glyphs = new String[width * height];
    }

    public boolean inside(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public void set(int x, int y, String color, String glyph) {
        if (!inside(x, y)) return;
        int index = y * width + x;
        colors[index] = Colors.normalise(color);
        String mark = glyph == null ? null : glyph.trim();
        glyphs[index] = mark == null || mark.isEmpty() ? null : mark;
    }

    public String colorAt(int x, int y) {
        return inside(x, y) ? colors[y * width + x] : null;
    }

    public String glyphAt(int x, int y) {
        return inside(x, y) ? glyphs[y * width + x] : null;
    }

    public void clear() {
        Arrays.fill(colors, null);
        Arrays.fill(glyphs, null);
    }

    public int painted() {
        int count = 0;
        for (String color : colors) {
            if (color != null) count++;
        }
        return count;
    }

    public boolean matches(Canvas other) {
        return mismatches(other) == 0;
    }

    public int mismatches(Canvas other) {
        if (other == null || other.width != width || other.height != height) return width * height;
        int wrong = 0;
        for (int i = 0; i < colors.length; i++) {
            if (!same(colors[i], other.colors[i]) || !same(glyphs[i], other.glyphs[i])) wrong++;
        }
        return wrong;
    }

    public boolean isWrong(int x, int y, Canvas other) {
        if (!inside(x, y) || other == null || other.width != width || other.height != height) {
            return false;
        }
        int index = y * width + x;
        return !same(colors[index], other.colors[index]) || !same(glyphs[index], other.glyphs[index]);
    }

    private static boolean same(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public Canvas copy() {
        Canvas clone = new Canvas(width, height);
        System.arraycopy(colors, 0, clone.colors, 0, colors.length);
        System.arraycopy(glyphs, 0, clone.glyphs, 0, glyphs.length);
        return clone;
    }

    /** Builds a canvas from rows of legend letters, see Colors. */
    public static Canvas fromRows(String[] rows) {
        int height = rows.length;
        int width = rows[0].length();
        Canvas canvas = new Canvas(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width && x < rows[y].length(); x++) {
                canvas.set(x, y, Colors.fromLegend(rows[y].charAt(x)), null);
            }
        }
        return canvas;
    }
}
