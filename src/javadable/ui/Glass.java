package javadable.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Frosted glass. The panel shows a blurred slice of the backdrop behind it,
 * a white veil over that, a highlight along the top edge and a thin rim.
 */
public final class Glass {

    private Glass() {
    }

    public static void panel(Graphics2D g, JComponent owner, double x, double y,
                             double width, double height, double arc) {
        panel(g, owner, x, y, width, height, arc, 13, 48);
    }

    /** The same glass, but veiled in ink instead of white, for text heavy surfaces. */
    public static void dark(Graphics2D g, JComponent owner, double x, double y,
                            double width, double height, double arc, int veil, int rim) {
        panel(g, owner, x, y, width, height, arc, 0, rim, Palette.alpha(Palette.INK, veil));
    }

    public static void panel(Graphics2D g, JComponent owner, double x, double y,
                             double width, double height, double arc, int frost, int rim) {
        panel(g, owner, x, y, width, height, arc, frost, rim, null);
    }

    public static void panel(Graphics2D g, JComponent owner, double x, double y,
                             double width, double height, double arc, int frost, int rim,
                             Color veil) {
        if (width <= 0 || height <= 0) return;
        Shape shape = new RoundRectangle2D.Double(x, y, width, height, arc, arc);

        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(0, 0, 0, 26));
            g.fill(new RoundRectangle2D.Double(x - i, y + i * 3.0, width + i * 2, height, arc, arc));
        }

        if (!blurUnder(g, owner, x, y, width, height, shape)) {
            g.setColor(Palette.alpha(Palette.PLUM, 120));
            g.fill(shape);
        }

        if (veil == null) {
            g.setColor(new Color(12, 0, 14, 44));
            g.fill(shape);
        }
        g.setColor(veil != null ? veil : new Color(255, 255, 255, frost));
        g.fill(shape);

        Shape clip = g.getClip();
        g.clip(shape);
        g.setPaint(new GradientPaint(
                (float) x, (float) y, new Color(255, 255, 255, veil != null ? 22 : Math.min(70, frost + 22)),
                (float) x, (float) (y + height * 0.55), new Color(255, 255, 255, 0)));
        g.fill(shape);
        g.setPaint(new GradientPaint(
                (float) x, (float) (y + height * 0.6), new Color(0, 0, 0, 0),
                (float) x, (float) (y + height), new Color(25, 0, 25, 40)));
        g.fill(shape);
        g.setClip(clip);

        g.setStroke(new BasicStroke(1.3f));
        g.setColor(new Color(255, 255, 255, rim));
        g.draw(shape);
        g.setStroke(new BasicStroke(1f));
    }

    /** Draws the slice of the backdrop that lies under this panel. */
    private static boolean blurUnder(Graphics2D g, JComponent owner, double x, double y,
                                     double width, double height, Shape shape) {
        BufferedImage buffer = Backdrop.buffer();
        JComponent root = Backdrop.root();
        if (buffer == null || root == null || owner == null
                || root.getWidth() <= 0 || root.getHeight() <= 0) {
            return false;
        }

        Point topLeft;
        try {
            topLeft = SwingUtilities.convertPoint(owner, (int) x, (int) y, root);
        } catch (Exception e) {
            return false;
        }

        double kx = buffer.getWidth() / (double) root.getWidth();
        double ky = buffer.getHeight() / (double) root.getHeight();
        int sx1 = clamp((int) Math.floor(topLeft.x * kx), 0, buffer.getWidth() - 1);
        int sy1 = clamp((int) Math.floor(topLeft.y * ky), 0, buffer.getHeight() - 1);
        int sx2 = clamp((int) Math.ceil((topLeft.x + width) * kx), sx1 + 1, buffer.getWidth());
        int sy2 = clamp((int) Math.ceil((topLeft.y + height) * ky), sy1 + 1, buffer.getHeight());

        Shape clip = g.getClip();
        g.clip(shape);
        Object previous = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(buffer, (int) x, (int) y, (int) (x + width), (int) (y + height),
                sx1, sy1, sx2, sy2, null);
        if (previous != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previous);
        }
        g.setClip(clip);
        return true;
    }

    private static int clamp(int value, int low, int high) {
        return Math.max(low, Math.min(high, value));
    }
}
