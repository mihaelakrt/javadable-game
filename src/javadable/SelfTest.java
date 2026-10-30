package javadable;

import javadable.core.Puzzle;
import javadable.core.Puzzles;
import javadable.core.Runner;

/**
 * Run with: java -jar javadable.jar --selftest
 * Checks that the live compiler works in whatever runtime started the game.
 */
public final class SelfTest {

    private SelfTest() {
    }

    public static void run() {
        System.out.println("java       " + System.getProperty("java.version"));
        System.out.println("classpath  " + System.getProperty("java.class.path"));
        System.out.println("compiler   " + (Runner.available() ? "available" : "MISSING, a JDK is needed"));

        Puzzle puzzle = Puzzles.get(0);
        Runner.Result result = new Runner().run("p(2,2,\"red\");", puzzle.width(), puzzle.height());
        boolean solved = result.error == null && result.canvas.matches(puzzle.target());
        System.out.println("test run   " + (solved ? "solved " + puzzle.title + " in "
                + result.millis + " ms" : "FAILED " + result.error));
        System.out.println("levels     " + Puzzles.count());
    }
}
