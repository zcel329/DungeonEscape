package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.Random;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Chat;
import nz.ac.auckland.se206.Controller;
import nz.ac.auckland.se206.CustomNotifications;
import nz.ac.auckland.se206.DungeonMaster;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.Music;
import nz.ac.auckland.se206.TimerCounter;
import nz.ac.auckland.se206.Utility;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for handling puzzle-related logic and UI interactions. */
public class PuzzleController implements Controller {

  private static PuzzleController instance;

  public static PuzzleController getInstance() {
    return instance;
  }

  private String[][] tiles;
  private String[][] solution;
  @FXML private Pane puzzlePane;
  @FXML private ImageView one;
  @FXML private ImageView two;
  @FXML private ImageView seven;
  @FXML private ImageView four;
  @FXML private ImageView five;
  @FXML private ImageView six;
  @FXML private ImageView zero;
  @FXML private ImageView eight;
  @FXML private ImageView nine;

  @FXML private ImageView exclamationMark;
  @FXML private ImageView soundToggle;

  private DungeonMaster callDungeonMaster;

  @FXML private Label lblTime;

  @FXML private Pane popUp;
  @FXML private Pane visualDungeonMaster;

  @FXML private TextArea textArea;
  @FXML private TextField inputText;
  @FXML private Button showButton;
  @FXML private Button closeButton;
  @FXML private Button sendButton;
  @FXML private ImageView chatBackground;
  @FXML private Button switchButton;
  @FXML private Label hintField;
  @FXML private Label lblObjectiveMarker;
  @FXML private VBox inventoryKey1;
  @FXML private VBox inventoryKey2;
  @FXML private VBox inventoryKey3;

  private HintNode hintNode;
  private Chat.AppUi appUi;
  private boolean firstSolve;

  /**
   * Initializes the PuzzleController. This method is automatically called after the FXML file has
   * been loaded.
   */
  public void initialize() {

    TimerCounter.addTimerLabel(lblTime);

    // set the instance
    instance = this;

    popUp.toBack();

    visualDungeonMaster.visibleProperty().set(false);
    visualDungeonMaster.mouseTransparentProperty().set(true);

    TranslateTransition translateTransition = GameState.translate(exclamationMark);
    translateTransition.play();

    // set the tiles and solution
    generateSetup();

    callDungeonMaster = new DungeonMaster();
  }

  /** Generates the initial setup for the puzzle by shuffling the tiles. */
  private void generateSetup() {
    // use mod and current time to gen ran num
    Random random = new Random();
    int randNum = random.nextInt(3);

    // generate solution and tile set
    solution =
        new String[][] {
          {"one", "two", "zero"}, {"four", "five", "six"}, {"seven", "eight", "nine"}
        };
    tiles =
        new String[][] {
          {"one", "two", "six"}, {"four", "eight", "five"}, {"zero", "seven", "nine"}
        };
    if (randNum == 0) {
      System.out.println("Type zero maze");
    } else if (randNum == 1) {
      // random pattern
      swapImage(eight, two);
      swapImage(seven, eight);
      swapImage(seven, four);
      swapImage(four, one);
    } else { // another generated pattern
      swapImage(four, five);
      swapImage(four, nine);
      swapImage(four, eight);
      swapImage(four, six);
      swapImage(four, two);
      swapImage(four, one);
    }
  }

  public double getPuzzleWidth() {
    return puzzlePane.getPrefWidth();
  }

  public double getPuzzleHeight() {
    return puzzlePane.getPrefHeight();
  }

  @FXML
  private void clickedBack(MouseEvent event) throws IOException {
    App.setRoot(AppUi.PUZZLEROOM);
  }

  @FXML
  private void clickedTile(MouseEvent event) throws IOException {
    Music.playTileSlide();
    clicked((ImageView) event.getSource());
  }

  /**
   * Handles the logic when an ImageView object is clicked in the puzzle game.
   *
   * @param object The ImageView object that was clicked.
   */
  private void clicked(ImageView object) {
    // find the positions of the tiles
    int[] apos = findPos(object.getId());
    int[] bpos = findPos(zero.getId());

    // if the tiles are adjacent, swap them
    if ((apos[0] == bpos[0] && Math.abs(apos[1] - bpos[1]) == 1)
        ^ (apos[1] == bpos[1] && Math.abs(apos[0] - bpos[0]) == 1)) {
      swapImage(object, zero);
    }
    // check if the puzzle is solved
    checkSolution();
  }

  /**
   * Swaps the positions of two ImageView objects in the puzzle grid.
   *
   * @param object The first ImageView object.
   * @param other The second ImageView object to swap with.
   */
  private void swapImage(ImageView object, ImageView other) {

    double startOpacity = 0.5;
    double durationTime = 0.1;
    FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationTime), object);
    fadeTransition.setFromValue(startOpacity); // Starting opacity (completely transparent)
    fadeTransition.setToValue(1.0); // Ending opacity (fully visible)

    // Play the fade transition
    fadeTransition.play();
    fadeTransition = new FadeTransition(Duration.seconds(durationTime), other);
    fadeTransition.setFromValue(startOpacity); // Starting opacity (completely transparent)
    fadeTransition.setToValue(1.0); // Ending opacity (fully visible)

    // Play the fade transition
    fadeTransition.play();

    // Find the positions of the ImageView objects in the grid.
    int[] apos = findPos(object.getId());
    int[] bpos = findPos(other.getId());

    // Update the tile positions in the grid array.
    tiles[apos[0]][apos[1]] = other.getId();
    tiles[bpos[0]][bpos[1]] = object.getId();

    // Swap the layout coordinates of the ImageView objects.
    double ax = object.getLayoutX();
    double ay = object.getLayoutY();
    object.setLayoutX(other.getLayoutX());
    object.setLayoutY(other.getLayoutY());
    other.setLayoutX(ax);
    other.setLayoutY(ay);
  }

  private int[] findPos(String s) {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (tiles[i][j].equals(s)) {
          return new int[] {i, j};
        }
      }
    }
    return null;
  }

  // check if the puzzle is solved
  private void checkSolution() {
    // count the number of tiles in the correct position
    int counter = 0;
    // check if the tiles are in the correct position
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (tiles[i][j].equals(solution[i][j])) {
          counter++;
        }
      }
    }
    // if all the tiles are in the correct position, the puzzle is solved
    if (counter == 9 && !firstSolve) {
      firstSolve = true;
      CustomNotifications.generateNotification(
          "Something Happens!", "You hear something fall to the ground...");
      GameState.setPuzzleRoomSolved(true);

      visualDungeonMaster.visibleProperty().set(true);
      visualDungeonMaster.mouseTransparentProperty().set(false);
    }
  }

  /** Initializes the leaderboard by adding sample scores and sorting them. */
  public void updateInventory() {

    // set key visibility
    GameState.setKeys(inventoryKey1, inventoryKey2, inventoryKey3);

    // Create a Timeline to revert the shadow back to its original state after 2 seconds
  }

  /**
   * Handles the event when the player interacts with the AI button, displaying a popup to
   * communicate with the Dungeon Master.
   *
   * @param event The MouseEvent representing the player's interaction with the AI button.
   */
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

  /**
   * Updates the mute button's image based on the current mute state of the game. If the game is not
   * muted, sets the button image to audio on; otherwise, sets it to audio off.
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
  private void onKeyEntered(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER) {
      onProcessMessage(null);
    }
  }

  /**
   * Creates and initializes the puzzle-related chat functionality. Sets the chat to be disabled in
   * the PUZZLE view.
   */
  public void createClass() {
    // Set the chat to be disabled
    appUi = Chat.AppUi.PUZZLE;
    // Initialise the chat
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
    // Add the chat to the map
    GameState.chat.addToMap(appUi, hintNode);
    onDeleteChat(null);
    // Add the chat to the chat list
    GameState.chat.addChat(textArea);
  }

  @FXML
  private void onCreateChat(ActionEvent event) {
    GameState.chat.massEnable(appUi);
  }

  private void enableTextReturn() {
    try {
      GameState.chat.onSendMessage(inputText.getText(), appUi);
    } catch (Exception e) {
      e.printStackTrace();
    }
    inputText.clear();
  }

  @FXML
  private void onProcessMessage(ActionEvent event) {
    enableTextReturn();
  }

  @FXML
  private void onDeleteChat(ActionEvent event) {
    GameState.chat.massDisable(appUi);
  }

  @FXML
  private void onReverseChat(ActionEvent event) {
    GameState.chat.lastHintToggle();
  }

  @Override
  public void updateObjective() {
    lblObjectiveMarker.setText(ObjectiveMarker.getObjective());
  }
}
