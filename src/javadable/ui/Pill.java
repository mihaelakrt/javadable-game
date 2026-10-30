package javadable.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.Timer;

/** A rounded button in the palette, with a soft hover. */
public class Pill extends JButton {

    public enum Style { PRIMARY, GHOST, SOFT }

    private Style style;
    private float hover;
    private boolean pointerInside;
    private Timer animator;

    public Pill(String text, Style style) {
        super(text);
        this.style = style;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(style == Style.PRIMARY ? Palette.INK : Palette.CREAM);
        setFont(Palette.tracked(15, Font.BOLD, 0.06));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        animator = new Timer(16, e -> {
            float target = pointerInside && isEnabled() ? 1f : 0f;
            if (Math.abs(hover - target) < 0.02f) {
                hover = target;
                animator.stop();
            } else {
                hover += (target - hover) * 0.25f;
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pointerInside = true;
                animator.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pointerInside = false;
                animator.start();
            }
        });
    }

    public Pill(String text) {
        this(text, Style.SOFT);
    }

    public void setStyle(Style style) {
        this.style = style;
        setForeground(style == Style.PRIMARY ? Palette.INK : Palette.CREAM);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width + 34, Math.max(size.height + 16, 42));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Palette.smooth(g.create());
        int width = getWidth();
        int height = getHeight();
        float lift = getModel().isPressed() ? 0f : 2f;

        RoundRectangle2D shape =
                new RoundRectangle2D.Double(0, lift == 0 ? 2 : 0, width, height - 2, height, height);

        double top = lift == 0 ? 2 : 0;
        Color text = getForeground();

        if (!isEnabled()) {
            Glass.panel(g2, this, 0, top, width, height - 2, height, 12, 45);
            text = Palette.alpha(Palette.ROSE, 115);
        } else if (style == Style.PRIMARY) {
            g2.setColor(Palette.alpha(Palette.INK, 80));
            g2.fill(new RoundRectangle2D.Double(0, 5, width, height - 2, height, height));
            g2.setColor(Palette.mix(Palette.CREAM, Palette.ROSE, hover * 0.55));
            g2.fill(shape);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.draw(shape);
            text = Palette.INK;
        } else if (style == Style.GHOST) {
            Glass.panel(g2, this, 0, top, width, height - 2, height,
                    (int) (22 + 40 * hover), (int) (95 + 90 * hover));
        } else {
            Glass.panel(g2, this, 0, top, width, height - 2, height,
                    (int) (34 + 42 * hover), (int) (85 + 90 * hover));
        }

        g2.setFont(getFont());
        g2.setColor(text);
        Palette.centerIn(g2, getText(), 0, lift == 0 ? 2 : 0, width, height - 2);
        g2.dispose();
    }
}
