
// Project Prolog
// Purpose: Add functionality to an existing program.
// Refactor an existing program in order to support change.
// Provide a GUI interface to the game.
// Display images in a grid.
// Incrementally enhance an existing application.
// Perform regression testing.
//
// To run the program, user must supply a map text file
// in the command line argument.
// ex: java Adventure map1.txt

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class Adventure extends Application {
    /** text area transcript. */
    public static TextArea transcript = new TextArea();
    /** GUI grid of the map. */
    private static GridPane mapGrid = new GridPane();
    /** Stores the file save path. */
    private static File savedFilePath;
    /** Stores the file save path string. */
    private static String savedFileName;
    /** Stores the file chosen by user. */
    private static FileChooser selectFile = new FileChooser();
    /** Game character object. */
    private static GameChar gameChar;
    /** Input field for user. */
    private static TextField commandField = new TextField();
    /** Keeps track of the amount of current image rows in grid. */
    private static int gridRowImgCounter = 0;
    /** Keeps track of the amount of current image columns in grid. */
    private static int gridColImgCounter = 0;
    /** Map object. */
    private static Map map;
    /** width of '-' used to seperate command options from the options. */
    private static final int WIDTH = 22;
    /** Stores the command options to output. */
    private static String introMessage = "Command options:\n"
            + "-".repeat(WIDTH) + "\n"
            + "-Type \"G\" followed by a space and a direction"
            + "(north, east, south, or west)"
            + " to move your character.\n"
            + "-Type \"I\" to view inventory.\n"
            + "-Type \"T\" and item name to take item available"
            + " at current location.\n"
            + "-Type \"D\" to drop an item from your inventory"
            + " at current location.\n"
            + "-Type \"Q\" to quit.\n";

    @Override
    public void start(Stage stage) {
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        selectFile.getExtensionFilters().add(txtFilter);

        commandField.setMaxWidth(280);

        Button saveBtn = new Button("Save");
        Button openBtn = new Button("Open");
        Button quitBtn = new Button("Quit");
        openBtn.setPrefWidth(70);
        openBtn.setPrefHeight(20);

        HBox buttons = new HBox(3);
        buttons.getChildren().addAll(saveBtn, openBtn, quitBtn);

        transcript.setWrapText(true);
        transcript.setEditable(false);

        mapGrid.setHgap(10);
        mapGrid.setVgap(10);
        mapGrid.setAlignment(Pos.CENTER);

        BorderPane outerBorder = new BorderPane();
        outerBorder.setBottom(commandField);
        outerBorder.setLeft(transcript);
        outerBorder.setCenter(mapGrid);
        outerBorder.setTop(buttons);

        BorderPane.setMargin(commandField, new Insets(20, 0, 20, 50));
        BorderPane.setMargin(transcript, new Insets(5, 0, 0, 10));
        BorderPane.setMargin(buttons, new Insets(5, 0, 0, 10));

        Scene scene = new Scene(outerBorder, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Adventure Game");
        stage.show();
        saveBtn.setPrefWidth(openBtn.getWidth());
        quitBtn.setPrefWidth(openBtn.getWidth());
        saveBtn.setPrefHeight(openBtn.getHeight());
        quitBtn.setPrefHeight(openBtn.getHeight());

        // Event handler for textfield.
        commandField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent key) {
                String output;
                String message;
                String command;

                if (key.getCode().equals(KeyCode.ENTER)) {
                    message = commandField.getText();
                    command = message.substring(0, 1).toUpperCase();
                    if (command.charAt(0) != 'Q') {
                        output = gameChar.performCommand(command,
                                message);

                        transcript.appendText(output);
                        output = gameChar.returnLocation();
                        transcript.appendText(output);
                        commandField.clear();
                        clearDisplayMap();
                        // Updates the new location
                        gameChar.getCharMapView();

                        if (gameChar.discoveredItem()) {
                            output = gameChar.outputItemsFound();
                            transcript.appendText(output);
                        }

                        transcript.appendText("\n" + introMessage);

                    } else {
                        transcript.appendText("/nFarewell\n" + gameChar.returnLocation()
                                + "Don't forget to save your progress!");
                        commandField.setEditable(false);
                        commandField.clear();
                    }

                }
            }
        });

        // Event handler for save button.
        saveBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        if (savedFilePath == null) {
                            savedFilePath = selectFile.showSaveDialog(stage);
                            if (savedFilePath != null) {
                                savedFileName = savedFilePath.getName();
                                try {
                                    writeToFile(savedFilePath);
                                    transcript.appendText("\nFile saved succesfully.\n");
                                } catch (IOException ex) {
                                    System.out.println("Failed to save file.");
                                    transcript.appendText("\nFailed to save file.\n");
                                    savedFilePath = null;
                                }
                            } // User canceled save.
                        } else {
                            // there is a previous saved file.
                            // update the file.
                            try {
                                writeToFile(savedFilePath);
                                transcript.appendText("\nFile saved succesfully.\n");
                            } catch (IOException ex) {
                                System.out.println("Failed to save file.");
                                transcript.appendText("\nFailed to save file.\n");
                            }
                        }
                    }
                });

        // Event handler for open button.
        openBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File openFile = selectFile.showOpenDialog(stage);
                        if (openFile != null) {
                            map = new Map(openFile.toString());
                            gameChar = new GameChar(map,
                                    map.getPreviousInventoryItems());

                            int[] savedCoord = map.getLastSavedCoord();
                            // update the map to last saved location.
                            // Otherwise use default coordinates (0,0).
                            if (savedCoord[0] != 0
                                    || savedCoord[1] != 0) {
                                gameChar.setRow(savedCoord[0]);
                                gameChar.setColumn(savedCoord[1]);
                            }

                            savedFilePath = openFile;
                            clearDisplayMap();
                            gameChar.getCharMapView();
                            transcript.clear();
                            transcript.appendText(introMessage);
                            transcript.appendText("\n" + gameChar.returnLocation());
                            // Display any items available at current location.
                            if (gameChar.discoveredItem()) {
                                transcript.appendText(
                                        gameChar.outputItemsFound());
                            }

                        }
                    }
                });

        // quits the scene.
        quitBtn.setOnAction((ActionEvent event) -> {
            Platform.exit();
        });

    }

    private void writeToFile(File savedFile) throws IOException {
        int mapRow = map.getMapRowLen();
        int mapCol = map.getMapColLen();
        String newTxtFileName;
        // char[][] newMap = new char[mapRow][mapCol];
        FileWriter outFile = new FileWriter(savedFile);

        // Write the map row and col, and all map chars.
        outFile.write(mapRow + " " + mapCol + "\n");
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCol; j++) {
                outFile.write(map.getMap()[i][j]);
            }

            outFile.write("\n");
        }

        // Write tile size
        if (map.getImgHeight() > 0) {
            outFile.write(map.getImgHeight() + " ");
            if (map.getImgWidth() > 0) {
                outFile.write(String.valueOf(map.getImgWidth())
                        + "\n");
            }
        } else {
            // default height, width ratio will be preserved.
            outFile.write("60\n");

        }
        // creates name for the map items txt file.
        newTxtFileName = savedFileName.substring(0, savedFileName.length() - 4)
                + "items.txt";

        outFile.write(newTxtFileName + "\n");
        try {
            // Writes items on map terrains in txt file.
            writeMapItems(newTxtFileName);
            // Writes <terrain char>;<terrain description>;<img path>;.
            writeTerrains(outFile);
            // Writes the characters location to the file.
            outFile.write("Last saved location:"
                    + gameChar.getRow() + " " + gameChar.getColumn()
                    + "\n");

            // Creates name for inventory text file.
            newTxtFileName = savedFileName.substring(0, savedFileName.length() - 4)
                    + "InventoryItems.txt";
            outFile.write(newTxtFileName);
            // Writes items in inventory to file.
            writeInventoryItems(newTxtFileName);

        } catch (Exception e) {
            System.out.println("Failed to save file.");
            transcript.appendText("\nFailed to save file.\n");
        }
        outFile.close();
    }

    private static void writeTerrains(FileWriter outFile) throws IOException {
        Hashtable<Character, String[]> mapTerrains = map.getMapTerrain();
        mapTerrains.forEach((key, value) -> {
            try {
                outFile.write(key + ";" + value[0]
                        + ";" + value[1] + ";\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void writeInventoryItems(String newTxtFileName) throws Exception {

        File inventoryFile = new File(newTxtFileName);
        FileWriter outFile = new FileWriter(inventoryFile);
        List<Item> itemsInInventory = gameChar.getInventory();
        for (Item item : itemsInInventory) {
            outFile.write(item.getDescription() + "\n");
        }
        outFile.close();
    }

    private static void writeMapItems(String itemsName) throws Exception {
        File itemsFile = new File(itemsName);
        FileWriter outFile = new FileWriter(itemsFile);
        List<Item> itemsInMap = map.getItemsInMap();

        for (Item item : itemsInMap) {
            outFile.write(item.getCoordinates()[0]
                    + ";" + item.getCoordinates()[1]
                    + ";a " + item.getDescription()
                    + ";\n");
        }

        outFile.close();
    }

    private static void addToDisplayMap(ImageView img) {

        if (gridRowImgCounter < gameChar.getLocalView()[0]
                && gridColImgCounter < gameChar.getLocalView()[1]) {
            mapGrid.add(img, gridColImgCounter, gridRowImgCounter);

        } else {
            System.out.println("Error: Could not add image to display grid.");
            System.exit(0);
        }

        if (gridColImgCounter == gameChar.getLocalView()[1] - 1) {
            gridColImgCounter = 0;
            gridRowImgCounter++;

        } else {
            gridColImgCounter++;
        }
    }

    private void clearDisplayMap() {
        ObservableList<Node> childrens = mapGrid.getChildren();
        mapGrid.getChildren().removeAll(childrens);
        gridRowImgCounter = 0;
        gridColImgCounter = 0;

    }

    public static void createTile(String src) {
        try {
            ImageView img = new ImageView(new Image(src));
            // Check if image needs to be resized
            // Else use default image size.
            if (map.getImgHeight() != 0) {
                img.setFitHeight(map.getImgHeight());
                if (map.getImgWidth() != 0) {
                    img.setFitWidth(map.getImgWidth());
                } else {
                    // Height was set but not width.
                    // Preserve image ratio.
                    img.setPreserveRatio(true);
                }
            }
            addToDisplayMap(img);

        } catch (IllegalArgumentException e) {
            // handle exception
            System.out.println("Error: Invalid img file: " + src);
            System.exit(0);
        }

    }

    /**
     * Displays the command prompt and waits for user input, continues to
     * loop until user selects to quit.
     *
     * @param args input from user from the terminal.
     *             Input needs a text file to specify dimensions and
     *             terrain of the map.
     */
    public static void main(final String[] args) {
        int[] savedCoord;
        if (args.length > 0) {
            map = new Map(args[0]);

            if (map.hasPreviousInventoryItems()) {
                gameChar = new GameChar(map,
                        map.getPreviousInventoryItems());
            } else {
                gameChar = new GameChar(map);
            }

            savedCoord = map.getLastSavedCoord();

            if (savedCoord[0] != 0
                    || savedCoord[1] != 0) {
                gameChar.setRow(savedCoord[0]);
                gameChar.setColumn(savedCoord[1]);
            }

            transcript.appendText(introMessage);
            transcript.appendText("\n" + gameChar.returnLocation());
            gameChar.getCharMapView();

            if (gameChar.discoveredItem()) {
                transcript.appendText(
                        gameChar.outputItemsFound());
            }

            Application.launch(args);

        } else {
            System.out.println("Error: No text file was entered.");
            System.exit(0);
        }
    }

}
