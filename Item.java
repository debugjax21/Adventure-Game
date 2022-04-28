/**
 * Class for each object in the game.
 * Stores the coordinates to where the object
 * is/was located, and description of the object.
 */
public class Item {
    private int[] coordinates = new int[2];
    private String description;

    // Constructors.
    public Item(int row, int col, String disc) {
        coordinates[0] = row;
        coordinates[1] = col;
        description = disc;
    }

    // This constructor is used if the item is not
    // on the map floor.
    public Item(String disc) {
        description = disc;
    }

    public void setCoordinates(int row, int col) {
        coordinates[0] = row;
        coordinates[1] = col;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public String getDescription() {
        return description;
    }

}
