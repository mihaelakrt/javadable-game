package javadable.core;

import java.util.Arrays;
import java.util.List;

/**
 * The little symbols that can be written on a cell. Windows draws the emoji as
 * white silhouettes, which suits the game, and the plain symbols always work.
 */
public final class Marks {

    public static final String NONE = "";

    private static final String[] GLYPHS = {
            "\u2605",            // star
            "\u2665",            // heart
            "\u271A",            // cross
            "\u25CF",            // dot
            "\u25C6",            // diamond
            "\u25B2",            // triangle
            "\u273F",            // blossom
            "\u2600",            // sun
            "\u263E",            // moon
            "\u2726",            // sparkle
            "\u266A",            // note
            "\u2713",            // tick
            "\uD83D\uDE00",      // grinning face
            "\uD83D\uDC31",      // cat
            "\uD83C\uDF38",      // cherry blossom
            "\uD83D\uDD25",      // fire
            "\u2B50",            // glowing star
            "\uD83C\uDF4E",      // apple
            "\uD83D\uDC9C",      // purple heart
            "\uD83C\uDF19",      // crescent moon
            "\uD83C\uDF08",      // rainbow
            "\uD83D\uDC7E",      // alien monster
            "\uD83C\uDF40",      // clover
            "\uD83C\uDF88"       // balloon
    };

    private Marks() {
    }

    public static List<String> all() {
        return Arrays.asList(GLYPHS);
    }

    public static String clean(String mark) {
        if (mark == null) return null;
        String trimmed = mark.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
