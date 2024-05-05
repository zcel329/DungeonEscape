package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Chat.AppUi;
import nz.ac.auckland.se206.Controller;
import nz.ac.auckland.se206.CustomNotifications;
import nz.ac.auckland.se206.DungeonMaster;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.Music;
import nz.ac.auckland.se206.Riddle;
import nz.ac.auckland.se206.TimerCounter;
import nz.ac.auckland.se206.Utility;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for managing interactions related to the chest puzzle in the game. */
public class ChestController implements Controller {

  private static ChestController instance;

  public static ChestController getInstance() {

    return instance;
  }

  private HashMap<String, Integer> keyHoleMap = new HashMap<String, Integer>();
  private HashMap<String, String> correctKeyMap = new HashMap<String, String>();
  private HashMap<String, Integer> keyMap = new HashMap<String, Integer>();
  private List<Integer> keys = Arrays.asList(1, 2, 3);
  private Rectangle[] keysInHoles = new Rectangle[6];

  @FXML private Button riddleButton;

  @FXML private Label lblTime;

  @FXML private ImageView keyHole1;
  @FXML private ImageView keyHole2;
  @FXML private ImageView keyHole3;
  @FXML private ImageView keyHole4;
  @FXML private ImageView keyHole5;
  @FXML private ImageView keyHole6;

  @FXML private Label lblKey1;
  @FXML private Label lblKey2;
  @FXML private Label lblKey3;
  @FXML private Label lblKey4;
  @FXML private Label lblKey5;
  @FXML private Label lblKey6;
  @FXML private Rectangle keyinlock1;
  @FXML private Rectangle keyinlock2;
  @FXML private Rectangle keyinlock3;
  @FXML private Rectangle keyinlock4;
  @FXML private Rectangle keyinlock5;
  @FXML private Rectangle keyinlock6;
  @FXML private Label lblObjectiveMarker;
  @FXML private ImageView exclamationMark;
  @FXML private ImageView soundToggle;

  @FXML private Pane chestPane;
  @FXML private Pane popUp;
  @FXML private Pane riddleDisplay;
  @FXML private Pane visualDungeonMaster;

  private String riddleQuestion;
  private String callQuestion;

  private DungeonMaster callDungeonMaster;
  private DungeonMaster riddleDungeonMaster;
  private Boolean riddleCalled = false;
  private Boolean key1Correct = false;
  private Boolean key2Correct = false;
  private Boolean key3Correct = false;

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

  private HintNode hintNode;
  private AppUi appUi;

  private int currentKey = 0;

  /**
   * Initializes the class. This method is typically used for setting up initial state and
   * properties.
   */
  public void initialize() {
    TimerCounter.addTimerLabel(lblTime);

    // Initialize the instance field with the current instance of the class
    instance = this;

    callDungeonMaster = new DungeonMaster();
    riddleDungeonMaster = new DungeonMaster();

    visualDungeonMaster.visibleProperty().set(false);
    visualDungeonMaster.mouseTransparentProperty().set(true);

    TranslateTransition translateTransition = GameState.translate(exclamationMark);
    translateTransition.play();

    // Set text for key labels
    lblKey1.setText("");
    lblKey2.setText("");
    lblKey3.setText("");
    lblKey4.setText("");
    lblKey5.setText("");
    lblKey6.setText("");

    // Create an ArrayList to store random numbers and a map to represent key holes
    // Initialize the key hole map with empty slots
    ArrayList<Integer> randomNumbers = new ArrayList<Integer>();
    for (int i = 1; i <= 6; i++) {
      randomNumbers.add(i);
      keyHoleMap.put("hole" + i, 0);
    }

    // Create an array to store solutions and shuffle the random numbers
    int[] solutions = new int[3];
    Collections.shuffle(randomNumbers);

    // Assign keys to shuffled key holes and print the assignments
    for (int i = 0; i < 3; i++) {
      keyHoleMap.put("hole" + randomNumbers.get(i), keys.get(i));
      System.out.println("hole" + randomNumbers.get(i) + " " + keys.get(i));
      solutions[i] = randomNumbers.get(i);
    }

    // Initialize the correct key map with empty slots
    for (int i = 0; i < 6; i++) {
      correctKeyMap.put("hole" + (i + 1), "empty");
    }

    // Create a riddle question
    riddleQuestion =
        "You are the dungeon master of an escape room. Tell me a riddle where the first solution is"
            + " "
            + solutions[0]
            + ", the second solution is "
            + solutions[1]
            + ", and the third solution is "
            + solutions[2]
            + ". Hide the answers within the riddle but do not use the numbers within the riddle"
            + " instead use synonyms. Do not, under no circumstance, give the user the answer to"
            + " the riddles. After every sentence do a line break. Make the riddle a few sentences"
            + " long. Do not go over 100 words.";
    String riddleAnswer =
        "the third solution is"
            + solutions[0]
            + ", the second solution is"
            + solutions[1]
            + " and the third solution is"
            + solutions[2];
    GameState.riddleAnswer = riddleAnswer;
    System.out.println(riddleQuestion);

    callQuestion =
        "Congratulate the player on solving the riddle and unlocking the chest with the solution"
            + " that key1 goest into keyhole "
            + solutions[0]
            + ", key2 goes into keyhole "
            + solutions[1]
            + ", and key3 goes into keyhole "
            + solutions[2]
            + " Tell the player that the sword and shield have fallen out to the corridor. Tell the"
            + " player that they can now return to the corridor and fight you. Be antagonistic and"
            + " confident that you will win. Keep this message short";

    keysInHoles[0] = keyinlock1;
    keysInHoles[1] = keyinlock2;
    keysInHoles[2] = keyinlock3;
    keysInHoles[3] = keyinlock4;
    keysInHoles[4] = keyinlock5;
    keysInHoles[5] = keyinlock6;
  }

  /**
   * Handles the mouse click event to open the chest. Checks if the correct key combination is
   * inserted.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  public void openChest(MouseEvent event) {
    System.out.println("open chest");
    // check if correct combination
    updateKeys();
    System.out.println("key1Correct " + key1Correct);
    System.out.println("key2Correct " + key2Correct);
    System.out.println("key3Correct " + key3Correct);
    if (key1Correct && key2Correct && key3Correct) {
      GameState.isChestOpened = true;
      // disable riddle button when finished
      riddleButton.visibleProperty().set(false);
      riddleButton.mouseTransparentProperty().set(true);
      ObjectiveMarker.setObjective("Return to the corridor");
      updateObjective();
      App.makeSwordAndShieldAppear();
      // open chest
      System.out.println("chest opened");
      visualDungeonMaster.visibleProperty().set(true);
      visualDungeonMaster.mouseTransparentProperty().set(false);
      CustomNotifications.generateNotification(
          "Chest Opened!", "You hear the clanging of metal on the floor of the corridor...");
    }
  }

  public double getChestHeight() {
    return chestPane.getPrefHeight();
  }

  public double getChestWidth() {
    return chestPane.getPrefWidth();
  }

  private void updateKeys() {
    // check if correct key

    System.out.println("update keys");
  }

  /** Updates the inventory choice box with the current inventory. Also sets the key visibility */
  public void updateInventory() {

    GameState.setKeys(inventoryKey1, inventoryKey2, inventoryKey3);
  }

  @FXML
  public void getAi(MouseEvent event) {
    callAi();
  }

  /**
   * Handles the mouse click event on the first key hole. Checks if the correct key is inserted.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  @FXML
  public void clickKeyHole1(MouseEvent event) {
    // check if correct key
    ImageView keyHole = (ImageView) event.getSource();
    clickKeyHoleHelper(1, keyHole);
  }

  /**
   * Handles the mouse click event on the second key hole. Checks if the correct key is inserted.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  @FXML
  public void clickKeyHole2(MouseEvent event) {
    // check if correct key
    ImageView keyHole = (ImageView) event.getSource();
    clickKeyHoleHelper(2, keyHole);
  }

  /**
   * Handles the mouse click event on the third key hole. Checks if the correct key is inserted.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  @FXML
  public void clickKeyHole3(MouseEvent event) {
    // check if correct key
    ImageView keyHole = (ImageView) event.getSource();
    clickKeyHoleHelper(3, keyHole);
  }

  /**
   * Handles the mouse click event on the fourth key hole. Checks if the correct key is inserted.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  @FXML
  public void clickKeyHole4(MouseEvent event) {
    // check if correct key
    ImageView keyHole = (ImageView) event.getSource();
    clickKeyHoleHelper(4, keyHole);
  }

  /**
   * Handles the mouse click event on the fifth key hole. Checks if the correct key is inserted.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  @FXML
  public void clickKeyHole5(MouseEvent event) {
    // check if correct key
    ImageView keyHole = (ImageView) event.getSource();
    clickKeyHoleHelper(5, keyHole);
  }

  /**
   * Handles the mouse click event on the sixth key hole. Checks if the correct key is inserted.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  @FXML
  public void clickKeyHole6(MouseEvent event) {
    // check if correct key
    ImageView keyHole = (ImageView) event.getSource();
    clickKeyHoleHelper(6, keyHole);
  }

  @FXML
  private void onReturnToCorridorClicked(ActionEvent event) {
    // if the chest is opened and the player doesnt have the sword and shield set the objective to
    // equip the sword and shield
    if (GameState.isChestOpened == true && GameState.hasSwordAndShield == false) {
      ObjectiveMarker.setObjective("Equip the sword and shield");

      ObjectiveMarker.update();
    }

    App.returnToCorridor();
  }

  /**
   * Handles getting the riddle from the Dungeon Master. Displays the riddle either as a pop-up
   * dialogue or in a dedicated riddle pane, depending on whether the riddle has been previously
   * called.
   */
  @FXML
  public void getRiddle() {
    System.out.println("get riddle");
    if (riddleCalled) {
      System.out.println("riddle pane called");
      // gets the riddle pane if already asked dungeon master for riddle
      String riddleText = riddleDungeonMaster.getRiddle();
      Pane riddlePane = Riddle.riddlePane(riddleText);
      riddleDisplay.getChildren().add(riddlePane);
      riddlePane.getStyleClass().add("riddle");
      riddleDisplay.toFront();
      riddleDisplay.visibleProperty().set(true);
      riddleDisplay.mouseTransparentProperty().set(false);

    } else {
      // gets the dungeon master to speak the riddle dialogue
      popUp.visibleProperty().set(false);
      riddleDungeonMaster.createPopUp(popUp);
      riddleDungeonMaster.getText("user", riddleQuestion);
      // set style class
      popUp.getStyleClass().add("popUp");
      popUp.visibleProperty().set(true);
      popUp.mouseTransparentProperty().set(false);
      popUp.toFront();
      riddleCalled = true;
    }
  }

  private void clickKeyHoleHelper(int num, ImageView keyHole) {
    // check if correct key
    // if the key is already correct then do nothing
    // check if inserting a key
    System.out.println("click key hole " + num);
    if (correctKeyMap.get("hole" + num) == "empty") {
      // in the case that the key hole is empty when clicked
      System.out.println("empty key hole");
      if (currentKey == 0) {
        return;
      }

      keysInHoles[num - 1].setVisible(true);
      keyMap.put("hole" + num, currentKey);
      String key = "key" + currentKey;
      setLabelKeyHole(num, key);

      // check if correct key
      if (keyHoleMap.get("hole" + num).equals(currentKey)) {
        System.out.println("correct key");
        Music.playCorrectKey();
        // sets the key states
        Image newImage = new Image("/images/unlockedlock.png");
        keyHole.setImage(newImage);
        if (currentKey == 1) {
          key1Correct = true;
          System.out.println("key1 correct");
        } else if (currentKey == 2) {
          key2Correct = true;
          System.out.println("key2 correct");
        } else if (currentKey == 3) {
          key3Correct = true;
          System.out.println("key3 correct");
        }
        correctKeyMap.put("hole" + num, "true");
        System.out.println("removed key " + currentKey + " from inventory");
        removeKey(currentKey);
      } else {
        System.out.println("incorrect key");
        correctKeyMap.put("hole" + num, "false");

        removeKey(currentKey);
      }

      // Create a Timeline to revert the shadow back to its original state after 2 seconds

      // sets to yellow for filled
    } else {
      // if its filled then set back to default (get back key) on click
      // keyHole.styleProperty().set("-fx-fill: #1e90ff");
      // resets the key states
      if (correctKeyMap.get("hole" + num) == "false") {
        keysInHoles[num - 1].setVisible(false);

        if (keyMap.get("hole" + num).equals(1)) {

          System.out.println("key1 incorrect");
        } else if (keyMap.get("hole" + num).equals(2)) {

          System.out.println("key2 incorrect");
        } else if (keyMap.get("hole" + num).equals(3)) {

          System.out.println("key3 incorrect");
        }
        // puts key states back to normal
        correctKeyMap.put("hole" + num, "empty");
        returnKey(keyMap.get("hole" + num));
        setLabelKeyHole(num, "");
        System.out.println("got back key " + keyHoleMap.get("hole" + num));
      }
    }
  }

  /**
   * Sets the label for a keyhole to display the inserted key's status.
   *
   * @param num The number of the keyhole.
   * @param key The key inserted into the keyhole.
   */
  private void setLabelKeyHole(int num, String key) {
    // Print a debug message indicating which keyhole is being set
    System.out.println("set label key hole " + num + " to " + key);

    // Check if a key is inserted
    if (!key.isEmpty()) {
      // Depending on the keyhole number, set the corresponding label to display the key inserted
      switch (num) {
        case 1:
          // lblKey1.setText(key + " inserted");
          break;
        case 2:
          // lblKey2.setText(key + " inserted");
          break;
        case 3:
          // lblKey3.setText(key + " inserted");
          break;
        case 4:
          // lblKey4.setText(key + " inserted");
          break;
        case 5:
          // lblKey5.setText(key + " inserted");
          break;
        case 6:
          // lblKey6.setText(key + " inserted");
          break;
      }
    } else {
      // If no key is inserted, clear the label for the corresponding keyhole
      switch (num) {
        case 1:
          lblKey1.setText("");
          break;
        case 2:
          lblKey2.setText("");
          break;
        case 3:
          lblKey3.setText("");
          break;
        case 4:
          lblKey4.setText("");
          break;
        case 5:
          lblKey5.setText("");
          break;
        case 6:
          lblKey6.setText("");
          break;
      }
    }
  }

  // Call the AI to give a hint
  private void callAi() {
    // Get the dungeon master and the pop up pane
    popUp.visibleProperty().set(false);
    callDungeonMaster.createPopUp(popUp);
    callDungeonMaster.getText("user", callQuestion);
    // Set style class
    popUp.getStyleClass().add("popUp");
    popUp.visibleProperty().set(true);
    popUp.mouseTransparentProperty().set(false);
    popUp.toFront();

    visualDungeonMaster.visibleProperty().set(false);
    visualDungeonMaster.mouseTransparentProperty().set(true);
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
  private void onKeyboardInput(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER) {
      onClickSend(null);
    }
  }

  @FXML
  private void onCreate(ActionEvent event) {
    GameState.chat.massEnable(appUi);
  }

  private void processChatRequest() {
    try {
      GameState.chat.onSendMessage(inputText.getText(), appUi);
    } catch (Exception e) {
      e.printStackTrace();
    }
    inputText.clear();
  }

  @FXML
  private void onClickSend(ActionEvent event) {
    processChatRequest();
  }

  /**
   * Initializes the UI state at the start of the game. Sets the initial UI state to 'CHEST',
   * creates a new HintNode instance with various UI elements, adds the UI state and its
   * corresponding HintNode to the chat interface, closes the chat interface to reset its state, and
   * adds the text area for displaying chat messages to the chat interface.
   */
  public void initialiseStart() {
    // Set the initial UI state to 'CHEST'.
    appUi = AppUi.CHEST;

    // Create a new HintNode instance with various UI elements.
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

    // Add the UI state and its corresponding HintNode to the chat interface.
    GameState.chat.addToMap(appUi, hintNode);

    // Close the chat interface to reset its state.
    onHandleChat(null);

    // Add the text area for displaying chat messages to the chat interface.
    GameState.chat.addChat(textArea);
  }

  @FXML
  private void onHandleChat(ActionEvent event) {
    GameState.chat.massDisable(appUi);
  }

  @FXML
  private void onSwitchView(ActionEvent event) {
    GameState.chat.lastHintToggle();
  }

  private void returnKey(int key) {
    // add key back to inventory and update it in the UI by making opacity 1
    if (key == 1) {
      GameState.hasKeyOne = true;
      inventoryKey1.getChildren().get(1).setOpacity(1);
    } else if (key == 2) {
      GameState.hasKeyTwo = true;
      inventoryKey2.getChildren().get(1).setOpacity(1);
    } else if (key == 3) {
      GameState.hasKeyThree = true;
      inventoryKey3.getChildren().get(1).setOpacity(1);
    }
    Inventory.update();
  }

  private void removeKey(int key) {
    // remove the key from inventory and make its image opaque in the UI
    currentKey = 0;
    if (key == 1) {
      GameState.hasKeyOne = false;
      inventoryKey1.getChildren().get(1).setOpacity(0.35);
      inventoryKey1.setStyle("-fx-border-color: transparent");
    } else if (key == 2) {
      GameState.hasKeyTwo = false;
      inventoryKey2.getChildren().get(1).setOpacity(0.35);
      inventoryKey2.setStyle("-fx-border-color: transparent");
    } else if (key == 3) {
      GameState.hasKeyThree = false;
      inventoryKey3.getChildren().get(1).setOpacity(0.35);
      inventoryKey3.setStyle("-fx-border-color: transparent");
    }
    Inventory.update();
  }

  @FXML
  private void key1Click(MouseEvent event) {
    if (!GameState.hasKeyOne) {
      return;
    }
    // update the inventory style to show the key has been selected

    currentKey = 1;
    inventoryKey1.setStyle("-fx-border-color: #00ff00; -fx-border-width: 3px;");
    inventoryKey2.setStyle("-fx-border-color: transparent");
    inventoryKey3.setStyle("-fx-border-color: transparent");
  }

  @FXML
  private void key2Click(MouseEvent event) {
    if (!GameState.hasKeyTwo) {
      return;
    }
    // update the inventory style to show the key has been selected

    currentKey = 2;
    inventoryKey1.setStyle("-fx-border-color: transparent");
    inventoryKey2.setStyle("-fx-border-color: #00ff00;-fx-border-width: 3px;");
    inventoryKey3.setStyle("-fx-border-color: transparent");
  }

  @FXML
  private void key3Click(MouseEvent event) {
    if (!GameState.hasKeyThree) {
      return;
    }
    // update the inventory style to show the key has been selected
    currentKey = 3;
    inventoryKey1.setStyle("-fx-border-color: transparent");
    inventoryKey2.setStyle("-fx-border-color: transparent");
    inventoryKey3.setStyle("-fx-border-color: #00ff00;-fx-border-width: 3px;");
  }

  @Override
  public void updateObjective() {
    lblObjectiveMarker.setText(ObjectiveMarker.getObjective());
  }
}
