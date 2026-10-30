package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** The one big pane of glass a screen lives on. */
public class Sheet extends JPanel {

    private final double arc;

    public Sheet(int padTop, int padSide, int padBottom) {
        this(padTop, padSide, padBottom, 30);
    }

    public Sheet(int padTop, int padSide, int padBottom, double arc) {
        this.arc = arc;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(padTop, padSide, padBottom, padSide));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Palette.smooth(g.create());
        Glass.panel(g2, this, 0, 0, getWidth(), getHeight(), arc, 11, 42);
        g2.dispose();
    }
}
