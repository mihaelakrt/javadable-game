package javadable.core;

import java.util.Random;

/**
 * Makes levels out of thin air. Every pattern is produced twice: once as the
 * picture to aim at, and once as the code that draws it, so the budget can be
 * measured against a solution that is known to exist.
 */
public final class Generator {

    /** A level and the reference solution it was built from. */
    public static final class Made {
        public final String title;
        public final String brief;
        public final String hint;
        public final String code;
        public final Canvas target;

        Made(String title, String brief, String hint, String code, Canvas target) {
            this.title = title;
            this.brief = brief;
            this.hint = hint;
            this.code = code;
            this.target = target;
        }

        public int par() {
            return (int) Math.ceil(Runner.characters(code) * 1.15 / 5) * 5;
        }

        public int limit() {
            return (int) Math.ceil(par() * 1.8 / 5) * 5;
        }
    }

    private static final char Q = 34;

    private static final String[] PAINTS = {
            "red", "coral", "orange", "gold", "yellow", "lime", "green", "mint",
            "teal", "cyan", "sky", "blue", "navy", "purple", "pink", "brown",
            "white", "cream", "rose", "mauve", "plum", "deep"};

    private static final String[] FAMILY_NAMES = {
            "Rings", "Bullseye", "Blocks", "Chevrons", "Stripes", "Diamond",
            "Disc", "Cross", "Saltire", "Steps", "Frame", "Weave", "Dots", "Sigil"};

    private Generator() {
    }

    public static Made make(long seed, int tier) {
        Random random = new Random(seed * 2654435761L + 17);
        int family = pickFamily(random, tier);
        int size = pickSize(random, tier);
        return build(family, size, random, tier);
    }

    private static int pickFamily(Random random, int tier) {
        int[] easy = {2, 4, 7, 8, 9, 10, 12};
        int[] all = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        int[] pool = tier <= 1 ? easy : all;
        return pool[random.nextInt(pool.length)];
    }

    private static int pickSize(Random random, int tier) {
        int low = Math.min(5 + tier / 2, 9);
        int high = Math.min(6 + tier, 12);
        return low + random.nextInt(Math.max(1, high - low + 1));
    }

    private static String[] paints(Random random, int howMany) {
        String[] chosen = new String[howMany];
        for (int i = 0; i < howMany; i++) {
            String candidate;
            boolean clash;
            do {
                candidate = PAINTS[random.nextInt(PAINTS.length)];
                clash = false;
                for (int j = 0; j < i; j++) {
                    if (chosen[j].equals(candidate)) clash = true;
                }
            } while (clash);
            chosen[i] = candidate;
        }
        return chosen;
    }

    private static String array(String name, String[] colours) {
        StringBuilder out = new StringBuilder("String[]").append(name).append("={");
        for (int i = 0; i < colours.length; i++) {
            if (i > 0) out.append(',');
            out.append(Q).append(colours[i]).append(Q);
        }
        return out.append("};").toString();
    }

    private static String quoted(String colour) {
        return Q + colour + Q;
    }

    private static String title(int family, long seed) {
        return FAMILY_NAMES[family] + " " + (Math.floorMod(seed, 89) + 11);
    }

    private static Made build(int family, int size, Random random, int tier) {
        Canvas canvas = new Canvas(size, size);
        int middle = size / 2;
        String code;
        String brief;
        String hint;

        switch (family) {
            case 0: {
                int k = 2 + random.nextInt(3);
                String[] c = paints(random, k);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        int r = Math.min(Math.min(x, y), Math.min(size - 1 - x, size - 1 - y));
                        canvas.set(x, y, c[r % k], null);
                    }
                }
                code = array("c", c) + "for(int x=0;x<W;x++)for(int y=0;y<H;y++)"
                        + "p(x,y,c[Math.min(Math.min(x,y),Math.min(W-1-x,H-1-y))%" + k + "]);";
                brief = "Square rings running inward, " + k + " colours over and over.";
                hint = "The ring number is the smallest of x, y, W-1-x and H-1-y.";
                break;
            }
            case 1: {
                int k = 2 + random.nextInt(3);
                String[] c = paints(random, k);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        int r = Math.max(Math.abs(x - middle), Math.abs(y - middle));
                        canvas.set(x, y, c[r % k], null);
                    }
                }
                code = array("c", c) + "for(int x=0;x<W;x++)for(int y=0;y<H;y++)"
                        + "p(x,y,c[Math.max(Math.abs(x-" + middle + "),Math.abs(y-" + middle + "))%"
                        + k + "]);";
                brief = "Rings growing out of the middle in " + k + " colours.";
                hint = "Ring number is the larger of |x-" + middle + "| and |y-" + middle + "|.";
                break;
            }
            case 2: {
                int block = 1 + random.nextInt(3);
                String[] c = paints(random, 2);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        canvas.set(x, y, (x / block + y / block) % 2 == 0 ? c[0] : c[1], null);
                    }
                }
                String test = block == 1 ? "(x+y)%2==0"
                        : "(x/" + block + "+y/" + block + ")%2==0";
                code = "for(int x=0;x<W;x++)for(int y=0;y<H;y++)p(x,y," + test + "?"
                        + quoted(c[0]) + ":" + quoted(c[1]) + ");";
                brief = block == 1 ? "A checkerboard, nothing left empty."
                        : "A checkerboard in blocks of " + block + " by " + block + ".";
                hint = "Divide x and y by " + block + " first, then see whether the sum is even.";
                break;
            }
            case 3: {
                int k = 3 + random.nextInt(3);
                String[] c = paints(random, k);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) canvas.set(x, y, c[(x + y) % k], null);
                }
                code = array("c", c) + "for(int x=0;x<W;x++)for(int y=0;y<H;y++)"
                        + "p(x,y,c[(x+y)%" + k + "]);";
                brief = "Diagonal bands in " + k + " colours.";
                hint = "Index the colours with (x + y) % " + k + ".";
                break;
            }
            case 4: {
                int k = 2 + random.nextInt(3);
                boolean vertical = random.nextBoolean();
                String[] c = paints(random, k);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        canvas.set(x, y, c[(vertical ? x : y) % k], null);
                    }
                }
                code = array("c", c) + "for(int x=0;x<W;x++)for(int y=0;y<H;y++)"
                        + "p(x,y,c[" + (vertical ? "x" : "y") + "%" + k + "]);";
                brief = (vertical ? "Vertical" : "Horizontal") + " bands cycling " + k + " colours.";
                hint = "The band is " + (vertical ? "x" : "y") + " % " + k + ".";
                break;
            }
            default:
                return second(family, size, random, canvas, middle);
        }
        return new Made(title(family, random.nextInt(900)), brief, hint, code, canvas);
    }

    private static Made second(int family, int size, Random random, Canvas canvas, int middle) {
        String code;
        String brief;
        String hint;

        switch (family) {
            case 5: {
                int radius = 2 + random.nextInt(Math.max(1, size / 2 - 1));
                String colour = paints(random, 1)[0];
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        if (Math.abs(x - middle) + Math.abs(y - middle) <= radius) {
                            canvas.set(x, y, colour, null);
                        }
                    }
                }
                code = "for(int x=0;x<W;x++)for(int y=0;y<H;y++)if(Math.abs(x-" + middle
                        + ")+Math.abs(y-" + middle + ")<=" + radius + ")p(x,y,"
                        + quoted(colour) + ");";
                brief = "A diamond, " + radius + " cells from the middle to each point.";
                hint = "Paint when |x-" + middle + "| plus |y-" + middle + "| is "
                        + radius + " or less.";
                break;
            }
            case 6: {
                int radius = 2 + random.nextInt(Math.max(1, size / 2 - 1));
                String colour = paints(random, 1)[0];
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        int a = x - middle;
                        int b = y - middle;
                        if (a * a + b * b <= radius * radius) canvas.set(x, y, colour, null);
                    }
                }
                code = "for(int x=0;x<W;x++)for(int y=0;y<H;y++){int a=x-" + middle + ",b=y-"
                        + middle + ";if(a*a+b*b<=" + (radius * radius) + ")p(x,y,"
                        + quoted(colour) + ");}";
                brief = "A round blob of radius " + radius + ".";
                hint = "Square the distances: a*a + b*b <= " + (radius * radius) + ".";
                break;
            }
            case 7: {
                String colour = paints(random, 1)[0];
                for (int i = 0; i < size; i++) {
                    canvas.set(i, middle, colour, null);
                    canvas.set(middle, i, colour, null);
                }
                code = "for(int i=0;i<W;i++){p(i," + middle + "," + quoted(colour) + ");p("
                        + middle + ",i," + quoted(colour) + ");}";
                brief = "A cross through the middle row and the middle column.";
                hint = "One loop paints both arms at the same index.";
                break;
            }
            case 8: {
                String colour = paints(random, 1)[0];
                for (int i = 0; i < size; i++) {
                    canvas.set(i, i, colour, null);
                    canvas.set(i, size - 1 - i, colour, null);
                }
                code = "for(int i=0;i<W;i++){p(i,i," + quoted(colour) + ");p(i,W-1-i,"
                        + quoted(colour) + ");}";
                brief = "Both diagonals, corner to corner.";
                hint = "The second diagonal is the cells where x plus y is W-1.";
                break;
            }
            case 9: {
                String colour = paints(random, 1)[0];
                boolean flipped = random.nextBoolean();
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x <= y; x++) {
                        canvas.set(flipped ? size - 1 - x : x, y, colour, null);
                    }
                }
                code = flipped
                        ? "for(int y=0;y<H;y++)for(int x=0;x<=y;x++)p(W-1-x,y," + quoted(colour) + ");"
                        : "for(int y=0;y<H;y++)for(int x=0;x<=y;x++)p(x,y," + quoted(colour) + ");";
                brief = "A staircase climbing from the top " + (flipped ? "right" : "left") + ".";
                hint = "On row y, paint every x up to and including y.";
                break;
            }
            default:
                return third(family, size, random, canvas, middle);
        }
        return new Made(title(family, random.nextInt(900)), brief, hint, code, canvas);
    }

    private static Made third(int family, int size, Random random, Canvas canvas, int middle) {
        String code;
        String brief;
        String hint;

        switch (family) {
            case 10: {
                int thickness = 1 + random.nextInt(Math.max(1, size / 3));
                String colour = paints(random, 1)[0];
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        int r = Math.min(Math.min(x, y), Math.min(size - 1 - x, size - 1 - y));
                        if (r < thickness) canvas.set(x, y, colour, null);
                    }
                }
                code = "for(int x=0;x<W;x++)for(int y=0;y<H;y++)if(Math.min(Math.min(x,y),"
                        + "Math.min(W-1-x,H-1-y))<" + thickness + ")p(x,y," + quoted(colour) + ");";
                brief = "A border " + thickness + (thickness == 1 ? " cell" : " cells") + " thick.";
                hint = "Distance to the nearest edge is the smallest of x, y, W-1-x and H-1-y.";
                break;
            }
            case 11: {
                int k = 3 + random.nextInt(3);
                int a = 1 + random.nextInt(3);
                int b = 1 + random.nextInt(3);
                String[] c = paints(random, k);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        canvas.set(x, y, c[(x * a + y * b) % k], null);
                    }
                }
                code = array("c", c) + "for(int x=0;x<W;x++)for(int y=0;y<H;y++)"
                        + "p(x,y,c[(x*" + a + "+y*" + b + ")%" + k + "]);";
                brief = "A woven pattern of " + k + " colours.";
                hint = "The colour is c[(x*" + a + " + y*" + b + ") % " + k + "].";
                break;
            }
            case 12: {
                int step = 2 + random.nextInt(2);
                String colour = paints(random, 1)[0];
                for (int x = 0; x < size; x += step) {
                    for (int y = 0; y < size; y += step) canvas.set(x, y, colour, null);
                }
                code = "for(int x=0;x<W;x+=" + step + ")for(int y=0;y<H;y+=" + step
                        + ")p(x,y," + quoted(colour) + ");";
                brief = "A field of dots, every " + step + " cells.";
                hint = "Step the loops by " + step + " instead of 1.";
                break;
            }
            default: {
                String colour = paints(random, 1)[0];
                StringBuilder packed = new StringBuilder();
                int half = (size + 1) / 2;
                boolean[][] left = new boolean[half][size];
                for (int x = 0; x < half; x++) {
                    for (int y = 0; y < size; y++) left[x][y] = random.nextInt(100) < 48;
                }
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        boolean on = left[Math.min(x, size - 1 - x)][y];
                        if (on) canvas.set(x, y, colour, null);
                        packed.append(on ? '1' : '.');
                    }
                }
                code = "String s=" + Q + packed + Q + ";for(int i=0;i<" + (size * size)
                        + ";i++)if(s.charAt(i)=='1')p(i%" + size + ",i/" + size + ","
                        + quoted(colour) + ");";
                brief = "A made up sign. It is symmetric, so half of it is the whole story.";
                hint = "Pack the picture into one string and walk it with charAt.";
                break;
            }
        }
        return new Made(title(family, random.nextInt(900)), brief, hint, code, canvas);
    }
}
