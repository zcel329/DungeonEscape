package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Chat.AppUi;
import nz.ac.auckland.se206.Controller;
import nz.ac.auckland.se206.CustomNotifications;
import nz.ac.auckland.se206.DungeonMaster;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.Music;
import nz.ac.auckland.se206.TimerCounter;
import nz.ac.auckland.se206.Utility;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for managing room-related functionality. */
public class RoomController implements Controller {
  private static RoomController instance;

  public static RoomController getInstance() {
    return instance;
  }

  /**
   * Converts a color name to a JavaFX Color object.
   *
   * @param colorName The name of the color to be converted.
   * @return The corresponding JavaFX Color object.
   */
  public static Color convertStringToColor(String colorName) {
    switch (colorName) {
      case "Red":
        return Color.RED;
      case "Green":
        return Color.GREEN;

      case "Blue":
        return Color.BLUE;
      case "Purple":
        return Color.PURPLE;
      case "Yellow":
        return Color.YELLOW;

        // Add more color mappings as needed
      default:
        return Color.BLACK;
    } // Default to black if the color name is not recognized
  }

  /**
   * Calculates and returns the average color resulting from blending two input colors.
   *
   * @param color1 The first color to be blended.
   * @param color2 The second color to be blended.
   * @return The average color obtained by averaging the RGB values of the input colors.
   */
  public static Color calculateAverageColor(Color color1, Color color2) {
    // Calculate the average RGB values of the input colors
    double avgRed = (color1.getRed() + color2.getRed()) / 2.0;
    double avgGreen = (color1.getGreen() + color2.getGreen()) / 2.0;
    double avgBlue = (color1.getBlue() + color2.getBlue()) / 2.0;

    // Return the average color with fully opaque alpha value (1.0)
    return new Color(avgRed, avgGreen, avgBlue, 1.0);
  }

  @FXML private Pane potionsRoomPane;
  @FXML private Pane popUp;
  @FXML private Pane visualDungeonMaster;

  @FXML private Button btnReturnToCorridor;

  @FXML private Label lblTime;

  @FXML private ImageView key1;

  @FXML private ImageView boulder;

  @FXML private ImageView note;

  @FXML private ImageView yellowPotion;
  @FXML private ImageView redPotion;
  @FXML private ImageView bluePotion;
  @FXML private ImageView greenPotion;
  @FXML private ImageView purplePotion;
  @FXML private Label lblObjectiveMarker;
  @FXML private ImageView cauldron;

  @FXML private ImageView exclamationMark;
  @FXML private TextArea chatTextArea;

  @FXML private ImageView soundToggle;

  @FXML private Button btnHideNote;
  private double horizontalOffset = 0;
  private double verticalOffset = 0;

  private DungeonMaster callDungeonMaster;
  @FXML private Pane cursorPane;
  @FXML private TextArea textArea;
  @FXML private TextField inputText;
  @FXML private Button showButton;
  @FXML private Button closeButton;
  @FXML private Button sendButton;
  @FXML private ImageView chatBackground;
  @FXML private Button switchButton;
  @FXML private Label hintField;
  @FXML private ImageView hand;
  @FXML private VBox inventoryKey1;
  @FXML private VBox inventoryKey2;
  @FXML private VBox inventoryKey3;

  private HintNode hintNode;
  private AppUi appUi;

  @FXML
  public double getRoomWidth() {

    return potionsRoomPane.getPrefWidth();
  }

  @FXML
  public double getRoomHeight() {

    return potionsRoomPane.getPrefHeight();
  }

  @FXML
  public void getAi(MouseEvent event) {
    popUp.visibleProperty().set(false);
    callDungeonMaster.createPopUp(popUp);
    String context = DungeonMaster.getDungeonMasterComment();
    callDungeonMaster.getText("user", context);
    // sets popup styling and formatting
    popUp.getStyleClass().add("popUp");
    GameState.popUpShow(popUp, visualDungeonMaster);
  }

  /**
   * Initializes the Puzzle Room UI components and sets up the puzzle logic. This method is called
   * after the Puzzle Room UI is loaded.
   */
  public void initialize() throws ApiProxyException {
    TimerCounter.addTimerLabel(lblTime);
    // Set the instance variable to this object
    instance = this;

    callDungeonMaster = new DungeonMaster();

    popUp.toBack();
    visualDungeonMaster.visibleProperty().set(false);
    visualDungeonMaster.mouseTransparentProperty().set(true);
    // Set the dungeon master to be invisible and mouse transparent
    TranslateTransition translateTransition = GameState.translate(exclamationMark);
    translateTransition.play();

    // Set style sheets
    chatTextArea
        .getStylesheets()
        .add(getClass().getResource("/css/roomStylesheet.css").toExternalForm());
    chatTextArea.getStyleClass().add("text-area .content");
    btnHideNote.getStyleClass().add("custom-button");
    String[] colors = {"Blue", "Yellow", "Purple", "Red", "Green"};

    Random random = new Random();
    // Randomly select two colors from the array
    int firstIndex = random.nextInt(colors.length);
    GameState.firstPotionColour = colors[firstIndex];
    GameState.firstPotion = "" + GameState.firstPotionColour + "Potion";
    int secondIndex;
    do {
      secondIndex = random.nextInt(colors.length);
    } while (secondIndex == firstIndex); // Ensure the second color is different from the first
    // Randomly select two colors from the array
    GameState.secondPotionColour = colors[secondIndex];
    GameState.secondPotion = GameState.secondPotionColour + "Potion";
    // Set the tiles and solution
    chatTextArea.appendText(
        "Dear Future Captives,\nI was close, so very close, to mastering the potion. \n Mix the "
            + GameState.firstPotionColour
            + " potion and "
            + GameState.secondPotionColour
            + " potion in the cauldron the fumes should give you incredible Power. \n"
            + "I pray you succeed where I couldn't. In fading memory,A Lost Soul");
    // Set the tiles and solution

    // Allow the boulder to be dragged and dropped
    for (int i = 0; i < GameState.potionsSelected.length; i++) {
      GameState.potionsSelected[i] = false;
    }
    makePotionImageArray();
  }

  @FXML
  private void enlarge(ImageView image) {
    // if the image is the boulder enlarge it less than the other items
    if (image.getId().contains("boulder")) {
      image.setScaleX(1.2);
      image.setScaleY(1.2);
    } else {
      image.setScaleX(1.5);
      image.setScaleY(1.5);
    }
  }

  @FXML
  private void shrink(ImageView image) {
    image.setScaleX(1.0);
    image.setScaleY(1.0);
  }

  @FXML
  private void onBoulderClicked(MouseEvent event) {
    // if the boulder is clicked and is draggable and has no effect add a dropshadow to it
    ImageView image = (ImageView) event.getSource();
    if (image.getId().contains("boulder")) {
      if ((image.getEffect() == null) && (GameState.isBoulderDraggable == true)) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setHeight(60);
        dropShadow.setWidth(60);
        dropShadow.setSpread(0.35);
        dropShadow.setColor(Color.WHITE);
        image.setEffect(dropShadow);
      } else {
        // else if there is an effect remove it
        if (image.getEffect() != null) {

          image.setEffect(null);
        }
      }
    }
  }

  @FXML
  private void onPotionClicked(MouseEvent event) {
    // if the potion is clicked and has no effect and no potion is selected add a dropshadow to it
    ImageView image = (ImageView) event.getSource();
    if ((image.getEffect() == null)
        && (GameState.isPotionSelected == false)
        && (image.getId().contains("Potion"))) {
      Music.playMovePotion();
      DropShadow dropShadow = new DropShadow();
      dropShadow.setHeight(60);
      dropShadow.setWidth(60);
      dropShadow.setSpread(0.35);
      dropShadow.setColor(Color.WHITE);
      image.setEffect(dropShadow);
      // add the effect to the cauldron as well
      cauldron.setEffect(dropShadow);
      GameState.isPotionSelected = true;
    }
    // else if there is an effect remove it
    else {
      if (image.getEffect() != null) {

        image.setEffect(null);
        cauldron.setEffect(null);
        GameState.isPotionSelected = false;
      }
    }
  }

  /** Animates the cursor to move to the puzzle room. */
  public void animateCursor() {
    hand.setVisible(true);
    Duration duration = Duration.millis(1200);
    // Create new translate transition
    TranslateTransition transition = new TranslateTransition(duration, hand);

    // Play the delay first

    // Move in X axis by +200

    transition.setByX(80);
    // Move in Y axis by +100
    transition.setByY(60);
    // Go back to previous position after 2.5 seconds
    transition.setAutoReverse(true);
    // Repeat animation twice
    transition.setCycleCount(2);
    // Delay for .5 seconds

    // Change the image after the delay
    PauseTransition delay = new PauseTransition(Duration.millis(300)); // Delay for 2.5 seconds

    // Change the image after the delay
    delay.setOnFinished(
        event -> {
          Image newImage = new Image("images/hand2.png"); // Load the new image
          hand.setImage(newImage);
          transition.play();
        });

    // Play the delay, and when it's finished, start the translation animation

    // Play the delay first
    delay.play();
    // Play the delay, and when it's finished, start the translation animation
    transition.setOnFinished(
        event -> {
          potionsRoomPane.getChildren().remove(cursorPane);
        });
  }

  /** Updates the inventory choice box with the current inventory. Also sets the key visibility */
  public void updateInventory() {

    // set key visibility
    GameState.setKeys(inventoryKey1, inventoryKey2, inventoryKey3);

    // Create a Timeline to revert the shadow back to its original state after 2 seconds
  }

  @FXML
  private void onHideNote() {
    // make the note visible and clickable
    Music.playPageTurn();
    note.setVisible(true);
    note.setDisable(false);
    // make the text area invisible and unclickable
    chatTextArea.setVisible(false);
    chatTextArea.setDisable(true);
    btnHideNote.setDisable(true);
    btnHideNote.setVisible(false);
    animateCursor();
  }

  // Allow the image to be dragged and dropped
  @FXML
  private void allowImageToBeDragged(ImageView image) {

    // When the mouse is pressed it records the offset from the top left corner
    image.setOnMousePressed(
        (MouseEvent event) -> {
          Cursor cursor = Cursor.MOVE;
          // change the cursor to a hand
          image.setCursor(cursor);
          onPotionClicked(event);
          onBoulderClicked(event);
          horizontalOffset = event.getSceneX() - image.getLayoutX();
          verticalOffset = event.getSceneY() - image.getLayoutY();
        });
    // When the mouse is dragged it sets the new position of the image
    image.setOnMouseDragged(
        (MouseEvent event) -> {
          double newX = event.getSceneX() - horizontalOffset;
          double newY = event.getSceneY() - verticalOffset;
          image.setLayoutX(newX);
          image.setLayoutY(newY);
        });
    // When the mouse is released it checks if the image is intersecting with the cauldron
    image.setOnMouseReleased(
        (MouseEvent event) -> {
          if ((cauldron.getBoundsInParent().intersects(image.getBoundsInParent())
              && (image.getId().contains("Potion")))) {
            // if the image is intersecting with the cauldron and is a potion it is made invisible
            // and unclickable
            Music.playDropPotion();
            image.setVisible(false);
            image.setDisable(true);
            GameState.isPotionSelected = false;
            cauldron.setEffect(null);
            if ((image.getId().toLowerCase().contains(GameState.firstPotion.toLowerCase())
                || (image.getId().toLowerCase().contains(GameState.secondPotion.toLowerCase())))) {
              GameState.correctPotions++;
              if (GameState.correctPotions == 2) {
                tintScene(potionsRoomPane);
                Music.playPowerUp();
                CustomNotifications.generateNotification(
                    "Something Happens!",
                    "You feel far stronger... like energy's coursing through you and you could move"
                        + " anything...");
                // make the boulder draggable
                allowImageToBeDragged(boulder);
                Cursor cursor = Cursor.HAND;
                boulder.setCursor(cursor);
                GameState.isBoulderDraggable = true;
                cauldron.setEffect(null);
              }
            }
          }
        });
  }

  @FXML
  private void enlargeItem(MouseEvent event) {
    // only enlarge the boulder if it is draggable
    ImageView image = (ImageView) event.getSource();
    if (image.getId().contains("boulder")) {
      if (GameState.isBoulderDraggable == true) {
        enlarge((ImageView) event.getSource());
      }
    }
    // enlarge all other items
    else {

      enlarge((ImageView) event.getSource());
    }
  }

  @FXML
  private void shrinkItem(MouseEvent event) {
    // only shrink the boulder if it is draggable
    ImageView image = (ImageView) event.getSource();
    if (image.getId().contains("boulder")) {
      if (GameState.isBoulderDraggable == true) {
        shrink((ImageView) event.getSource());
      }
    }
    // shrink all other items
    else {
      shrink((ImageView) event.getSource());
    }
  }

  @FXML
  private void onNoteClicked(MouseEvent event) {
    Music.playPageTurn();
    // Check if a note is selected in the combo box
    note.setVisible(false);
    note.setDisable(true);
    chatTextArea.setVisible(true);
    chatTextArea.setDisable(false);
    // if a note is selected it is made visible in the scene

    btnHideNote.setDisable(false);
    btnHideNote.setVisible(true);
  }

  @FXML
  private void onKey1Clicked(MouseEvent event) {
    Music.playEquipSound();
    GameState.hasKeyOne = true;

    GameState.isKey1Collected = true;

    // make the key invisible and unclickable
    key1.visibleProperty().set(false);
    key1.mouseTransparentProperty().set(true);

    visualDungeonMaster.visibleProperty().set(true);
    visualDungeonMaster.mouseTransparentProperty().set(false);

    Inventory.update();

    // update the objective marker depending on previous keys collected
    if (GameState.isKey2Collected == false && GameState.isKey3Collected == false) {
      ObjectiveMarker.setObjective("Find the other keys");
    } else if (GameState.isKey2Collected == true && GameState.isKey3Collected == false) {
      ObjectiveMarker.setObjective("Find key 3");
    } else if (GameState.isKey3Collected == true && GameState.isKey2Collected == false) {
      ObjectiveMarker.setObjective("Find key 2");
    } else {
      ObjectiveMarker.setObjective("Return to the corridor ");
    }
    ObjectiveMarker.update();
  }

  @FXML
  private void onReturnToCorridorClicked(ActionEvent event) {

    // if the chest isnt opened and all keys are collected update the objective marker
    if (GameState.isKey1Collected == true
        && GameState.isKey2Collected == true
        && GameState.isKey3Collected == true
        && GameState.isChestOpened == false) {

      ObjectiveMarker.setObjective("Open the Chest");

      ObjectiveMarker.update();
    }
    App.returnToCorridor();
    GameState.currentRoom = GameState.State.CHEST;
  }

  private void tintScene(Pane potionsRoomPane) {
    Color colour1 = convertStringToColor(GameState.firstPotionColour);
    Color colour2 = convertStringToColor(GameState.secondPotionColour);
    Color colour3 = calculateAverageColor(colour1, colour2);
    // Create a colored rectangle to overlay the scene

    Rectangle tintRectangle =
        new Rectangle(potionsRoomPane.getWidth(), potionsRoomPane.getHeight(), colour3);
    tintRectangle.setOpacity(0); // Initially, make it fully transparent

    // Add the rectangle to the root layout
    potionsRoomPane.getChildren().add(tintRectangle);

    // Create a timeline animation to control the tint effect
    Timeline timeline =
        new Timeline(
            new KeyFrame(Duration.seconds(0), new KeyValue(tintRectangle.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(tintRectangle.opacityProperty(), 0.60)),
            new KeyFrame(Duration.seconds(2), new KeyValue(tintRectangle.opacityProperty(), 0.0)));
    timeline.setOnFinished(
        event -> {
          potionsRoomPane
              .getChildren()
              .remove(tintRectangle); // Remove the tint rectangle from the root
        });
    // Play the animation
    timeline.play();
  }

  @FXML
  private void clickExit(MouseEvent event) {
    // Handle click on exit
    Utility.exitGame();
  }

  @FXML
  private void mute() {
    // Handle click on mute
    GameState.mute();
  }

  @FXML
  public void updateMute() {
    if (!GameState.isMuted) {
      soundToggle.setImage(new ImageView("images/sound/audioOn.png").getImage());
      return;
    }
    soundToggle.setImage(new ImageView("images/sound/audioOff.png").getImage());
  }

  @FXML
  private void onKeyPressEvent(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER) {
      onTextInputHandled();
    }
  }

  private void onTextInputHandled() {
    try {
      GameState.chat.onSendMessage(inputText.getText(), appUi);
    } catch (Exception e) {
      e.printStackTrace();
    }
    inputText.clear();
  }

  @FXML
  private void makePotionImageArray() {
    ImageView[] potionImages = {redPotion, bluePotion, greenPotion, yellowPotion, purplePotion};
    for (int i = 0; i < potionImages.length; i++) {
      allowImageToBeDragged(potionImages[i]);
    }
  }

  @FXML
  private void onMessageSent(ActionEvent event) {
    onTextInputHandled();
  }

  @FXML
  private void onChatShown(ActionEvent event) {
    GameState.chat.massEnable(appUi);
  }

  @FXML
  private void onChatClosed(ActionEvent event) {
    GameState.chat.massDisable(appUi);
  }

  /**
   * Performs initialization tasks after the application starts. Sets the instance variable "appUi"
   * to the FIRST_ROOM, creates and sets up a HintNode, adds the HintNode to the chat map in the
   * GameState, sets the current application UI to FIRST_ROOM, and adds the text area to the chat in
   * the GameState.
   */
  public void onInitializationAfterStart() {
    // Set the instance variable to this object
    appUi = AppUi.FIRST_ROOM;

    // HintNode setup
    hintNode =
        new HintNode(
            textArea,
            inputText,
            showButton,
            closeButton,
            sendButton,
            chatBackground,
            switchButton,
            hintField);

    // Add HintNode to the chat map in GameState
    GameState.chat.addToMap(appUi, hintNode);

    // Set the current application UI to FIRST_ROOM
    onChatClosed(null);

    // Add the text area to the chat in GameState
    GameState.chat.addChat(textArea);
  }

  @FXML
  private void onChatViewChanged(ActionEvent event) {
    GameState.chat.lastHintToggle();
  }

  @Override
  public void updateObjective() {
    lblObjectiveMarker.setText(ObjectiveMarker.getObjective());
  }
}
