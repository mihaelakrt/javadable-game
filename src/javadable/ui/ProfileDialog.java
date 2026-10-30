package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javadable.core.Colors;
import javadable.core.Profile;

/** Choose a name and a badge for yourself. */
public class ProfileDialog extends JDialog {

    private String colour;
    private String mark;

    public ProfileDialog(Window owner, Profile profile) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        try {
            setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {
            // per pixel translucency is not available everywhere
        }

        this.colour = profile.avatarColour;
        this.mark = profile.avatarMark;

        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Palette.smooth(g.create());
                Glass.dark(g2, this, 0, 0, getWidth(), getHeight(), 26, 168, 120);
                g2.setFont(Palette.tracked(22, Font.PLAIN, 0.02));
                g2.setColor(Palette.CREAM);
                g2.drawString("Your profile", 30f, 44f);
                g2.setFont(Palette.font(12, Font.PLAIN));
                g2.setColor(Palette.alpha(Palette.ROSE, 215));
                g2.drawString("A name for the scoreboard, and a badge to go with it.", 30f, 64f);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(84, 28, 20, 28));

        Preview preview = new Preview();

        JTextField field = new JTextField(profile.name);
        field.setFont(Palette.font(16, Font.PLAIN));
        field.setForeground(Palette.CREAM);
        field.setCaretColor(Palette.ROSE);
        field.setSelectionColor(Palette.alpha(Palette.MAUVE, 160));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel fieldBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Palette.smooth(g.create());
                Glass.panel(g2, this, 0, 0, getWidth(), getHeight(), getHeight() / 2.0, 12, 60);
                g2.dispose();
            }
        };
        fieldBox.setOpaque(false);
        fieldBox.setPreferredSize(new Dimension(0, 44));
        fieldBox.add(field, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.add(preview, BorderLayout.WEST);
        top.add(fieldBox, BorderLayout.CENTER);

        SwatchBar bar = new SwatchBar(new SwatchBar.Listener() {
            @Override
            public void pickedColour(String picked) {
                colour = picked;
                preview.repaint();
            }

            @Override
            public void pickedMark(String picked) {
                if (picked != null && !picked.isEmpty()) {
                    mark = picked;
                    preview.repaint();
                }
            }
        });
        bar.setSelectedColour(colour);
        bar.setSelectedMark(mark);

        JPanel middle = new JPanel(new BorderLayout(0, 14));
        middle.setOpaque(false);
        middle.add(top, BorderLayout.NORTH);
        middle.add(bar, BorderLayout.CENTER);
        card.add(middle, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);
        Pill save = new Pill("Save", Pill.Style.PRIMARY);
        save.addActionListener(e -> {
            profile.name = field.getText();
            profile.avatarColour = colour;
            profile.avatarMark = mark;
            profile.save();
            dispose();
        });
        Pill cancel = new Pill("Cancel", Pill.Style.GHOST);
        cancel.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(cancel);
        card.add(buttons, BorderLayout.SOUTH);

        setContentPane(card);
        setSize(new Dimension(660, 310));
        setLocationRelativeTo(owner);
    }

    /** The avatar as it will look. */
    private final class Preview extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(58, 58);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            double size = Math.min(getWidth(), getHeight()) - 4;
            Color fill = new Color(Colors.rgb(Colors.normalise(colour)));
            g2.setColor(Palette.alpha(Palette.INK, 110));
            g2.fill(new Ellipse2D.Double(2, 5, size, size));
            g2.setColor(fill);
            g2.fill(new Ellipse2D.Double(2, 2, size, size));
            g2.setColor(new Color(255, 255, 255, 120));
            g2.draw(new Ellipse2D.Double(2, 2, size, size));
            g2.setFont(Palette.markFont(mark, (int) (size * 0.5)));
            g2.setColor(Palette.contrast(fill));
            Palette.centerIn(g2, mark, 2, 2, size, size);
            g2.dispose();
        }
    }
}
