package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import java.awt.CardLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import javadable.core.Achievement;
import javadable.core.PowerUp;
import javadable.core.Profile;
import javadable.core.Puzzle;
import javadable.core.Puzzles;
import javadable.core.Reward;
import javadable.core.Runner;

/** The screen where the game is actually played. */
public class PuzzlePanel extends JPanel {

    private final GameFrame frame;
    private final Profile profile;
    private final Runner runner = new Runner();

    private final GridView yours = new GridView("YOUR SKETCH");
    private final GridView target = new GridView("TARGET");
    private final CodeEditor editor = new CodeEditor();
    private final JTextArea console = new JTextArea();
    private final CardLayout tabLayout = new CardLayout();
    private final JPanel tabArea = new JPanel(tabLayout);
    private final Pill codeTab = new Pill("Code", Pill.Style.PRIMARY);
    private final Pill consoleTab = new Pill("Console", Pill.Style.GHOST);
    private final Pill helpTab = new Pill("Cheatsheet", Pill.Style.GHOST);
    private final Pill runButton = new Pill("Run", Pill.Style.PRIMARY);
    private final Header header = new Header();
    private final Counter counter = new Counter();
    private final JPanel powerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final SwatchBar swatches = new SwatchBar(new SwatchBar.Listener() {
        @Override
        public void pickedColour(String colour) {
            insertColour(colour);
        }

        @Override
        public void pickedMark(String mark) {
            insertMark(mark);
        }
    });
    private final Map<PowerUp, Pill> powerPills = new EnumMap<>(PowerUp.class);

    private Puzzle puzzle;
    private boolean daily;
    private int extraBudget;
    private int attempts;
    private boolean usedPowerUp;
    private boolean running;
    private String currentTab = "";

    public PuzzlePanel(GameFrame frame, Profile profile) {
        this.frame = frame;
        this.profile = profile;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 34, 18, 34));
        Sheet sheet = new Sheet(14, 22, 14, 28);
        sheet.setLayout(new BorderLayout(0, 10));
        sheet.add(header, BorderLayout.NORTH);

        JPanel grids = new JPanel(new GridLayout(1, 2, 18, 0));
        grids.setOpaque(false);
        grids.setPreferredSize(new Dimension(0, 300));
        grids.add(yours);
        grids.add(target);

        console.setEditable(false);
        console.setOpaque(false);
        console.setForeground(Palette.CREAM);
        console.setFont(editor.textPane().getFont());
        console.setMargin(new java.awt.Insets(10, 12, 10, 10));
        JScrollPane consoleScroll = new JScrollPane(console);
        consoleScroll.setBorder(BorderFactory.createEmptyBorder());
        consoleScroll.setOpaque(false);
        consoleScroll.getViewport().setOpaque(false);
        SlimScrollBar.apply(consoleScroll);

        JTextArea help = new JTextArea(cheatsheet());
        help.setEditable(false);
        help.setOpaque(false);
        help.setForeground(Palette.alpha(Palette.CREAM, 230));
        help.setFont(editor.textPane().getFont().deriveFont(13f));
        help.setMargin(new java.awt.Insets(10, 12, 10, 10));
        JScrollPane helpScroll = new JScrollPane(help);
        helpScroll.setBorder(BorderFactory.createEmptyBorder());
        helpScroll.setOpaque(false);
        helpScroll.getViewport().setOpaque(false);
        SlimScrollBar.apply(helpScroll);

        tabArea.setOpaque(false);
        tabArea.setBorder(BorderFactory.createEmptyBorder());
        tabArea.add(editor, "code");
        tabArea.add(new GlassBox(consoleScroll, 20, 232, 60), "console");
        tabArea.add(new GlassBox(helpScroll, 20, 232, 60), "help");

        codeTab.addActionListener(e -> showTab("code"));
        consoleTab.addActionListener(e -> showTab("console"));
        helpTab.addActionListener(e -> showTab("help"));
        runButton.addActionListener(e -> run());

        JPanel tabsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabsLeft.setOpaque(false);
        tabsLeft.add(codeTab);
        tabsLeft.add(consoleTab);
        tabsLeft.add(helpTab);

        JPanel tabsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tabsRight.setOpaque(false);
        tabsRight.add(counter);
        tabsRight.add(runButton);

        JPanel toolRow = new JPanel(new BorderLayout());
        toolRow.setOpaque(false);
        toolRow.add(tabsLeft, BorderLayout.WEST);
        toolRow.add(tabsRight, BorderLayout.EAST);

        JPanel lower = new JPanel(new BorderLayout(0, 8));
        lower.setOpaque(false);
        lower.add(toolRow, BorderLayout.NORTH);
        lower.add(tabArea, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(grids, BorderLayout.CENTER);
        top.add(swatches, BorderLayout.SOUTH);

        JPanel centre = new JPanel(new BorderLayout(0, 12));
        centre.setOpaque(false);
        centre.add(top, BorderLayout.NORTH);
        centre.add(lower, BorderLayout.CENTER);
        sheet.add(centre, BorderLayout.CENTER);

        powerRow.setOpaque(false);
        for (PowerUp powerUp : PowerUp.values()) {
            Pill pill = new Pill(powerUp.label, Pill.Style.SOFT);
            pill.setToolTipText(powerUp.blurb);
            pill.addActionListener(e -> usePowerUp(powerUp));
            powerPills.put(powerUp, pill);
            powerRow.add(pill);
        }
        Pill shop = new Pill("Shop", Pill.Style.GHOST);
        shop.addActionListener(e -> {
            new ShopDialog(frame, profile).setVisible(true);
            refreshPowerPills();
            frame.repaint();
        });
        powerRow.add(shop);
        sheet.add(powerRow, BorderLayout.SOUTH);
        add(sheet, BorderLayout.CENTER);

        editor.setOnChange(this::updateCounter);
        installKeys();
    }

    // ------------------------------------------------------------- lifecycle

    public void open(Puzzle puzzle, boolean daily) {
        this.puzzle = puzzle;
        this.daily = daily;
        this.extraBudget = 0;
        this.attempts = 0;
        this.usedPowerUp = false;

        target.setCanvas(puzzle.target(), true);
        yours.setCanvas(new javadable.core.Canvas(puzzle.width(), puzzle.height()), false);
        yours.setCompare(puzzle.target());
        yours.setShowDiff(false);
        yours.setSolved(false);

        String saved = profile.codeFor(puzzle, LocalDate.now());
        editor.setCode(saved == null || saved.trim().isEmpty() ? starter() : saved);
        console.setText("Press Run, or Ctrl and Enter together.\n");
        showTab("code");
        refreshPowerPills();
        updateCounter();
        header.repaint();
        javax.swing.SwingUtilities.invokeLater(editor::requestFocus);
    }

    private void insertColour(String colour) {
        showTab("code");
        char quote = 34;
        editor.insert(quote + colour + quote);
        swatches.setSelectedColour(colour);
    }

    private void insertMark(String mark) {
        if (mark == null || mark.isEmpty()) return;
        showTab("code");
        char quote = 34;
        editor.insert(quote + mark + quote);
        swatches.setSelectedMark(mark);
    }

    private String starter() {
        if (puzzle.index == 0) {
            // the very first level gets a reminder of the one method there is
            return "// p(x, y, colour) paints one cell\n// the grid is W wide and H tall\n";
        }
        return "";
    }

    private void leave() {
        if (puzzle != null) {
            profile.setCodeFor(puzzle, editor.getCode(), LocalDate.now());
            profile.save();
        }
        frame.showLevels();
    }

    private void showTab(String name) {
        if (!name.equals(currentTab)) {
            currentTab = name;
            Fade.swap(tabArea, () -> tabLayout.show(tabArea, name));
        }
        codeTab.setStyle("code".equals(name) ? Pill.Style.PRIMARY : Pill.Style.GHOST);
        consoleTab.setStyle("console".equals(name) ? Pill.Style.PRIMARY : Pill.Style.GHOST);
        helpTab.setStyle("help".equals(name) ? Pill.Style.PRIMARY : Pill.Style.GHOST);
        if ("code".equals(name)) editor.requestFocus();
    }

    private void installKeys() {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK), "run");
        getActionMap().put("run", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "leave");
        getActionMap().put("leave", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leave();
            }
        });
        editor.textPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "indent");
        editor.textPane().getActionMap().put("indent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.textPane().replaceSelection("  ");
            }
        });
    }

    // ------------------------------------------------------------- the round

    private int budget() {
        return puzzle == null ? 0 : puzzle.limit + extraBudget;
    }

    private void run() {
        if (running || puzzle == null) return;
        final String code = editor.getCode();
        profile.setCodeFor(puzzle, code, LocalDate.now());
        running = true;
        runButton.setEnabled(false);
        runButton.setText("Running");

        new SwingWorker<Runner.Result, Void>() {
            @Override
            protected Runner.Result doInBackground() {
                return runner.run(code, puzzle.width(), puzzle.height());
            }

            @Override
            protected void done() {
                running = false;
                runButton.setEnabled(true);
                runButton.setText("Run");
                try {
                    handle(get(), code);
                } catch (Exception e) {
                    console.setText("Something went wrong: " + e);
                    showTab("console");
                }
            }
        }.execute();
    }

    private void handle(Runner.Result result, String code) {
        attempts++;
        profile.runs++;
        yours.setCanvas(result.canvas, true);

        int characters = Runner.characters(code);
        StringBuilder log = new StringBuilder();
        if (!result.console.isEmpty()) log.append(result.console);

        if (result.error != null) {
            log.append(result.error).append('\n');
            console.setText(log.toString());
            showTab("console");
            profile.save();
            return;
        }

        int wrong = result.canvas.mismatches(puzzle.target());
        int cells = puzzle.width() * puzzle.height();
        log.append("ran in ").append(result.millis).append(" ms, ")
                .append(characters).append(" characters").append('\n');

        if (wrong == 0 && characters <= budget()) {
            log.append("solved").append('\n');
            console.setText(log.toString());
            solved(characters, code);
            return;
        }
        if (wrong == 0) {
            log.append("the picture is right, but you are ")
                    .append(characters - budget()).append(" characters over the budget").append('\n');
            showTab("console");
        } else {
            log.append(wrong).append(" of ").append(cells).append(" cells do not match yet").append('\n');
        }
        console.setText(log.toString());
        profile.save();
        updateCounter();
    }

    private void solved(int characters, String code) {
        boolean cleanRun = attempts == 1 && !usedPowerUp;
        Reward reward = profile.solve(puzzle, characters, cleanRun, usedPowerUp);

        List<Achievement> unlocked = new ArrayList<>();
        award(unlocked, Achievement.FIRST_PIXEL, true);
        award(unlocked, Achievement.CLEAN_RUN, cleanRun);
        award(unlocked, Achievement.UNDER_PAR, characters <= puzzle.par);
        award(unlocked, Achievement.MINIMALIST, characters * 2 <= puzzle.par);
        award(unlocked, Achievement.ONE_LINER, codeLines(code) == 1);
        award(unlocked, Achievement.BOSS_SLAYER, puzzle.boss);
        award(unlocked, Achievement.CHAPTER_CLEAR, profile.chapterPerfect(puzzle.chapter));
        award(unlocked, Achievement.CARTOGRAPHER, profile.allSolved());
        award(unlocked, Achievement.PURIST, profile.cleanSolvesWithoutHelp >= 10);
        award(unlocked, Achievement.TOP_OF_THE_PALETTE,
                profile.rank() == javadable.core.Rank.CREAM);

        if (daily) {
            profile.registerDaily(LocalDate.now());
            award(unlocked, Achievement.ON_FIRE, profile.streak >= 5);
            award(unlocked, Achievement.UNSTOPPABLE, profile.streak >= 15);
        }

        profile.save();
        yours.setSolved(true);
        header.repaint();
        updateCounter();
        frame.refreshAll();

        boolean hasNext = !puzzle.isDaily() && puzzle.index + 1 < Puzzles.count()
                && profile.isUnlocked(puzzle.index + 1);
        Runnable next = hasNext ? () -> open(Puzzles.get(puzzle.index + 1), false) : null;

        ResultDialog dialog = new ResultDialog(frame, puzzle, profile.starsFor(puzzle),
                characters, reward, unlocked, next, frame::showLevels);
        dialog.setVisible(true);
    }

    private void award(List<Achievement> unlocked, Achievement achievement, boolean earned) {
        if (earned && profile.unlock(achievement)) unlocked.add(achievement);
    }

    private static int codeLines(String code) {
        int lines = 0;
        for (String line : code.split("\n")) {
            if (!line.trim().isEmpty()) lines++;
        }
        return lines;
    }

    // -------------------------------------------------------------- powerups

    private void usePowerUp(PowerUp powerUp) {
        if (puzzle == null) return;
        if (profile.count(powerUp) <= 0) {
            console.setText("No " + powerUp.label + " left. There are more in the shop.\n");
            showTab("console");
            return;
        }
        profile.consume(powerUp);
        usedPowerUp = true;

        switch (powerUp) {
            case HINT:
                console.setText("Hint: " + puzzle.hint + "\n");
                showTab("console");
                break;
            case DIFF:
                yours.setShowDiff(true);
                console.setText("Every cell that is still wrong now has a red outline.\n");
                break;
            case ROOM:
                extraBudget += 50;
                updateCounter();
                console.setText("Budget raised to " + budget() + " characters for this level.\n");
                break;
            case SKIP:
                if (!puzzle.isDaily() && profile.stars[puzzle.index] == 0) {
                    profile.stars[puzzle.index] = 1;
                    profile.solved++;
                }
                profile.save();
                refreshPowerPills();
                frame.refreshAll();
                frame.showLevels();
                return;
            default:
                break;
        }
        profile.save();
        refreshPowerPills();
        frame.repaint();
    }

    private void refreshPowerPills() {
        for (Map.Entry<PowerUp, Pill> entry : powerPills.entrySet()) {
            int left = profile.count(entry.getKey());
            entry.getValue().setText(entry.getKey().label + "  " + left);
            entry.getValue().setEnabled(left > 0);
        }
        powerRow.revalidate();
        powerRow.repaint();
    }

    private void updateCounter() {
        counter.follow();
        counter.repaint();
    }

    private static String cheatsheet() {
        return "The editor holds the body of a draw() method, so plain Java goes straight in.\n\n"
                + "  p(x, y, colour)           paint one cell\n"
                + "  p(x, y, colour, mark)     paint a cell and write a short mark on it\n"
                + "  in(x, y)                  true when the cell is on the grid\n"
                + "  print(anything)           write a line to the Console tab\n"
                + "  W, H                      width and height of this level\n\n"
                + "Colours: ink, deep, plum, mauve, rose, cream, white, black, red, coral,\n"
                + "orange, gold, yellow, lime, green, mint, teal, cyan, sky, blue, navy,\n"
                + "purple, pink, brown.\n\n"
                + "The bar under the grids writes a colour into your code when you click it.\n"
                + "Press PALETTE on the left of that bar to flip it over to the marks: stars,\n"
                + "hearts, dots and a handful of emoji, which go in the fourth argument of p.\n\n"
                + "Only non whitespace characters count towards the budget, so indent freely.\n"
                + "Ctrl and Enter runs the sketch. Escape goes back to the levels.\n\n"
                + "Example:\n"
                + "  for (int x = 0; x < W; x++) {\n"
                + "      p(x, 2, \"rose\");\n"
                + "  }\n";
    }

    // ----------------------------------------------------------- small parts

    private final class Header extends JPanel {

        Header() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(0, 78));
            Pill back = new Pill("Levels", Pill.Style.GHOST);
            back.addActionListener(e -> leave());
            JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 18));
            west.setOpaque(false);
            west.add(back);
            add(west, BorderLayout.WEST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (puzzle == null) return;
            Graphics2D g2 = Palette.smooth(g.create());
            double left = 118;

            g2.setFont(Palette.tracked(23, Font.BOLD, 0.06));
            g2.setColor(Palette.CREAM);
            String title = (puzzle.isDaily() ? "Daily   " : "Level " + (puzzle.index + 1) + "   ") + puzzle.title;
            g2.drawString(title, (float) left, 34f);

            double after = left + g2.getFontMetrics().stringWidth(title) + 14;
            if (puzzle.boss) {
                Decor.chip(g2, "BOSS", after, 14, Palette.CREAM);
            }

            g2.setFont(Palette.font(13, Font.PLAIN));
            g2.setColor(Palette.alpha(Palette.ROSE, 220));
            g2.drawString(puzzle.chapterName() + "  ·  " + puzzle.brief, (float) left, 58f);

            double right = getWidth();
            Decor.stars(g2, right - 74, 14, 20, profile.starsFor(puzzle));
            g2.setFont(Palette.tracked(11, Font.BOLD, 0.12));
            g2.setColor(Palette.alpha(Palette.ROSE, 210));
            String pars = "par " + puzzle.par + "   ·   budget " + budget();
            g2.drawString(pars, (float) (right - g2.getFontMetrics().stringWidth(pars)), 58f);
            g2.dispose();
        }
    }

    private final class Counter extends JComponent {

        private double shown;
        private javax.swing.Timer ease;

        Counter() {
            ease = new javax.swing.Timer(16, e -> {
                double target = puzzle == null ? 0 : Runner.characters(editor.getCode());
                if (Math.abs(shown - target) < 0.4) {
                    shown = target;
                    ease.stop();
                } else {
                    shown += (target - shown) * 0.22;
                }
                repaint();
            });
        }

        void follow() {
            ease.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(190, 42);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (puzzle == null) return;
            Graphics2D g2 = Palette.smooth(g.create());
            int characters = Runner.characters(editor.getCode());
            boolean over = characters > budget();
            boolean underPar = characters <= puzzle.par && characters > 0;

            g2.setFont(Palette.tracked(13, Font.BOLD, 0.06));
            g2.setColor(over ? new Color(0xE2, 0x49, 0x5B)
                    : underPar ? Palette.CREAM : Palette.alpha(Palette.ROSE, 230));
            String text = characters + " / " + budget() + " chars";
            g2.drawString(text, 4f, 18f);

            Color fill = over ? new Color(0xE2, 0x49, 0x5B)
                    : underPar ? Palette.CREAM : Palette.MAUVE;
            Decor.meter(g2, 4, 24, getWidth() - 10, 8, shown / budget(), fill);

            double parMark = 4 + (getWidth() - 10) * Math.min(1.0, puzzle.par / (double) budget());
            g2.setColor(Palette.alpha(Palette.CREAM, 200));
            g2.drawLine((int) parMark, 22, (int) parMark, 34);
            g2.dispose();
        }
    }
}
