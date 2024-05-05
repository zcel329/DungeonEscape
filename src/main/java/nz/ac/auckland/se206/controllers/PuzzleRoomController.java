package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.beans.binding.BooleanExpression;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Chat;
import nz.ac.auckland.se206.Controller;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.Music;
import nz.ac.auckland.se206.TimerCounter;
import nz.ac.auckland.se206.Utility;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for managing interactions and logic within the Puzzle Room UI. */
public class PuzzleRoomController implements Controller {

  private static PuzzleRoomController instance;

  public static PuzzleRoomController getInstance() {
    return instance;
  }

  @FXML private Pane puzzleRoomPane;
  @FXML private Label lblTime;
  @FXML private ImageView key3;
  @FXML private ImageView soundToggle;
  @FXML private Polygon doorPolygon;
  @FXML private Label lblObjectiveMarker;
  @FXML private TextArea textArea;
  @FXML private TextField inputText;
  @FXML private Button showButton;
  @FXML private Button closeButton;
  @FXML private Button sendButton;
  @FXML private ImageView chatBackground;
  @FXML private Button switchButton;
  @FXML private Label hintField;

  @FXML private VBox inventoryKey1;
  @FXML private VBox inventoryKey2;
  @FXML private VBox inventoryKey3;

  @FXML private Rectangle puzzleButton;

  private HintNode hintNode;
  private Chat.AppUi appUi;

  /**
   * Initializes the class. This method is typically used for setting up initial state and
   * properties.
   */
  public void initialize() {
    TimerCounter.addTimerLabel(lblTime);

    // Set the instance
    key3.visibleProperty().bind(GameState.puzzleRoomSolved);
    // Bind the key3's disable property to the puzzleRoomSolved property
    key3.disableProperty().bind(((BooleanExpression) GameState.getPuzzleRoomSolved()).not());
    // Bind the inventory choice box to the inventory
    instance = this;
  }

  @FXML
  private void clickPuzzle(MouseEvent event) throws IOException {
    App.setRoot(AppUi.PUZZLE);
  }

  @FXML
  private void onKey3Clicked(MouseEvent event) {
    Music.playEquipSound();
    GameState.hasKeyThree = true;
    // change the key3's visibility and disable it
    key3.visibleProperty().unbind();
    key3.disableProperty().unbind();

    key3.setVisible(false);
    key3.setDisable(true);
    // update the game state
    GameState.isKey3Collected = true;
    Inventory.update();
    // depending on previous keys collected update the objective marker
    if (GameState.isKey2Collected == false && GameState.isKey1Collected == false) {
      ObjectiveMarker.setObjective("Find the other keys");
    } else if (GameState.isKey2Collected == true && GameState.isKey1Collected == false) {
      ObjectiveMarker.setObjective("Find key 1");
    } else if (GameState.isKey1Collected == true && GameState.isKey2Collected == false) {
      ObjectiveMarker.setObjective("Find key 2");
    } else {
      ObjectiveMarker.setObjective("Return to the corridor");
    }
    ObjectiveMarker.update();
  }

  /**
   * Applies a drop shadow effect to the specified polygon image when hovered over.
   *
   * @param image The polygon image to which the shadow effect is applied.
   */
  @FXML
  private void shadowEffect(Polygon image) {
    // create a drop shadow for the image when its hovered over
    DropShadow dropShadow = new DropShadow();
    dropShadow.setHeight(40);
    dropShadow.setWidth(40);
    dropShadow.setSpread(0.95);
    dropShadow.setColor(Color.WHITE);

    image.setEffect(dropShadow);
  }

  @FXML
  private void onImageHover(MouseEvent event) {
    Polygon image = (Polygon) event.getSource();
    shadowEffect(image);
  }

  @FXML
  private void onImageHoverEnd(MouseEvent event) {
    Polygon image = (Polygon) event.getSource();
    image.setEffect(null);
  }

  @FXML
  private void onReturnToCorridorClicked(ActionEvent event) {
    // if the chest isnt opened and the keys are collected update the objective marker
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

  /**
   * Updates the inventory UI by setting items to the inventory choice box and applying a temporary
   * shadow effect.
   */
  public void updateInventory() {

    // set key visibility
    GameState.setKeys(inventoryKey1, inventoryKey2, inventoryKey3);
  }

  @FXML
  public double getPuzzleRoomWidth() {

    return puzzleRoomPane.getPrefWidth();
  }

  @FXML
  public double getPuzzleRoomHeight() {

    return puzzleRoomPane.getPrefHeight();
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
  private void enlargeItem(MouseEvent event) {
    enlarge((ImageView) event.getSource());
  }

  @FXML
  private void shrinkItem(MouseEvent event) {
    shrink((ImageView) event.getSource());
  }

  @FXML
  private void shrink(ImageView image) {
    image.setScaleX(1.0);
    image.setScaleY(1.0);
  }

  @FXML
  private void enlarge(ImageView image) {
    image.setScaleX(1.5);
    image.setScaleY(1.5);
  }

  /**
   * Updates the mute button's appearance based on the current sound state (muted or unmuted). If
   * the sound is unmuted, it displays the audio-on icon; otherwise, it displays the audio-off icon.
   */
  @FXML
  public void updateMute() {
    if (!GameState.isMuted) {
      soundToggle.setImage(new ImageView("images/sound/audioOn.png").getImage());
      return;
    }
    soundToggle.setImage(new ImageView("images/sound/audioOff.png").getImage());
  }

  @FXML
  private void onKeyPress(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER) {
      onTextInput();
    }
  }

  private void onTextInput() {
    try {
      GameState.chat.onSendMessage(inputText.getText(), appUi);
    } catch (Exception e) {
      e.printStackTrace();
    }
    inputText.clear();
  }

  @FXML
  private void onMessageSent(ActionEvent event) {
    onTextInput();
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
   * Initializes the chat functionality after the application start. Sets the current application UI
   * to PUZZLEROOM and creates a HintNode with necessary UI components. Adds the HintNode to the
   * chat map in the GameState, closes the chat interface, and adds the text area to the chat.
   */
  public void onInitializationAfterStart() {
    // Set the current application UI to PUZZLEROOM
    appUi = Chat.AppUi.PUZZLEROOM;

    // Create a new HintNode with UI components: textArea, inputText, showButton,
    // closeButton, sendButton, chatBackground, switchButton, and hintField
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

    // Add the HintNode to the chat map in the GameState
    GameState.chat.addToMap(appUi, hintNode);

    // Close the chat interface (onCloseChat method is called with null parameter)
    onChatClosed(null);

    // Add the text area to the chat
    GameState.chat.addChat(textArea);
  }

  @FXML
  private void onChatViewSwitched(ActionEvent event) {
    GameState.chat.lastHintToggle();
  }

  @Override
  public void updateObjective() {
    lblObjectiveMarker.setText(ObjectiveMarker.getObjective());
  }
}
