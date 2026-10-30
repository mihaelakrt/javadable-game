package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javadable.core.Canvas;
import javadable.core.Runner;

/**
 * Free drawing. Pick a colour, paint the grid with the mouse, and the game
 * writes the code that would have painted it.
 */
public class StudioPanel extends JPanel {

    private final GameFrame frame;
    private final GridView grid = new GridView("YOUR CANVAS");
    private final JTextArea code = new JTextArea();
    private final SwatchBar swatches;
    private final Header header = new Header();

    private Canvas canvas = new Canvas(8, 8);
    private String colour = "cream";
    private String mark;
    private int size = 8;

    public StudioPanel(GameFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 34, 18, 34));

        Sheet sheet = new Sheet(14, 24, 16, 28);
        sheet.setLayout(new BorderLayout(0, 12));
        add(sheet, BorderLayout.CENTER);

        swatches = new SwatchBar(new SwatchBar.Listener() {
            @Override
            public void pickedColour(String picked) {
                colour = picked;
                swatches.setSelectedColour(picked);
                header.repaint();
            }

            @Override
            public void pickedMark(String picked) {
                mark = picked == null || picked.isEmpty() ? null : picked;
                swatches.setSelectedMark(picked);
                header.repaint();
            }
        });

        sheet.add(header, BorderLayout.NORTH);

        grid.setCanvas(canvas, false);
        grid.setCellListener((x, y, erase) -> {
            canvas.set(x, y, erase ? null : colour, erase ? null : mark);
            grid.repaint();
            refreshCode();
        });

        code.setEditable(false);
        code.setOpaque(false);
        code.setForeground(Palette.CREAM);
        code.setFont(new Font("Consolas", Font.PLAIN, 14));
        code.setMargin(new java.awt.Insets(12, 14, 12, 12));
        JScrollPane scroll = new JScrollPane(code);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        SlimScrollBar.apply(scroll);

        JPanel middle = new JPanel(new GridLayout(1, 2, 18, 0));
        middle.setOpaque(false);
        middle.add(grid);
        middle.add(new GlassBox(scroll, 22, 230, 70));
        sheet.add(middle, BorderLayout.CENTER);

        sheet.add(swatches, BorderLayout.SOUTH);

        swatches.setSelectedColour(colour);
        refreshCode();
    }

    public void refresh() {
        grid.repaint();
        repaint();
    }

    private void changeSize(int delta) {
        int wanted = Math.max(4, Math.min(14, size + delta));
        if (wanted == size) return;
        Canvas bigger = new Canvas(wanted, wanted);
        for (int y = 0; y < Math.min(size, wanted); y++) {
            for (int x = 0; x < Math.min(size, wanted); x++) {
                bigger.set(x, y, canvas.colorAt(x, y), canvas.glyphAt(x, y));
            }
        }
        size = wanted;
        canvas = bigger;
        grid.setCanvas(canvas, true);
        refreshCode();
        header.repaint();
    }

    private void clear() {
        canvas.clear();
        grid.repaint();
        refreshCode();
    }

    private void refreshCode() {
        code.setText(generate());
        code.setCaretPosition(0);
        header.repaint();
    }

    private static boolean same(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    /** Turns the painted grid into the code that would draw it. */
    private String generate() {
        StringBuilder out = new StringBuilder();
        char quote = 34;
        for (int y = 0; y < canvas.height; y++) {
            int x = 0;
            while (x < canvas.width) {
                String cell = canvas.colorAt(x, y);
                if (cell == null) {
                    x++;
                    continue;
                }
                String glyph = canvas.glyphAt(x, y);
                int end = x;
                while (end + 1 < canvas.width
                        && cell.equals(canvas.colorAt(end + 1, y))
                        && same(glyph, canvas.glyphAt(end + 1, y))) {
                    end++;
                }
                String tail = glyph == null ? "" : ", " + quote + glyph + quote;
                if (end - x + 1 >= 3) {
                    out.append("for (int x = ").append(x).append("; x <= ").append(end)
                            .append("; x++) p(x, ").append(y).append(", ")
                            .append(quote).append(cell).append(quote).append(tail).append(");\n");
                } else {
                    for (int i = x; i <= end; i++) {
                        out.append("p(").append(i).append(", ").append(y).append(", ")
                                .append(quote).append(cell).append(quote).append(tail).append(");\n");
                    }
                }
                x = end + 1;
            }
        }
        if (out.length() == 0) {
            return "// click the grid to paint, right click to rub out\n"
                    + "// the code that draws your picture shows up here\n";
        }
        return out.toString();
    }

    private final class Header extends JPanel {

        Header() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(0, 72));

            Pill home = new Pill("Home", Pill.Style.GHOST);
            home.addActionListener(e -> frame.showHome());
            JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
            west.setOpaque(false);
            west.add(home);
            add(west, BorderLayout.WEST);

            Pill smaller = new Pill("Smaller", Pill.Style.SOFT);
            smaller.addActionListener(e -> changeSize(-1));
            Pill bigger = new Pill("Bigger", Pill.Style.SOFT);
            bigger.addActionListener(e -> changeSize(1));
            Pill clear = new Pill("Clear", Pill.Style.SOFT);
            clear.addActionListener(e -> clear());
            Pill copy = new Pill("Copy code", Pill.Style.PRIMARY);
            copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(code.getText()), null));

            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 16));
            east.setOpaque(false);
            east.add(smaller);
            east.add(bigger);
            east.add(clear);
            east.add(copy);
            add(east, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            g2.setFont(Palette.tracked(26, Font.BOLD, 0.1));
            g2.setColor(Palette.CREAM);
            g2.drawString("Studio", 182f, 38f);
            g2.setFont(Palette.font(13, Font.PLAIN));
            g2.setColor(Palette.alpha(Palette.ROSE, 220));
            String info = size + " by " + size + "   ·   " + colour + "   ·   "
                    + Runner.characters(code.getText()) + " characters of code";
            g2.drawString(info, 182f, 58f);
            if (mark != null) {
                double x = 182 + g2.getFontMetrics().stringWidth(info) + 14;
                g2.setColor(Palette.alpha(Palette.CREAM, 230));
                g2.setFont(Palette.markFont(mark, 15));
                g2.drawString(mark, (float) x, 58f);
            }
            g2.dispose();
        }
    }
}
