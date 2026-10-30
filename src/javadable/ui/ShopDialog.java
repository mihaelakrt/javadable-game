package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import javadable.core.PowerUp;
import javadable.core.Profile;

/** Spend coins on the four helpers. */
public class ShopDialog extends JDialog {

    private final Profile profile;
    private final JPanel rows = new JPanel(new GridLayout(0, 1, 0, 10));

    public ShopDialog(Window owner, Profile profile) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.profile = profile;
        setUndecorated(true);
        try {
            setBackground(new java.awt.Color(0, 0, 0, 0));
        } catch (Exception ignored) {
            // per pixel translucency is not available everywhere
        }

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Palette.smooth(g.create());
                Glass.dark(g2, this, 0, 0, getWidth(), getHeight(), 26, 168, 120);
                g2.setFont(Palette.tracked(22, Font.BOLD, 0.1));
                g2.setColor(Palette.CREAM);
                Palette.centerString(g2, "Shop", getWidth() / 2.0, 42);
                g2.setFont(Palette.font(13, Font.PLAIN));
                g2.setColor(Palette.alpha(Palette.ROSE, 230));
                Palette.centerString(g2, ShopDialog.this.profile.coins + " coins in the jar",
                        getWidth() / 2.0, 64);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(82, 22, 18, 22));

        rows.setOpaque(false);
        build();
        card.add(rows, BorderLayout.CENTER);

        Pill close = new Pill("Done", Pill.Style.PRIMARY);
        close.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        south.setOpaque(false);
        south.add(close);
        card.add(south, BorderLayout.SOUTH);

        setContentPane(card);
        setSize(new Dimension(470, 400));
        setLocationRelativeTo(owner);
    }

    private void build() {
        rows.removeAll();
        for (PowerUp powerUp : PowerUp.values()) {
            rows.add(row(powerUp));
        }
        rows.revalidate();
        rows.repaint();
    }

    private JPanel row(PowerUp powerUp) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Palette.smooth(g.create());
                Glass.panel(g2, this, 0, 0, getWidth(), getHeight(), 20);
                g2.setFont(Palette.tracked(14, Font.BOLD, 0.05));
                g2.setColor(Palette.CREAM);
                g2.drawString(powerUp.label + "   x" + profile.count(powerUp), 16f, 26f);
                g2.setFont(Palette.font(12, Font.PLAIN));
                g2.setColor(Palette.alpha(Palette.ROSE, 220));
                g2.drawString(powerUp.blurb, 16f, 46f);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 62));

        Pill buy = new Pill(powerUp.price + " coins", Pill.Style.GHOST);
        buy.setEnabled(profile.coins >= powerUp.price);
        buy.addActionListener(e -> {
            if (profile.buy(powerUp)) {
                profile.save();
                build();
                repaint();
                getContentPane().repaint();
            }
        });
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        east.setOpaque(false);
        east.add(buy);
        row.add(east, BorderLayout.EAST);
        return row;
    }
}
