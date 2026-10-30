package javadable.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** The colours a sketch may paint with, and the letters the puzzles use. */
public final class Colors {

    public static final String EMPTY = "none";

    private static final Map<String, Integer> RGB = new LinkedHashMap<>();
    private static final Map<Character, String> LEGEND = new LinkedHashMap<>();

    static {
        // the house palette
        RGB.put("ink", 0x190019);
        RGB.put("deep", 0x2B124C);
        RGB.put("plum", 0x522B5B);
        RGB.put("mauve", 0x854F6C);
        RGB.put("rose", 0xDFB6B2);
        RGB.put("cream", 0xFBE4D8);
        // the bright half
        RGB.put("white", 0xFFFFFF);
        RGB.put("black", 0x140C18);
        RGB.put("red", 0xF2415A);
        RGB.put("coral", 0xFF7F6B);
        RGB.put("orange", 0xF59038);
        RGB.put("gold", 0xF5C74A);
        RGB.put("yellow", 0xF7E463);
        RGB.put("lime", 0xA6E063);
        RGB.put("green", 0x4FBF73);
        RGB.put("mint", 0x7FE3C4);
        RGB.put("teal", 0x3FB5B5);
        RGB.put("cyan", 0x5CD8F2);
        RGB.put("sky", 0x6FA8F5);
        RGB.put("blue", 0x4668E0);
        RGB.put("navy", 0x27317A);
        RGB.put("purple", 0x9158DE);
        RGB.put("pink", 0xF18FD0);
        RGB.put("brown", 0x7E4A32);

        LEGEND.put('.', EMPTY);
        LEGEND.put('K', "ink");
        LEGEND.put('D', "deep");
        LEGEND.put('P', "plum");
        LEGEND.put('M', "mauve");
        LEGEND.put('R', "rose");
        LEGEND.put('C', "cream");
        LEGEND.put('W', "white");
        LEGEND.put('X', "black");
        LEGEND.put('r', "red");
        LEGEND.put('q', "coral");
        LEGEND.put('o', "orange");
        LEGEND.put('G', "gold");
        LEGEND.put('y', "yellow");
        LEGEND.put('l', "lime");
        LEGEND.put('g', "green");
        LEGEND.put('m', "mint");
        LEGEND.put('t', "teal");
        LEGEND.put('c', "cyan");
        LEGEND.put('s', "sky");
        LEGEND.put('b', "blue");
        LEGEND.put('N', "navy");
        LEGEND.put('u', "purple");
        LEGEND.put('n', "pink");
        LEGEND.put('B', "brown");
    }

    private Colors() {
    }

    /** Folds anything the player typed into a known colour name, or null for empty. */
    public static String normalise(String name) {
        if (name == null) return null;
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty() || key.equals(EMPTY)) return null;
        if (RGB.containsKey(key)) return key;
        if (key.equals("grey") || key.equals("gray")) return "plum";
        if (key.equals("violet")) return "purple";
        return key;
    }

    public static boolean isKnown(String name) {
        return name != null && RGB.containsKey(name);
    }

    public static int rgb(String name) {
        Integer value = RGB.get(name);
        return value == null ? 0x6E6E6E : value;
    }

    public static String fromLegend(char symbol) {
        String name = LEGEND.get(symbol);
        return name == null || name.equals(EMPTY) ? null : name;
    }

    public static List<String> names() {
        return new ArrayList<>(RGB.keySet());
    }
}
