package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javadable.core.Achievement;
import javadable.core.Colors;
import javadable.core.Profile;
import javadable.core.Puzzle;
import javadable.core.Puzzles;
import javadable.core.Rank;

/** Who you are and how far you have come. */
public class ProfilePanel extends JPanel {

    private final GameFrame frame;
    private final Profile profile;
    private final Header header = new Header();
    private final JPanel badges = new JPanel(new GridLayout(0, 2, 12, 12)) {
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    };
    private final LevelTable table = new LevelTable();

    public ProfilePanel(GameFrame frame, Profile profile) {
        this.frame = frame;
        this.profile = profile;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 34, 18, 34));

        Sheet sheet = new Sheet(16, 26, 18, 28);
        sheet.setLayout(new BorderLayout(0, 12));
        add(sheet, BorderLayout.CENTER);
        sheet.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(new Summary());
        body.add(Box.createVerticalStrut(18));
        body.add(table);
        body.add(Box.createVerticalStrut(18));
        body.add(new SectionTitle("BADGES"));
        body.add(Box.createVerticalStrut(8));
        badges.setOpaque(false);
        body.add(badges);

        JScrollPane scroll = new JScrollPane(body);
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
        table.revalidate();
        repaint();
    }

    /** The avatar, the name and the rank. */
    private final class Header extends JPanel {

        Header() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(0, 108));

            Pill home = new Pill("Home", Pill.Style.GHOST);
            home.addActionListener(e -> frame.showHome());
            JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
            west.setOpaque(false);
            west.add(home);
            add(west, BorderLayout.WEST);

            Pill edit = new Pill("Edit profile", Pill.Style.GHOST);
            edit.addActionListener(e -> {
                new ProfileDialog(frame, profile).setVisible(true);
                profile.save();
                frame.refreshAll();
            });
            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
            east.setOpaque(false);
            east.add(edit);
            add(east, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            double size = 58;
            double left = 120;
            double top = 16;

            Color fill = new Color(Colors.rgb(Colors.normalise(profile.avatarColour)));
            g2.setColor(Palette.alpha(Palette.INK, 110));
            g2.fill(new Ellipse2D.Double(left, top + 3, size, size));
            g2.setColor(fill);
            g2.fill(new Ellipse2D.Double(left, top, size, size));
            g2.setColor(new Color(255, 255, 255, 110));
            g2.draw(new Ellipse2D.Double(left, top, size, size));
            g2.setFont(Palette.markFont(profile.avatarMark, (int) (size * 0.5)));
            g2.setColor(Palette.contrast(fill));
            Palette.centerIn(g2, profile.avatarMark, left, top, size, size);

            double textLeft = left + size + 18;
            g2.setFont(Palette.tracked(26, Font.PLAIN, -0.01));
            g2.setColor(Palette.CREAM);
            g2.drawString(profile.displayName(), (float) textLeft, (float) (top + 26));

            Rank rank = profile.rank();
            int next = rank.nextAt();
            g2.setFont(Palette.tracked(10, Font.BOLD, 0.16));
            g2.setColor(Palette.alpha(Palette.ROSE, 215));
            String line = rank.label.toUpperCase() + "   ·   " + profile.xp
                    + (next < 0 ? " XP" : " / " + next + " XP")
                    + (profile.firstPlayed.isEmpty() ? "" : "   ·   SINCE " + profile.firstPlayed);
            g2.drawString(line, (float) textLeft, (float) (top + 46));

            double meterWidth = Math.min(320, getWidth() - textLeft - 180);
            double fraction = next < 0 ? 1
                    : (profile.xp - rank.xp) / (double) Math.max(1, next - rank.xp);
            Decor.meter(g2, textLeft, top + 56, meterWidth, 8, fraction, Palette.ROSE);
            g2.dispose();
        }
    }

    private static final class SectionTitle extends JComponent {

        private final String text;

        SectionTitle(String text) {
            this.text = text;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 22);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 22);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            g2.setFont(Palette.tracked(10, Font.BOLD, 0.2));
            g2.setColor(Palette.alpha(Palette.ROSE, 200));
            g2.drawString(text, 2f, 14f);
            g2.dispose();
        }
    }

    /** The row of numbers. */
    private final class Summary extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 104);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 104);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            int[] par = profile.parRecord();
            String[] labels = {"SOLVED", "STARS", "COMPLETE", "STREAK", "BEST STREAK", "UNDER PAR"};
            String[] values = {
                    profile.solvedCount() + " / " + Puzzles.count(),
                    profile.totalStars() + " / " + (Puzzles.count() * 3),
                    profile.completion() + "%",
                    String.valueOf(profile.streak),
                    String.valueOf(profile.bestStreak),
                    par[0] + " / " + (par[0] + par[1])};

            double cardWidth = (getWidth() - 2.0) / labels.length - 10;
            for (int i = 0; i < labels.length; i++) {
                double x = i * (cardWidth + 12);
                Glass.panel(g2, this, x, 14, cardWidth, 76, 20);
                g2.setFont(Palette.tracked(10, Font.BOLD, 0.16));
                g2.setColor(Palette.alpha(Palette.ROSE, 200));
                Palette.centerIn(g2, labels[i], x, 24, cardWidth, 20);
                g2.setFont(Palette.tracked(21, Font.PLAIN, 0.02));
                g2.setColor(Palette.CREAM);
                Palette.centerIn(g2, values[i], x, 48, cardWidth, 30);
            }
            g2.dispose();
        }
    }

    /** Every level, with the stars and the best run on it. */
    private final class LevelTable extends JComponent {

        private static final int ROW = 27;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 30 + Puzzles.count() * ROW);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            g2.setFont(Palette.tracked(10, Font.BOLD, 0.2));
            g2.setColor(Palette.alpha(Palette.ROSE, 200));
            g2.drawString("LEVELS", 2f, 14f);

            double width = getWidth();
            java.awt.Rectangle clip = g2.getClipBounds();
            for (Puzzle puzzle : Puzzles.all()) {
                int index = puzzle.index;
                double y = 26 + index * ROW;
                if (clip != null && (y + ROW < clip.y || y > clip.y + clip.height)) continue;
                boolean unlocked = profile.isUnlocked(index);
                int stars = profile.stars[index];
                int record = profile.best[index];

                if (index % 2 == 0) {
                    g2.setColor(new Color(255, 255, 255, 8));
                    g2.fillRoundRect(0, (int) y, (int) width, ROW - 3, 10, 10);
                }

                g2.setFont(Palette.tracked(12, Font.BOLD, 0.04));
                g2.setColor(Palette.alpha(Palette.CREAM, unlocked ? 235 : 110));
                g2.drawString(String.valueOf(index + 1), 10f, (float) (y + 16));
                g2.setFont(Palette.font(12, Font.PLAIN));
                g2.drawString(unlocked ? puzzle.title : "locked", 40f, (float) (y + 16));

                g2.setFont(Palette.tracked(9, Font.BOLD, 0.16));
                g2.setColor(Palette.alpha(Palette.ROSE, 170));
                g2.drawString(puzzle.chapterName().toUpperCase(), 160f, (float) (y + 16));

                Decor.stars(g2, 250, y + 5, 12, stars);

                if (record > 0) {
                    boolean underPar = record <= puzzle.par;
                    g2.setFont(Palette.font(11, Font.PLAIN));
                    g2.setColor(underPar ? Palette.CREAM : Palette.alpha(Palette.ROSE, 210));
                    String text = record + " chars   ·   par " + puzzle.par;
                    g2.drawString(text, 310f, (float) (y + 16));

                    double meterLeft = 470;
                    double meterWidth = Math.max(40, width - meterLeft - 16);
                    Decor.meter(g2, meterLeft, y + 8, meterWidth, 7,
                            record / (double) Math.max(1, puzzle.limit),
                            underPar ? Palette.CREAM : Palette.MAUVE);
                    double parMark = meterLeft + meterWidth
                            * Math.min(1.0, puzzle.par / (double) puzzle.limit);
                    g2.setColor(Palette.alpha(Palette.CREAM, 190));
                    g2.drawLine((int) parMark, (int) y + 5, (int) parMark, (int) y + 20);
                } else if (unlocked) {
                    g2.setFont(Palette.font(11, Font.PLAIN));
                    g2.setColor(Palette.alpha(Palette.ROSE, 140));
                    g2.drawString((stars > 0 ? "skipped" : "not solved yet")
                            + "   ·   par " + puzzle.par, 310f, (float) (y + 16));
                }
            }
            g2.dispose();
        }
    }

    private final class Badge extends JPanel {

        private final Achievement achievement;

        Badge(Achievement achievement) {
            this.achievement = achievement;
            setOpaque(false);
            setPreferredSize(new Dimension(300, 74));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            boolean earned = profile.has(achievement);
            Glass.panel(g2, this, 0, 0, getWidth(), getHeight() - 4, 22,
                    earned ? 30 : 8, earned ? 130 : 40);

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
