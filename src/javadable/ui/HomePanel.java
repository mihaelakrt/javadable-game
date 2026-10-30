package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import javadable.core.Profile;
import javadable.core.Puzzles;
import javadable.core.Rank;
import javadable.core.Runner;

/** The front door, laid out like a single sheet of dark glass. */
public class HomePanel extends JPanel {

    private final GameFrame frame;
    private final Profile profile;
    private final Pill daily = new Pill("Daily puzzle", Pill.Style.GHOST);
    private final Footer footer = new Footer();

    public HomePanel(GameFrame frame, Profile profile) {
        this.frame = frame;
        this.profile = profile;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(34, 90, 26, 90));

        Sheet sheet = new Sheet(34, 44, 30);
        sheet.add(new Masthead(), BorderLayout.NORTH);
        sheet.add(new Hero(), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 14));
        south.setOpaque(false);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        links.setOpaque(false);
        daily.addActionListener(e -> frame.play(Puzzles.daily(LocalDate.now()), true));
        links.add(daily);
        links.add(link("Studio", e -> frame.showStudio()));
        links.add(link("Stats", e -> frame.showStats()));
        links.add(link("How to play", e -> new HelpDialog(frame).setVisible(true)));
        links.add(link("Quit", e -> {
            profile.save();
            System.exit(0);
        }));
        south.add(links, BorderLayout.NORTH);

        JPanel action = new JPanel(new BorderLayout());
        action.setOpaque(false);
        action.setPreferredSize(new Dimension(0, 58));

        CircleButton play = new CircleButton(50, "Play");
        play.addActionListener(e -> frame.showLevels());
        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        west.setOpaque(false);
        west.add(play);
        west.add(new PlayLabel());
        action.add(west, BorderLayout.WEST);
        action.add(new Mark(), BorderLayout.EAST);
        south.add(action, BorderLayout.SOUTH);

        sheet.add(south, BorderLayout.SOUTH);
        add(sheet, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        refresh();
    }

    private Pill link(String text, java.awt.event.ActionListener action) {
        Pill pill = new Pill(text, Pill.Style.GHOST);
        pill.addActionListener(action);
        return pill;
    }

    public void refresh() {
        boolean done = profile.dailyDoneToday(LocalDate.now());
        daily.setText(done ? "Daily done" : "Daily puzzle");
        daily.setEnabled(!done);
        repaint();
    }

    /** The small print along the top of the sheet. */
    private final class Masthead extends JPanel {

        Masthead() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            g2.setFont(Palette.tracked(11, Font.PLAIN, 0.06));
            g2.setColor(Palette.alpha(Palette.CREAM, 165));
            g2.drawString("Code puzzle /", 0f, 13f);
            g2.drawString("Draw it with loops", 0f, 28f);

            g2.setFont(Palette.tracked(11, Font.BOLD, 0.06));
            g2.setColor(Palette.alpha(Palette.CREAM, 215));
            String right = "+Javadable";
            g2.drawString(right, getWidth() - g2.getFontMetrics().stringWidth(right), 13f);
            g2.dispose();
        }
    }

    /** The headline. */
    private final class Hero extends JPanel {

        Hero() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            double top = getHeight() * 0.30;

            g2.setFont(Palette.tracked(50, Font.PLAIN, -0.018));
            g2.setColor(Palette.CREAM);
            g2.drawString("Eighteen pictures.", 0f, (float) top);
            g2.drawString("One method.", 0f, (float) (top + 58));
            g2.drawString("Fewer characters.", 0f, (float) (top + 116));

            g2.setFont(Palette.font(14, Font.PLAIN));
            g2.setColor(Palette.alpha(Palette.ROSE, 215));
            g2.drawString("Write Java in the panel, run it, and watch the grid",
                    0f, (float) (top + 152));
            g2.drawString("fill in. Under par is three stars.", 0f, (float) (top + 171));

            if (!Runner.available()) {
                g2.setFont(Palette.font(13, Font.BOLD));
                g2.setColor(new Color(0xF2, 0x41, 0x5A));
                g2.drawString("No Java compiler here. Start the game with a JDK.",
                        0f, (float) (top + 200));
            }
            g2.dispose();
        }
    }

    private final class PlayLabel extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(70, 50);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            g2.setFont(Palette.tracked(12, Font.BOLD, 0.22));
            g2.setColor(Palette.alpha(Palette.CREAM, 230));
            g2.drawString("PLAY", 0f, 30f);
            g2.dispose();
        }
    }

    private final class Mark extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(54, 54);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            Decor.asterisk(g2, getWidth() / 2.0, getHeight() / 2.0 + 2, 15,
                    Palette.alpha(Palette.CREAM, 235));
            g2.dispose();
        }
    }

    /** The little pill under the sheet, the way a caption sits under a card. */
    private final class Footer extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 36);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            Rank rank = profile.rank();
            int next = rank.nextAt();
            String text = rank.label.toUpperCase()
                    + "   ·   " + profile.xp + (next < 0 ? " XP" : " / " + next + " XP")
                    + "   ·   STREAK " + profile.streak
                    + "   ·   " + profile.coins + " COINS"
                    + "   ·   " + profile.solvedCount() + " / " + Puzzles.count() + " SOLVED";

            g2.setFont(Palette.tracked(10, Font.BOLD, 0.14));
            double width = g2.getFontMetrics().stringWidth(text) + 40;
            double x = (getWidth() - width) / 2;
            RoundRectangle2D shape = new RoundRectangle2D.Double(x, 2, width, 30, 30, 30);
            g2.setColor(new Color(255, 255, 255, 14));
            g2.fill(shape);
            g2.setColor(new Color(255, 255, 255, 60));
            g2.draw(shape);
            g2.setColor(Palette.alpha(Palette.CREAM, 225));
            Palette.centerIn(g2, text, x, 2, width, 30);
            g2.dispose();
        }
    }
}
