package javadable.core;

public enum PowerUp {
    HINT("Hint", 20, "Spell out the rule behind the picture."),
    DIFF("Diff", 15, "Mark every cell that is still wrong."),
    ROOM("Elbow room", 30, "Add fifty characters to this level's budget."),
    SKIP("Skip", 70, "Call it done and open the next level.");

    public final String label;
    public final int price;
    public final String blurb;

    PowerUp(String label, int price, String blurb) {
        this.label = label;
        this.price = price;
        this.blurb = blurb;
    }
}
