import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameChar {
    /** Holds the game map. */
    private Map map;
    /** Lower bound limit of map. */
    private final int LOWER_BOUND = -1;
    /** holds the inventory list. */
    private List<Item> inventory = new ArrayList<>();
    /** Stores the x-coordinate. */
    private int xCoord = 0;
    /** Stores the y-coordinate. */
    private int yCoord = 0;
    /** Default 5x5 view of map */
    private int[] localView = { 5, 5 };

    /** ouputs the result to the textarea. */
    String output;

    /**
     * Constructor, adds the map.
     *
     * @param gameMap
     */
    public GameChar(final Map gameMap) {
        this.map = gameMap;
        // Default inventory
        Collections.addAll(inventory,
                new Item("brass lantern"),
                new Item("rope"),
                new Item("rations"),
                new Item("staff"));
    }

    /**
     * Constrcutor, adds game map, and
     * equipment.
     *
     * @param gameMap
     * @param equipment
     */
    public GameChar(final Map gameMap, final List<Item> equipment) {
        this.map = gameMap;
        this.inventory = equipment;
    }

    /**
     * @return output string that will be displayed to the text area.
     * @param command   valid command, either "go", "inventory" "take", or "drop"
     * @param direction of travel (N,E,S,W)
     */

    public final String performCommand(final String command,
            String details) {
        // Performs the desired command given the input.
        String shortDirection;

        switch (command) {
            case "G" -> {
                try {
                    details = details.split(" ")[1];
                    shortDirection = details.substring(0, 1).toUpperCase();
                    this.updateQuard(shortDirection);
                } catch (Exception e) {
                    output = "\nFailed to enter direction of travel...\n";
                }

            }
            case "I" -> {
                if (inventory.size() != 0) {
                    output = "\nYou are carrying:";
                    for (Item obj : inventory) {
                        output += String.format("\n-%s", obj.getDescription());
                    }
                } else {
                    output = "\nYour inventory is empty!";
                }
                output += "\n";
            }
            case "T" -> {
                // Checks if user intered an item to take.
                try {
                    String test = details.split(" ")[1];
                    details = details.substring(details.indexOf(" ") + 1);
                    output = take(details);

                } catch (Exception e) {
                    output = "\nFailed to enter item to take...\n";
                }
            }
            case "D" -> {
                try {
                    String test = details.split(" ")[1];
                    details = details.substring(details.indexOf(" ") + 1);
                    output = drop(details);

                } catch (Exception e) {
                    output = "\nFailed to enter item to drop...\n";
                }
            }
            default -> {
                output = "\nInvalid command: " + command + "\n";
            }
        }
        return output;
    }

    /** @return string showing the current location (x and y coordinates) */
    public final String returnLocation() {
        // Returns the x and y coordinate points
        char terrain = map.getTerrain(yCoord, xCoord);
        return "You are at location " + this.yCoord
                + "," + this.xCoord
                + " in terrain " + map.getTerrainDescription(terrain)
                + ".\n";
    }

    /**
     * @param direction to travel, single letter(N S E W)
     * @return true if the direction user wants to travel will
     *         be inbounds, false otherwise.
     */
    private boolean inBounds(final String direction) {
        int newCoord;
        switch (direction) {
            case "N" -> {
                newCoord = this.yCoord;
                if (--newCoord > LOWER_BOUND) {
                    return true;
                }
                output = "\nYou can't go that far north...\n";
            }
            case "E" -> {
                newCoord = xCoord;
                if (++newCoord < map.getMapColLen()) {
                    return true;
                }
                output = "\nYou can't go that far east...\n";
            }
            case "S" -> {
                newCoord = yCoord;
                if (++newCoord < map.getMapRowLen()) {
                    return true;
                }
                output = "\nYou can't go that far south...\n";
            }
            case "W" -> {
                newCoord = xCoord;
                if (--newCoord > LOWER_BOUND) {
                    return true;
                }
                output = "\nYou can't go that far west...\n";
            }
            default -> {
                output = "\nYou can't go that way\n";
                return false;
            }
        }
        return false;
    }

    /**
     * Creates a 5x5 map showing local terrain
     * near the player.
     */
    public void getCharMapView() {
        char[][] localMapView = new char[localView[0]][localView[1]];
        int dimensions = localMapView.length;
        int tempX;
        int tempY;
        int xDirection;
        int yDirection = -3;

        String src;

        for (int row = 0; row < dimensions; row++) {
            xDirection = -2;
            yDirection++;
            char terrainChar;

            for (int col = 0; col < dimensions; col++) {
                tempX = this.xCoord + xDirection;
                tempY = this.yCoord + yDirection;
                if (tempX <= LOWER_BOUND || tempY <= LOWER_BOUND
                        || tempX >= map.getMapColLen()
                        || tempY >= map.getMapRowLen()) {
                    // Out of bounds
                    src = map.getTerrainImgFile('-');
                    Adventure.createTile(src);

                } else {
                    // Current player location
                    if (tempX == xCoord && tempY == yCoord) {
                        src = map.getTerrainImgFile('1');
                        Adventure.createTile(src);
                    } else {
                        terrainChar = map.getTerrain(tempY, tempX);
                        src = map.getTerrainImgFile(terrainChar);
                        Adventure.createTile(src);
                    }
                }
                xDirection++;
            }
        }
    }

    /**
     * @param direction direction of travel (N E S W)
     * @return string of the direction user is moving in, if valid direction.
     */
    private String updateQuard(final String direction) {
        // Checks if desired direction is inbounds and updates accordingly.
        if (inBounds(direction)) {
            switch (direction) {
                case "N" -> {
                    output = "\nMoving north...\n";
                    yCoord--;
                }
                case "E" -> {
                    output = "\nMoving east...\n";
                    xCoord++;
                }
                case "S" -> {
                    output = "\nMoving south...\n";
                    yCoord++;
                }
                case "W" -> {
                    output = "\nMoving west...\n";
                    xCoord--;
                }
                default -> {
                    output = "\nInvalid direction\n";
                }
            }
        }
        return output;
    }

    public boolean discoveredItem() {
        List<Item> mapItems = map.getItemsInMap();
        for (Item item : mapItems) {
            if (item.getCoordinates()[0] == yCoord
                    && item.getCoordinates()[1] == xCoord) {
                // Found an item!
                return true;
            }
        }
        return false;
    }

    public List<Item> getListOfItemsFound() {
        List<Item> itemsFound = new ArrayList<>();
        List<Item> mapItems = map.getItemsInMap();

        for (Item item : mapItems) {
            if (item.getCoordinates()[0] == yCoord
                    && item.getCoordinates()[1] == xCoord) {
                // Found an item!
                itemsFound.add(item);
            }
        }
        return itemsFound;
    }

    public String take(String itemToTake) {
        itemToTake = itemToTake.toLowerCase();
        List<Item> mapItems = map.getItemsInMap();

        // removes the "a" at the beginning of the sentence.
        if (itemToTake.indexOf("a") == 0
                && itemToTake.indexOf(" ") == 1) {
            itemToTake = itemToTake.replaceFirst("a ", "");
        }

        for (Item item : mapItems) {
            if (item.getDescription().equals(itemToTake.toLowerCase())
                    && item.getCoordinates()[0] == yCoord
                    && item.getCoordinates()[1] == xCoord) {
                inventory.add(item);
                map.removeItemFromMap(item);
                output = "\nItem: " + item.getDescription()
                        + " was added to your inventory.\n";
                return output;
            }

        }
        output = "\nItem: \"" + itemToTake + "\" was not found.\n";
        output += outputItemsFound();
        return output;

    }

    public String drop(String itemToDrop) {
        itemToDrop = itemToDrop.toLowerCase();
        if (itemToDrop.indexOf("a") == 0
                && itemToDrop.indexOf(" ") == 1) {
            itemToDrop = itemToDrop.replaceFirst("a ", "");
        }
        for (Item item : inventory) {
            if (item.getDescription().equals(itemToDrop)) {
                item.setCoordinates(yCoord, xCoord);
                inventory.remove(item);
                map.addItemToMap(item);
                output = "\nYou dropped a " + itemToDrop + "\n";
                return output;
            }
        }
        output = "\nYou don't have item \"" + itemToDrop
                + "\" in your inventory.\n";
        return output;
    }

    public String outputItemsFound() {
        List<Item> foundItems = getListOfItemsFound();
        if (foundItems.size() == 0) {
            return "";
        }
        output = foundItems.size() > 1 ? "Items " : "Item ";
        output += "available at current location:\n";

        for (Item item : foundItems) {
            output += "- a " + item.getDescription() + "\n";
        }
        return output;
    }

    /**
     * @return xCoord value
     */
    public final int getRow() {
        return this.yCoord;
    }

    /**
     * @param value updates yCoord to value.
     */
    public final void setRow(final int value) {
        this.yCoord = value;
    }

    /**
     * @return xCoord value
     */
    public final int getColumn() {
        return this.xCoord;
    }

    /**
     * @param value updates yCoord to value.
     */
    public final void setColumn(final int value) {
        this.xCoord = value;
    }

    public void setMap(Map mp) {
        map = mp;
    }

    /**
     * @return inventory array
     */
    public final List<Item> getInventory() {
        return this.inventory;
    }

    public int[] getLocalView() {
        return localView;
    }
}
