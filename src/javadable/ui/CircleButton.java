package javadable.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import javax.swing.JButton;
import javax.swing.Timer;

/** The thin outlined circle with an arrow in it. */
public class CircleButton extends JButton {

    private final int size;
    private float hover;
    private boolean inside;
    private Timer animator;

    public CircleButton(int size, String tooltip) {
        this.size = size;
        setToolTipText(tooltip);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        animator = new Timer(16, e -> {
            float target = inside ? 1f : 0f;
            if (Math.abs(hover - target) < 0.02f) {
                hover = target;
                animator.stop();
            } else {
                hover += (target - hover) * 0.22f;
            }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                inside = true;
                animator.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                inside = false;
                animator.start();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Palette.smooth(g.create());
        double inset = 2;
        double d = size - inset * 2;
        Ellipse2D circle = new Ellipse2D.Double(inset, inset, d, d);

        g2.setColor(new Color(255, 255, 255, (int) (10 + 30 * hover)));
        g2.fill(circle);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(new Color(255, 255, 255, (int) (120 + 110 * hover)));
        g2.draw(circle);

        double cx = size / 2.0;
        double cy = size / 2.0;
        double arm = size * 0.17;
        g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Palette.CREAM);
        Path2D.Double arrow = new Path2D.Double();
        arrow.moveTo(cx - arm, cy);
        arrow.lineTo(cx + arm, cy);
        arrow.moveTo(cx + arm * 0.35, cy - arm * 0.62);
        arrow.lineTo(cx + arm, cy);
        arrow.lineTo(cx + arm * 0.35, cy + arm * 0.62);
        g2.draw(arrow);
        g2.dispose();
    }
}
