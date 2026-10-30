package javadable.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.Timer;

import javadable.core.Canvas;
import javadable.core.Colors;

/** Draws one grid: either what the player painted or what the level wants. */
public class GridView extends JPanel {

    private static final int APPEAR_STEP = 16;
    private static final int APPEAR_TIME = 240;

    private final String caption;
    private Canvas canvas;
    private Canvas compare;
    private boolean showDiff;
    private boolean solved;
    private long appearStart = -1;
    private Timer ticker;
    private CellListener cellListener;
    private boolean interactive;
    private double cellSize;
    private double cellGap;
    private double gridLeft;
    private double gridOrigin;

    public GridView(String caption) {
        this.caption = caption;
        setOpaque(false);
        ticker = new Timer(16, e -> {
            if (appearStart > 0 && System.currentTimeMillis() - appearStart > 1600) {
                appearStart = -1;
                ticker.stop();
            }
            repaint();
        });
    }

    public void setCanvas(Canvas canvas, boolean animate) {
        this.canvas = canvas;
        if (animate) {
            appearStart = System.currentTimeMillis();
            ticker.start();
        } else {
            appearStart = -1;
        }
        repaint();
    }

    public void setCompare(Canvas compare) {
        this.compare = compare;
    }

    public void setShowDiff(boolean showDiff) {
        this.showDiff = showDiff;
        repaint();
    }

    public boolean isShowingDiff() {
        return showDiff;
    }

    /** Told which cell was pressed, so the grid can be painted by hand. */
    public interface CellListener {
        void cell(int x, int y, boolean erase);
    }

    public void setCellListener(CellListener listener) {
        this.cellListener = listener;
        setCursor(java.awt.Cursor.getPredefinedCursor(
                listener == null ? java.awt.Cursor.DEFAULT_CURSOR : java.awt.Cursor.CROSSHAIR_CURSOR));
        if (listener != null && !interactive) {
            interactive = true;
            java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    paintAt(e);
                }

                @Override
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    paintAt(e);
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }
    }

    private void paintAt(java.awt.event.MouseEvent event) {
        if (cellListener == null || canvas == null || cellSize <= 0) return;
        int x = (int) Math.floor((event.getX() - gridLeft) / (cellSize + cellGap));
        int y = (int) Math.floor((event.getY() - gridOrigin) / (cellSize + cellGap));
        if (!canvas.inside(x, y)) return;
        boolean erase = javax.swing.SwingUtilities.isRightMouseButton(event)
                || event.isControlDown();
        cellListener.cell(x, y, erase);
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(280, 300);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Palette.smooth(g.create());
        int width = getWidth();
        int height = getHeight();

        g2.setFont(Palette.tracked(11, Font.BOLD, 0.2));
        g2.setColor(Palette.alpha(Palette.ROSE, 190));
        Palette.centerString(g2, caption, width / 2.0, 14);

        double top = 24;
        double boardHeight = height - top;
        Glass.panel(g2, this, 0, top, width, boardHeight, 24, solved ? 26 : 9,
                solved ? 195 : 38);

        if (canvas == null) {
            g2.dispose();
            return;
        }

        double padding = 16;
        double gap = Math.max(2, 22.0 / canvas.width);
        double cell = Math.min(
                (width - padding * 2 - gap * (canvas.width - 1)) / canvas.width,
                (boardHeight - padding * 2 - gap * (canvas.height - 1)) / canvas.height);
        double gridWidth = canvas.width * cell + gap * (canvas.width - 1);
        double gridHeight = canvas.height * cell + gap * (canvas.height - 1);
        double left = (width - gridWidth) / 2;
        double gridTop = top + (boardHeight - gridHeight) / 2;
        double arc = Math.max(4, cell * 0.3);

        this.cellSize = cell;
        this.cellGap = gap;
        this.gridLeft = left;
        this.gridOrigin = gridTop;

        long now = System.currentTimeMillis();

        for (int y = 0; y < canvas.height; y++) {
            for (int x = 0; x < canvas.width; x++) {
                double cellX = left + x * (cell + gap);
                double cellY = gridTop + y * (cell + gap);
                String color = canvas.colorAt(x, y);

                double scale = 1;
                if (appearStart > 0 && color != null) {
                    double t = (now - appearStart - (double) (x + y) * APPEAR_STEP) / APPEAR_TIME;
                    if (t <= 0) scale = 0;
                    else if (t < 1) scale = 0.45 + 0.55 * Math.sin(Math.PI / 2 * t);
                }

                AffineTransform original = g2.getTransform();
                if (scale != 1) {
                    g2.translate(cellX + cell / 2, cellY + cell / 2);
                    g2.scale(scale, scale);
                    g2.translate(-cellX - cell / 2, -cellY - cell / 2);
                }

                RoundRectangle2D shape = new RoundRectangle2D.Double(cellX, cellY, cell, cell, arc, arc);
                if (color == null) {
                    g2.setColor(new Color(255, 255, 255, 16));
                    g2.fill(shape);
                    g2.setColor(new Color(255, 255, 255, 42));
                    g2.draw(shape);
                } else {
                    Color paint = new Color(Colors.rgb(color));
                    g2.setColor(Palette.alpha(Palette.INK, 95));
                    g2.fill(new RoundRectangle2D.Double(cellX, cellY + 3, cell, cell, arc, arc));
                    g2.setColor(paint);
                    g2.fill(shape);

                    Shape clip = g2.getClip();
                    g2.clip(shape);
                    g2.setPaint(new java.awt.GradientPaint(
                            (float) cellX, (float) cellY, new Color(255, 255, 255, 85),
                            (float) cellX, (float) (cellY + cell * 0.62), new Color(255, 255, 255, 0)));
                    g2.fill(shape);
                    g2.setClip(clip);

                    g2.setColor(new Color(255, 255, 255, 70));
                    g2.draw(shape);
                }

                String glyph = canvas.glyphAt(x, y);
                if (glyph != null && cell > 14) {
                    g2.setFont(Palette.markFont(glyph, (int) (cell * 0.62)));
                    g2.setColor(color == null ? Palette.CREAM
                            : Palette.contrast(new Color(Colors.rgb(color))));
                    Palette.centerIn(g2, glyph, cellX, cellY, cell, cell);
                }

                if (showDiff && compare != null && canvas.isWrong(x, y, compare)) {
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.setColor(new Color(0xE2, 0x49, 0x5B));
                    g2.draw(new RoundRectangle2D.Double(cellX - 1, cellY - 1,
                            cell + 2, cell + 2, arc, arc));
                    g2.setStroke(new BasicStroke(1f));
                }

                g2.setTransform(original);
            }
        }
        g2.dispose();
    }
}
