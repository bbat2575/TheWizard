package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Brings together and manages all the elements that comprise the map and draws them on screen.
 */
public class Map {
    // Gremlin offset amount to center the 20x20 px gremlin sprite inside the 32x32 px path
    private static final int GREMLIN_OFFSET = 6;

    // Map level layout
    private String layout;
    // Landmark object for the wizard's house
    private Landmark wizardHouse;
    // ArrayList's of Landmark objects to store grass, shrubs & paths
    private ArrayList<Landmark> grass = new ArrayList<>();
    private ArrayList<Landmark> shrubs = new ArrayList<>();
    private ArrayList<Landmark> paths = new ArrayList<>();
    // Landmark images
    private PImage grassImage;
    private PImage shrubImage;
    private PImage wizardImage;

    // An ArrayList to store ArrayLists of path PImages for drawing the map
    private ArrayList<ArrayList<PImage>> pathImages = new ArrayList<>();
    // Store path locations for map design
    private ArrayList<ArrayList<Integer>> pathCoordinates = new ArrayList<>();
    // Store the spawn points and paths the monsters will take to the wizard house for each level
    private ArrayList<ArrayList<ArrayList<Integer>>> monsterSpawn = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<Integer>>> monsterPaths = new ArrayList<>();
    // Store wizard house location
    private ArrayList<Integer> wizardLocation = new ArrayList<>();

    /**
     * Constructs a Map object given a layout name and grass, shrub, wizard house and path images.
     * @param layout current layout name
     * @param grassImage grass image
     * @param shrubImage shrub image
     * @param wizardImage wizard image
     * @param pathImages path images
     */
    public Map(String layout, PImage grassImage, PImage shrubImage, PImage wizardImage, ArrayList<ArrayList<PImage>> pathImages) {
        this.layout = layout;
        this.grassImage = grassImage;
        this.shrubImage = shrubImage;
        this.wizardImage = wizardImage;
        this.pathImages = pathImages;
        // Setup all the resources needed to draw the map
        this.setup();
    }

    /**
     * Draws all elements of the map to the screen.
     * @param app the PApplet class of the game
     */
    public void draw(PApplet app) {
        // Handling graphics
        grass.forEach(grass -> grass.draw(app));
        shrubs.forEach(shrub -> shrub.draw(app));
        paths.forEach(path -> path.draw(app));
    }

    /**
     * Gets the paths that the monsters travel on.
     * @return a multi-dimensional ArrayList of coordinates for paths that the monsters travel
     */
    public ArrayList<ArrayList<ArrayList<Integer>>> getMonsterPaths() {
        return monsterPaths;
    }

    /**
     * Gets the wizard house Landmark object.
     * @return wizard house Landmark object
     */
    public Landmark getWizardHouse() {
        return wizardHouse;
    }

    /**
     * Gets the grass Landmark objects.
     * @return an ArrayList of grass Landmark objects
     */
    public ArrayList<Landmark> getGrass() {
        return grass;
    }

    /**
     * Map setup for preparing Landmark tiles and monster spawn points.
     */
    public void setup() {
        // Create FileReader object for level text file
        FileReader f = new FileReader(layout);

        // Populate ArrayLists for Landmark objects based on level text files
        for(int i = 0; i < 20; i++) {
            for(int j = 0; j < 20; j++) {
                if(f.getSymbols()[i][j].equals(" ")) {
                    grass.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, grassImage));
                } else if(f.getSymbols()[i][j].equals("S")) {
                    shrubs.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, shrubImage));
                } else if(f.getSymbols()[i][j].equals("W")) {
                    grass.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, grassImage));
                    wizardLocation.add(j); wizardLocation.add(i);
                    // Offset wizard house by 8 pixels to center it in the cell
                    wizardHouse = new Landmark(App.CELLSIZE * j - 8, App.CELLSIZE * i + App.TOPBAR - 8, wizardImage);
                } else if(f.getSymbols()[i][j].equals("X")) {
                    // Populate the pathCoordinates
                    ArrayList<Integer> coordinates = new ArrayList<Integer>();
                    coordinates.add(j); coordinates.add(i);
                    pathCoordinates.add(coordinates);
                }
            }
        }

    // Populate paths ArrayList with Landmark objects
    for(int i = 0; i < 20; i++) {
        for(int j = 0; j < 20; j++) {
            ArrayList<Integer> left = new ArrayList<Integer>();
            ArrayList<Integer> right = new ArrayList<Integer>();
            ArrayList<Integer> above = new ArrayList<Integer>();
            ArrayList<Integer> below = new ArrayList<Integer>();

            left.add(j-1); left.add(i);
            right.add(j+1); right.add(i);
            above.add(j); above.add(i-1);
            below.add(j); below.add(i+1);

            if(f.getSymbols()[i][j].equals("X")) {
                // If there's a path to the left, to the right, above and below, add a 4-way intersection
                if(pathCoordinates.contains(left) && pathCoordinates.contains(right) && pathCoordinates.contains(above) && pathCoordinates.contains(below)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(3).get(0)));
                // If there's a path to the left, to the right and below, add a 3-way intersection
                } else if(pathCoordinates.contains(left) && pathCoordinates.contains(right) && pathCoordinates.contains(below)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(2).get(0)));
                // If there's a path to the left, to the right and above, add a rotated 3-way intersection
                } else if(pathCoordinates.contains(left) && pathCoordinates.contains(right) && pathCoordinates.contains(above)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(2).get(2)));
                // If there's a path to the left, above and below, add a rotated 3-way intersection
                } else if(pathCoordinates.contains(left) && pathCoordinates.contains(above) && pathCoordinates.contains(below)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(2).get(1)));
                // If there's a path to the right, above and below, add a rotated 3-way intersection
                } else if(pathCoordinates.contains(right) && pathCoordinates.contains(above) && pathCoordinates.contains(below)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(2).get(3)));
                // If there's a path to the left and below, add a turn
                } else if(pathCoordinates.contains(left) && pathCoordinates.contains(below)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(1).get(0)));
                // If there's a path to the right and below, add a rotated turn
                } else if(pathCoordinates.contains(right) && pathCoordinates.contains(below)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(1).get(3)));
                // If there's a path to the right and above, add a rotated turn
                } else if(pathCoordinates.contains(right) && pathCoordinates.contains(above)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(1).get(2)));
                // If there's a path to the left and above, add a rotated turn
                } else if(pathCoordinates.contains(left) && pathCoordinates.contains(above)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(1).get(1)));
                // If there's a path above and below, or just above or just below, add a rotated straight path
                } else if(pathCoordinates.contains(above) && pathCoordinates.contains(below) || pathCoordinates.contains(above) || pathCoordinates.contains(below)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(0).get(1)));
                // If there's a path to the left and to the right, or just left or just right, add a straight path
                } else if(pathCoordinates.contains(left) && pathCoordinates.contains(right) || pathCoordinates.contains(left) || pathCoordinates.contains(right)) {
                    paths.add(new Landmark(App.CELLSIZE * j, App.CELLSIZE * i + App.TOPBAR, pathImages.get(0).get(0)));
                }
            }
        }
    }

        // Populate monsterSpawn arraylist with spawn points for entering paths
        for(ArrayList<Integer> position: pathCoordinates) {
            if(position.contains(0) || position.contains(19)) {
                // Create an arraylist to store an arraylist of coordinates for a path to the wizard house
                ArrayList<ArrayList<Integer>> coordArray = new ArrayList<>();
                // Create an arraylist to store a pair of x,y-coordinates
                ArrayList<Integer> coordinates = new ArrayList<>();
                // Add starting positions of gremlins to coordinates arraylist but subtract 1 from 0 and/or add 1 to 19 so the gremlin spawns outside the map
                if(position.get(0) == 0) {
                    coordinates.add(position.get(0) - 1); coordinates.add(position.get(1));
                } else if (position.get(0) == 19) {
                    coordinates.add(position.get(0) + 1); coordinates.add(position.get(1));
                } else if(position.get(1) == 0) {
                    coordinates.add(position.get(0)); coordinates.add(position.get(1) - 1);
                } else if (position.get(1) == 19) {
                    coordinates.add(position.get(0)); coordinates.add(position.get(1) + 1);
                }
                coordArray.add(coordinates);
                monsterSpawn.add(coordArray);
            }
        }

        // Call the algorithm to find paths to the wizard's house
        monsterPaths = findPaths(f);
    }

    /**
     * Algorithm that finds all available paths to the wizard house.
     * @param file FileReader object containing path and other level information in a 20 x 20 array
     */
    @SuppressWarnings("unchecked") // Suppress unchecked warning for ArrayList.clone()
    public ArrayList<ArrayList<ArrayList<Integer>>> findPaths(FileReader file) {
        //FileReader f = new FileReader("level3.txt");
        ArrayList<Integer> wizardLocation = new ArrayList<>();
        ArrayList<ArrayList<Integer>> pathCoordinates = new ArrayList<>();

        // Populate ArrayLists for Landmark objects based on level text files
        for(int i = 0; i < 20; i++) {
            for(int j = 0; j < 20; j++) {
                if(file.getSymbols()[i][j].equals(" ")) {
                } else if(file.getSymbols()[i][j].equals("S")) {
                } else if(file.getSymbols()[i][j].equals("W")) {
                    wizardLocation.add(j); wizardLocation.add(i);
                } else if(file.getSymbols()[i][j].equals("X")) {
                    // Populate the pathCoordinates
                    ArrayList<Integer> coordinates = new ArrayList<Integer>();
                    coordinates.add(j); coordinates.add(i);
                    pathCoordinates.add(coordinates);
                }
            }
        }

        // Create arraylists to store gremlin spawn points and first path tile
        ArrayList<ArrayList<ArrayList<Integer>>> monsterSpawn = new ArrayList<>();
        ArrayList<ArrayList<ArrayList<Integer>>> monsterPaths = new ArrayList<>();

        // Populate monsterSpawn and monsterPaths arraylists
        for(ArrayList<Integer> position: pathCoordinates) {
            if(position.contains(0) || position.contains(19)) {
                // Create an arraylist to store an arraylist of coordinates for a path to the wizard house
                ArrayList<ArrayList<Integer>> coordArray = new ArrayList<>();
                ArrayList<ArrayList<Integer>> coordArray2 = new ArrayList<>();
                // Create an arraylist to store a pair of x,y-coordinates
                ArrayList<Integer> coordinates = new ArrayList<>();
                ArrayList<Integer> coordinates2 = new ArrayList<>();
                // Add starting positions of gremlins to coordinates arraylist but subtract 1 from 0 and/or add 1 to 19 so the gremlin spawns outside the map
                if(position.get(0) == 0) {
                    coordinates.add(position.get(0) - 1); coordinates.add(position.get(1));
                    coordinates2.add(position.get(0)); coordinates2.add(position.get(1));
                } else if (position.get(0) == 19) {
                    coordinates.add(position.get(0) + 1); coordinates.add(position.get(1));
                    coordinates2.add(position.get(0)); coordinates2.add(position.get(1));
                } else if(position.get(1) == 0) {
                    coordinates.add(position.get(0)); coordinates.add(position.get(1) - 1);
                    coordinates2.add(position.get(0)); coordinates2.add(position.get(1));
                } else if (position.get(1) == 19) {
                    coordinates.add(position.get(0)); coordinates.add(position.get(1) + 1);
                    coordinates2.add(position.get(0)); coordinates2.add(position.get(1));
                }
                coordArray.add(coordinates);
                coordArray2.add(coordinates2);
                monsterSpawn.add(coordArray);
                monsterPaths.add(coordArray2);
            }
        }

        // Create an arraylist of turning points and populate it
        ArrayList<ArrayList<Integer>> forks = new ArrayList<>();
        
        for(int i = 0; i < 20; i++) {
            for(int j = 0; j < 20; j++) {
                ArrayList<Integer> left = new ArrayList<Integer>();
                ArrayList<Integer> right = new ArrayList<Integer>();
                ArrayList<Integer> above = new ArrayList<Integer>();
                ArrayList<Integer> below = new ArrayList<Integer>();

                left.add(j-1); left.add(i);
                right.add(j+1); right.add(i);
                above.add(j); above.add(i-1);
                below.add(j); below.add(i+1);

                if(file.getSymbols()[i][j].equals("X")) {
                    // If there's a path to the left, to the right, above and below, add a 4-way intersection
                    if(pathCoordinates.contains(left) && pathCoordinates.contains(right) && pathCoordinates.contains(above) && pathCoordinates.contains(below)) {
                        ArrayList<Integer> coordinates = new ArrayList<>();
                        coordinates.add(j); coordinates.add(i);
                        forks.add(coordinates);
                    // If there's a path to the left, to the right and below, add a 3-way intersection
                    } else if(pathCoordinates.contains(left) && pathCoordinates.contains(right) && pathCoordinates.contains(below)) {
                        ArrayList<Integer> coordinates = new ArrayList<>();
                        coordinates.add(j); coordinates.add(i);
                        forks.add(coordinates);
                    // If there's a path to the left, to the right and above, add a 3-way intersection
                    } else if(pathCoordinates.contains(left) && pathCoordinates.contains(right) && pathCoordinates.contains(above)) {
                        ArrayList<Integer> coordinates = new ArrayList<>();
                        coordinates.add(j); coordinates.add(i);
                        forks.add(coordinates);
                    // If there's a path to the left, above and below, add a 3-way intersection
                    } else if(pathCoordinates.contains(left) && pathCoordinates.contains(above) && pathCoordinates.contains(below)) {
                        ArrayList<Integer> coordinates = new ArrayList<>();
                        coordinates.add(j); coordinates.add(i);
                        forks.add(coordinates);
                    // If there's a path to the right, above and below, add a 3-way intersection
                    } else if(pathCoordinates.contains(right) && pathCoordinates.contains(above) && pathCoordinates.contains(below)) {
                        ArrayList<Integer> coordinates = new ArrayList<>();
                        coordinates.add(j); coordinates.add(i);
                        forks.add(coordinates);
                    }
                }
            }
        }

        // Used to locate up, down, left and right direction cells
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, 1, -1};
        // Create a blacklist to mark paths/routes you've already found
        boolean[][] blacklist = new boolean[20][20];
        ArrayList<ArrayList<Integer>> pathCoordinates2 = new ArrayList<>();
        boolean complete = false;
        // Create a variable to keep track of how many paths are found
        int noOfPaths = monsterPaths.size();
          
        while(noOfPaths > 0) {
            // Make sure to clear pathCoordinates2 on each iteration to make room for a new path search
            pathCoordinates2.clear();
            complete = false;
            // The search will begin at the wizard house's location
            int lastx = wizardLocation.get(0);
            int lasty = wizardLocation.get(1);
            // Create placeholder variables to store lastx and lasty (lastlastx and lastlasty) at the end of each iteration of pathCoordinates for the adjacency search
            // to prevent the adjacency search from getting stuck on the same cell (infinite loop)
            int lastlastx = -1;
            int lastlasty = -1;
            // Create a visited array to mark cells that are visited during adjacency search below
            boolean[] visited = new boolean[pathCoordinates.size()];

            // Iterate through pathCoordinates and do an adjacency search - looking for adjacent path cells
            while(!complete){
                for(int i = 0; i < pathCoordinates.size(); i++) {
                    for(int j = 0; j < 4; j++) {
                        int xx = lastx + dr[j];
                        int yy = lasty + dc[j];

                        ArrayList<Integer> coos = new ArrayList<>();
                        coos.add(xx);
                        coos.add(yy);

                        if(xx < 0 || yy < 0 || xx > 19 || yy > 19) {
                            complete = true;
                            break;
                        } else if (visited[i]) {
                            continue;
                        } else if (blacklist[pathCoordinates.get(i).get(0)][pathCoordinates.get(i).get(1)]) {
                            continue;
                        } else if (pathCoordinates.get(i).get(0) == xx && pathCoordinates.get(i).get(1) == yy) {
                            ArrayList<Integer> coordinates = new ArrayList<>();
                            coordinates.add(pathCoordinates.get(i).get(0));
                            coordinates.add(pathCoordinates.get(i).get(1));
                            pathCoordinates2.add(coordinates);
                            visited[i] = true;
                            lastx = pathCoordinates.get(i).get(0);
                            lasty = pathCoordinates.get(i).get(1);
                            continue;
                        }
                    }

                    if(complete) {
                        break;
                    }
                }
                if(lastlastx == lastx && lastlasty == lasty) {
                    break;
                }
                lastlastx = lastx;
                lastlasty = lasty;
            }
            // Reverse the arraylist (so the spawn point is at index 0 and the wizard house at the last index)
            Collections.reverse(pathCoordinates2);

            // If pathCoordinates2 = path from a spawn point to the wizard's location, store it in monsterPaths
            for(ArrayList<ArrayList<Integer>> arr: monsterPaths) {
                if(arr.get(0).equals((pathCoordinates2.get(0)))) {
                    noOfPaths--;
                    arr.clear();
                    // Add the gremlin's spawn point (outside the map)
                    arr.add(monsterSpawn.get(monsterPaths.indexOf(arr)).get(0));
                    // Add the path
                    arr.addAll(pathCoordinates2);
                    // Add the wizard house location - need to make a deep copy first so the coordinates are in a different memory location for use later in Gremlin.java
                    ArrayList<Integer> wizard = (ArrayList<Integer>)wizardLocation.clone();
                    arr.add(wizard);
                }
            }
           
            // Find the nearest fork in the path to the spawn point and blacklist the path cell next to this fork (closest to the spawn point) so this path isn't searched again
            boolean forkFound = false;
            for(int i = 0; i < pathCoordinates2.size(); i++) {
                for(ArrayList<Integer> fork: forks) {
                    if(pathCoordinates2.get(i).equals(fork)) {
                        // Try to blacklist a path next to the fork but in the direction of the spawn point
                        try {
                            blacklist[pathCoordinates2.get(i-1).get(0)][pathCoordinates2.get(i-1).get(1)] = true;
                        // If all paths around the fork are blacklist, then blacklist the fork itself (so it's not added to any new paths) and remove it from the forks arraylist (so we don't bother looking for it again)
                        } catch (Exception e) {
                            blacklist[pathCoordinates2.get(i).get(0)][pathCoordinates2.get(i).get(1)] = true;
                            //forks.remove(fork);
                        }
                        forkFound = true;
                        break;
                    }
                }
                if(forkFound) {
                    break;
                }
            }
        }

        for(ArrayList<ArrayList<Integer>> arr: monsterPaths) {
            for(ArrayList<Integer> coords: arr) {
                coords.set(0, coords.get(0) * App.CELLSIZE + GREMLIN_OFFSET);
                coords.set(1, coords.get(1) * App.CELLSIZE + GREMLIN_OFFSET + App.TOPBAR);
            }
        }

        return monsterPaths;
    }
}
    
