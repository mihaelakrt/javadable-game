package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javadable.core.Profile;
import javadable.core.Puzzle;
import javadable.core.Puzzles;

/** The level picker. */
public class LevelsPanel extends JPanel {

    private final GameFrame frame;
    private final Profile profile;
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));

    public LevelsPanel(GameFrame frame, Profile profile) {
        this.frame = frame;
        this.profile = profile;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 34, 18, 34));
        Sheet sheet = new Sheet(16, 26, 18, 28);
        sheet.setLayout(new BorderLayout(0, 12));
        add(sheet, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Palette.smooth(g.create());
                g2.setFont(Palette.tracked(28, Font.BOLD, 0.1));
                g2.setColor(Palette.CREAM);
                g2.drawString("Levels", 178f, 38f);
                g2.setFont(Palette.font(13, Font.PLAIN));
                g2.setColor(Palette.alpha(Palette.ROSE, 220));
                g2.drawString(profile.solvedCount() + " of " + Puzzles.count() + " solved   ·   "
                        + profile.totalStars() + " of " + (Puzzles.count() * 3) + " stars", 178f, 58f);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 70));
        Pill back = new Pill("Home", Pill.Style.GHOST);
        back.addActionListener(e -> frame.showHome());
        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
        west.setOpaque(false);
        west.add(back);
        header.add(west, BorderLayout.WEST);
        sheet.add(header, BorderLayout.NORTH);

        grid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        SlimScrollBar.apply(scroll);
        sheet.add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        grid.removeAll();
        for (Puzzle puzzle : Puzzles.all()) {
            grid.add(new Card(puzzle));
        }
        grid.revalidate();
        grid.repaint();
        repaint();
    }

    private final class Card extends JPanel {

        private final Puzzle puzzle;
        private float hover;
        private boolean pointerInside;
        private javax.swing.Timer animator;

        Card(Puzzle puzzle) {
            this.puzzle = puzzle;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 132));
            boolean unlocked = profile.isUnlocked(puzzle.index);
            if (unlocked) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            animator = new javax.swing.Timer(16, e -> {
                float goal = pointerInside ? 1f : 0f;
                if (Math.abs(hover - goal) < 0.02f) {
                    hover = goal;
                    animator.stop();
                } else {
                    hover += (goal - hover) * 0.2f;
                }
                repaint();
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    pointerInside = true;
                    animator.start();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    pointerInside = false;
                    animator.start();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (profile.isUnlocked(Card.this.puzzle.index)) {
                        frame.play(Card.this.puzzle, false);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            boolean unlocked = profile.isUnlocked(puzzle.index);
            int stars = profile.stars[puzzle.index];

            double lift = 3 * hover;
            g2.translate(0, -lift);
            Glass.panel(g2, this, 0, 0, getWidth(), getHeight() - 4, 24,
                    unlocked ? (int) (9 + 30 * hover) : 6,
                    puzzle.boss ? (int) (120 + 90 * hover) : (int) (38 + 90 * hover));

            g2.setFont(Palette.tracked(10, Font.BOLD, 0.2));
            g2.setColor(Palette.alpha(Palette.ROSE, 190));
            g2.drawString(puzzle.chapterName().toUpperCase()
                    + (puzzle.boss ? "   ·   BOSS" : ""), 18f, 26f);

            g2.setFont(Palette.tracked(20, Font.BOLD, 0.05));
            g2.setColor(unlocked ? Palette.CREAM : Palette.alpha(Palette.CREAM, 110));
            g2.drawString((puzzle.index + 1) + ".  " + (unlocked ? puzzle.title : "Locked"), 18f, 54f);

            g2.setFont(Palette.font(12, Font.PLAIN));
            g2.setColor(Palette.alpha(Palette.ROSE, unlocked ? 215 : 110));
            String line = unlocked
                    ? puzzle.width() + " by " + puzzle.height() + "   ·   par " + puzzle.par
                    : "solve the level before it";
            g2.drawString(line, 18f, 76f);

            if (unlocked && profile.best[puzzle.index] > 0) {
                g2.setColor(Palette.alpha(Palette.CREAM, 200));
                g2.drawString("best " + profile.best[puzzle.index] + " chars", 18f, 96f);
            }

            Decor.stars(g2, 18, getHeight() - 36, 17, stars);

            double preview = Math.min(74, getHeight() - 44);
            Decor.miniGrid(g2, puzzle.target(), getWidth() - preview - 20,
                    (getHeight() - preview) / 2 - 2, preview, !unlocked);
            g2.dispose();
        }
    }
}
