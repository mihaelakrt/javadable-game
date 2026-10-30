package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/** Wraps a component in a sheet of frosted glass. */
public class GlassBox extends JPanel {

    private final int arc;
    private final int veil;
    private final int rim;

    public GlassBox(Component content, int arc, int veil, int rim) {
        this.arc = arc;
        this.veil = veil;
        this.rim = rim;
        setOpaque(false);
        setLayout(new BorderLayout());
        if (content != null) add(content, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Palette.smooth(g.create());
        Glass.dark(g2, this, 0, 0, getWidth(), getHeight(), arc, veil, rim);
        g2.dispose();
    }
}
