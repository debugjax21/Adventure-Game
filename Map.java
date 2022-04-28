import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileNotFoundException;

public class Map { // Extends Application?
    /** Holds the map. */
    private char[][] map;
    /** Stores the amount of rows in map. */
    private int rowLen;
    /** Stores the amount of columns in map. */
    private int colLen;
    /** Keeps track of all the items throughout the map. */
    private List<Item> itemsInMap = new ArrayList<>();
    /**
     * When reading file, stores all inventory items specified from
     * file in this variable. Does not keep a live record of what's in
     * inventory. Just what was in inventory when last saved.
     */
    private List<Item> previousItemsInInventory = new ArrayList<>();

    /** Keeps track of terrain character, description, and file path. */
    private Hashtable<Character, String[]> mapTerrain = new Hashtable<>();

    private Set<Character> differentTerrains = new HashSet<Character>();
    /**
     * Stores the coordinates to the players last saved location.
     * Default location is 0,0.
     */
    private int[] lastSavedCoord = { 0, 0 };

    private int tileHeight;
    private int tileWidth;

    /**
     * Constructor, creates map from file.
     *
     * @param file holds the name of the text file
     */
    public Map(final String file) {
        readInputFile(file);
    }

    /**
     * Validates the text file and creates a map.
     *
     * @param file holds the name of the text file
     */
    private void readInputFile(final String file) {
        final Scanner readFile;

        try {
            String line;
            readFile = new Scanner(new File(file));
            map = createMap(readFile);
            differentTerrains = createAllPossibleTerrainsSet();

            // Check for tile dimensions.
            if (readFile.hasNextInt()) {
                tileHeight = readFile.nextInt();
                if (readFile.hasNextInt()) {
                    tileWidth = readFile.nextInt();
                    readFile.nextLine();
                } else {
                    readFile.nextLine();

                }
            } // else use default img size.

            // Get map items.
            line = readFile.nextLine();
            addMapItems(line);

            // Creates hashTable for
            // Terrain character, description, and
            // image src file.
            line = addMapTerrain(readFile);

            // get the coordinates of last saved location,
            // and what was in the characters inventory.
            if (line.contains("Last saved location:")) {
                addLastSavedLocation(line);
                line = readFile.nextLine();
                addInventoryItems(line);
            } else if (!line.equals("")) {
                // this is here if you want to change
                // the default inventory of the character
                // for a certain map, without changing the
                // overall default inventory for all maps.
                addInventoryItems(line);

                if (readFile.hasNextLine()) {
                    // There shouldn't be any more text to read,
                    // If there is, the file format is incorrect.
                    throw new Exception();
                }
            }

            // Makes sure every symbol in map has
            // an image associated to it.
            if (!validateMap()) {
                throw new Exception();
            }
            readFile.close();
        } catch (

        FileNotFoundException x) {
            System.out.println("Error: Unable to find file.");
            System.exit(0); // TERMINATE THE PROGRAM

        } catch (Exception e) {
            System.out.println("Error: Improper file format.");
            System.exit(0);
        }
    }

    private String addMapTerrain(Scanner file) {
        String[] terrainLine;
        String temp;
        while (file.hasNextLine()) {
            temp = file.nextLine();
            terrainLine = temp.split(";");
            if (terrainLine.length == 3) {
                if (!mapTerrain.containsKey(terrainLine[0].charAt(0))) {
                    String[] terrainDetails = new String[2];
                    terrainDetails[0] = terrainLine[1];
                    try {
                        // Validate the source file for image.
                        Image testImg = new Image(terrainLine[2]);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: Unable to find image file: "
                                + terrainLine[2]);
                        System.exit(0);
                    }

                    terrainDetails[1] = terrainLine[2];
                    mapTerrain.put(terrainLine[0].charAt(0), terrainDetails);
                }

            } else
                return temp;
        }
        return "";
    }

    private void addLastSavedLocation(String line) throws Exception {
        String coords = line.split(":")[1];
        lastSavedCoord[0] = Integer.parseInt(coords.split(" ")[0]);
        lastSavedCoord[1] = Integer.parseInt(coords.split(" ")[1]);

    }

    private void addMapItems(String fileName) throws Exception {
        final Scanner mapItemsFile;
        String line;
        String[] lineItem;
        try {
            mapItemsFile = new Scanner(new File(fileName));
            while (mapItemsFile.hasNextLine()) {
                line = mapItemsFile.nextLine();
                lineItem = line.split(";");
                if (lineItem.length == 3) {
                    // removes "a" in front of the item name.
                    if (lineItem[2].indexOf("a") == 0
                            && lineItem[2].indexOf(" ") == 1) {
                        lineItem[2] = lineItem[2].replaceFirst("a ", "");
                    }
                    itemsInMap.add(new Item(Integer.parseInt(lineItem[0]),
                            Integer.parseInt(lineItem[1]),
                            lineItem[2]));
                } else
                    throw new Exception();

            }
            mapItemsFile.close();

        } catch (FileNotFoundException e) {
            System.out.println("No file for map items provided "
                    + "or invalid file name: " + fileName);
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Error: Improper map items file format.");
            System.exit(0);
        }
    }

    private void addInventoryItems(String fileName) {
        Scanner inventoryFile;
        String line;
        try {
            inventoryFile = new Scanner(new File(fileName));
            while (inventoryFile.hasNextLine()) {
                line = inventoryFile.nextLine();
                // "gets rid of "a" at the beginning of item description.
                if (line.indexOf("a") == 0
                        && line.indexOf(" ") == 1) {
                    line.replaceFirst("a", "");
                }

                previousItemsInInventory.add(new Item(line));

            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open file " + fileName);
            System.exit(0);
        }
    }

    private char[][] createMap(Scanner readFile) {
        final int row;
        final int col;
        String line;

        row = readFile.nextInt();
        col = readFile.nextInt();
        char[][] map = new char[row][col];
        readFile.nextLine();
        line = readFile.nextLine();
        if (line.length() == col) {
            for (int i = 0; i < row; i++) {
                if (line.length() == col) {
                    for (int j = 0; j < col; j++) {
                        map[i][j] = line.charAt(j);
                    }

                    // If we've gone through the file
                    // and have not filled up all the rows...
                    if ((!readFile.hasNext()) && i < row - 1) {
                        throw new InputMismatchException();
                    }

                    if (i < row - 1) {
                        line = readFile.nextLine();
                    }
                } else {
                    // improper amount of columns
                    throw new InputMismatchException();
                }
            }
        } else {
            // Number of columns stated don't match
            // the actual number of columns in file.
            throw new InputMismatchException();
        }
        this.rowLen = row;
        this.colLen = col;
        return map;
    }

    /**
     * returns the terrain where the character
     * is at.
     *
     * @param row x coordinate.
     * @param col y coordinate.
     * @return terrain at character location.
     */
    public char getTerrain(final int row, final int col) {
        return map[row][col];
    }

    public Hashtable<Character, String[]> getMapTerrain() {
        return mapTerrain;
    }

    /**
     * Returns the amount of rows in the map.
     *
     * @return rows
     */
    public int getMapRowLen() {
        return rowLen;
    }

    /**
     * Returns the amount of columns in the map.
     *
     * @return columns
     */
    public int getMapColLen() {
        return colLen;
    }

    public String getTerrainImgFile(char symbol) {
        try {
            return mapTerrain.get(symbol)[1];
        } catch (NullPointerException e) {
            System.out.println("Error: no image file"
                    + " for symbol: " + symbol);
            System.exit(0);
        }
        return null;
    }

    public String getTerrainDescription(char symbol) {
        try {
            String description = mapTerrain.get(symbol)[0];
            description = (description == "" ? "Unknown" : description);
            return description;
        } catch (NullPointerException e) {
            System.out.println("Error: no description"
                    + " for symbol: " + symbol);
            System.exit(0);
        }
        return null;
    }

    public List<Item> getPreviousInventoryItems() {
        return previousItemsInInventory;
    }

    public int getImgHeight() {
        return tileHeight;
    }

    public int getImgWidth() {
        return tileWidth;
    }

    public List<Item> getItemsInMap() {
        return itemsInMap;
    }

    public char[][] getMap() {
        return map;
    }

    public int[] getLastSavedCoord() {
        return lastSavedCoord;
    }

    public void addItemToMap(Item item) {
        itemsInMap.add(item);
    }

    private Set<Character> createAllPossibleTerrainsSet() {
        Set<Character> terrainSymbols = new HashSet<Character>();

        for (char[] row : map) {
            for (char terrain : row) {
                terrainSymbols.add(terrain);
            }
        }
        return terrainSymbols;
    }

    private boolean validateMap() {
        for (char terrain : differentTerrains) {
            if (mapTerrain.get(terrain) == null) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPreviousInventoryItems() {
        return !previousItemsInInventory.isEmpty();
    }

    public void removeItemFromMap(Item item) {
        itemsInMap.remove(item);
    }

}
