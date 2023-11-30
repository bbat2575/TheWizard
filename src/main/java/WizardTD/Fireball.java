package WizardTD;

import processing.core.PImage;
import processing.core.PApplet;

import java.lang.Math;

/**
 * Shot by towers and used to kill monsters.
 */
public class Fireball {
    // Fireball coordinates
    private float x;
    private float y;
    // Fireball image
    private PImage sprite;
    // Keep track of whether fireball is being shot
    private boolean isShooting;
    // Keep track of when first shot is taken
    private boolean justShot;
    // Store speed of fireball
    private double speed;

    /**
     * Constructs a Fireball object given an initial position and fireball image.
     * @param x initial x-coordinate position (tower position)
     * @param y initial y-coordinate position (tower position)
     * @param sprite fireball image
     */
    public Fireball(int x, int y, PImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
    }

    /**
     * Updates the location of the fireball.
     * @param tower the tower that shoots the fireball
     * @param monster the monster that is being shot
     */
    public void tick(Tower tower, Monster monster) {
        // If tower locates a target and begins shooting
        if(isShooting) {
            if (monster.getSpeed() < 3.5)
                speed = 3.5;
            else
                this.speed = monster.getSpeed() + 1.6;
        
            // Let the shooting begin!
            if(justShot) {
                x = tower.getXCenter();
                y = tower.getYCenter();
                justShot = false;
            // If fireball reaches monster
            } else if (Math.abs(monster.getXCenter() - x) <= speed && Math.abs(monster.getYCenter() - y) <= speed) {
                isShooting = false;
                // Damage monster's hp (accounting for monster's armour)
                monster.setHp(monster.getHp() - (int)(tower.getDamage() * monster.getArmour()));
                if (monster.getHp() <= 0)
                    monster.setIsDying(true);
            // If fireball chasing monster
            // Monster is at the bottom right of the tower
            } else if (monster.getXCenter() > x && monster.getYCenter() > y) {
                x += speed;
                y += speed;
            // Monster is at the bottom left of the tower
            } else if (monster.getXCenter() < x && monster.getYCenter() > y) {
                x -= speed;
                y += speed;
            // Monster is at the top right of the tower
            } else if (monster.getXCenter() > x && monster.getYCenter() < y) {
                x += speed;
                y -= speed;
            // Monster is at the top left of the tower
            } else if (monster.getXCenter() < x && monster.getYCenter() < y) {
                x -= speed;
                y -= speed;
            // Monster is at the bottom of the tower
            }else if (monster.getXCenter() == x && monster.getYCenter() > y) {
                y += speed;
            // Monster is at the left of the tower
            } else if (monster.getXCenter() < x && monster.getYCenter() == y) {
                x += speed;
            // Monster is at the top of the tower
            } else if (monster.getXCenter() == x && monster.getYCenter() < y) {
                y += speed;
            // Monster is at the right of the tower
            } else if (monster.getXCenter() > x && monster.getYCenter() == y) {
                x += speed;
            }
        }
    }

    /**
     * Draws the firebal onto the map.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // Handling graphics
        if (isShooting)
            app.image(sprite, x, y);
    }

    /**
     * Shoots the fireball.
     */
    public void shoot() {
        isShooting = true;
        justShot = true;
    }

    /**
     * Informs that the fireball has been shot.
     */
    public void shot() {
        isShooting = false;
    }

    /**
     * Returns whether or not fireball has been shot.
     * @return true if fireball has been shot
     */
    public boolean getIsShooting() {
        return isShooting;
    }
}
