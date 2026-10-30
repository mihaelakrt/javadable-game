package javadable.core;

/** What a solved level paid out, so the UI can show it. */
public class Reward {

    public final int xp;
    public final int coins;
    public final int stars;
    public final boolean firstTime;
    public final boolean rankedUp;
    public final boolean improved;

    public Reward(int xp, int coins, int stars, boolean firstTime, boolean rankedUp, boolean improved) {
        this.xp = xp;
        this.coins = coins;
        this.stars = stars;
        this.firstTime = firstTime;
        this.rankedUp = rankedUp;
        this.improved = improved;
    }
}
