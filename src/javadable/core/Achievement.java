package javadable.core;

public enum Achievement {
    FIRST_PIXEL("First pixel", "Solve your first level."),
    CLEAN_RUN("Clean run", "Solve a level on the very first run."),
    UNDER_PAR("Under par", "Finish a level inside par."),
    MINIMALIST("Minimalist", "Finish a level in half of par."),
    ONE_LINER("One liner", "Solve a level with a single line of code."),
    BOSS_SLAYER("Boss slayer", "Beat a boss level."),
    CHAPTER_CLEAR("Chapter clear", "Three stars on every level of a chapter."),
    CARTOGRAPHER("Cartographer", "Solve every level in the game."),
    ON_FIRE("On fire", "Keep a five day streak."),
    UNSTOPPABLE("Unstoppable", "Keep a fifteen day streak."),
    PURIST("Purist", "Solve ten levels without spending a power up."),
    TOP_OF_THE_PALETTE("Top of the palette", "Reach the Cream rank.");

    public final String label;
    public final String blurb;

    Achievement(String label, String blurb) {
        this.label = label;
        this.blurb = blurb;
    }
}
