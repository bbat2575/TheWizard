package WizardTD;

import java.io.File;
import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Random;

/**
 * Plays game sounds including starting screen, shooting, monster deaths, tower upgrades, mana pool spell, game over and winning sounds.
 */
public class Soundboard {
    // Clip object and File object
    private static Clip clip;
    private static File file;

    /**
     * Plays the requested game sound.
     * @param request the requested game sound
     */
    public static void playSound(String request){
        Random rand = new Random();

        switch(request) {
            // Opening screen sound
            case "start":
                file = new File("sounds/start.wav");
                break;
            // Tower shots
            case "shoot":
                file = new File("sounds/shoot.wav");
                break;
            // Three monster death sounds randomly chosen from
            case "death":
                file = new File("sounds/death"+ rand.nextInt(3) + ".wav");
                break; 
            // Tower upgrade sound
            case "tower":
                file = new File("sounds/tower.wav");
                break;
            // Mana pool spell sound
            case "manapool":
                file = new File("sounds/manapool.wav");
                break;
            // Game over sound
            case "gameover":
                file = new File("sounds/gameover.wav");
                break;
            // Winning game sound
            case "win":
                file = new File("sounds/win.wav");
                break;
        }
        try {
            AudioInputStream sound = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(sound);
        }
        catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }

        clip.start();
    }
}
