package javadable.core;

/** Ranks are named after the palette the whole game is painted with. */
public enum Rank {
    MIDNIGHT("Midnight", 0),
    INDIGO("Indigo", 250),
    PLUM("Plum", 700),
    MAUVE("Mauve", 1500),
    ROSE("Rose", 2800),
    CREAM("Cream", 4500);

    public final String label;
    public final int xp;

    Rank(String label, int xp) {
        this.label = label;
        this.xp = xp;
    }

    public static Rank of(int xp) {
        Rank best = MIDNIGHT;
        for (Rank r : values()) {
            if (xp >= r.xp) best = r;
        }
        return best;
    }

    /** xp needed for the next rank, or -1 when already at the top. */
    public int nextAt() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i].xp : -1;
    }
}
