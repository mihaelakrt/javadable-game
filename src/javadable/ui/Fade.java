package javadable.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Cross fades one screen into the next: the old picture is frozen, laid over
 * the new one and dissolved away.
 */
public final class Fade {

    private static final int DURATION = 240;

    private Fade() {
    }

    public static void swap(JComponent content, Runnable change) {
        BufferedImage before = snapshot(content);
        change.run();
        content.revalidate();
        content.repaint();
        if (before == null) return;

        JRootPane rootPane = SwingUtilities.getRootPane(content);
        if (rootPane == null) return;
        JLayeredPane layers = rootPane.getLayeredPane();
        Point where = SwingUtilities.convertPoint(content, 0, 0, layers);

        Veil veil = new Veil(before);
        veil.setBounds(where.x, where.y, before.getWidth(), before.getHeight());
        layers.add(veil, JLayeredPane.DRAG_LAYER);
        layers.repaint();

        final long started = System.currentTimeMillis();
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            double t = (System.currentTimeMillis() - started) / (double) DURATION;
            if (t >= 1) {
                timer.stop();
                layers.remove(veil);
                layers.repaint();
                return;
            }
            veil.alpha = (float) Math.max(0, 1 - Math.pow(t, 1.5));
            veil.repaint();
        });
        timer.start();
    }

    private static BufferedImage snapshot(JComponent content) {
        int width = content.getWidth();
        int height = content.getHeight();
        if (width <= 0 || height <= 0) return null;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        content.paint(g);
        g.dispose();
        return image;
    }

    private static final class Veil extends JComponent {

        private final BufferedImage image;
        private float alpha = 1f;

        Veil(BufferedImage image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(image, 0, 0, null);
            g2.dispose();
        }
    }
}
