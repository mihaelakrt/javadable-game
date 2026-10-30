package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import javadable.core.Achievement;
import javadable.core.Puzzle;
import javadable.core.Reward;

/** The card that drops in when a level is solved. */
public class ResultDialog extends JDialog {

    public ResultDialog(Window owner, Puzzle puzzle, int stars, int characters, Reward reward,
                        List<Achievement> unlocked, Runnable onNext, Runnable onLevels) {
        super(owner, ModalityType.APPLICATION_MODAL);
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
                int width = getWidth();
                int height = getHeight();
                Glass.dark(g2, this, 0, 0, width, height, 28, 168, 135);

                g2.setFont(Palette.tracked(13, Font.BOLD, 0.2));
                g2.setColor(Palette.alpha(Palette.ROSE, 220));
                Palette.centerString(g2, puzzle.title.toUpperCase(), width / 2.0, 36);

                g2.setFont(Palette.tracked(30, Font.BOLD, 0.08));
                g2.setColor(Palette.CREAM);
                Palette.centerString(g2, stars == 3 ? "Beautiful" : "Solved", width / 2.0, 74);

                Decor.stars(g2, width / 2.0 - 41, 88, 26, stars);

                g2.setFont(Palette.font(14, Font.PLAIN));
                g2.setColor(Palette.alpha(Palette.ROSE, 230));
                String line = characters + " characters, par is " + puzzle.par;
                Palette.centerString(g2, line, width / 2.0, 142);

                g2.setColor(Palette.CREAM);
                g2.setFont(Palette.tracked(14, Font.BOLD, 0.04));
                String gains = "+" + reward.xp + " xp    +" + reward.coins + " coins";
                Palette.centerString(g2, gains, width / 2.0, 166);

                if (!unlocked.isEmpty()) {
                    g2.setFont(Palette.tracked(11, Font.BOLD, 0.16));
                    g2.setColor(Palette.alpha(Palette.ROSE, 200));
                    Palette.centerString(g2, "UNLOCKED", width / 2.0, 194);
                    g2.setFont(Palette.font(13, Font.PLAIN));
                    g2.setColor(Palette.CREAM);
                    int y = 214;
                    for (Achievement achievement : unlocked) {
                        Palette.centerString(g2, achievement.label, width / 2.0, y);
                        y += 19;
                    }
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        int extra = unlocked.isEmpty() ? 0 : 24 + unlocked.size() * 19;
        card.setBorder(BorderFactory.createEmptyBorder(190 + extra, 24, 20, 24));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        if (onNext != null) {
            Pill next = new Pill("Next level", Pill.Style.PRIMARY);
            next.addActionListener(e -> {
                dispose();
                onNext.run();
            });
            buttons.add(next);
        }

        Pill stay = new Pill("Keep golfing", onNext == null ? Pill.Style.PRIMARY : Pill.Style.GHOST);
        stay.addActionListener(e -> dispose());
        buttons.add(stay);

        Pill levels = new Pill("Levels", Pill.Style.GHOST);
        levels.addActionListener(e -> {
            dispose();
            if (onLevels != null) onLevels.run();
        });
        buttons.add(levels);

        card.add(buttons, BorderLayout.CENTER);
        setContentPane(card);
        setSize(new Dimension(520, 262 + extra));
        setLocationRelativeTo(owner);
    }
}
