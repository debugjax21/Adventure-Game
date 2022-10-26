# Adventure-Game
Simple adventure game I made. The program utilizes the JavaFX framework.

How to run from command line:

-Compile
javac --module-path javafx-sdk-18/lib --add-modules javafx.controls,javafx.fxml Adventure.java

-Run
java --module-path javafx-sdk-18/lib --add-modules javafx.controls,javafx.fxml Adventure <texFileName.txt>


I made it so the person does not have to put in
the desired image height and the program will still run
with default image size. If the user enters a height, but
not a width, the width will auto adjust to preserve the height,
width ratio of the image.

If there's no terrain description but still 3 delimiters ";"
then the program will output "unkown" whenever user is on that terrain.

When the user types to quit the program, farewell message is displayed,
and user can no longer type commands until the user opens up the file/program
again.

When picking up an item, you must type out the whole item name in order to 
pick it up, you don't have to type "a" before the item.
Just like other commands, it is not case sensitive, typing t or T 
and the items name is sufficent.
Example: "Take a dagger" or "t dagger" etc...

When saving, the line "Last saved location:" and the coordinate points
to the players location is saved at the bottom of the map text file. 
This line is not required in order to open a new map, but when 
present, the map will load with those coordinates.
There will be another text file pointer after the line 
"Last saved location" named after the pattern <mapFileName>InventoryItems.txt.
This text file stores the list of items in the characters inventory that was
present when the file was originally saved.

The inventory items text file is also optional to have at the bottom of the map file.
This allows the user to change the default items the character starts out 
with for the particular map, without having to hardcode 
it into the program. However, if the user is opening up a previously saved file
(has "Last saved location:" in the text file) there must be an inventory items
text file on the line below, an error will display otherwise.

![Screenshot (92)](https://user-images.githubusercontent.com/80862425/167490412-c416c67c-b3b3-4dd6-9fb8-562ccc8c4074.png)

