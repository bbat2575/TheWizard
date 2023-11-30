package WizardTD;

import processing.core.PImage;
import processing.core.PApplet;

/**
 * Represents any tile that the game map is comprised of including grass, shrubs and path ways.
 */
public class Landmark {
    // Landmark coordinates
    private int x;
    private int y;
    // Landmark image
    private PImage sprite;

    /**
     * Constructs a Landmark object given x,y-coordinates and an image.
     * @param x x-coordinate of this tile
     * @param y y-coordinate of this tile
     * @param sprite image of Landmark
     */
    public Landmark(int x, int y, PImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
    }

    /**
     * Draws the Landmark tile onto the map.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // Handling graphics
        app.image(sprite, x, y);
    }

    /**
     * Gets the location of the Landmark object.
     * @return an int array containing the x,y-coordinates
     */
    public int[] getLocation() {
        int[] location = {x, y};
        return location;
    }
}