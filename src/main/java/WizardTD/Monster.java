package WizardTD;

import processing.core.PImage;
import processing.core.PApplet;

import java.util.*;
import java.lang.Math;

/**
 * Monsters that are attacking the wizard house.
 */
public class Monster {
    // Size of monster sprite
    private static final int SIZE = 20;
    // Monster coordinates
    private float x;
    private float y;
    // Monster's initial health points, current health points, speed and armour
    private int initialHp;
    private int hp;
    private double speed;
    private double armour;
    // Monster image
    private PImage sprite;
    // An array list containing all the gremlin sprites (for death animation)
    private ArrayList<PImage> monsterImages;
    // journey keeps track of where the gremlin is on the path
    private int journey = 0;
    // isSpawned keeps track of whether or not the gremlin has spawned (used for when the gremlin reaches the wizard's house and needs to respawn on the path again)
    private boolean isSpawned;
    // isAlive keeps track of whether gremlin is alive and animated
    private boolean isAlive;
    // isDying keeps track of when monster dying animation is played
    private boolean isDying;
    // isDead keeps track of whether monster has been shot dead or not
    private boolean isDead;
    // Indicates counted as dead by MonsterCollection
    private boolean counted;
    // Used to count ticks + frames for well-timed monster death animation
    private int deathAnimationCount;
    private int deathAnimationFrames;
    // Track when monster hits the wizard house
    private boolean hitWizard;

    // Pick a gremlin's path to wizard house
    private ArrayList<ArrayList<Integer>> monsterPath;

    /**
     * Constructs a Monster object given monster health points, speed, armour, images, and path to travel.
     * @param hp monster health points
     * @param speed monster speed
     * @param armour monster armour
     * @param monsterImages monster images including frames for death animation
     * @param monsterPath path for monster to take to wizard house
     */
    public Monster(int hp, double speed, double armour, ArrayList<PImage> monsterImages, ArrayList<ArrayList<Integer>> monsterPath) {
        this.initialHp = hp;
        this.hp = hp;
        this.speed = speed;
        this.armour = armour;
        this.sprite = monsterImages.get(0);
        this.monsterImages = monsterImages;
        this.monsterPath = monsterPath;
        this.deathAnimationCount = 1;
    }

    /**
     * Updates the monster's position along the path is takes to the wizard house.
     */
    public void tick() {
        // Set up the gremlin's spawn point outside the map
        if(journey == 0 && !isSpawned && !isDead) {
            x = monsterPath.get(0).get(0);
            y = monsterPath.get(0).get(1);
            isSpawned = true;
        }

        // Logic for monster travelling along a path
        if (isAlive) {
            if (journey != monsterPath.size() - 1){
                // If coordinate of current path tile has the same x-value but a lower y-value as the next path tile, then increment y-value only (since same x-value means same column =  gremlin is traversing a column downwards)
                if(Math.abs(x - monsterPath.get(journey + 1).get(0).floatValue()) < speed && y < monsterPath.get(journey + 1).get(1).floatValue()) {
                    x = monsterPath.get(journey).get(0);
                    y += speed;
                    if (y >= monsterPath.get(journey + 1).get(1).floatValue()) {
                        journey++;
                    }
                // If coordinate of current path tile has the same y-value but a lower x-value as the next path tile, then increment x-value only (since same y-value means same row = gremlin is traversing a row to the right)
                } else if (Math.abs(y - monsterPath.get(journey + 1).get(1).floatValue()) < speed && x < monsterPath.get(journey + 1).get(0).floatValue()) {
                    x += speed;
                    y = monsterPath.get(journey).get(1);
                    if (x >= monsterPath.get(journey + 1).get(0).floatValue()) {
                        journey++;
                    }
                // If coordinate of current path tile has the same x-value but a higher y-value as the next path tile, then decrement y-value only (since same x-value means same column =  gremlin is traversing a column upwards)
                } else if (Math.abs(x - monsterPath.get(journey + 1).get(0).floatValue()) < speed && y > monsterPath.get(journey + 1).get(1).floatValue()) {
                    x = monsterPath.get(journey).get(0);
                    y -= speed;
                    if (y <= monsterPath.get(journey + 1).get(1).floatValue()) {
                        journey++;
                    }
                // If coordinate of current path tile has the same y-value but a higher x-value as the next path tile, then decrement x-value only (since same y-value means same row = gremlin is traversing a row to the left)
                } else if (Math.abs(y - monsterPath.get(journey + 1).get(1).floatValue()) < speed && x > monsterPath.get(journey + 1).get(0).floatValue()) {
                    x -= speed;
                    y = monsterPath.get(journey).get(1);
                    if (x <= monsterPath.get(journey + 1).get(0).floatValue()) {
                        journey++;
                    }
                }
            // Reached the wizard house
            } else {
                hitWizard = true;
                journey = 0;
                isSpawned = false;
            }
        }
    }

    /**
     * Draws the monster onto the path along with its health bar, or draw it dying.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        if (isDying) {
            isAlive = false;

            app.image(monsterImages.get(deathAnimationCount), x, y);

            // Handle death animation timing
            deathAnimationFrames++;
            
            if(deathAnimationFrames == 3) {
                deathAnimationCount++;
                deathAnimationFrames = 0;
            }
            
            if(deathAnimationCount == 6) {
                deathAnimationCount = 1;
                isDying = false;
                isDead = true;
            }

        }

        // Only draw if alive
        if (isAlive) {
            app.image(sprite, x, y);

            // Display health bar
            app.noStroke();
            app.fill(227, 20, 34);
            app.rect(x - 4.5f, y - 4.4f, 29.5f, 3);
            app.fill(73, 255, 66);
            app.rect(x - 4.5f, y - 4.4f, 29.5f * ((float)hp/(float)initialHp), 3);
        }

    }

    /**
     * Gets the monster's x-coordinate position.
     * @return monster's x-coordinate
     */
    public int getX() {
        return (int)x;
    }

    /**
     * Gets the monster's y-coordinate position.
     * @return monster's y-coordinate
     */
    public int getY() {
        return (int)y;
    }

    /**
     * Gets the center of the monster's x-coordinate position (used by Fireball).
     * @return monster's x-coordinate
     */
    public float getXCenter() {
        return x + SIZE/2;
    }

    /**
     * Gets the center of the monster's y-coordinate position (used by Fireball).
     * @return monster's y-coordinate
     */
    public float getYCenter() {
        return y + SIZE/2;
    }

    /**
     * Gets the size of the monster.
     * @return size of monster
     */
    public int getSize() {
        return SIZE;
    }

    /**
     * Sets the monster on its way to the wizard house.
     */
    public void bringToLife() {
        isAlive = true;
    }

    /**
     * Gets whether or not the monster is on its way to the wizard house.
     * @return true if monster is moving towards the wizard house
     */
    public boolean getIsAlive() {
        return isAlive;
    }

    /**
     * Gets the speed that the monster travels.
     * @return monster's speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Gets the monster's current health points.
     * @return monster's health points
     */
    public int getHp() {
        return hp;
    }

    /**
     * Sets the monster's current health points.
     * @param hp monster's new health point value
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * Gets the monster's armour level.
     * @return monster's armour level
     */
    public double getArmour() {
        return armour;
    }

    /**
     * Indicates whether or not monster is in the process of dying.
     * @return true if monster is currently dying
     */
    public boolean getIsDying() {
        return isDying;
    }

    /**
     * Sets if monster is currently dying or not.
     * @param isDying if monster is dying or not
     */
    public void setIsDying(boolean isDying) {
        this.isDying = isDying;
        // Play monster death sound
        Soundboard.playSound("death");
    }

    /**
     * Gets if monster is dead.
     * @return true if monster is dead
     */
    public boolean getIsDead() {
        return isDead;
    }

    /**
     * Sets whether or not monster is dead.
     * @param isDead if monster is dead or not
     */
    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    /**
     * Indicates if the monster's death has been counted.
     * @return true if monster death has been counted
     */
    public boolean getCounted() {
        return counted;
    }

    /**
     * Sets whether or not the monster's death has been counted.
     * @param counted true if monster's death has been counted
     */
    public void setCounted(boolean counted) {
        this.counted = counted;
    }

    /**
     * Gets whether or not monster has reached the wizard house.
     * @return true if monster has reached the wizard house
     */
    public boolean getHitWizard() {
        return hitWizard;
    }

    /**
     * Indicates that the monster has reached the wizard house or not.
     * @param hitWizard if the monster has reached the wizard house
     */
    public void setHitWizard(boolean hitWizard) {
        this.hitWizard = hitWizard;
    }
}
