package javadable.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javadable.core.Profile;
import javadable.core.Puzzle;

/** The window, and the four screens inside it. */
public class GameFrame extends JFrame {

    private final Profile profile;
    private final CardLayout cards = new CardLayout();
    private final JPanel root;
    private final HomePanel home;
    private final LevelsPanel levels;
    private final StatsPanel stats;
    private final StudioPanel studio;
    private final PuzzlePanel puzzlePanel;

    public GameFrame() {
        this.profile = Profile.load();

        root = new JPanel(cards) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Palette.smooth(g.create());
                Backdrop.paint(g2, getWidth(), getHeight(), profile.rank());
                g2.dispose();
            }
        };
        root.setOpaque(true);
        Backdrop.setRoot(root);

        // the colour field drifts, so the glass on top of it never looks static
        new javax.swing.Timer(40, e -> {
            if (isShowing()) root.repaint();
        }).start();

        home = new HomePanel(this, profile);
        levels = new LevelsPanel(this, profile);
        stats = new StatsPanel(this, profile);
        studio = new StudioPanel(this);
        puzzlePanel = new PuzzlePanel(this, profile);

        root.add(home, "home");
        root.add(levels, "levels");
        root.add(stats, "stats");
        root.add(studio, "studio");
        root.add(puzzlePanel, "puzzle");

        setContentPane(root);
        setTitle("Javadable");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        java.awt.Rectangle screen = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(1180, screen.width - 60);
        int height = Math.min(900, screen.height - 50);
        setSize(width, height);
        setMinimumSize(new Dimension(Math.min(940, width), Math.min(680, height)));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                profile.save();
            }
        });

        showHome();
    }

    public Profile profile() {
        return profile;
    }

    public void showHome() {
        Fade.swap(root, () -> {
            home.refresh();
            cards.show(root, "home");
        });
    }

    public void showLevels() {
        Fade.swap(root, () -> {
            levels.refresh();
            cards.show(root, "levels");
        });
    }

    public void showStudio() {
        Fade.swap(root, () -> {
            studio.refresh();
            cards.show(root, "studio");
        });
    }

    public void showStats() {
        Fade.swap(root, () -> {
            stats.refresh();
            cards.show(root, "stats");
        });
    }

    public void play(Puzzle puzzle, boolean daily) {
        Fade.swap(root, () -> {
            puzzlePanel.open(puzzle, daily);
            cards.show(root, "puzzle");
        });
    }

    public void refreshAll() {
        home.refresh();
        levels.refresh();
        stats.refresh();
        repaint();
    }
}
