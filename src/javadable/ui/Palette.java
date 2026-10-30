package javadable.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

import javadable.core.Rank;

/** The six colours the whole game is painted with, plus small drawing helpers. */
public final class Palette {

    public static final Color INK = new Color(0x19, 0x00, 0x19);
    public static final Color DEEP = new Color(0x2B, 0x12, 0x4C);
    public static final Color PLUM = new Color(0x52, 0x2B, 0x5B);
    public static final Color MAUVE = new Color(0x85, 0x4F, 0x6C);
    public static final Color ROSE = new Color(0xDF, 0xB6, 0xB2);
    public static final Color CREAM = new Color(0xFB, 0xE4, 0xD8);

    private static final String FAMILY = "Segoe UI";

    private Palette() {
    }

    public static Color alpha(Color color, int a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
    }

    public static Color mix(Color a, Color b, double t) {
        double k = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k));
    }

    /** Ink on light colours, cream on dark ones. */
    public static Color contrast(Color background) {
        double luminance = (0.299 * background.getRed()
                + 0.587 * background.getGreen()
                + 0.114 * background.getBlue()) / 255.0;
        return luminance > 0.6 ? INK : CREAM;
    }

    private static final String[] MARK_FONTS =
            {"Segoe UI Emoji", "Segoe UI Symbol", "Segoe UI", java.awt.Font.DIALOG};

    /** The first font on this machine that can actually draw the glyph. */
    public static Font markFont(String glyph, int size) {
        for (String name : MARK_FONTS) {
            Font candidate = new Font(name, Font.PLAIN, size);
            if (glyph == null || candidate.canDisplayUpTo(glyph) < 0) return candidate;
        }
        return new Font(java.awt.Font.DIALOG, Font.PLAIN, size);
    }

    public static Font font(int size, int style) {
        return new Font(FAMILY, style, size);
    }

    /** A font with extra letter spacing, used for titles and tiles. */
    public static Font tracked(int size, int style, double tracking) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, tracking);
        return font(size, style).deriveFont(attributes);
    }

    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    /** Top and bottom colour of the backdrop. It warms up as the player ranks up. */
    public static Color[] backdrop(Rank rank) {
        switch (rank) {
            case INDIGO:   return new Color[]{DEEP, INK};
            case PLUM:     return new Color[]{INK, PLUM};
            case MAUVE:    return new Color[]{DEEP, MAUVE};
            case ROSE:     return new Color[]{PLUM, ROSE};
            case CREAM:    return new Color[]{MAUVE, CREAM};
            case MIDNIGHT:
            default:       return new Color[]{INK, DEEP};
        }
    }

    public static void paintBackdrop(Graphics2D g, int width, int height, Rank rank) {
        Color[] stops = backdrop(rank);
        g.setPaint(new LinearGradientPaint(
                new Point(0, 0), new Point(0, Math.max(1, height)),
                new float[]{0f, 0.55f, 1f},
                new Color[]{stops[0], mix(stops[0], stops[1], 0.45), stops[1]}));
        g.fillRect(0, 0, width, height);

        int glowRadius = Math.max(width, height);
        g.setPaint(new RadialGradientPaint(
                new Point(width / 2, (int) (height * 0.42)), glowRadius / 2f,
                new float[]{0f, 1f},
                new Color[]{alpha(ROSE, 38), alpha(ROSE, 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillRect(0, 0, width, height);
    }

    /** A soft dark card, the surface every piece of content sits on. */
    public static void card(Graphics2D g, double x, double y, double width, double height,
                            double arc, Color fill, Color border) {
        RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, width, height, arc, arc);
        g.setColor(alpha(INK, 70));
        g.fill(new RoundRectangle2D.Double(x, y + 5, width, height, arc, arc));
        g.setColor(fill);
        g.fill(shape);
        if (border != null) {
            g.setColor(border);
            g.draw(shape);
        }
    }

    public static Color surface() {
        return alpha(INK, 150);
    }

    public static Color surfaceLight() {
        return alpha(PLUM, 120);
    }

    /** Draws text centred on a point. */
    public static void centerString(Graphics2D g, String text, double centerX, double baselineY) {
        int width = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (float) (centerX - width / 2.0), (float) baselineY);
    }

    /** Draws text centred inside a rectangle, vertically too. */
    public static void centerIn(Graphics2D g, String text, double x, double y,
                                double width, double height) {
        java.awt.FontMetrics metrics = g.getFontMetrics();
        double textX = x + (width - metrics.stringWidth(text)) / 2.0;
        double textY = y + (height - metrics.getHeight()) / 2.0 + metrics.getAscent();
        g.drawString(text, (float) textX, (float) textY);
    }
}
