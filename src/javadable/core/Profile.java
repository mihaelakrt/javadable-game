package javadable.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything that survives between sessions: stars, best character counts, the
 * code you left behind on every level, the streak, coins and power ups.
 */
public class Profile {

    private static final Path FILE =
            Paths.get(System.getProperty("user.home"), ".javadable", "profile.json");

    public int xp;
    public int coins = 60;
    public int runs;
    public int solved;
    public int cleanSolves;
    public int streak;
    public int bestStreak;
    public String lastDaily = "";

    /** Who is playing, and the little badge they chose for themselves. */
    /** The daily puzzle is not in the catalogue, so its draft lives on its own. */
    public String dailyCode = "";
    public String dailyCodeDate = "";

    public String name = "";
    public String avatarMark = "★";
    public String avatarColour = "rose";
    public String firstPlayed = "";

    public final int[] stars = new int[Puzzles.count()];
    public final int[] best = new int[Puzzles.count()];
    public final String[] code = new String[Puzzles.count()];
    public final Map<PowerUp, Integer> powerUps = new EnumMap<>(PowerUp.class);
    public final Set<String> achievements = new LinkedHashSet<>();

    public Profile() {
        for (PowerUp powerUp : PowerUp.values()) {
            powerUps.put(powerUp, 1);
        }
        for (int i = 0; i < code.length; i++) {
            code[i] = "";
        }
    }

    // -------------------------------------------------------------- progress

    public Rank rank() {
        return Rank.of(xp);
    }

    /** The code left on a level, which for the daily is kept apart and only for today. */
    public String codeFor(Puzzle puzzle, LocalDate today) {
        if (!puzzle.isDaily()) {
            String saved = code[puzzle.index];
            return saved == null ? "" : saved;
        }
        if (!today.toString().equals(dailyCodeDate)) {
            dailyCode = "";
            dailyCodeDate = today.toString();
        }
        return dailyCode;
    }

    public void setCodeFor(Puzzle puzzle, String written, LocalDate today) {
        if (puzzle.isDaily()) {
            dailyCode = written;
            dailyCodeDate = today.toString();
        } else {
            code[puzzle.index] = written;
        }
    }

    public int starsFor(Puzzle puzzle) {
        return puzzle.isDaily() ? 0 : stars[puzzle.index];
    }

    /** The name to show, which is never empty. */
    public String displayName() {
        return name == null || name.trim().isEmpty() ? "Player one" : name.trim();
    }

    /** How many of the three stars have been collected, as a percentage. */
    public int completion() {
        int possible = stars.length * 3;
        return possible == 0 ? 0 : Math.round(totalStars() * 100f / possible);
    }

    /** Characters spent on the levels that have been solved, against their par. */
    public int[] parRecord() {
        int under = 0;
        int over = 0;
        for (Puzzle puzzle : Puzzles.all()) {
            int record = best[puzzle.index];
            if (record <= 0) continue;
            if (record <= puzzle.par) under++;
            else over++;
        }
        return new int[]{under, over};
    }

    public int totalStars() {
        int sum = 0;
        for (int value : stars) sum += value;
        return sum;
    }

    public int solvedCount() {
        int count = 0;
        for (int value : stars) {
            if (value > 0) count++;
        }
        return count;
    }

    public int levelsUnlocked() {
        int unlocked = 1;
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] > 0) unlocked = Math.min(i + 2, stars.length);
        }
        return unlocked;
    }

    public boolean isUnlocked(int index) {
        return index < levelsUnlocked();
    }

    public boolean chapterPerfect(int chapter) {
        boolean any = false;
        for (Puzzle puzzle : Puzzles.all()) {
            if (puzzle.chapter != chapter) continue;
            any = true;
            if (stars[puzzle.index] < 3) return false;
        }
        return any;
    }

    public boolean allSolved() {
        for (int value : stars) {
            if (value == 0) return false;
        }
        return true;
    }

    /** Books a solved level and returns what it paid. */
    public Reward solve(Puzzle puzzle, int characters, boolean firstRun, boolean usedPowerUp) {
        Rank before = rank();
        int earned = puzzle.stars(characters);
        boolean daily = puzzle.isDaily();
        boolean firstTime = daily || stars[puzzle.index] == 0;
        boolean improved = daily || best[puzzle.index] == 0 || characters < best[puzzle.index];

        if (firstTime && !daily) solved++;
        if (firstRun) cleanSolves++;
        if (!daily && earned > stars[puzzle.index]) stars[puzzle.index] = earned;
        if (!daily && improved) best[puzzle.index] = characters;

        int gainedXp = firstTime ? 60 + earned * 30 + puzzle.chapter * 10 : (improved ? 25 : 10);
        if (puzzle.boss && firstTime) gainedXp += 40;
        int gainedCoins = firstTime ? 15 + earned * 8 : (improved ? 8 : 3);

        xp += gainedXp;
        coins += gainedCoins;
        if (firstTime && !usedPowerUp) cleanSolvesWithoutHelp++;

        return new Reward(gainedXp, gainedCoins, earned, firstTime, rank() != before, improved);
    }

    public int cleanSolvesWithoutHelp;

    // ---------------------------------------------------------------- daily

    public String registerDaily(LocalDate today) {
        String todayText = today.toString();
        if (todayText.equals(lastDaily)) return null;

        String note = null;
        if (lastDaily.isEmpty()) {
            streak = 1;
        } else {
            LocalDate last = LocalDate.parse(lastDaily);
            long gap = ChronoUnit.DAYS.between(last, today);
            if (gap == 1) {
                streak++;
            } else if (gap > 1) {
                streak = 1;
                note = "Streak restarted";
            }
        }
        lastDaily = todayText;
        if (streak > bestStreak) bestStreak = streak;
        return note;
    }

    public boolean dailyDoneToday(LocalDate today) {
        return today.toString().equals(lastDaily);
    }

    // ------------------------------------------------------------- powerups

    public int count(PowerUp powerUp) {
        Integer value = powerUps.get(powerUp);
        return value == null ? 0 : value;
    }

    public boolean consume(PowerUp powerUp) {
        int left = count(powerUp);
        if (left <= 0) return false;
        powerUps.put(powerUp, left - 1);
        return true;
    }

    public boolean buy(PowerUp powerUp) {
        if (coins < powerUp.price) return false;
        coins -= powerUp.price;
        powerUps.put(powerUp, count(powerUp) + 1);
        return true;
    }

    public boolean unlock(Achievement achievement) {
        return achievements.add(achievement.name());
    }

    public boolean has(Achievement achievement) {
        return achievements.contains(achievement.name());
    }

    // ----------------------------------------------------------- persistence

    public static Profile load() {
        Profile profile = new Profile();
        try {
            if (!Files.exists(FILE)) return profile;
            String text = new String(Files.readAllBytes(FILE), StandardCharsets.UTF_8);
            Map<String, Object> map = Json.asMap(Json.parse(text));
            profile.xp = Json.getInt(map, "xp", 0);
            profile.coins = Json.getInt(map, "coins", 60);
            profile.runs = Json.getInt(map, "runs", 0);
            profile.solved = Json.getInt(map, "solved", 0);
            profile.cleanSolves = Json.getInt(map, "cleanSolves", 0);
            profile.cleanSolvesWithoutHelp = Json.getInt(map, "unaided", 0);
            profile.streak = Json.getInt(map, "streak", 0);
            profile.bestStreak = Json.getInt(map, "bestStreak", 0);
            profile.lastDaily = Json.getString(map, "lastDaily", "");
            profile.dailyCode = Json.getString(map, "dailyCode", "");
            profile.dailyCodeDate = Json.getString(map, "dailyCodeDate", "");
            profile.name = Json.getString(map, "name", "");
            profile.avatarMark = Json.getString(map, "avatarMark", "★");
            profile.avatarColour = Json.getString(map, "avatarColour", "rose");
            profile.firstPlayed = Json.getString(map, "firstPlayed", "");

            readInts(Json.asList(map.get("stars")), profile.stars);
            readInts(Json.asList(map.get("best")), profile.best);

            List<Object> saved = Json.asList(map.get("code"));
            for (int i = 0; i < saved.size() && i < profile.code.length; i++) {
                profile.code[i] = String.valueOf(saved.get(i));
            }
            Map<String, Object> powerUps = Json.asMap(map.get("powerUps"));
            for (PowerUp powerUp : PowerUp.values()) {
                profile.powerUps.put(powerUp, Json.getInt(powerUps, powerUp.name(), 0));
            }
            for (Object name : Json.asList(map.get("achievements"))) {
                profile.achievements.add(String.valueOf(name));
            }
        } catch (Exception e) {
            System.err.println("Could not read the save file, starting fresh: " + e);
        }
        return profile;
    }

    private static void readInts(List<Object> values, int[] target) {
        for (int i = 0; i < values.size() && i < target.length; i++) {
            Object value = values.get(i);
            if (value instanceof Number) target[i] = ((Number) value).intValue();
        }
    }

    public void save() {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("xp", xp);
            map.put("coins", coins);
            map.put("runs", runs);
            map.put("solved", solved);
            map.put("cleanSolves", cleanSolves);
            map.put("unaided", cleanSolvesWithoutHelp);
            map.put("streak", streak);
            map.put("bestStreak", bestStreak);
            map.put("lastDaily", lastDaily);
            map.put("dailyCode", dailyCode);
            map.put("dailyCodeDate", dailyCodeDate);
            map.put("name", name);
            map.put("avatarMark", avatarMark);
            map.put("avatarColour", avatarColour);
            if (firstPlayed.isEmpty()) firstPlayed = LocalDate.now().toString();
            map.put("firstPlayed", firstPlayed);

            List<Object> starList = new ArrayList<>();
            for (int value : stars) starList.add(value);
            map.put("stars", starList);

            List<Object> bestList = new ArrayList<>();
            for (int value : best) bestList.add(value);
            map.put("best", bestList);

            List<Object> codeList = new ArrayList<>();
            for (String value : code) codeList.add(value == null ? "" : value);
            map.put("code", codeList);

            Map<String, Object> powerUpMap = new LinkedHashMap<>();
            for (PowerUp powerUp : PowerUp.values()) {
                powerUpMap.put(powerUp.name(), count(powerUp));
            }
            map.put("powerUps", powerUpMap);
            map.put("achievements", new ArrayList<Object>(achievements));

            Files.createDirectories(FILE.getParent());
            Files.write(FILE, Json.write(map).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Could not write the save file: " + e);
        }
    }

    public static Path file() {
        return FILE;
    }
}
