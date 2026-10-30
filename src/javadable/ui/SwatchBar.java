package javadable.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import javadable.core.Colors;
import javadable.core.Marks;

/** The palette: colours on one page, marks on the other. */
public class SwatchBar extends JPanel {

    public interface Listener {
        void pickedColour(String colour);

        void pickedMark(String mark);
    }

    private final List<String> colours = Colors.names();
    private final List<String> marks = new ArrayList<>();
    private final List<Rectangle2D> boxes = new ArrayList<>();
    private final Listener listener;

    private boolean marksPage;
    private int hovered = -1;
    private boolean hoveredCaption;
    private String selectedColour;
    private String selectedMark;
    private Rectangle2D captionBox = new Rectangle2D.Double();

    public SwatchBar(Listener listener) {
        this.listener = listener;
        setOpaque(false);
        setToolTipText("");
        marks.add(Marks.NONE);
        marks.addAll(Marks.all());

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (captionBox.contains(e.getPoint())) {
                    marksPage = !marksPage;
                    hovered = -1;
                    repaint();
                    return;
                }
                int index = indexAt(e.getPoint());
                if (index < 0 || listener == null) return;
                if (marksPage) {
                    selectedMark = marks.get(index);
                    listener.pickedMark(selectedMark);
                } else {
                    selectedColour = colours.get(index);
                    listener.pickedColour(selectedColour);
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int index = indexAt(e.getPoint());
                boolean caption = captionBox.contains(e.getPoint());
                if (index != hovered || caption != hoveredCaption) {
                    hovered = index;
                    hoveredCaption = caption;
                    setCursor(Cursor.getPredefinedCursor(index >= 0 || caption
                            ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = -1;
                hoveredCaption = false;
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setSelectedColour(String colour) {
        this.selectedColour = colour;
        repaint();
    }

    public void setSelectedMark(String mark) {
        this.selectedMark = mark;
        repaint();
    }

    private List<String> page() {
        return marksPage ? marks : colours;
    }

    private int indexAt(Point point) {
        for (int i = 0; i < boxes.size(); i++) {
            if (boxes.get(i).contains(point)) return i;
        }
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (captionBox.contains(event.getPoint())) {
            return marksPage ? "show the colours" : "show the marks";
        }
        int index = indexAt(event.getPoint());
        if (index < 0) return null;
        String value = page().get(index);
        return marksPage && value.isEmpty() ? "no mark" : value;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 58);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Palette.smooth(g.create());
        int width = getWidth();
        int height = getHeight();
        Glass.panel(g2, this, 0, 0, width, height, height / 2.0, 9, 38);

        String caption = marksPage ? "MARKS" : "PALETTE";
        g2.setFont(Palette.tracked(10, Font.BOLD, 0.18));
        double captionWidth = g2.getFontMetrics().stringWidth(caption) + 34;
        captionBox = new Rectangle2D.Double(10, height / 2.0 - 13, captionWidth - 10, 26);
        RoundRectangle2D captionShape = new RoundRectangle2D.Double(
                captionBox.getX(), captionBox.getY(), captionBox.getWidth(), 26, 26, 26);
        g2.setColor(new Color(255, 255, 255, hoveredCaption ? 40 : 16));
        g2.fill(captionShape);
        g2.setColor(new Color(255, 255, 255, hoveredCaption ? 150 : 70));
        g2.draw(captionShape);
        g2.setColor(Palette.alpha(Palette.CREAM, 225));
        Palette.centerIn(g2, caption, captionBox.getX(), captionBox.getY(),
                captionBox.getWidth(), 26);

        List<String> page = page();
        String label = hovered >= 0 ? page.get(hovered)
                : (marksPage ? selectedMark : selectedColour);
        if (label == null) label = "";
        boolean labelIsText = !marksPage;
        double labelWidth = 0;
        if (labelIsText && !label.isEmpty()) {
            g2.setFont(Palette.tracked(11, Font.BOLD, 0.14));
            labelWidth = g2.getFontMetrics().stringWidth(label.toUpperCase()) + 26;
        }

        boxes.clear();
        int count = page.size();
        double available = width - captionWidth - labelWidth - 26;
        double gap = 5;
        double size = Math.min(34, (available - gap * (count - 1)) / count);
        double left = captionWidth + 8;
        double top = (height - size) / 2;

        for (int i = 0; i < count; i++) {
            double x = left + i * (size + gap);
            boxes.add(new Rectangle2D.Double(x, top, size, size));
            double grow = i == hovered ? 3 : 0;
            RoundRectangle2D shape = new RoundRectangle2D.Double(
                    x - grow, top - grow, size + grow * 2, size + grow * 2,
                    size * 0.38, size * 0.38);

            if (marksPage) {
                String glyph = page.get(i);
                boolean chosen = glyph.equals(selectedMark);
                g2.setColor(new Color(255, 255, 255, chosen ? 46 : (i == hovered ? 30 : 12)));
                g2.fill(shape);
                g2.setColor(new Color(255, 255, 255, chosen ? 220 : 60));
                g2.setStroke(new BasicStroke(chosen ? 2f : 1f));
                g2.draw(shape);
                g2.setStroke(new BasicStroke(1f));
                if (glyph.isEmpty()) {
                    g2.setColor(Palette.alpha(Palette.ROSE, 200));
                    double pad = size * 0.3;
                    g2.drawLine((int) (x + pad), (int) (top + pad),
                            (int) (x + size - pad), (int) (top + size - pad));
                    g2.drawLine((int) (x + size - pad), (int) (top + pad),
                            (int) (x + pad), (int) (top + size - pad));
                } else {
                    g2.setFont(Palette.markFont(glyph, (int) (size * 0.62)));
                    g2.setColor(Palette.CREAM);
                    Palette.centerIn(g2, glyph, x - grow, top - grow,
                            size + grow * 2, size + grow * 2);
                }
            } else {
                String colour = page.get(i);
                boolean chosen = colour.equals(selectedColour);
                g2.setColor(Palette.alpha(Palette.INK, 90));
                g2.fill(new RoundRectangle2D.Double(x - grow, top - grow + 3,
                        size + grow * 2, size + grow * 2, size * 0.38, size * 0.38));
                g2.setColor(new Color(Colors.rgb(colour)));
                g2.fill(shape);
                g2.setColor(new Color(255, 255, 255, chosen ? 230 : 90));
                g2.setStroke(new BasicStroke(chosen ? 2.4f : 1.2f));
                g2.draw(shape);
                g2.setStroke(new BasicStroke(1f));
            }
        }

        if (labelIsText && !label.isEmpty()) {
            g2.setFont(Palette.tracked(11, Font.BOLD, 0.14));
            g2.setColor(Palette.CREAM);
            g2.drawString(label.toUpperCase(), (float) (width - labelWidth + 8), height / 2f + 4f);
        }
        g2.dispose();
    }
}
