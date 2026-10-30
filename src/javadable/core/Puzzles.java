package javadable.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The level catalogue: six chapters of three, the last of each one a boss. */
public final class Puzzles {

    public interface Painter {
        void paint(Canvas canvas);
    }

    /** How many levels the generator adds behind the hand made ones. */
    public static final int GENERATED = 270;

    private static final String[] CHAPTER_BASE = {
            "Midnight", "Indigo", "Plum", "Mauve", "Rose", "Cream"
    };

    public static String[] CHAPTERS = CHAPTER_BASE;

    private static final String[] PALETTE = {"ink", "deep", "plum", "mauve", "rose", "cream"};
    private static final List<Puzzle> ALL = new ArrayList<>();

    static {
        build();
    }

    private Puzzles() {
    }

    public static List<Puzzle> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static Puzzle get(int index) {
        return ALL.get(Math.max(0, Math.min(index, ALL.size() - 1)));
    }

    public static int count() {
        return ALL.size();
    }

    /**
     * A fresh level for the day, made up on the spot from the date, so everyone
     * playing on the same day gets the same picture and nobody has seen it before.
     */
    public static Puzzle daily(LocalDate date) {
        long seed = date.toEpochDay();
        int tier = 2 + (int) Math.floorMod(seed, 6);
        Generator.Made made = Generator.make(seed * 7919 + 13, tier);
        return new Puzzle(-1, 0, made.title, made.brief, made.hint, false,
                made.par(), made.limit(), made.target);
    }

    private static void add(int chapter, String title, String brief, String hint, boolean boss,
                            int par, int limit, int width, int height, Painter painter) {
        Canvas target = new Canvas(width, height);
        painter.paint(target);
        ALL.add(new Puzzle(ALL.size(), chapter, title, brief, hint, boss, par, limit, target));
    }

    private static void addRows(int chapter, String title, String brief, String hint, boolean boss,
                                int par, int limit, String[] rows) {
        Canvas target = Canvas.fromRows(rows);
        ALL.add(new Puzzle(ALL.size(), chapter, title, brief, hint, boss, par, limit, target));
    }

    private static void build() {

        // ------------------------------------------------------- Midnight

        add(0, "Hello dot",
                "One red square, dead centre of a five by five grid.",
                "The middle of a five wide grid is column 2, row 2.",
                false, 20, 45, 5, 5,
                canvas -> canvas.set(2, 2, "red", null));

        add(0, "Horizon",
                "A cyan line straight across the middle.",
                "Loop x from 0 to W and paint row 2 every time.",
                false, 40, 80, 5, 5,
                canvas -> {
                    for (int x = 0; x < 5; x++) canvas.set(x, 2, "cyan", null);
                });

        add(0, "Four corners",
                "Gold in all four corners, nothing in between.",
                "Two nested loops over 0 and 1, times four, gives the corners.",
                false, 65, 120, 5, 5,
                canvas -> {
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) canvas.set(i * 4, j * 4, "gold", null);
                    }
                });

        add(0, "Plus",
                "A coral cross through the middle row and the middle column.",
                "One loop can paint both: row 2 and column 2 at the same index.",
                false, 60, 115, 5, 5,
                canvas -> {
                    for (int i = 0; i < 5; i++) {
                        canvas.set(i, 2, "coral", null);
                        canvas.set(2, i, "coral", null);
                    }
                });

        add(0, "Frame",
                "A white border around an empty middle.",
                "A cell is on the border when x or y is 0, or W-1, or H-1.",
                true, 100, 180, 5, 5,
                canvas -> {
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            if (x == 0 || y == 0 || x == 4 || y == 4) canvas.set(x, y, "white", null);
                        }
                    }
                });

        // --------------------------------------------------------- Indigo

        add(1, "Slash",
                "A lime diagonal from the top left corner.",
                "One loop is enough: the cells where x equals y.",
                false, 40, 80, 5, 5,
                canvas -> {
                    for (int i = 0; i < 5; i++) canvas.set(i, i, "lime", null);
                });

        add(1, "Crossfire",
                "A red X with a gold square where the arms meet.",
                "Paint both diagonals red, then drop gold on the centre.",
                false, 75, 145, 5, 5,
                canvas -> {
                    for (int i = 0; i < 5; i++) {
                        canvas.set(i, i, "red", null);
                        canvas.set(i, 4 - i, "red", null);
                    }
                    canvas.set(2, 2, "gold", null);
                });

        add(1, "Zigzag",
                "Lime squares bouncing between row 1 and row 3.",
                "Row 1 on even columns, row 3 on odd ones: x % 2 decides.",
                false, 55, 110, 5, 5,
                canvas -> {
                    for (int x = 0; x < 5; x++) canvas.set(x, x % 2 == 0 ? 1 : 3, "lime", null);
                });

        addRows(1, "Arrow",
                "A sky blue arrow pointing up.",
                "Five rows of five. A string and one loop will do it.",
                false, 120, 220, new String[]{
                        "..s..",
                        ".sss.",
                        "sssss",
                        "..s..",
                        "..s.."});

        add(1, "Checkerboard",
                "White and red, every other square, nothing left empty.",
                "Paint white when x plus y is even, red when it is odd.",
                true, 85, 155, 5, 5,
                canvas -> {
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            canvas.set(x, y, (x + y) % 2 == 0 ? "white" : "red", null);
                        }
                    }
                });

        // ----------------------------------------------------------- Plum

        add(2, "The Stack",
                "The house palette, one colour per row, dark at the top.",
                "Put the six names in an array and index it with y.",
                false, 120, 200, 6, 6,
                canvas -> {
                    for (int y = 0; y < 6; y++) {
                        for (int x = 0; x < 6; x++) canvas.set(x, y, PALETTE[y], null);
                    }
                });

        add(2, "Staircase",
                "A mint staircase climbing from the top left.",
                "On row y, paint every x that is less than or equal to y.",
                false, 65, 120, 6, 6,
                canvas -> {
                    for (int y = 0; y < 6; y++) {
                        for (int x = 0; x <= y; x++) canvas.set(x, y, "mint", null);
                    }
                });

        add(2, "Stripes",
                "Vertical bands cycling cyan, blue, navy.",
                "The band is x % 3, so index an array of three names with it.",
                false, 95, 170, 6, 6,
                canvas -> {
                    String[] band = {"cyan", "blue", "navy"};
                    for (int x = 0; x < 6; x++) {
                        for (int y = 0; y < 6; y++) canvas.set(x, y, band[x % 3], null);
                    }
                });

        add(2, "Smiley",
                "A gold face with two dark eyes and a brown mouth.",
                "Fill it gold, then write the eye marks with the fourth argument "
                        + "of p, then paint the mouth.",
                false, 145, 260, 6, 6,
                canvas -> {
                    for (int x = 0; x < 6; x++) {
                        for (int y = 0; y < 6; y++) canvas.set(x, y, "gold", null);
                    }
                    canvas.set(1, 2, "gold", "\u25CF");
                    canvas.set(4, 2, "gold", "\u25CF");
                    for (int x = 1; x < 5; x++) canvas.set(x, 4, "brown", null);
                });

        add(2, "Rings",
                "Square rings in white, red and navy, from the edge inward.",
                "The ring number is the smallest of x, y, W-1-x and H-1-y.",
                true, 150, 250, 6, 6,
                canvas -> {
                    String[] ring = {"white", "red", "navy"};
                    for (int x = 0; x < 6; x++) {
                        for (int y = 0; y < 6; y++) {
                            int r = Math.min(Math.min(x, y), Math.min(5 - x, 5 - y));
                            canvas.set(x, y, ring[r % 3], null);
                        }
                    }
                });

        // ---------------------------------------------------------- Mauve

        add(3, "Diamond",
                "A coral diamond filling most of a seven by seven grid.",
                "Paint when the distance |x-3| plus |y-3| is 3 or less.",
                false, 105, 190, 7, 7,
                canvas -> {
                    for (int x = 0; x < 7; x++) {
                        for (int y = 0; y < 7; y++) {
                            if (Math.abs(x - 3) + Math.abs(y - 3) <= 3) canvas.set(x, y, "coral", null);
                        }
                    }
                });

        addRows(3, "Little tree",
                "A green tree on a brown trunk.",
                "Store the shape as text and walk it, or paint four rows by hand.",
                false, 170, 320, new String[]{
                        "...g...",
                        "..ggg..",
                        ".ggggg.",
                        "ggggggg",
                        ".ggggg.",
                        "...B...",
                        "...B..."});

        addRows(3, "House",
                "A red roof, cream walls and a brown door.",
                "Three colours, one string of forty nine characters.",
                false, 225, 400, new String[]{
                        "...r...",
                        "..rrr..",
                        ".rrrrr.",
                        "rrrrrrr",
                        ".CCCCC.",
                        ".CCBCC.",
                        ".CCBCC."});

        addRows(3, "Ghost",
                "A white ghost with navy eyes and a ragged hem.",
                "The hem is the last row: white on even columns only.",
                false, 180, 320, new String[]{
                        "..WWW..",
                        ".WWWWW.",
                        "WWNWNWW",
                        "WWWWWWW",
                        "WWWWWWW",
                        "WWWWWWW",
                        "W.W.W.W"});

        addRows(3, "Invader",
                "An old friend from the arcade, in lime.",
                "Seven rows of seven. Pack them into one string and read it with charAt.",
                true, 140, 260, new String[]{
                        "..l.l..",
                        ".lllll.",
                        "ll.l.ll",
                        "lllllll",
                        "l.lll.l",
                        ".l...l.",
                        "l.....l"});

        // ----------------------------------------------------------- Rose

        add(4, "Say my name",
                "Four sky squares spelling JAVA, one letter written on each.",
                "p takes a fourth argument: the mark to write on the cell.",
                false, 80, 165, 6, 6,
                canvas -> {
                    String word = "JAVA";
                    for (int i = 0; i < 4; i++) {
                        canvas.set(i + 1, 2, "sky", word.substring(i, i + 1));
                    }
                });

        add(4, "Wave",
                "A cyan wave, one painted cell per column.",
                "Row = round(3.5 + 2.5 * sin(x * 0.9)). Or just write the eight rows down.",
                false, 75, 150, 8, 8,
                canvas -> {
                    for (int x = 0; x < 8; x++) {
                        int y = (int) Math.round(3.5 + 2.5 * Math.sin(x * 0.9));
                        canvas.set(x, y, "cyan", null);
                    }
                });

        add(4, "Lucky five",
                "A white die showing five, the pips written as marks.",
                "Fill it white, then two nested loops place the four corner pips.",
                false, 165, 300, 7, 7,
                canvas -> {
                    for (int x = 0; x < 7; x++) {
                        for (int y = 0; y < 7; y++) canvas.set(x, y, "white", null);
                    }
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            canvas.set(1 + i * 4, 1 + j * 4, "white", "\u25CF");
                        }
                    }
                    canvas.set(3, 3, "white", "\u25CF");
                });

        add(4, "Rainbow",
                "Seven colours running diagonally across the grid.",
                "Index a seven colour array with (x + y) % 7.",
                false, 130, 230, 8, 8,
                canvas -> {
                    String[] bow = {"red", "orange", "gold", "green", "cyan", "blue", "purple"};
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) canvas.set(x, y, bow[(x + y) % 7], null);
                    }
                });

        add(4, "Sierpinski",
                "A gold triangle that contains smaller copies of itself.",
                "Paint the cell when x AND y is zero, using the bitwise and.",
                true, 75, 150, 8, 8,
                canvas -> {
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            if ((x & y) == 0) canvas.set(x, y, "gold", null);
                        }
                    }
                });

        // ---------------------------------------------------------- Cream

        add(5, "Bullseye",
                "Nine by nine square rings cycling red, white, navy.",
                "Ring number is the larger of |x-4| and |y-4|.",
                false, 140, 240, 9, 9,
                canvas -> {
                    String[] ring = {"red", "white", "navy"};
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 9; y++) {
                            int r = Math.max(Math.abs(x - 4), Math.abs(y - 4));
                            canvas.set(x, y, ring[r % 3], null);
                        }
                    }
                });

        add(5, "Flower",
                "Pink petals, a gold heart, a green stem and two leaves.",
                "The petals are the cells where (x-4) squared plus (y-3) squared is 4 or less.",
                false, 190, 340, 9, 9,
                canvas -> {
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 9; y++) {
                            int dx = x - 4;
                            int dy = y - 3;
                            if (dx * dx + dy * dy <= 4) canvas.set(x, y, "pink", null);
                        }
                    }
                    canvas.set(4, 3, "gold", null);
                    for (int y = 6; y < 9; y++) canvas.set(4, y, "green", null);
                    canvas.set(3, 7, "green", null);
                    canvas.set(5, 7, "green", null);
                });

        addRows(5, "Cat",
                "A mauve cat with lime eyes and a pink nose.",
                "Nine rows of nine, four colours. One string, one loop, one switch.",
                false, 260, 460, new String[]{
                        ".M.....M.",
                        "MMM...MMM",
                        "MMMMMMMMM",
                        "MlMMMMMlM",
                        "MMMMMMMMM",
                        "MMMMnMMMM",
                        "MMMMMMMMM",
                        ".MMMMMMM.",
                        "..MMMMM.."});

        add(5, "Labyrinth",
                "Cream rings with a gap between each one.",
                "Paint only when the ring number is even.",
                false, 115, 200, 9, 9,
                canvas -> {
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 9; y++) {
                            int r = Math.min(Math.min(x, y), Math.min(8 - x, 8 - y));
                            if (r % 2 == 0) canvas.set(x, y, "cream", null);
                        }
                    }
                });

        add(5, "Mandala",
                "The whole house palette folded into a nine by nine bloom.",
                "Centre is cream, the diagonals rose, the straight arms mauve, "
                        + "everything else palette[(|x-4|+|y-4|) % 6].",
                true, 225, 380, 9, 9,
                canvas -> {
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 9; y++) {
                            int dx = Math.abs(x - 4);
                            int dy = Math.abs(y - 4);
                            String color;
                            if (dx == 0 && dy == 0) color = "cream";
                            else if (dx == dy) color = "rose";
                            else if (dx == 0 || dy == 0) color = "mauve";
                            else color = PALETTE[(dx + dy) % 6];
                            canvas.set(x, y, color, null);
                        }
                    }
                });

        grow();
    }

    /** Fills the rest of the catalogue with generated levels. */
    private static void grow() {
        int handMade = ALL.size();
        for (int i = 0; i < GENERATED; i++) {
            int tier = Math.min(9, i * 10 / GENERATED);
            Generator.Made made = Generator.make(104729L + i * 31L, tier);
            int chapter = handMade / 5 + i / 10;
            boolean boss = i % 10 == 9;
            ALL.add(new Puzzle(ALL.size(), chapter, made.title, made.brief, made.hint,
                    boss, made.par(), made.limit(), made.target));
        }

        int chapters = handMade / 5 + GENERATED / 10;
        CHAPTERS = new String[chapters];
        for (int i = 0; i < chapters; i++) {
            CHAPTERS[i] = i < CHAPTER_BASE.length
                    ? CHAPTER_BASE[i]
                    : CHAPTER_BASE[i % CHAPTER_BASE.length] + " " + (i / CHAPTER_BASE.length + 1);
        }
    }
}
