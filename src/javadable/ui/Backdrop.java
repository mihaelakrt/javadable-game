package javadable.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import javadable.core.Rank;

/**
 * Near black, with slow folds of silk running through it. Painted small and
 * blown up, which is what gives the glass something soft to sit on.
 */
public final class Backdrop {

    private static final int SCALE = 5;

    private static BufferedImage buffer;
    private static JComponent root;
    private static final long START = System.currentTimeMillis();

    private Backdrop() {
    }

    public static void setRoot(JComponent component) {
        root = component;
    }

    public static JComponent root() {
        return root;
    }

    public static BufferedImage buffer() {
        return buffer;
    }

    public static void paint(Graphics2D g, int width, int height, Rank rank) {
        render(width, height, rank);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(buffer, 0, 0, width, height, null);
    }

    private static void render(int width, int height, Rank rank) {
        int w = Math.max(2, width / SCALE);
        int h = Math.max(2, height / SCALE);
        if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
            buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color[] stops = Palette.backdrop(rank);
        Color base = Palette.mix(stops[0], Color.BLACK, 0.74);
        g.setColor(base);
        g.fillRect(0, 0, w, h);

        double time = (System.currentTimeMillis() - START) / 1000.0;

        // the folds: deep shadow with one lit edge, the way satin behaves
        silk(g, w, h, 0.14 + 0.012 * Math.sin(time * 0.08), 0.58, 1.25, Palette.PLUM, 52);
        silk(g, w, h, 0.40 + 0.015 * Math.cos(time * 0.06), 0.46, 1.05, Palette.MAUVE, 66);
        silk(g, w, h, 0.68 + 0.013 * Math.sin(time * 0.05), 0.40, 0.90, Palette.ROSE, 46);
        silk(g, w, h, 0.90 + 0.010 * Math.cos(time * 0.07), 0.32, 0.70, Palette.MAUVE, 34);

        // the few places where the light actually catches
        glow(g, w, h, 0.33 + 0.02 * Math.sin(time * 0.09), 0.28, 0.10, Palette.ROSE, 60);
        glow(g, w, h, 0.52 + 0.02 * Math.cos(time * 0.05), 0.70, 0.08, Palette.CREAM, 42);
        glow(g, w, h, 0.76 + 0.02 * Math.sin(time * 0.06), 0.20, 0.07, Palette.MAUVE, 55);

        g.dispose();
    }

    /** One fold: a wide curved band of shadow with a thin lit crease in it. */
    private static void silk(Graphics2D g, int w, int h, double centre, double width,
                             double bend, Color sheen, int alpha) {
        double x = centre * w;
        double thickness = width * w * 0.5;
        double curve = bend * w * 0.55;

        Path2D.Double band = new Path2D.Double();
        band.moveTo(x - thickness, -h * 0.2);
        band.curveTo(x - thickness + curve, h * 0.25,
                x - thickness - curve * 0.6, h * 0.7,
                x - thickness * 0.4, h * 1.2);
        band.lineTo(x + thickness, h * 1.2);
        band.curveTo(x + thickness - curve * 0.5, h * 0.72,
                x + thickness + curve * 0.8, h * 0.3,
                x + thickness * 0.6, -h * 0.2);
        band.closePath();

        Color shadow = new Color(0, 0, 0, 150);
        Color clear = new Color(0, 0, 0, 0);
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(x - thickness, 0), new Point2D.Double(x + thickness, h),
                new float[]{0f, 0.34f, 0.52f, 0.58f, 0.72f, 1f},
                new Color[]{clear, shadow, Palette.alpha(sheen, alpha),
                        Palette.alpha(sheen, alpha / 3), shadow, clear},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(band);
    }

    private static void glow(Graphics2D g, int w, int h, double cx, double cy,
                             double radius, Color color, int alpha) {
        float r = (float) (Math.max(w, h) * radius);
        if (r <= 0) return;
        g.setPaint(new RadialGradientPaint(
                new Point((int) (cx * w), (int) (cy * h)), r,
                new float[]{0f, 1f},
                new Color[]{Palette.alpha(color, alpha), Palette.alpha(color, 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillRect(0, 0, w, h);
    }
}
