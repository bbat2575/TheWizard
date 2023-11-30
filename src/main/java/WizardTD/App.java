package WizardTD;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;
import processing.event.MouseEvent;

/**
 * The PAppet class central to the game. Responible for creating the game window, drawing all elements on screen, 
 * handling mouse and keyboard input, gameplay loops, and setting up major classes responsible for the games functionality.
 */
public class App extends PApplet {
    // Frames per second
    public static final int FPS = 60;
    // Size of each cell/tile, the topbar and sidebar
    public static final int CELLSIZE = 32;
    public static final int TOPBAR = 40;
    private static final int SIDEBAR = 120;
    // Width of the map (excluding sidebar and topbar)
    private static final int BOARD_WIDTH = 20;

    // Total width and height of the window
    private static int WIDTH = CELLSIZE*BOARD_WIDTH+SIDEBAR;
    private static int HEIGHT = BOARD_WIDTH*CELLSIZE+TOPBAR;

    // Initial tower upgrade cost, cost increase of each upgrade
    private static final int INITIAL_UPGRADE_COST = 20;
    private static final int UPGRADE_COST_INCREASE = 10;
    // Range and speed upgrade values
    private static final int RANGE_UPGRADE = CELLSIZE * 2;
    private static final double SPEED_UPGRADE = 0.5;

    // The json.config path and JSONObject
    private String configPath;
    private JSONObject jsonObj;
	
    // Tower images
    private ArrayList<PImage> towerImages = new ArrayList<PImage>();
    private ArrayList<Tower> towers = new ArrayList<Tower>();
    // Initial tower cost (w/out any upgrades)
    private int initialTowerCost;
    // Total tower cost (w/ any triggered upgrades)
    private int towerCost;
    // Keeps track of number of upgrade buttons triggered
    private int numberOfUpgrades;
    // Triggered if build tower button pressed
    private boolean placingTower;
    // Fireball image
    private PImage fballImage;
    // Wand images for cursor and wandCounter to cycle through them
    private ArrayList<PImage> wands = new ArrayList<>();
    private int wandCounter;
    // Triggered if relevant upgrade button pressed
    private boolean upgradingRange;
    private boolean upgradingSpeed;
    private boolean upgradingDamage;

    // Waves and Map objects
    private Waves wave;
    private Map map;

    // Images for landmark objects
    private PImage grassImage;
    private PImage shrubImage;
    private PImage wizardImage;
    
    // An ArrayList to store ArrayLists of path PImages for drawing the map
    private ArrayList<ArrayList<PImage>> pathImages = new ArrayList<>();
    
    // A hashmap to store different monster types and their images
    private HashMap<String, ArrayList<PImage>> monsterImages = new HashMap<>();
    private ArrayList<PImage> gremlinImages = new ArrayList<>();

    // Variables for the mana pool spell
    private int manaPoolSpellCost;
    private int manaPoolSpellCostIncreasePerUse;
    private double manaPoolSpellCapMultiplier;
    private double manaPoolSpellManaGainedMultiplier;

    // A list to store buttons
    private ArrayList<Button> buttons = new ArrayList<>();

    // Triggered if pause button pressed or restart key hit at game over screen
    private boolean paused;
    private boolean restart;

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images and set up some gameplay configurations. Initialise map elements and 
     * instantiate Buttons, Map, and Waves objects.
     */
	@Override
    public void setup() {
        frameRate(FPS);

        // Create json object and retrieve the layout name
        jsonObj = loadJSONObject(configPath);
        String layout = jsonObj.getString("layout");

        // Load all images into corresponding PImage objects
        grassImage = loadImage("src/main/resources/WizardTD/grass.png");
        shrubImage = loadImage("src/main/resources/WizardTD/shrub.png");
        wizardImage = loadImage("src/main/resources/WizardTD/wizard_house.png");

        // Populate gremlinImages starting with the first image
        // Used to create monster collections in Waves.java
        gremlinImages.add(loadImage("src/main/resources/WizardTD/gremlin.png"));

        // Followed by the remaining images for gremlin death animation
        for (int i = 1; i <= 5; i++) {
            gremlinImages.add(loadImage("src/main/resources/WizardTD/gremlin"+i+".png"));
        }
        
        // Populate monster images with gremlin images
        monsterImages.put("gremlin", gremlinImages);

        // Populate pathImages ArrayList with paths and rotated paths that are needed
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j <= 270; j += 90) {
                if(j == 0) {
                    ArrayList<PImage> pathImage = new ArrayList<>();
                    pathImage.add(loadImage("src/main/resources/WizardTD/path" + i + ".png"));
                    pathImages.add(pathImage);
                } else if(i == 0 && j == 90) {
                    PImage pathImage = rotateImageByDegrees(pathImages.get(i).get(0), j);
                    pathImages.get(i).add(pathImage);
                } else if(i == 1 || i == 2) {
                    PImage pathImage = rotateImageByDegrees(pathImages.get(i).get(0), j);
                    pathImages.get(i).add(pathImage);
                }
            }
        }

        // Get tower ready
        initialTowerCost = jsonObj.getInt("tower_cost");
        towerCost = initialTowerCost;
        // Get tower images
        for(int i = 0; i < 3; i++) {
            towerImages.add(loadImage("src/main/resources/WizardTD/tower" + i + ".png"));
        }
        
        // Get fireball image
        fballImage = loadImage("src/main/resources/WizardTD/fireball.png");
        
        // Get wand images
        for(int i = 0; i <= 2; i++)
            wands.add(loadImage("src/main/resources/WizardTD/wand" + i + ".png"));

        // Instantiate map and wave objects
        map = new Map(layout, grassImage, shrubImage, wizardImage, pathImages);
        wave = new Waves(jsonObj, monsterImages, map);

        // Get mana pool spell attributes
        manaPoolSpellCost = jsonObj.getInt("mana_pool_spell_initial_cost");
        manaPoolSpellCostIncreasePerUse = jsonObj.getInt("mana_pool_spell_cost_increase_per_use");
        manaPoolSpellCapMultiplier = jsonObj.getDouble("mana_pool_spell_cap_multiplier");
        manaPoolSpellManaGainedMultiplier = jsonObj.getDouble("mana_pool_spell_mana_gained_multiplier");

        // Create all the buttons
        buttons.add(new Button(50, "FF", "2x speed"));
        buttons.add(new Button(100, "P", "PAUSE"));
        buttons.add(new Button(150, "T", "Build\ntower"));
        buttons.get(2).setIsTower(true);
        buttons.add(new Button(200, "U1", "Upgrade\nrange"));
        buttons.add(new Button(250, "U2", "Upgrade\nspeed"));
        buttons.add(new Button(300, "U3", "Upgrade\ndamage"));
        buttons.add(new Button(350, "M", "Mana pool\ncost: " + manaPoolSpellCost));
        buttons.get(6).setIsMana(true);

        // Play starting sound
        Soundboard.playSound("start");
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(){
        switch(key) {
            // If 2x speed key pressed
            case 'f':
                buttons.get(0).switchOnOff();
                if(buttons.get(0).getIsOn())
                    frameRate(FPS*2);
                else
                    frameRate(FPS);
                break;
            // If pause key pressed
            case 'p':
                buttons.get(1).switchOnOff();
                // Highlight the button before pausing
                buttons.get(1).draw(this);
                if(buttons.get(1).getIsOn())
                    paused = true;
                else
                    paused = false;
                break;
            // If tower key pressed
            case 't':
                buttons.get(2).switchOnOff();
                if(buttons.get(2).getIsOn())
                    placingTower = true;
                else
                    placingTower = false;
                break;
            // If upgrade range key pressed
            case '1':
                buttons.get(3).switchOnOff();
                if(buttons.get(3).getIsOn()) {
                    upgradingRange = true;
                    numberOfUpgrades++;
                } else {
                    upgradingRange = false;
                    numberOfUpgrades--;
                }
                break;
            // If upgrade speed key pressed
            case '2':
                buttons.get(4).switchOnOff();
                if(buttons.get(4).getIsOn()) {
                    upgradingSpeed = true;
                    numberOfUpgrades++;
                } else {
                    upgradingSpeed = false;
                    numberOfUpgrades--;
                }
                break;
            // If upgrade damage key pressed
            case '3':
                buttons.get(5).switchOnOff();
                if(buttons.get(5).getIsOn()) {
                    upgradingDamage = true;
                    numberOfUpgrades++;
                } else {
                    upgradingDamage = false;
                    numberOfUpgrades--;
                }
                break;
            // If mana pool key pressed
            case 'm':
                manaPoolTriggered();
        }

        // If game over, allow restart if 'r' key is pressed
        if(wave.isGameOver() && key == 'r') {
            // Restart the game
            restart = true;
            loop();
            // Reset the tower and button states 
            // (built towers are removed and buttons aren't drawn on top of buttons - looks odd)
            towers = new ArrayList<Tower>();
            buttons = new ArrayList<Button>();
            // Ensure that booleans not initialised in setup() are all set back to false
            placingTower = false;
            upgradingRange = false;
            upgradingSpeed = false;
            upgradingDamage = false;
            numberOfUpgrades = 0;
            setup();
        }    
    }

    /**
     * Receive mouse pressed signal from the mouse.
     * @param e indicates that a mouse action has occured
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // If tower button pressed and hovering/clicking over map, create a tower and place it (can keep placing until tower button pressed again)
        if(placingTower && overMap()) {
            // Check that it's being placed on a grass tile
            for(Landmark grass: map.getGrass()) {
                // Make sure we're on a grass tile and not the wizard house (since there's a grass tile unde the wizard house to fill in white space)
                if(overElement(grass.getLocation()[0], grass.getLocation()[1], CELLSIZE, CELLSIZE) && 
                (overElement(map.getWizardHouse().getLocation()[0] + 8, map.getWizardHouse().getLocation()[1] + 8, CELLSIZE, CELLSIZE) ==  false)) {
                    // Make sure we don't build a tower on top of another tower
                    boolean towerPresent = false;
                    for(Tower twr: towers) {
                        if(overElement(twr.getX(), twr.getY(), CELLSIZE))
                            towerPresent = true;     
                    }
                    // If not tower already present at this location and there's enough mana to spend, build the tower (ensure player can't kill themselves by overbuying)
                    if (!towerPresent && wave.getManaBar().getMana() > initialTowerCost) {
                        towers.add(new Tower(grass.getLocation()[0], grass.getLocation()[1], towerImages, fballImage, jsonObj));
                        // Charge the mana bar the cost of the tower
                        wave.getManaBar().setMana(wave.getManaBar().getMana() - initialTowerCost);
                    }
                }
            }
        }

        // If upgrade range button pressed and hovering over a tower, upgrade tower's range
        if(upgradingRange) {
            for(Tower twr: towers) {
                if(overElement(twr.getX(), twr.getY(), CELLSIZE) && wave.getManaBar().getMana() > (INITIAL_UPGRADE_COST + (UPGRADE_COST_INCREASE * twr.getRangeUpgradeLevel()))) {
                    wave.getManaBar().setMana(wave.getManaBar().getMana() - (INITIAL_UPGRADE_COST + (UPGRADE_COST_INCREASE * twr.getRangeUpgradeLevel())));
                    twr.setRange(twr.getRange() + RANGE_UPGRADE);
                }
            }
        }

        // If upgrade speed button pressed and hovering over a tower, upgrade tower's speed
        if(upgradingSpeed) {
            for(Tower twr: towers) {
                if(overElement(twr.getX(), twr.getY(), CELLSIZE) && twr.getSpeed() != 0 && wave.getManaBar().getMana() > (INITIAL_UPGRADE_COST + (UPGRADE_COST_INCREASE * twr.getSpeedUpgradeLevel()))) {
                    wave.getManaBar().setMana(wave.getManaBar().getMana() - (INITIAL_UPGRADE_COST + (UPGRADE_COST_INCREASE * twr.getSpeedUpgradeLevel())));
                    twr.setSpeed(twr.getSpeed() - SPEED_UPGRADE);
                }
            }
        }

        // If upgrade damage button pressed and hovering over a tower, upgrade tower's damage
        if(upgradingDamage) {
            for(Tower twr: towers) {
                if(overElement(twr.getX(), twr.getY(), CELLSIZE) && wave.getManaBar().getMana() > (INITIAL_UPGRADE_COST + (UPGRADE_COST_INCREASE * twr.getDamageUpgradeLevel()))) {
                    wave.getManaBar().setMana(wave.getManaBar().getMana() - (INITIAL_UPGRADE_COST + (UPGRADE_COST_INCREASE * twr.getDamageUpgradeLevel())));
                    twr.setDamage(twr.getDamage() + (twr.getInitialTowerDamage() / 2));
                }
            }
        }

        // Check which button was clicked
        for(int i = 0; i < buttons.size(); i++) {
            if (overElement(buttons.get(i).getInfo()[0], buttons.get(i).getInfo()[1], buttons.get(i).getInfo()[2])) {
                // Switch button on
                if(!buttons.get(i).isMana())
                    buttons.get(i).switchOnOff();

                switch(i) {
                    // If 2x speed button clicked
                    case 0:
                        if(buttons.get(i).getIsOn())
                            frameRate(FPS*2);
                        else
                            frameRate(FPS);
                        break;
                    // If pause button clicked
                    case 1:
                        if(buttons.get(i).getIsOn()) {
                            // Highlight the button before pausing
                            buttons.get(1).draw(this);
                            //noLoop();
                            paused = true;
                        }
                        else
                            paused = false;
                            //loop();
                        break;
                    // If tower button clicked
                    case 2:
                        if(buttons.get(i).getIsOn())
                            placingTower = true;
                        else
                            placingTower = false;
                        break;
                    // If upgrade range button clicked
                    case 3:
                        if(buttons.get(i).getIsOn()) {
                            upgradingRange = true;
                            numberOfUpgrades++;
                        } else {
                            upgradingRange = false;
                            numberOfUpgrades--;
                        }
                        break;
                    // If upgrade speed button clicked
                    case 4:
                        if(buttons.get(i).getIsOn()) {
                            upgradingSpeed = true;
                            numberOfUpgrades++;
                        } else {
                            upgradingSpeed = false;
                            numberOfUpgrades--;
                        }
                        break;
                    // If upgrade damage button clicked
                    case 5:
                        if(buttons.get(i).getIsOn()) {
                            upgradingDamage = true;
                            numberOfUpgrades++;
                        } else {
                            upgradingDamage = false;
                            numberOfUpgrades--;
                        }
                        break;
                    // If mana pool button clicked
                    case 6:
                        manaPoolTriggered();
                }          
            }        
        }
        
    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        // If build tower button triggered along with all 3 upgrades and there's enough money, replace cursor with orange tower
        if(placingTower && numberOfUpgrades == 3 && wave.getManaBar().getMana() > INITIAL_UPGRADE_COST + INITIAL_UPGRADE_COST + INITIAL_UPGRADE_COST)
            cursor(towerImages.get(1));
        // If build tower button triggered, replace cursor with regular tower
        else if(placingTower)
            cursor(towerImages.get(0));
        // Else replace cursor with wand animation
        else {
            if(wandCounter % 8 == 0)
                cursor(wands.get(wandCounter / 8), 8, 5);
            if (wandCounter == 16)
                wandCounter = 0;
            else
                wandCounter++;
        }

        if(!paused) {
            // Update timer
            Timer.tick();
        }
        
        // Draw map
        map.draw(this);
        
        // Update wave
        if(!paused)
            wave.tick();
        // Update the mana bar even if game is paused
        // (allows player to buy towers while paused)
        else
            wave.manaTick();

        // Draw all the monsters
        wave.draw(this);

        // Update tower cost
        towerCost = initialTowerCost + (INITIAL_UPGRADE_COST * numberOfUpgrades);

        // Update towers
        if(!paused)
            towers.forEach(twr -> twr.tick(overElement(twr.getX(), twr.getY(), CELLSIZE), wave.getMonsters()));
        // If game is paused, still display the radius of the tower
        else
            towers.forEach(twr -> twr.setMouseOver(overElement(twr.getX(), twr.getY(), CELLSIZE)));
        
        // Draw towers
        towers.forEach(twr -> twr.draw(this));

        // Draw wizard house last so it superimposes monsters
        map.getWizardHouse().draw(this);
            
        // Draw top and side bars (need to be drawn in, instead of relying on background fill, so gremlins don't appear on top of them when spawning in)
        fill(132, 115, 74);
        stroke(132, 115, 74);
        rect(0, 0, WIDTH, 40);
        rect(20*CELLSIZE, 40, WIDTH, HEIGHT);

        // If hovering over a tower
        boolean overTower = false;
        // ... and an upgrade button is triggered, display upgrade box in bottom right corner
        if(upgradingRange || upgradingSpeed || upgradingDamage) {
            for(Tower twr: towers) {
                if(twr.getMouseOver()) {
                    overTower = true;
                    break;
                }
            }
            if(overTower) {
                drawUpgradeBox();
            }
        }

        // Draw wave timer and mana bar over the top bar
        wave.getWaveTimer().draw(this);
        wave.getManaBar().draw(this);

        // Update and draw the buttons
        buttons.forEach(button -> {button.tick(overElement(button.getInfo()[0], button.getInfo()[1], button.getInfo()[2]), towerCost, manaPoolSpellCost); button.draw(this);});

        // If game over, draw game over screen and play sound
        if(wave.isGameOver() && !restart) {
            noLoop();
            textSize(35);
            fill(73, 255, 66);
            text("YOU LOST", 254, 250);
            textSize(22);
            text("Press 'r' to restart", 241, 301);
            Soundboard.playSound("gameover");
        // Set restart back to false
        } else if (wave.isGameOver()) {
            restart = false;
        // If all monsters are killed and game is won, draw win screen and play sound
        } else if (wave.doWeHaveAWinner()) {
            noLoop();
            textSize(35);
            fill(217, 31, 240);
            //text("YOU WIN", 254, 265);
            text("YOU WIN", 254, 228);
            Soundboard.playSound("win");
        }
    }

    public static void main(String[] args) {
        PApplet.main("WizardTD.App");
    }

    /**
     * Checks if the user's mouse is hovering over an element in the game window.
     * @param elementX the x-coordinate of the element
     * @param elementY the y-coordinate of the element
     * @param size the size of the element
     * @return true if mouse is hovering over the element in question
     */
    public boolean overElement(int elementX, int elementY, int size) {
        return (mouseX >= elementX && mouseX <= elementX + size) && (mouseY >= elementY && mouseY <= elementY + size);
    }

    /**
     * Checks if the user's mouse is hovering over an element in the game window.
     * @param elementX the x-coordinate of the element
     * @param elementY the y-coordinate of the element
     * @param width the width of the element
     * @param height the height of the element
     * @return true if mouse is hovering over the element in question
     */
    public boolean overElement(int elementX, int elementY, int width, int height) {
        return (mouseX >= elementX && mouseX <= elementX + width) && (mouseY >= elementY && mouseY <= elementY + height);
    }

    /**
     * Checks if the user's mouse is hovering over the map.
     * @return true if mouse is hovering over the map
     */
    public boolean overMap() {
        return (mouseX <= CELLSIZE*BOARD_WIDTH) && (mouseY >= TOPBAR && mouseY <= CELLSIZE*BOARD_WIDTH + TOPBAR);
    }

    /**
     * Performs the events necessary after user activates the mana pool spell. This includes charging 
     * the cost of the spell to the mana bar as well as increasing the mana pool spell cost, mana bar 
     * cap, mana trickle gained, and amount of mana earned from killing monsters. 
     */
    public void manaPoolTriggered() {
        // If you have enough mana to spend
        if(wave.getManaBar().getMana() > manaPoolSpellCost) {
            // Play mana pool spell sound
            Soundboard.playSound("manapool");
            // Charge the cost to the mana bar
            wave.getManaBar().setMana(wave.getManaBar().getMana() - manaPoolSpellCost);
            // Increase the mana pool spell cost
            manaPoolSpellCost += manaPoolSpellCostIncreasePerUse;
            // Increase the mana bar cap/maximum
            wave.getManaBar().setManaCap((int)(wave.getManaBar().getManaCap() * manaPoolSpellCapMultiplier));
            // Increase the mana trickle
            wave.getManaBar().setManaGPS(wave.getManaBar().getManaGPS() * manaPoolSpellManaGainedMultiplier);
            // Increase amount of mana earned from monster kills
            wave.getManaBar().setManaPoolSpellMultiplier(wave.getManaBar().getManaPoolSpellMultiplier() * manaPoolSpellManaGainedMultiplier);
        }
    }

    /**
     * Draw upgrade dialog box and relevant information in the bottom right corner of the window when upgrading a tower.
     * This occurs when one or more upgrade buttons are activated and the user's mouse is hovering over a tower.
     */
    public void drawUpgradeBox() {
        // Find the tower the mouse is hovering over to get upgrade costs
        int rangePrice = INITIAL_UPGRADE_COST;
        int speedPrice = INITIAL_UPGRADE_COST;
        int damagePrice = INITIAL_UPGRADE_COST;

        for(Tower twr: towers) {
            if(twr.getMouseOver()) {
                rangePrice += UPGRADE_COST_INCREASE * twr.getRangeUpgradeLevel();
                speedPrice += UPGRADE_COST_INCREASE * twr.getSpeedUpgradeLevel();
                damagePrice += UPGRADE_COST_INCREASE * twr.getDamageUpgradeLevel();
                break;
            }
        }

        // Get to drawing!
        stroke(0); // black border
        strokeWeight(1);
        fill(255, 255, 255); // white background
        rect(658, 578, 80, 20);
        textSize(12);
        fill(0); // make text black
        text("Upgrade cost", 659, 592);

        stroke(0);
        strokeWeight(1);
        fill(255, 255, 255);
        rect(658, 598, 80, 20);
        textSize(12);
        fill(0);

        if(upgradingRange) {
            text("range:      " + rangePrice, 659, 612);

            if (upgradingSpeed || upgradingDamage) {
                stroke(0);
                strokeWeight(1);
                fill(255, 255, 255);
                rect(658, 618, 80, 20);
                textSize(12);
                fill(0);

                if (upgradingSpeed) {
                    text("speed:      " + speedPrice, 659, 632);

                    if(upgradingDamage) {
                        stroke(0);
                        strokeWeight(1);
                        fill(255, 255, 255);
                        rect(658, 638, 80, 20);
                        textSize(12);
                        fill(0);
                        text("damage:   " + damagePrice, 659, 652);


                        stroke(0);
                        strokeWeight(1);
                        fill(255, 255, 255);
                        rect(658, 658, 80, 20);
                        textSize(12);
                        fill(0);
                        int total = rangePrice + speedPrice + damagePrice;
                        // Account for 3-digit totals
                        if(total < 100)
                            text("Total:       " + total, 659, 672);
                        else
                            text("Total:      " + total, 656, 672);
                    } else {
                        stroke(0);
                        strokeWeight(1);
                        fill(255, 255, 255);
                        rect(658, 638, 80, 20);
                        textSize(12);
                        fill(0);
                        int total = rangePrice + speedPrice;
                        if(total < 100)
                            text("Total:       " + total, 659, 652);
                        else
                            text("Total:      " + total, 656, 652);
                    }
                } else if (upgradingDamage) {
                    text("damage:   " + damagePrice, 659, 632);

                    stroke(0);
                    strokeWeight(1);
                    fill(255, 255, 255);
                    rect(658, 638, 80, 20);
                    textSize(12);
                    fill(0);
                    int total = rangePrice + damagePrice;
                    if(total < 100)
                        text("Total:       " + total, 659, 652);
                    else
                        text("Total:      " + total, 656, 652);
                }
            } else {
                stroke(0);
                strokeWeight(1);
                fill(255, 255, 255);
                rect(658, 618, 80, 20);
                textSize(12);
                fill(0);
                if(rangePrice < 100)
                    text("Total:       " + rangePrice, 659, 632);
                else
                    text("Total:      " + rangePrice, 656, 632);
            }

        } else if (upgradingSpeed) {
            text("speed:      " + speedPrice, 659, 612);

            if(upgradingDamage) {
                stroke(0);
                strokeWeight(1);
                fill(255, 255, 255);
                rect(658, 618, 80, 20);
                textSize(12);
                fill(0);
                text("damage:   " + damagePrice, 659, 632);

                stroke(0);
                strokeWeight(1);
                fill(255, 255, 255);
                rect(658, 638, 80, 20);
                textSize(12);
                fill(0);
                int total = speedPrice + damagePrice;
                if(total < 100)
                    text("Total:       " + total, 659, 652);
                else
                    text("Total:      " + total, 656, 652);
            } else {
                stroke(0);
                strokeWeight(1);
                fill(255, 255, 255);
                rect(658, 618, 80, 20);
                textSize(12);
                fill(0);
                text("Total:       " + speedPrice, 659, 632);

            }
        } else if (upgradingDamage) {
            text("damage:   " + damagePrice, 659, 612);
            stroke(0);
            strokeWeight(1);
            fill(255, 255, 255);
            rect(658, 618, 80, 20);
            textSize(12);
            fill(0);
            if(damagePrice < 100)
                text("Total:       " + damagePrice, 659, 632);
            else
                text("Total:      " + damagePrice, 656, 632);
        }

    }

    /**
     * Source: https://stackoverflow.com/questions/37758061/rotate-a-buffered-image-in-java
     * @param pimg The image to be rotated
     * @param angle between 0 and 360 degrees
     * @return the new rotated image
     */
    public PImage rotateImageByDegrees(PImage pimg, double angle) {
        BufferedImage img = (BufferedImage) pimg.getNative();
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        PImage result = this.createImage(newWidth, newHeight, RGB);
        //BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage rotated = (BufferedImage) result.getNative();
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                result.set(i, j, rotated.getRGB(i, j));
            }
        }

        return result;
    }
}
