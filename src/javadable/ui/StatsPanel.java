package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javadable.core.Achievement;
import javadable.core.Profile;
import javadable.core.Puzzles;

/** Numbers, badges and where the save file lives. */
public class StatsPanel extends JPanel {

    private final Profile profile;
    private final JPanel badges = new JPanel(new GridLayout(0, 2, 12, 12));

    public StatsPanel(GameFrame frame, Profile profile) {
        this.profile = profile;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 34, 18, 34));
        Sheet sheet = new Sheet(16, 26, 18, 28);
        sheet.setLayout(new BorderLayout(0, 12));
        add(sheet, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 190));
        header.add(new Numbers(), BorderLayout.CENTER);
        Pill back = new Pill("Home", Pill.Style.GHOST);
        back.addActionListener(e -> frame.showHome());
        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
        west.setOpaque(false);
        west.add(back);
        header.add(west, BorderLayout.WEST);
        sheet.add(header, BorderLayout.NORTH);

        badges.setOpaque(false);
        JScrollPane scroll = new JScrollPane(badges);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        SlimScrollBar.apply(scroll);
        sheet.add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        badges.removeAll();
        for (Achievement achievement : Achievement.values()) {
            badges.add(new Badge(achievement));
        }
        badges.revalidate();
        badges.repaint();
        repaint();
    }

    private final class Numbers extends JPanel {

        Numbers() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            g2.setFont(Palette.tracked(28, Font.BOLD, 0.1));
            g2.setColor(Palette.CREAM);
            g2.drawString("Stats", 78f, 38f);

            String[] labels = {"SOLVED", "STARS", "RUNS", "STREAK", "BEST STREAK", "RANK"};
            String[] values = {
                    profile.solvedCount() + " / " + Puzzles.count(),
                    profile.totalStars() + " / " + (Puzzles.count() * 3),
                    String.valueOf(profile.runs),
                    String.valueOf(profile.streak),
                    String.valueOf(profile.bestStreak),
                    profile.rank().label};

            double cardWidth = (getWidth() - 10.0) / 6 - 10;
            for (int i = 0; i < labels.length; i++) {
                double x = i * (cardWidth + 12);
                Glass.panel(g2, this, x, 62, cardWidth, 74, 20);
                g2.setFont(Palette.tracked(10, Font.BOLD, 0.18));
                g2.setColor(Palette.alpha(Palette.ROSE, 200));
                Palette.centerIn(g2, labels[i], x, 72, cardWidth, 20);
                g2.setFont(Palette.tracked(22, Font.BOLD, 0.04));
                g2.setColor(Palette.CREAM);
                Palette.centerIn(g2, values[i], x, 96, cardWidth, 30);
            }

            g2.setFont(Palette.font(11, Font.PLAIN));
            g2.setColor(Palette.alpha(Palette.ROSE, 160));
            g2.drawString("progress is kept in " + Profile.file(), 2f, 158f);
            g2.dispose();
        }
    }

    private final class Badge extends JPanel {

        private final Achievement achievement;

        Badge(Achievement achievement) {
            this.achievement = achievement;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 74));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            boolean earned = profile.has(achievement);
            Glass.panel(g2, this, 0, 0, getWidth(), getHeight() - 4, 22,
                    earned ? 52 : 18, earned ? 150 : 70);

            Decor.diamond(g2, 20, 26, 18, earned);

            g2.setFont(Palette.tracked(15, Font.BOLD, 0.05));
            g2.setColor(earned ? Palette.CREAM : Palette.alpha(Palette.CREAM, 120));
            g2.drawString(achievement.label, 56f, 32f);
            g2.setFont(Palette.font(12, Font.PLAIN));
            g2.setColor(Palette.alpha(Palette.ROSE, earned ? 220 : 130));
            g2.drawString(achievement.blurb, 56f, 52f);
            g2.dispose();
        }
    }
}
