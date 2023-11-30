package WizardTD;

import processing.core.PApplet;
import processing.data.JSONObject;

/**
 * Displays the wizard's mana on the top right of the window. Is used to build towers, buy upgrades and mana pool spell. 
 * Also used as the wizard's health bar.
 */
public class ManaBar {
    // Variables to store the text position
    private static final int TEXT_X = 310;
    private static final int TEXT_Y = 28;
    // Variables to store mana counter
    private static final int COUNTER_X = 493;
    private static final int COUNTER_Y = 28;
    // Text to display
    private static final String TEXT = "MANA:";
    // Variables to store mana border position
    private static final int BORDER_X = 380;
    private static final int BORDER_Y = 10;
    // Store the size of the mana border
    private static final int BSIZE_X = 340;
    private static final int BSIZE_Y = 21;
    // Variables to store mana position (this is the rectangle that fills the box with mana)
    private static final int MANA_X = 380;
    private static final int MANA_Y = 10;
    // Store the size of the mana size (msizeX changes as mana is gained and lost)
    private int msizeX;
    private static final int MSIZE_Y = 21;
    // Store the amount of mana, maximum amount of mana, and mana gained per second
    private int mana;
    private int manaCap;
    private double manaGPS;
    // The text to be displayed inside the mana bar; format: [current mana]/[total mana]
    private String manaCounter;
    // Mana pool spell multiplier for increasing the amount of mana gained per monster kill
    private double manaPoolSpellMultiplier;

    /**
     * Constucts a ManaBar object given a JSONObject.
     * @param jsonObj a JSONObject used to retrieve information required by the mana bar
     */
    public ManaBar(JSONObject jsonObj) {
        this.mana = jsonObj.getInt("initial_mana");
        this.manaCap = jsonObj.getInt("initial_mana_cap");
        this.manaGPS = jsonObj.getInt("initial_mana_gained_per_second");
        this.msizeX = (int)(((double)this.mana / (double)this.manaCap) * BSIZE_X);
        this.manaCounter = this.mana + " / " + this.manaCap;
        this.manaPoolSpellMultiplier = 1;
    }

    /**
     * Updates the mana bar based on mana gained from killing monsters and wizard health lost
     * from monsters reaching the wizard house.
     * @param manaGainedOnKill amount of mana gained per monster killed
     * @param monstersKilled number of monsters killed since last tick
     * @param wizardHpLoss amount of health the wizard has lost
     */
    public void tick(int manaGainedOnKill, int monstersKilled, int wizardHpLoss) {
        if (mana < manaCap) {
            // Mana trickle
            if (Timer.getSeconds() % 0.5 == 0) {
                // If the next mana trickle overshoots the maximum
                if(manaCap - mana <= manaGPS/2)
                    mana = manaCap;
                else
                    mana += manaGPS/2;
            }
        }

        // Mana gained from killing a monster
        if(monstersKilled > 0) {
            if(manaCap - mana <= manaGainedOnKill) 
                mana = manaCap;
            else 
                mana += manaGainedOnKill * monstersKilled * manaPoolSpellMultiplier;
        }

        // Mana lost from monster reaching wizard house
        if(wizardHpLoss > 0) {
            if(mana - wizardHpLoss <= 0)
                mana = 0;
            else
                mana -= wizardHpLoss;
        }

        msizeX = (int)(((double)mana / (double)manaCap) * BSIZE_X);
        manaCounter = mana + " / " + manaCap;
    }

    /**
     * Draws the mana bar onto the top right of the window.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // The mana border is a rectangle with white fill and a black border of weight 2
        app.stroke(0);
        app.strokeWeight(2);
        app.fill(255, 255, 255);
        app.rect(BORDER_X, BORDER_Y, BSIZE_X, BSIZE_Y);
        app.fill(9, 202, 204);
        app.rect(MANA_X, MANA_Y, msizeX, MSIZE_Y);

        // Draw the texts
        app.textSize(20);
        // Make the text black
        app.fill(0);
        app.text(TEXT, TEXT_X, TEXT_Y);
        app.text(manaCounter, COUNTER_X, COUNTER_Y);
    }

    /**
     * Gets current amount of mana.
     * @return amount of mana
     */
    public int getMana() {
        return mana;
    }

    /**
     * Sets the current amount of mana.
     * @param mana new value for mana
     */
    public void setMana(int mana) {
        // Prevent over or under shooting the mana bar
        if(mana >= manaCap)
            this.mana = manaCap;
        else if(mana <= 0)
            this.mana = 0;
        else
            this.mana = mana;
    }

    /**
     * Gets the maximum mana possible.
     * @return maximum mana possible
     */
    public int getManaCap() {
        return manaCap;
    }

    /**
     * Sets the maximum mana.
     * @param mana new value for maximum mana
     */
    public void setManaCap(int manaCap) {
        this.manaCap = manaCap;
    }

    /**
     * Gets mana trickle gained per second.
     * @return mana gained per second
     */
    public double getManaGPS() {
        return manaGPS;
    }

    /**
     * Sets mana trickle gained per second.
     * @param manaGPS new mana gained per second
     */
    public void setManaGPS(double manaGPS) {
        this.manaGPS = manaGPS;
    }

    /**
     * Gets mana pool spell multiplier responsible for mana gained from monster killed.
     * @return mana pool spell multiplier
     */
    public double getManaPoolSpellMultiplier() {
        return manaPoolSpellMultiplier;
    }

    /**
     * Sets mana pool spell multiplier responsible for mana gained from monster killed.
     * @param manaPoolSpellMultiplier new mana pool spell multipler value
     */
    public void setManaPoolSpellMultiplier(double manaPoolSpellMultiplier) {
        this.manaPoolSpellMultiplier = manaPoolSpellMultiplier;
    }
    
}
