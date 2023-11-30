package WizardTD;

import java.util.*;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * A collection of monsters that are attacking the wizard house.
 */
public class MonsterCollection {
    // Store a collection of monsters
    private ArrayList<Monster> monsters = new ArrayList<>();
    // Quantity of monsters in a collection
    private int quantity;
    // Track how many monsters killed (used for mana bar)
    private int monstersKilled;
    // Track how much hp wizard loses when monster's make it to the wizard house (used for mana bar)
    private int wizardHpLoss;
    // Used to randomly choose a path to spawn/travel on from available options
    private Random rand = new Random();

    /**
     * Constructs a MonsterCollection object given quantity of monsters, monster health points, speed, armour, images, and a Map object.
     * @param quantity quantity of monsters in collection
     * @param type monster type
     * @param hp monster's health point
     * @param speed monster's speed
     * @param armour monster's armour level
     * @param monsterImages a collection of monster images
     * @param map a Map object
     */
    public MonsterCollection(int quantity, String type, int hp, double speed, double armour, HashMap<String, ArrayList<PImage>> monsterImages, Map map) { // Quantity = number of monsters to create
        for(int i = 0; i < quantity; i++) {
            this.monsters.add(new Monster(hp, speed, armour, monsterImages.get(type), map.getMonsterPaths().get(rand.nextInt(map.getMonsterPaths().size()))));
        }
        this.quantity = quantity;
    }

    /**
     * Update monsters in collection. Find out who's died and who's reached the wizard house.
     */
    public void tick() {
        // Check on monsters, check if any are killed or have reached the wizard house
        monsters.forEach(monster -> {monster.tick(); if(monster.getIsDead() && !monster.getCounted()){monstersKilled++; monster.setCounted(true);}});
        monsters.forEach(monster -> {if(monster.getHitWizard()){wizardHpLoss += monster.getHp(); monster.setHitWizard(false);}});
    }

    /**
     * Draws monsters in the collection onto the screen.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // Draw monsters in reverse order so that if 2 gremlins occupy the same position on the path,
        // the first one to spawn appears on top (helps revealing hp bar when tower starts shooting since
        // the tower searches for gremlins in order of the arraylist)
        for(int i = monsters.size() - 1; i >= 0; i--) {
            monsters.get(i).draw(app);
        }
    }

    /**
     * Gets Monsters in the collection.
     * @return an ArrayList of Monster objects
     */
    public ArrayList<Monster> getMonsters() {
        return monsters;
    }

    /**
     * Gets how many monster have been killed in the collection.
     * @return number of monsters killed
     */
    public int getMonstersKilled() {
        return monstersKilled;
    }

    /**
     * Gets how much hp the wizard loses when monster's make it to the wizard house.
     * @return health points the wizard loses
     */
    public int getWizardHpLoss() {
        return wizardHpLoss;
    }

    /**
     * Gets the quantity of monsters in this collection.
     * @return quantity of monster in the collection
     */
    public int getQuantity() {
        return quantity;
    }    
}
