package javadable.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Small painted ornaments shared by the screens. */
public final class Decor {

    private Decor() {
    }

    /** Three diamonds, filled for the stars that were earned. */
    public static void stars(Graphics2D g, double x, double y, double size, int earned) {
        for (int i = 0; i < 3; i++) {
            diamond(g, x + i * (size + 5), y, size, i < earned);
        }
    }

    /** One diamond, filled or hollow. */
    public static void diamond(Graphics2D g, double x, double y, double size, boolean filled) {
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x + size / 2, y);
        shape.lineTo(x + size, y + size / 2);
        shape.lineTo(x + size / 2, y + size);
        shape.lineTo(x, y + size / 2);
        shape.closePath();
        if (filled) {
            g.setColor(Palette.CREAM);
            g.fill(shape);
        } else {
            g.setColor(Palette.alpha(Palette.MAUVE, 140));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(shape);
            g.setStroke(new BasicStroke(1f));
        }
    }

    /** The six armed mark, the one ornament the layout allows itself. */
    public static void asterisk(Graphics2D g, double cx, double cy, double radius, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke((float) Math.max(1.6, radius * 0.17),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            double angle = Math.PI / 3 * i + Math.PI / 2;
            double dx = Math.cos(angle) * radius;
            double dy = Math.sin(angle) * radius;
            g.draw(new java.awt.geom.Line2D.Double(cx - dx, cy - dy, cx + dx, cy + dy));
        }
        g.setStroke(new BasicStroke(1f));
    }

    /** A thumbnail of a target picture, for the level cards. */
    public static void miniGrid(Graphics2D g, javadable.core.Canvas canvas,
                                double x, double y, double size, boolean dim) {
        if (canvas == null) return;
        double gap = size > 60 ? 2 : 1;
        double cell = (size - gap * (canvas.width - 1)) / canvas.width;
        double arc = Math.max(2, cell * 0.3);
        for (int row = 0; row < canvas.height; row++) {
            for (int column = 0; column < canvas.width; column++) {
                String colour = canvas.colorAt(column, row);
                double cx = x + column * (cell + gap);
                double cy = y + row * (cell + gap);
                RoundRectangle2D shape = new RoundRectangle2D.Double(cx, cy, cell, cell, arc, arc);
                if (colour == null) {
                    g.setColor(new Color(255, 255, 255, dim ? 10 : 22));
                } else {
                    Color paint = new Color(javadable.core.Colors.rgb(colour));
                    g.setColor(dim ? Palette.alpha(paint, 70) : paint);
                }
                g.fill(shape);
            }
        }
    }

    /** A small rounded label with a quiet background. */
    public static double chip(Graphics2D g, String text, double x, double y, Color textColor) {
        g.setFont(Palette.tracked(11, Font.BOLD, 0.12));
        int width = g.getFontMetrics().stringWidth(text) + 22;
        int height = 24;
        RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, width, height, height, height);
        g.setColor(Palette.alpha(Palette.PLUM, 150));
        g.fill(shape);
        g.setColor(Palette.alpha(Palette.MAUVE, 140));
        g.draw(shape);
        g.setColor(textColor);
        Palette.centerIn(g, text, x, y, width, height);
        return width;
    }

    /** The progress bar used for rank and for the character budget. */
    public static void meter(Graphics2D g, double x, double y, double width, double height,
                             double fraction, Color fill) {
        double arc = height;
        g.setColor(Palette.alpha(Palette.INK, 150));
        g.fill(new RoundRectangle2D.Double(x, y, width, height, arc, arc));
        double filled = Math.max(0, Math.min(1, fraction)) * width;
        if (filled > 1) {
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Double(x, y, Math.max(filled, height), height, arc, arc));
        }
        g.setColor(Palette.alpha(Palette.MAUVE, 120));
        g.draw(new RoundRectangle2D.Double(x, y, width, height, arc, arc));
    }
}
