package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;
import java.util.*;

/**
 * Manages several crucial elements of the game in one place including current wave number, wave configurations, mana bar, 
 * monster collections, and winning and losing.
 */
public class Waves {
    // Keep track of current wave (1, 2, or 3)
    private int waveNumber = 0;
    // WaveTimer object
    private WaveTimer waveTimer;
    // Map Object
    private Map map;
    // ManaBar object
    private ManaBar manaBar;
    // Store number of monsters spawned
    private int numberOfMonsters;
    // Store the interval between monster spawns
    private double spawnInterval;
    // Track if any monsters were just killed (to top up mana bar)
    private int lastMonstersKilled;
    private int newMonstersKilled;
    // Keep track of wizard's hp loss when monsters reach the house
    private int lastWizardHpLoss;
    private int newWizardHpLoss;
    // Game is over! You lose
    private boolean gameOver;
    // If zero monstersLeft, you win!
    private boolean weHaveAWinner;

    // A hashmap to store different monster types and their images
    private HashMap<String, ArrayList<PImage>> monsterImages;
    // Store the collections of monsters for each wave
    private ArrayList<MonsterCollection> monsterCollections = new ArrayList<>();

    // The configuration for each wave
    private ArrayList<JSONObject> waveConfs = new ArrayList<>();
    // Store monster configurations from each wave configuration
    private ArrayList<JSONObject> monsterConfs = new ArrayList<>();
    // Durations of each wave
    private int[] durations = new int[3];
    // Pre-wave pauses for each wave
    private double[] preWavePauses = new double[3];
    // Monster types on this map
    private String[] monsterTypes = new String[3];
    // Monster health point, speed, and armour values for each wave
    private int[] hps = new int[3];
    private double[] speeds = new double[3];
    private double[] armours = new double[3];
    // Mana gained on each monster kill for each wave
    private int[] manaGainedOnKills = new int[3];
    // Quantity of monsters for each wave
    private int[] quantities = new int[3];

    /**
     * Constructs a Waves object given a JSONObject, monsters images and a Map object.
     * @param jsonObj a JSONObject
     * @param monsterImages an ArrayList of monster images
     * @param map a Map object
     */
    public Waves(JSONObject jsonObj, HashMap<String, ArrayList<PImage>> monsterImages, Map map) {
        this.monsterImages = monsterImages;
        this.map = map;

        for(int i = 0; i < 3; i++) {
            this.waveConfs.add(jsonObj.getJSONArray("waves").getJSONObject(i));
            this.durations[i] = waveConfs.get(i).getInt("duration");
            this.preWavePauses[i] = waveConfs.get(i).getDouble("pre_wave_pause");

            this.monsterConfs.add(waveConfs.get(i).getJSONArray("monsters").getJSONObject(0));
            this.monsterTypes[i] = monsterConfs.get(i).getString("type");
            this.hps[i] = monsterConfs.get(i).getInt("hp");
            this.speeds[i] = monsterConfs.get(i).getDouble("speed");
            this.armours[i] = monsterConfs.get(i).getDouble("armour");
            this.manaGainedOnKills[i] = monsterConfs.get(i).getInt("mana_gained_on_kill");
            this.quantities[i] = monsterConfs.get(i).getInt("quantity");
        }

        // Create wave timer object for top left corner of gui
        this.waveTimer = new WaveTimer(this.durations, this.preWavePauses);
        this.waveNumber = this.waveTimer.getWaveNumber();
        manaBar = new ManaBar(jsonObj);
    }

    /**
     * Updates the current wave information including wave number, wave timer, monsters killed, mana gained/lost, and wins/losses.
     */
    public void tick() {      
        // If it's a new wave (based on wave timer) then update waveNumber and create new collection of monsters (first triggered at getWaveNumber 1)
        if (waveNumber != waveTimer.getWaveNumber()) {
            // Update wave number
            waveNumber = waveTimer.getWaveNumber();
            if (waveNumber > 0) {
                // Reset number of monsters
                numberOfMonsters = 0;
                // Create new monster collection
                monsterCollections.add(new MonsterCollection(quantities[waveNumber - 1], monsterTypes[waveNumber - 1], hps[waveNumber - 1], speeds[waveNumber - 1], armours[waveNumber - 1], monsterImages, map));
                // Get the interval at which the monsters spawn
                spawnInterval = (double)durations[waveNumber - 1] / (double)quantities[waveNumber - 1];
            }
        }

        // Only start spawning the monsters once wave 1 starts
        if (waveNumber > 0) {
            // Check if next spawnInterval reached for next spawn to occur
            if (Timer.getSeconds() >= spawnInterval * numberOfMonsters && numberOfMonsters < (quantities[waveNumber - 1])) {
                // Bring next monster to life
                monsterCollections.get(waveNumber - 1).getMonsters().get(numberOfMonsters).bringToLife();
                numberOfMonsters++;
            }
            newMonstersKilled = 0;
            newWizardHpLoss = 0;

            monsterCollections.forEach(mc -> mc.tick());

            for(MonsterCollection mc: monsterCollections) {
                newMonstersKilled += mc.getMonstersKilled();
                newWizardHpLoss += mc.getWizardHpLoss();

            }

            // If all waves have begun, then check and see if we have a winner
            if(waveNumber >= 3) {
                int totalMonsters = 0;

                for(MonsterCollection mc: monsterCollections) {
                    totalMonsters += mc.getQuantity();

                    for(Monster mtr: mc.getMonsters()) {
                        if(mtr.getIsDead())
                            totalMonsters--;
                    }
                }

                if(totalMonsters == 0)
                    weHaveAWinner = true;
            }

            // Update the mana bar
            manaBar.tick(manaGainedOnKills[waveNumber - 1], newMonstersKilled - lastMonstersKilled, newWizardHpLoss - lastWizardHpLoss);
        // Else if wave 1 hasn't started yet (allows for pre-wave 1 mana trickle)
        } else {
            manaBar.tick(0, 0, 0);
        }

        if(manaBar.getMana() == 0) {
            gameOver = true;
        }

        // Update wave timer
        waveTimer.tick();
    
        // Update lastMonstersKilled and lastWizardHit
        lastMonstersKilled = newMonstersKilled;
        lastWizardHpLoss = newWizardHpLoss;
    }

    /**
     * Draws the monster waves to screen.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // Only draw monsters once wave 1 starts
        if (waveNumber > 0) {
            monsterCollections.forEach(mc -> mc.draw(app));
        }
    }

    /**
     * Gets an ArrayList of all MonsterCollection's for the current and previous series of wave in this level.
     * @return ArrayList of MonsterCollections
     */
    public ArrayList<MonsterCollection> getMonsters() {
        return monsterCollections;
    }
    
    /**
     * Gets the WaveTimer object.
     * @return Wavetimer object
     */
    public WaveTimer getWaveTimer() {
        return waveTimer;
    }

    /**
     * Gets the ManaBar object.
     * @return ManaBar object
     */
    public ManaBar getManaBar() {
        return manaBar;
    }

    /**
     * Indicates if the user has lost the game due to too many monsters reaching the wizard house.
     * @return true if user has lost the game
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Sets whether or not user has lost the game due to too many monsters reaching the wizard house.
     * @param gameOver if the user has lost the game
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Indicates if the user has won the game by killing all the monsters across all waves for this level.
     * @return true if the user has won the game
     */
    public boolean doWeHaveAWinner() {
        return weHaveAWinner;
    }

    /**
     * Updates the mana bar if game is paused (used by App.java).
     */
    public void manaTick() {
        if (waveNumber > 0)
            manaBar.tick(manaGainedOnKills[waveNumber - 1], newMonstersKilled - lastMonstersKilled, newWizardHpLoss - lastWizardHpLoss);
        else
            manaBar.tick(0, 0, 0);
    }
    
}

