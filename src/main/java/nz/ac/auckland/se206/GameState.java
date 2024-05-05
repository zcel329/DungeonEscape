package nz.ac.auckland.se206;

import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.se206.controllers.SceneManager;
import nz.ac.auckland.se206.controllers.StartScreenController;
import nz.ac.auckland.se206.speech.TextToSpeech;

/** Represents the state of the game. */
public class GameState {

  /**
   * Enumeration representing different states or rooms in a game scenario. This enum helps
   * determine the current room the player is in. The possible states include MARCELLIN, RUSIRU,
   * ZACH, and CHEST. Players can use these states to track their location and progress within the
   * game.
   */
  public enum State {
    MARCELLIN,
    RUSIRU,
    ZACH,
    CHEST
  }

  /** Enum representing different difficulty levels for the game. */
  public enum Difficulty {
    EASY,
    MEDIUM,
    HARD,
  }

  /** Enumeration representing different time limits for the game. */
  public enum TimeLimit {
    TWO_MINUTES,
    FOUR_MINUTES,
    SIX_MINUTES,
  }

  public static Difficulty currentDifficulty = Difficulty.EASY;

  public static TimeLimit currentTimeLimit = TimeLimit.TWO_MINUTES;

  public static State currentRoom = State.CHEST;

  public static boolean aiCalled;

  /** Indicates whether the riddle has been resolved. */
  public static boolean isRiddleResolved = false;

  /** Indicates whether the key has been found. */
  public static boolean isKey1Collected = false;

  public static boolean isKey2Collected = false;
  public static boolean isKey3Collected = false;
  public static boolean isChestOpened = false;
  public static boolean isGameWon = false;
  public static boolean isMuted = false;
  public static String difficultyLevel = "";
  public static String gameTime = "";
  public static boolean isPotionSelected = false;
  public static String firstPotion = "";
  public static String secondPotion = "";
  public static String firstPotionColour = "";
  public static String secondPotionColour = "";

  public static ArrayList<ScoreEntry> scores = new ArrayList<ScoreEntry>();

  public static Riddle riddle;
  public static TextToSpeech tts = new TextToSpeech();
  public static boolean[] potionsSelected = new boolean[5];
  public static int hintsGiven = 0;
  public static int hintsUsed = 0;
  public static int totalTime = 0;
  public static int gamesWon = 0;
  public static int correctPotions = 0;
  public static boolean hasKeyOne = false;
  public static boolean hasKeyTwo = false;
  public static boolean hasKeyThree = false;
  public static boolean gotKeyOne = false;
  public static boolean gotKeyTwo = false;
  public static boolean gotKeyThree = false;
  public static boolean hasSwordAndShield = false;
  public static boolean isBoulderDraggable = false;
  public static boolean previousKeyPress = false;
  public static Chat chat = null;
  public static String riddleAnswer;

  public static SimpleBooleanProperty puzzleRoomSolved = new SimpleBooleanProperty(false);

  public static ObservableBooleanValue getPuzzleRoomSolved() {
    return puzzleRoomSolved;
  }

  public static void setPuzzleRoomSolved(boolean value) {
    puzzleRoomSolved.set(value);
  }

  public static boolean isPuzzleRoomSolved() {
    return puzzleRoomSolved.get();
  }

  /**
   * Creates a TranslateTransition for the specified ImageView, making it bounce vertically.
   *
   * @param image The ImageView to apply the bouncing animation to.
   * @return The TranslateTransition with bouncing animation properties.
   */
  public static TranslateTransition translate(ImageView image) {
    // Default transition for bouncing
    TranslateTransition transition = new TranslateTransition();
    transition.setDuration(Duration.seconds(1));
    transition.setNode(image);
    transition.setFromY(0);
    transition.setToY(10);
    transition.setCycleCount(TranslateTransition.INDEFINITE);
    transition.setAutoReverse(true);

    return transition;
  }

  /**
   * Creates a timeline to flash the inventory choice box. This function changes an
   * inventoryChoiceBox's style back to none after a set duration.
   *
   * @param inventoryChoiceBox The inventory choice box to flash.
   * @return The timeline with flashing animation properties.
   */
  public static Timeline flashAnimation(ComboBox<String> inventoryChoiceBox) {
    Duration duration = Duration.seconds(0.5);
    Timeline timeline =
        new Timeline(
            new KeyFrame(
                duration,
                eventflash -> {
                  // Revert the CSS style to remove the shadow (or set it to the original style)
                  inventoryChoiceBox.setStyle("");
                }));
    return timeline;
  }

  /**
   * Static method to reset all the game state variables. It sets various game state variables to
   * their initial values.
   */
  public static void reset() {
    // reset all the game state variables
    isRiddleResolved = false;
    // reset keys
    isKey1Collected = false;
    isKey2Collected = false;
    isKey3Collected = false;
    isBoulderDraggable = false;
    previousKeyPress = false;
    hasSwordAndShield = false;
    isPotionSelected = false;
    // change potion names
    firstPotionColour = "";
    secondPotionColour = "";
    isMuted = false;
    isPotionSelected = false;
    isChestOpened = false;
    isGameWon = false;
    correctPotions = 0;
    // change strings
    difficultyLevel = "";
    firstPotion = "";
    secondPotion = "";
    hintsGiven = 0;
    // set items to false
    hasKeyOne = false;
    hasKeyTwo = false;
    hasKeyThree = false;
    puzzleRoomSolved.set(false);
  }

  /**
   * Static method to toggle the mute state of the game. If the game is currently muted, it will be
   * unmuted; otherwise, it will be muted. This method also updates the mute status for all
   * controllers in the scene.
   */
  public static void mute() {
    // mute the game
    if (isMuted) {
      System.out.println("unmute");
      Music.unpause();
      isMuted = false;
    } else {
      System.out.println("mute");
      Music.pause();
      tts.cancel();
      isMuted = true;
    }
    StartScreenController.getInstance().updateMute();
    for (Controller controller : SceneManager.getControllers()) {
      controller.updateMute();
    }
  }

  /**
   * Static method to set the opacity of each key in the vbox to 1 if the player has collected them.
   *
   * @param key1 the vbox of the first key
   * @param key2 the vbox of the second key
   * @param key3 the vbox of the third key
   */
  public static void setKeys(VBox key1, VBox key2, VBox key3) {
    // sets the opacity of each key in the vbox to 1 if the player has collected them

    if (hasKeyOne) {
      key1.getChildren().get(1).setOpacity(1);
    } else {
      key1.getChildren().get(1).setOpacity(0.35);
    }
    if (hasKeyTwo) {
      key2.getChildren().get(1).setOpacity(1);
    } else {
      key2.getChildren().get(1).setOpacity(0.35);
    }
    if (hasKeyThree) {
      key3.getChildren().get(1).setOpacity(1);
    } else {
      key3.getChildren().get(1).setOpacity(0.35);
    }
  }

  /**
   * Method to display the pop-up and hide the visual representation of the dungeon master.
   *
   * @param popUp The pane containing the pop-up.
   * @param visualDungeonMaster The pane containing the visual representation of the dungeon master.
   */
  public static void popUpShow(Pane popUp, Pane visualDungeonMaster) {
    popUp.setVisible(true);
    popUp.mouseTransparentProperty().set(false);
    popUp.toFront();

    visualDungeonMaster.visibleProperty().set(false);
    visualDungeonMaster.mouseTransparentProperty().set(true);
  }
}
