package WizardTD;

import processing.core.PApplet;

/**
 * A button that sits on the sidebar of the game window and provides a gameplay function.
 */
public class Button {
    // Size of a button
    private static final int SIZE = 40;
    // Button border position
    private int rectX;
    private int rectY;
    // Button text position
    private int textX;
    private int textY;
    // Button's description position
    private int descX;
    private int descY;
    // Button text and description
    private String text;
    private String description;
    // Detect if mouse is hovering over button
    private boolean mouseOver;
    // Store whether button is on or not
    private boolean isOn;
    // Track if a button is a mana pool or build tower button
    private boolean isMana;
    private boolean isTower;
    // Total tower cost (w/ any triggered upgrades)
    private int towerCost;
    // Cost of the mana pool spell
    private int manaPoolSpellCost;

    /**
     * Constructs a Button object given a button position, 
     * text, and description.
     * @param rectY the button's position in the y-direction along the sidebar
     * @param text a letter or two that goes on the button
     * @param description text that goes alongside the button that describes what it does
     */
    public Button(int rectY, String text, String description) {
        this.rectX = 650;
        this.rectY = rectY;
        this.text = text;
        this.description = description;

        this.textX = rectX + 4;
        this.textY = rectY + 31;
        this.descX = rectX + 46;
        this.descY = rectY + 16;
    }

    /**
     * Updates the tower cost and mana pool spell cost.
     * @param mouseOver whether the mouse is hovering over the button
     * @param towerCost the cost to build a tower
     * @param manaPoolSpellCost the cost to cast the mana pool spell
     */
    public void tick(boolean mouseOver, int towerCost, int manaPoolSpellCost) {
        this.mouseOver = mouseOver;

        // If this is a build tower button, update the tower cost for the label
        if(isTower)
            this.towerCost = towerCost;
        // If this is a mana pool button, update the mana pool spell vost for the label and description
        if(isMana) {
            this.manaPoolSpellCost = manaPoolSpellCost;
            description = "Mana pool\ncost: " + this.manaPoolSpellCost;
        }
    }

    /**
     * Draws the button onto the sidebar.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // The button border is a rectangle with no fill and a black border of weight 2
        app.stroke(0);
        app.strokeWeight(2);

        // If mouse is hovering over button and button is off or is the mana pool button, highlight it white
        if(mouseOver && (!isOn || isMana)) {
            app.fill(255,255,255);
        // If it's on and it's not the mana button, highlight it yellow
        } else if (isOn && !isMana) {
            app.fill(255, 255, 0);
        // Else don't highlight the button
        } else {
            app.noFill();
        }

        //app.rect(645, 50, 50, 50);
        app.rect(rectX, rectY, SIZE, SIZE);  

        // Draw the button text
        app.textSize(25);
        app.fill(0); // make text and description black
        app.text(text, textX, textY);

        // Draw the button's description
        app.textSize(12);
        app.text(description, descX, descY);

        // Create hovering label text for build tower button
        if(mouseOver && isTower) {
            // Black border
            app.stroke(0);
            app.strokeWeight(1);
            app.fill(204, 204, 198);
            app.rect(rectX - 74, rectY, 58, 19);
            app.textSize(12);
            app.fill(0); // make label text black
            app.text("Cost: " + towerCost, rectX - 73, rectY + 15);
        }

        // Create hovering label text for mana pool button
        if(mouseOver && isMana) {
            app.stroke(0);
            app.strokeWeight(1);
            app.fill(204, 204, 198);
            app.rect(rectX - 74, rectY, 58, 19);
            app.textSize(12);
            app.fill(0); // make label text black
            app.text("Cost: " + manaPoolSpellCost, rectX - 73, rectY + 15);
        }
    }
    
    /**
     * Gets an array with the x,y-coordinates and size of the button
     * @return int array containing the x,y-coordinates of the button, and its size
     */
    public int[] getInfo() {
        int[] info = new int[3];
        info[0] = rectX;
        info[1] = rectY;
        info[2] = SIZE;

        return info;
    }

    /**
     * Returns if the button is on or off.
     * @return true if button is on, false if off
     */
    public boolean getIsOn() {
        return isOn;
    }

    /**
     * Switches the button on and off.
     */
    public void switchOnOff() {
        if (isOn)
            isOn = false;
        else
            isOn = true;
    }

    /**
     * Returns whether or not the button is a mana pool spell button
     * @return true if this button is a mana pool spell button
     */
    public boolean isMana() {
        return isMana;
    }

    /**
     * Indicate if a button is a mana pool spell button or not.
     * @param isMana true if button is a mana pool spell button
     */
    public void setIsMana(boolean isMana) {
        this.isMana = isMana;
    }

    /**
     * Indicate if a button is a build tower button or not.
     * @param isTower true if button is a build tower button
     */
    public void setIsTower(boolean isTower) {
        this.isTower = isTower;
    }
}
