package WizardTD;

/**
 * A game timer that counts in seconds when the game launches and can be shared across different classes.
 */
public class Timer {
    // Each second is made up of 60 tick cycles
    private static double seconds;

    /**
     * Updates seconds every tick cycle - 60 tick cycles make a second.
     */
    public static void tick() {
        seconds += 1 / (double)App.FPS;
    }

    /**
     * Gets the current number of seconds.
     * @return seconds as a double with 2 decimal places
     */
    public static double getSeconds() {
        // Round seconds to 2 decimal places
        return Double.parseDouble(String.format("%.2f", seconds));
    }

    /**
     * Resets the timer.
     */
    public static void resetTimer() {
        seconds = 0;
    }
}
