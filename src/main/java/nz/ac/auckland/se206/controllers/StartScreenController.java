package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Chat;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.Difficulty;
import nz.ac.auckland.se206.TimerCounter;
import nz.ac.auckland.se206.Utility;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for the game start screen. */
public class StartScreenController {
  private static StartScreenController instance;

  public static StartScreenController getInstance() {
    return instance;
  }

  @FXML private ImageView soundToggle;
  @FXML private Pane startScreenPane;
  @FXML private ChoiceBox<String> timerChoice;
  @FXML private ChoiceBox<String> difficultyChoice;
  @FXML private Button btnStart;

  @FXML
  private void initialize() {
    instance = this;
    // Add items to the choice box
    timerChoice.getItems().add("2 Minutes");
    timerChoice.getItems().add("4 Minutes");
    timerChoice.getItems().add("6 Minutes");
    timerChoice.setValue("2 Minutes");
    difficultyChoice.getItems().add("Easy");
    difficultyChoice.getItems().add("Medium");
    difficultyChoice.getItems().add("Hard");
    difficultyChoice.setValue("Easy");

    // You can perform additional initialization here if needed.
  }

  @FXML
  private void onStartGame(ActionEvent event) throws ApiProxyException {

    // Get the chosen values from the choice box
    String chosenTimeLimit = timerChoice.getValue();
    String chosenDifficulty = difficultyChoice.getValue();
    GameState.gameTime = chosenTimeLimit;
    GameState.difficultyLevel = chosenDifficulty;

    // Create a new timer object

    checkDifficultyAndTimeLimit(chosenTimeLimit, chosenDifficulty);

    GameState.chat = new Chat();
    GameState.chat.initialiseAfterStart();

    CorridorController.getInstance().initialiseAfterStart();
    RoomController.getInstance().onInitializationAfterStart();
    ChestController.getInstance().initialiseStart();
    PuzzleController.getInstance().createClass();
    PuzzleRoomController.getInstance().onInitializationAfterStart();
    UntangleRoomController.getInstance().enableClassAction();

    timerChoice.getStyleClass().add("choice-box");
    difficultyChoice.getStyleClass().add("choice-box");

    App.returnToCorridor();
  }

  @FXML
  public void toLeaderboard() {
    App.goToLeaderboard();
  }

  /**
   * Checks the chosen difficulty and time limit, then starts the game timer accordingly.
   *
   * @param time The chosen time limit ("2 Minutes", "4 Minutes", or "6 Minutes").
   * @param difficulty The chosen difficulty level ("Easy", "Medium", or "Hard").
   */
  @FXML
  public void checkDifficultyAndTimeLimit(String time, String difficulty) {

    // Create a new timer object
    TimerCounter timer = new TimerCounter();

    // Start the timer based on the chosen time limit
    if (time.equals("2 Minutes")) {
      GameState.currentTimeLimit = GameState.TimeLimit.TWO_MINUTES;
      timer.timerStart(120);
    } else if (time.equals("4 Minutes")) {
      GameState.currentTimeLimit = GameState.TimeLimit.FOUR_MINUTES;
      timer.timerStart(240);
    } else {
      GameState.currentTimeLimit = GameState.TimeLimit.SIX_MINUTES;
      timer.timerStart(360);
    }

    // Set the difficulty based on the chosen difficulty
    if (difficulty.equals("Easy")) {
      GameState.currentDifficulty = Difficulty.EASY;
    } else if (difficulty.equals("Medium")) {
      GameState.currentDifficulty = Difficulty.MEDIUM;
    } else {
      GameState.currentDifficulty = Difficulty.HARD;
    }
  }

  @FXML
  public double getStartScreenHeight() {
    return startScreenPane.getPrefHeight();
  }

  @FXML
  public double getStartScreenWidth() {
    return startScreenPane.getPrefWidth();
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
  private void clickExit(MouseEvent event) {
    // Handle click on exit
    Utility.exitGame();
  }
}
