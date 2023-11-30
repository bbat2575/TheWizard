package WizardTD;

import processing.core.PImage;
import processing.data.JSONObject;
import processing.core.PApplet;
import java.util.*;
import java.lang.Math;

/**
 * Towers are the wizard's only defense against the oncoming horde of monsters out to get him. Tower's shoot and kill monsters
 * which gives the wizard more mana to work with.
 */
public class Tower {
    // The tower's size and coordinates
    private static final int SIZE = 32;
    private int x;
    private int y;
    // Tower images
    private PImage sprite;
    private ArrayList<PImage> towerImages = new ArrayList<>();
    // Detect if mouse is hovering over the tower
    private boolean mouseOver;
    // Store tower range (diameter of circle around tower), speed and damage
    private int range;
    private double speed;
    private int damage;
    // Initial tower damage (before upgrades)
    private int initialTowerDamage;
    // Denote whether any upgrades are taking place
    private boolean rangeUpgraded;
    private boolean speedUpgraded;
    private boolean damageUpgraded;
    // Store the level that an upgrade is currently at (accounts for tower colour)
    private int rangeUpgradeLevel;
    private int speedUpgradeLevel;
    private int damageUpgradeLevel;
    // Store the overall level of an upgrade (doesn't account for tower color - used for App.java upgrade costs)
    private int rangeUpgradeLevelTotal;
    private int speedUpgradeLevelTotal;
    private int damageUpgradeLevelTotal;
    // Store the coordinates of visual upgrades
    private int[] rangeUpgradeVisual;
    private int[] speedUpgradeVisual;
    private int[] damageUpgradeVisual;
    // The Monster the tower is targetting
    private Monster target;
    // Fireball object
    Fireball fball;
    // Keeps track of seconds per frame (used for tower's firing speed)
    private double tickTimer;
    private boolean startTimer;

    /**
     * Constucs a Tower object given x,y-coordinates, tower images, a fireball image, and a JSONObject.
     * @param x tower's x-coordinate
     * @param y tower's y-coordinate
     * @param towerImages array of tower images for different upgrade levels
     * @param fballImage fireball image
     * @param jsonObj a JSONObject
     */
    public Tower(int x, int y, ArrayList<PImage> towerImages, PImage fballImage, JSONObject jsonObj) {
        this.x = x;
        this.y = y;
        this.sprite = towerImages.get(0);
        this.towerImages = towerImages;
        this.range = jsonObj.getInt("initial_tower_range")*2;
        this.speed = jsonObj.getDouble("initial_tower_firing_speed");
        this.initialTowerDamage = jsonObj.getInt("initial_tower_damage");
        this.damage = this.initialTowerDamage;
        this.rangeUpgradeVisual = new int[2];
        this.speedUpgradeVisual = new int[2];
        this.damageUpgradeVisual = new int[2];
        this.rangeUpgradeVisual[0] = this.x + 3;
        this.rangeUpgradeVisual[1] = this.y + 3;
        this.speedUpgradeVisual[0] = this.x + 6;
        this.speedUpgradeVisual[1] = this.y + 6;
        this.damageUpgradeVisual[0] = this.x + 1;
        this.damageUpgradeVisual[1] = this.y + 31;
        this.fball = new Fireball(this.x, this.y, fballImage);
        this.target = null;
        this.tickTimer = 0;
    }

    /**
     * Updates the tower, whether user's mouse is interacting with it, and finds targets to shoot and kill.
     */
    public void tick(boolean mouseOver, ArrayList<MonsterCollection> monsterCollections) {
        this.mouseOver = mouseOver;

        // If there's no current target and fireball isn't shooting, attempt to locate a target in range
        if(target == null && !fball.getIsShooting()) {
        
            // Store which monsters are in range of the tower
            ArrayList<Monster> monstersInRange = new ArrayList<Monster>();

            // Find which monsters are alive and in range of the tower
            for(MonsterCollection mc: monsterCollections) {
                for(Monster mtr: mc.getMonsters()) {
                    if(mtr.getIsAlive() && inRange(x + (SIZE/2), y + (SIZE/2), (range)/2, mtr.getX(), mtr.getY(), mtr.getSize()))
                        monstersInRange.add(mtr);
                }
            }

            // If targets in range, locate a target
            if(monstersInRange.size() > 0) {
                // Stores the closest monster, starting with the first one in the collection
                Monster closest = monstersInRange.get(0);

                for(Monster mtr: monstersInRange) {
                    if(Math.sqrt(Math.pow(mtr.getX() - x, 2) + Math.pow(mtr.getY() - y, 2)) < (Math.sqrt(Math.pow(closest.getX() - x, 2) + Math.pow(closest.getY() - y, 2)))) {
                        closest = mtr;
                    }
                }
                
                target = closest;
                // Start the timer
                startTimer = true;
            }
        }

        // If we have a target and we're not already shooting it, shoot the fireball and play the sound
        if(target != null && !fball.getIsShooting() && tickTimer == 0) {
            fball.shoot();
            Soundboard.playSound("shoot");
        }
        
        // If we have a target and the fireball is shooting, update the fireball
        if(target != null && fball.getIsShooting()) {
            fball.tick(this, target);
        }

        // If target is shot, set target to null
        if(target != null && !fball.getIsShooting()) {
            target = null;
        }

        // Ensure shots are only taken at intervals indicated by speed (fireballs per second)
        if(startTimer) {
            tickTimer += (1.0 / (double)App.FPS);
             // e.g. for a speed of 1.5 fireballs per second, you want to shoot a fireball every 1/1.5 seconds
            if(tickTimer >= 1 / speed) {
                startTimer = false;
                tickTimer = 0;
            }
        }

        // If all 3 tower upgrades are bought twice, then upgrade tower sprite to red tower and play sound
        if(sprite == towerImages.get(1) && rangeUpgradeLevel >= 1 && speedUpgradeLevel >= 1 && damageUpgradeLevel >= 1) {
            Soundboard.playSound("tower");
            sprite = towerImages.get(2);
            rangeUpgradeLevel -= 1;
            speedUpgradeLevel -= 1;
            damageUpgradeLevel -= 1;
            if(rangeUpgradeLevel == 0)
                rangeUpgraded = false;
            if(speedUpgradeLevel == 0)
                speedUpgraded = false;
            if(damageUpgradeLevel == 0)
                damageUpgraded = false;
        // If all 3 tower upgrades are bought once, then upgrade tower sprite to orange tower and play sound
        } else if(sprite == towerImages.get(0) && rangeUpgradeLevel >= 1 && speedUpgradeLevel >= 1 && damageUpgradeLevel >= 1) {
            Soundboard.playSound("tower");
            sprite = towerImages.get(1);
            rangeUpgradeLevel -= 1;
            speedUpgradeLevel -= 1;
            damageUpgradeLevel -= 1;
            if(rangeUpgradeLevel == 0)
                rangeUpgraded = false;
            if(speedUpgradeLevel == 0)
                speedUpgraded = false;
            if(damageUpgradeLevel == 0)
                damageUpgraded = false;
        }
    }

    /**
     * Draws the tower onto the map along with any upgrades.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // Draw fireball (draw first since should not superimpose tower)
        fball.draw(app);

        // Draw tower
        app.image(sprite, x, y);

        // Draw range circle when mouse hovers over tower
        if(mouseOver) {
            app.stroke(255, 255, 0);
            app.noFill();
            app.ellipse(x + (SIZE/2), y + (SIZE/2), range, range);
        }

        // Draw range upgrade visual on tower
        if (rangeUpgraded) {
            app.stroke(217, 31, 240);
            app.strokeWeight(1);
            app.noFill();

            for(int i = 0; i < rangeUpgradeLevel; i++)
                app.ellipse(rangeUpgradeVisual[0] + (i*6), rangeUpgradeVisual[1], 6, 6);
        }

        // Draw range upgrade visual on tower
        if (damageUpgraded) {
            app.textSize(11);
            app.fill(217, 31, 240);

            for(int i = 0; i < damageUpgradeLevel; i++) 
                app.text("x", damageUpgradeVisual[0] + (i*6), damageUpgradeVisual[1]);
        }

        // Draw range upgrade visual on tower
        if (speedUpgraded) {
            app.stroke(120, 167, 255);
            app.strokeWeight(2);
            app.noFill();

            for(int i = 0; i < speedUpgradeLevel; i++)
                app.rect((float)(speedUpgradeVisual[0] + (i*1.15)), (float)(speedUpgradeVisual[1] + (i*1.15)), (float)(20 - (i*2.15)), (float)(20 - (i*2.15)));
        }
    }

    /**
     * Gets the x-coordinate position of the tower.
     * @return x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate position of the tower.
     * @return y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the center of the tower's x-coordinate position (used by Fireball).
     * @return tower's x-coordinate
     */
    public int getXCenter() {
        return x + SIZE/2;
    }

    /**
     * Gets the center of the tower's y-coordinate position (used by Fireball).
     * @return tower's y-coordinate
     */
    public int getYCenter() {
        return y + SIZE/2;
    }

    /**
     * Indicates if user's mouse is hovering over the tower.
     * @return true if mouse is hovering over the tower
     */
    public boolean getMouseOver() {
        return mouseOver;
    }

    /**
     * Sets whether or not user's mouse is hovering over the tower.
     * @param mouseOver if user's mouse is hovering over the tower
     */
    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    /**
     * Gets the range of the tower.
     * @return range of the tower
     */
    public int getRange() {
        return range;
    }

    /**
     * Sets the range of the tower.
     * @param range new range for the tower
     */
    public void setRange(int range) {
        // There's no limit on number of upgrades
        this.range = range;
        rangeUpgraded = true;
        rangeUpgradeLevel++;
        rangeUpgradeLevelTotal++;
    }

    /**
     * Gets the total number of times range has been upgraded.
     * @return total range upgrades
     */
    public int getRangeUpgradeLevel() {
        return rangeUpgradeLevelTotal;
    }

    /**
     * Gets the firing speed of the tower.
     * @return firing speed of the tower
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Sets the firing speed of the tower.
     * @param speed the new firing speed of the tower
     */
    public void setSpeed(double speed) {
        // Can upgrade until speed = 0
        this.speed = speed;
        speedUpgraded = true;
        speedUpgradeLevel++;
        speedUpgradeLevelTotal++;
    }

    /**
     * Gets the total number of times speed has been upgraded.
     * @return total speed upgrades
     */
    public int getSpeedUpgradeLevel() {
        return speedUpgradeLevelTotal;
    }

    /**
     * Gets the damage that the tower inflicts on a monster (w/out monster armour applied).
     * @return tower's damage
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Sets the damage that the tower inflicts on a monster (w/out monster armour applied).
     * @param damage tower's new damage
     */
    public void setDamage(int damage) {
        // There's no limit on number of upgrades
        this.damage = damage;
        damageUpgraded = true;
        damageUpgradeLevel++;
        damageUpgradeLevelTotal++;
    }

    /**
     * Gets the total number of times damage has been upgraded.
     * @return total damage upgrades
     */
    public int getDamageUpgradeLevel() {
        return damageUpgradeLevelTotal;
    }

    /**
     * Get's the tower's initial damage before any upgrades.
     * @return tower's initial damage
     */
    public int getInitialTowerDamage() {
        return initialTowerDamage;
    }

    /**
     * Check if a monster is in range of the tower.
     * @param centerX centre x-coordinate of tower
     * @param centerY centre y-coordinate of tower
     * @param radius radius of tower's range
     * @param monsterX centre x-coordinate of monster
     * @param monsterY centre y-coordinate of monster
     * @param size monster's size
     */
    private boolean inRange(int centerX, int centerY, int radius, int monsterX, int monsterY, int size) {
        // Check if any of the 4 corners of the monster are within the range of the tower
        return (Math.sqrt(Math.pow(monsterX - centerX, 2) + Math.pow(monsterY - centerY, 2))
        <= radius) || (Math.sqrt(Math.pow((monsterX + size) - centerX, 2) + Math.pow(monsterY - centerY, 2))
        <= radius) || (Math.sqrt(Math.pow(monsterX - centerX, 2) + Math.pow((monsterY + size) - centerY, 2))
        <= radius) || (Math.sqrt(Math.pow((monsterX + size) - centerX, 2) + Math.pow((monsterY + size) - centerY, 2))
        <= radius);
    }
}
