package WizardTD;

import processing.core.PApplet;

/**
 * Displays start time until next wave begins in the top left corner of the window.
 */
public class WaveTimer {
    // Text coordinates
    private int textX;
    private int textY;
    // Text to display
    private String text;
    // Current wave number
    private int waveNumber;
    // Durations of each wave
    private int[] durations;
    // Pre-wave pause duration
    private double[] preWavePauses;

    /**
     * Constructs a button object given wave durations and pre-wave pauses.
     * @param duration int array containing duration of each wave for current level
     * @param preWavePauses double array containing pre-wave pause durations of each wave for current level
     */
    public WaveTimer(int[] durations, double[] preWavePauses) {
        this.textX = 15;
        this.textY = 30;
        this.durations = durations;
        this.preWavePauses = preWavePauses;
        // Note: this.text preWavePause for wave 1 is just 0 (since prewave pause is 0.5)
        // For wave 2, it's wave 1 duration + wave 2 pre wave pause = 8 + 10
        this.text = "Wave " + waveNumber + " starts " + (int)(preWavePauses[0]); 
    }

    /**
     * Updates the wave timer wave number and seconds until next wave begins.
     */
    public void tick() {
        // waveNumber 0: Wave timer for time until wave 1 starts (is not seen since it lasts 0.5 secs (< 1 sec))
        // waveNumber 1: Wave timer for time until wave 2, run during wave 1.
        // waveNumber 2: Wave timer for time until wave 3, run during wave 2.
        // waveNumber 3: No wave timer, run during wave 3 for duration of wave 3, until next map's wave 1 pre-count or end of game.

        // If wave 1 has outlived its pre-wave pause, increment wave number -> display the time until wave 2 starts
        if(waveNumber == 0 && Timer.getSeconds() >= preWavePauses[waveNumber]) {
            Timer.resetTimer();
            waveNumber++;
        // If waves after wave 1 have outlived the current wave's duration + the next wave's pre-wave pause, increment waveNumber -> display the time until next wave starts
        } else if(waveNumber > 0 && waveNumber < 3 && Timer.getSeconds() >= durations[waveNumber - 1] + preWavePauses[waveNumber]) {
            Timer.resetTimer();
            waveNumber++;
        // If the last wave is complete, reset waveNumber
        } else {
            // Before wave 1 you only want to wait out the wave 1 pre-wave pause (is not seen since 0.5 (< 1 sec)) before displaying the time until wave 2
            if (waveNumber == 0) {
                // Update the displayed text
                text = "Wave " + (waveNumber + 1) + " starts " + (int)(preWavePauses[waveNumber] - Timer.getSeconds());
            // On waves 1 & 2 you want to display their current duration + the next wave's pre-wave pause
            // Display: Wave [current waveNumber] starts [current wave's duration + next wave's pre-wave pause]
            } else if (waveNumber > 0 && waveNumber < 3) {
                text = "Wave " + (waveNumber + 1) + " starts " + (int)(durations[waveNumber - 1] + preWavePauses[waveNumber] - (Timer.getSeconds()));
            // On the third and final wave, don't print the wave timer
            } else if (waveNumber == 3) {
                text = "";
            }
        }
    }

    /**
     * Draws the the wave timer on the top left of the window.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        app.textSize(20);
        // make text black
        app.fill(0);
        app.text(text, textX, textY);
    }

    /**
     * Gets the current wave number.
     * @return current wave number
     */
    public int getWaveNumber() {
        return waveNumber;
    }  
}
