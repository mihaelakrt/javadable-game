package javadable.core;

/** One level: a target picture, a character budget and a little flavour. */
public class Puzzle {

    public final int index;
    public final int chapter;
    public final String title;
    public final String brief;
    public final String hint;
    public final boolean boss;
    public final int par;
    public final int limit;

    private final Canvas target;

    public Puzzle(int index, int chapter, String title, String brief, String hint,
                  boolean boss, int par, int limit, Canvas target) {
        this.index = index;
        this.chapter = chapter;
        this.title = title;
        this.brief = brief;
        this.hint = hint;
        this.boss = boss;
        this.par = par;
        this.limit = limit;
        this.target = target;
    }

    public Canvas target() {
        return target;
    }

    public int width() {
        return target.width;
    }

    public int height() {
        return target.height;
    }

    public String chapterName() {
        return Puzzles.CHAPTERS[chapter];
    }

    /** Three stars under par, two inside the limit, one for solving it at all. */
    public int stars(int characters) {
        if (characters <= par) return 3;
        if (characters <= limit) return 2;
        return 1;
    }
}
