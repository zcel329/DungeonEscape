package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
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
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Chat;
import nz.ac.auckland.se206.Chat.AppUi;
import nz.ac.auckland.se206.Controller;
import nz.ac.auckland.se206.CustomNotifications;
import nz.ac.auckland.se206.DungeonMaster;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.Music;
import nz.ac.auckland.se206.ScoreEntry;
import nz.ac.auckland.se206.TimerCounter;
import nz.ac.auckland.se206.Utility;

/** Controller class for managing interactions related to the corridor section in the game. */
public class CorridorController implements Controller {

  private static CorridorController instance;

  public static CorridorController getInstance() {
    return instance;
  }

  // Boolean properties to track key presses for movement
  private BooleanProperty forwardPressed = new SimpleBooleanProperty();
  private BooleanProperty leftPressed = new SimpleBooleanProperty();
  private BooleanProperty backwardPressed = new SimpleBooleanProperty();
  private BooleanProperty rightPressed = new SimpleBooleanProperty();

  // A binding to check if any movement key is pressed
  private BooleanBinding keyPressed =
      forwardPressed.or(leftPressed).or(backwardPressed).or(rightPressed);

  private int movementSpeed = 2;

  // JavaFX UI elements

  @FXML private ImageView backgroundImage;
  @FXML private ImageView soundToggle;
  @FXML private Polygon polygon;
  @FXML private Group group;
  @FXML private Rectangle player;
  @FXML private ImageView treasureChest;
  @FXML private Rectangle door1;

  @FXML private Rectangle door2;
  @FXML private Rectangle border1;
  @FXML private Rectangle door3;

  @FXML private ImageView swordandshield;
  @FXML private ImageView forwardsKey;
  @FXML private ImageView leftwardsKey;
  @FXML private ImageView backwardsKey;
  @FXML private ImageView rightwardsKey;
  @FXML private Pane corridor;
  @FXML private Pane popUp;
  @FXML private Pane riddleDisplay;
  @FXML private Label lblTime;

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

  private HintNode winterNode;
  private AppUi state;

  private DungeonMaster callDungeonMaster;

  private boolean hasSword = false;

  // Animation timer for player movement

  private AnimationTimer playerTimer =
      new AnimationTimer() {
        @Override
        public void handle(long timestamp) {
          // Handle player movement
          if (forwardPressed.get()) {
            player.rotateProperty().set(0);
            if (playerStaysInRoom(polygon, player, "W")) {
              player.setY(player.getY() - movementSpeed);
            }
          }
          // Handle left movement
          if (leftPressed.get()) {
            player.rotateProperty().set(-90);
            if (playerStaysInRoom(polygon, player, "A")) {
              player.setX(player.getX() - movementSpeed);
            }
          }
          // Handle backward movement
          if (backwardPressed.get()) {
            player.rotateProperty().set(180);
            if (playerStaysInRoom(polygon, player, "S")) {
              player.setY(player.getY() + movementSpeed);
            }
          }
          // Handle right movement
          if (rightPressed.get()) {
            player.rotateProperty().set(90);
            if (playerStaysInRoom(polygon, player, "D")) {
              player.setX(player.getX() + movementSpeed);
            }
          }
        }
      };

  // Animation timer for collision detection
  private AnimationTimer collisionTimer =
      new AnimationTimer() {
        @Override
        public void handle(long timestamp) {
          // Check for collisions with doors and handle navigation
          checkCollision();
        }
      };

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

  @FXML
  public void resetPlayerImage() {
    Image image = new Image("/images/character.png");
    player.setFill(new ImagePattern(image));
  }

  /**
   * Handles the mouse click event on the sword and shield item. Generates a notification indicating
   * that the player has become stronger and can now fight the dungeon master. Adds the sword and
   * shield to the player's inventory, hides and disables the sword and shield item, updates the
   * player's appearance, and adds a flash effect to the inventory choice box to indicate the item
   * has been picked up.
   *
   * @param event The MouseEvent triggered by the mouse click.
   */
  @FXML
  public void onSwordAndShieldClicked(MouseEvent event) {
    GameState.hasSwordAndShield = true;
    Music.playSwordDraw();
    CustomNotifications.generateNotification(
        "You've become stronger!", "Now you can fight the dungeon master!");
    Inventory.addToInventory("sword/shield");

    swordandshield.setVisible(false);
    swordandshield.setDisable(true);
    hasSword = true;

    // Then, set the ImageView as the fill for your shape:
    Image image2 =
        new Image(
            "/images/armouredCharacter.png", player.getWidth(), player.getHeight(), true, false);
    player.setFill(new ImagePattern(image2));
    ObjectiveMarker.setObjective("Slay the dungeon master");
    ObjectiveMarker.update();
  }

  /** Initializes the CorridorController. Sets up timers, player character. */
  public void initialize() {

    TimerCounter.addTimerLabel(lblTime);

    callDungeonMaster = new DungeonMaster();

    instance = this;
    Image image = new Image("/images/character.png");

    player.setFill(new ImagePattern(image));
    // Listener to start/stop timers based on key presses
    keyPressed.addListener(
        (observable, boolValue, randomVar) -> {
          if (!boolValue) {
            // Start the player movement and collision detection timers
            playerTimer.start();
            collisionTimer.start();
          } else {
            // Stop the timers when no movement keys are pressed
            playerTimer.stop();
            collisionTimer.stop();
          }
        });
  }

  // Method to check if the player stays in the room while moving
  private boolean playerStaysInRoom(Polygon polygon, Rectangle player, String direction) {
    double bottomRightX = player.getX() + player.getWidth();
    double bottomRightY = player.getY() + player.getHeight();

    // Check if player stays in the room while moving in the specified direction
    if (direction.equals("W")) {
      return (polygon.contains(player.getX(), player.getY() - movementSpeed))
          && (polygon.contains(bottomRightX, bottomRightY - movementSpeed));
    } else if (direction.equals("A")) {
      return (polygon.contains(player.getX() - movementSpeed, player.getY()))
          && polygon.contains(bottomRightX - movementSpeed, bottomRightY);
    } else if (direction.equals("S")) {
      return (polygon.contains(player.getX(), player.getY() + movementSpeed))
          && polygon.contains(bottomRightX, bottomRightY + movementSpeed);
    } else if (direction.equals("D")) {
      return (polygon.contains(player.getX() + movementSpeed, player.getY()))
          && polygon.contains(bottomRightX + movementSpeed, bottomRightY);
    } else {
      return false;
    }
  }

  // Method to check collision with doors and handle navigation
  private void checkCollision() {
    // Check collision with door1 and navigate to a new room if needed
    if (player.intersects(door1.getBoundsInParent())) {
      player.setY(0);
      player.setX(0);

      stopMovement();
      App.goToDoor1();

      GameState.currentRoom = GameState.State.RUSIRU;
    }

    // Check collision with door2 and navigate to a new room if needed
    if (player.getBoundsInParent().intersects(door2.getBoundsInParent())) {

      player.setY(0);
      player.setX(0);

      stopMovement();

      App.goToDoor2();
      GameState.currentRoom = GameState.State.MARCELLIN;
    }

    // Check collision with door3 and navigate to a new room if needed
    if (player.getBoundsInParent().intersects(door3.getBoundsInParent())) {
      player.setY(0);
      player.setX(0);
      stopMovement();
      GameState.currentRoom = GameState.State.ZACH;
      App.goToDoor3();
    }
  }

  // Method to stop player movement
  private void stopMovement() {
    Chat.getInstance().disableAll();
    forwardPressed.set(false);
    leftPressed.set(false);
    backwardPressed.set(false);
    rightPressed.set(false);
  }

  /**
   * Handles key pressed events. Triggers specific actions based on the pressed key, such as text
   * entry or movement.
   *
   * @param event The KeyEvent triggered by the key press.
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    // for the first key press fade the key images displayed
    int n;
    if (GameState.previousKeyPress == false) {
      n = 1;
    } else {
      n = 0;
    }

    if (event.getCode() == KeyCode.ENTER) {
      doTextEntry();
    }

    // Handle key press events
    switch (event.getCode()) {
      case W:
        forwardPressed.set(true);
        fadeControlKeyImages(n);

        break;
      case A:
        leftPressed.set(true);
        fadeControlKeyImages(n);

        break;
      case S:
        backwardPressed.set(true);
        fadeControlKeyImages(n);
        break;
      case D:
        rightPressed.set(true);
        fadeControlKeyImages(n);

        break;
      default:
        break;
    }
  }

  /**
   * Handles key released events. Stops specific movement actions when corresponding keys are
   * released.
   *
   * @param event The KeyEvent triggered by the key release.
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    // Handle key release events
    switch (event.getCode()) {
      case W:
        forwardPressed.set(false);
        break;
      case A:
        leftPressed.set(false);
        break;
      case S:
        backwardPressed.set(false);
        break;
      case D:
        rightPressed.set(false);
        break;
      default:
        break;
    }
  }

  /**
   * Handles the mouse click event on the treasure chest. Checks if the required keys are collected
   * and navigates to the chest screen if the conditions are met.
   *
   * @param event The MouseEvent triggered by the mouse click.
   * @throws IOException if an error occurs during screen navigation.
   */
  @FXML
  public void onTreasureChestClicked(MouseEvent event) throws IOException {

    // Handle click on treasure chest
    System.out.println("clicked");
    if (!GameState.isKey1Collected && !GameState.isKey2Collected && !GameState.isKey3Collected) {
      CustomNotifications.generateNotification(
          "No Keys!", "The chest is locked, maybe you should come back after finding some...");
      return;
    }

    App.goToChest();
  }

  /**
   * creates a drop shadow for the image to highlight it when hovered over.
   *
   * @param image The ImageView to which the shadow effect is applied.
   */
  @FXML
  private void shadowEffect(ImageView image) {
    // creates a drop shadow for the image to highlight it when hovered over
    DropShadow dropShadow = new DropShadow();
    dropShadow.setHeight(60);
    dropShadow.setWidth(60);
    dropShadow.setSpread(0.35);
    dropShadow.setColor(Color.WHITE);

    image.setEffect(dropShadow);
  }

  @FXML
  private void onImageHover(MouseEvent event) {
    ImageView image = (ImageView) event.getSource();
    shadowEffect(image);
  }

  @FXML
  private void onImageHoverEnd(MouseEvent event) {
    ImageView image = (ImageView) event.getSource();
    image.setEffect(null);
  }

  /** Handles the event when the treasure chest is unlocked. */
  @FXML
  public void onTreasureChestUnlocked() {
    // Check if the chest is opened, the "swordandshield" item is not in inventory, and it is
    // currently not visible
    if (GameState.isChestOpened
        && !Inventory.contains("sword/shield")
        && !swordandshield.isVisible()) {
      swordandshield.setVisible(true); // Make the "swordandshield" item visible
      swordandshield.setDisable(false); // Enable the "swordandshield" item for interaction
    }
  }

  /**
   * Handles the click event on the Dungeon Master character. Displays appropriate notifications
   * based on the game state and player's actions.
   */
  @FXML
  public void clickDungeonMaster() {
    // Handle click on dungeon master
    if (hasSword) {
      // win game
      //
      Music.playSwordHit();
      Music.playPerish();
      GameState.tts.cancel();
      GameState.isGameWon = true;
      // Calculate time taken to win
      String time = lblTime.getText();
      String[] timeSplit = time.split(":");
      int minutes = Integer.parseInt(timeSplit[0]);
      int seconds = Integer.parseInt(timeSplit[1]);
      int timeLeft = seconds + (minutes * 60);
      int totalTime;
      // Calculate total time taken
      if (GameState.currentTimeLimit == GameState.TimeLimit.TWO_MINUTES) {
        totalTime = 120 - timeLeft;
      } else if (GameState.currentTimeLimit == GameState.TimeLimit.FOUR_MINUTES) {
        totalTime = 240 - timeLeft;
      } else {
        totalTime = 360 - timeLeft;
      }
      // Update game state
      GameState.totalTime += totalTime;
      GameState.gamesWon++;
      GameState.hintsUsed += GameState.hintsGiven;
      // Add score to leaderboard
      ScoreEntry scoreEntry = new ScoreEntry(GameState.difficultyLevel, 0, time);
      GameState.scores.add(scoreEntry);
      LeaderboardController.getInstance().sortScores();
      App.goToWinLoss();
      // Add win/loss to leaderboard
      WinLossController.getInstance().checkGameStatus();
    } else {

      callDungeonMaster.createPopUp(popUp);
      String context = DungeonMaster.getDungeonMasterResponse();
      callDungeonMaster.getText("user", context);
      // set style class
      popUp.getStyleClass().add("popUp");
      popUp.visibleProperty().set(true);
      popUp.mouseTransparentProperty().set(false);
      popUp.toFront();
    }
  }

  @FXML
  private void clickExit(MouseEvent event) {
    // Handle click on exit
    Utility.exitGame();
  }

  private void fadeControlKeyImages(int n) {
    if (n == 1) {

      ImageView[] imageViews = new ImageView[4];
      imageViews[0] = forwardsKey;
      imageViews[1] = leftwardsKey;
      imageViews[2] = backwardsKey;
      imageViews[3] = rightwardsKey;
      FadeTransition[] fadeTransitions = new FadeTransition[imageViews.length];
      for (int i = 0; i < imageViews.length; i++) {
        fadeTransitions[i] = new FadeTransition(Duration.seconds(1.2), imageViews[i]);
        fadeTransitions[i].setToValue(0.0);
      }

      // Create a ParallelTransition to run all FadeTransitions concurrently
      ParallelTransition parallelTransition = new ParallelTransition(fadeTransitions);

      // Set an event handler to remove the images when the animation is complete
      parallelTransition.setOnFinished(
          d -> {
            corridor.getChildren().removeAll(imageViews);
          });

      parallelTransition.play();
      GameState.previousKeyPress = true;
    }
  }

  /** Updates the inventory choice box with the current inventory. Also sets the key visibility */
  public void updateInventory() {

    // set key visibility
    GameState.setKeys(inventoryKey1, inventoryKey2, inventoryKey3);

    // Create a Timeline to revert the shadow back to its original state after 2 seconds
  }

  @FXML
  public double getCorridorWidth() {

    return corridor.getPrefWidth();
  }

  @FXML
  public double getCorridorHeight() {

    return corridor.getPrefHeight();
  }

  /** Updates the mute state and toggles the sound icon image accordingly. */
  @FXML
  public void updateMute() {
    if (!GameState.isMuted) {
      soundToggle.setImage(new ImageView("images/sound/audioOn.png").getImage());
      return;
    }
    soundToggle.setImage(new ImageView("images/sound/audioOff.png").getImage());
  }

  @FXML
  private void mute() {
    // Handle click on mute
    GameState.mute();
  }

  @FXML
  private void onSendMessage(ActionEvent event) {
    doTextEntry();
  }

  /** Initializes the chat and sets up the UI state after the game starts. */
  public void initialiseAfterStart() {
    ObjectiveMarker.setObjective("Find the 3 keys");

    ObjectiveMarker.update();

    // Start the animation

    // Initialise the chat
    state = AppUi.CORRIDOR;
    // Create a CompletableFuture for the background task
    winterNode =
        new HintNode(
            textArea,
            inputText,
            showButton,
            closeButton,
            sendButton,
            chatBackground,
            switchButton,
            hintField);
    // Configure the chat completion request
    GameState.chat.addToMap(state, winterNode);
    GameState.chat.massDisable(state);
    GameState.chat.addChat(textArea);
  }

  @FXML
  private void onEnableHint(ActionEvent event) {
    GameState.chat.massEnable(state);
  }

  @FXML
  private void onDisableHint(ActionEvent event) {
    GameState.chat.massDisable(state);
  }

  @FXML
  private void onChatSwitch(ActionEvent event) {
    GameState.chat.lastHintToggle();
  }

  private void doTextEntry() {
    try {
      GameState.chat.onSendMessage(inputText.getText(), state);
    } catch (Exception e) {
      e.printStackTrace();
    }
    inputText.clear();
  }

  @Override
  public void updateObjective() {
    System.out.println("xde");
    lblObjectiveMarker.setText(ObjectiveMarker.getObjective());
    System.out.println(lblObjectiveMarker.getText());
  }
}
