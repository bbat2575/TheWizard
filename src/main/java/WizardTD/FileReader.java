package WizardTD;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Reads .txt level files and unpacks their contents into a 20 x 20 array.
 */
public class FileReader {
    // File object
    private File file;
    // Layout/level name
    private String level;
    // 20 x 20 array to store each character in the map file
    private String[][] symbols = new String[20][20];

    /**
     * Constructs a FileReader object given a filename.
     */
    public FileReader(String filename) {
        this.level = filename;
        this.file = new File(filename);
        this.populate();
    }

    /**
     * Populates a 20 x 20 array which every character in the .txt file.
     */
    public void populate() {
        try {
            // Create a scanner object and pass it the file
            Scanner scanLine = new Scanner(file);

            for(int i = 0; i < 20; i++) {
                // Create a Scanner object and pass it the next line in the file
                // Change default Scanner's default delimiter (whitespace) to empty string to read every character individually
                Scanner scanSymbol = new Scanner(scanLine.nextLine()).useDelimiter("");  
                for(int j = 0; j < 20; j++) {
                    // Store each character individully in the array (account for lines that don't end with enough spaces using conditional)
                    if(scanSymbol.hasNext())
                        symbols[i][j] = scanSymbol.next();
                    else
                        symbols[i][j] = " ";
                }
            }
            scanLine.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the 20 x 20 array.
     * @return a 20 x 20 array containing contents of the .txt file
     */
    public String[][] getSymbols() {
        return symbols;
    }

    /**
     * Gets the level name.
     * @return the level name
     */
    public String getLevel() {
        return level;
    }
}