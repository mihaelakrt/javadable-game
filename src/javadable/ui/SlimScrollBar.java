package javadable.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** A thin scrollbar that does not shout over the artwork. */
public class SlimScrollBar extends BasicScrollBarUI {

    public static void apply(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new SlimScrollBar());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(11, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUI(new SlimScrollBar());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 11));
    }

    @Override
    protected void configureScrollBarColors() {
        thumbColor = Palette.alpha(Palette.MAUVE, 190);
        trackColor = Palette.alpha(Palette.INK, 90);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return hidden();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return hidden();
    }

    private JButton hidden() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        Graphics2D g2 = Palette.smooth(g.create());
        g2.setColor(Palette.alpha(Palette.INK, 70));
        g2.fill(new RoundRectangle2D.Double(bounds.x + 3, bounds.y, bounds.width - 6,
                bounds.height, 8, 8));
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
        Graphics2D g2 = Palette.smooth(g.create());
        g2.setColor(Palette.alpha(Palette.MAUVE, isDragging ? 230 : 170));
        g2.fill(new RoundRectangle2D.Double(bounds.x + 2, bounds.y + 2, bounds.width - 5,
                bounds.height - 4, 9, 9));
        g2.dispose();
    }
}
