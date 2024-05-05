package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Controller;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.Music;

/** Controller class for handling the win and loss outcomes of the game. */
public class WinLossController implements Controller {
  // Singleton instance
  private static WinLossController instance;

  public static WinLossController getInstance() {
    return instance;
  }

  @FXML private Pane winlossPane;
  @FXML private Label gameOutcome;
  @FXML private Label gameOutcomeDescription;
  @FXML private Button exitGame;
  @FXML private Button replayGame;
  @FXML private Button newGame;
  @FXML private ImageView soundToggle;

  // Initialization method
  public void initialize() {
    instance = this;
  }

  @FXML
  public void toLeaderboard() {
    App.goToLeaderboard();
  }

  /**
   * Checks the game status and updates the UI components accordingly. If the game is won, sets the
   * text to indicate a win and escaping the dungeon. If the game is lost, sets the text to indicate
   * a loss and failure to escape.
   */
  public void checkGameStatus() {
    if (GameState.isGameWon) {

      // Set text for a win
      gameOutcome.setText("You Win!");
      gameOutcomeDescription.setText("You escaped the dungeon!");
    } else {
      System.out.println(GameState.isGameWon);
      // Set text for a loss
      gameOutcome.setText("You Lose!");
      gameOutcomeDescription.setText("You've failed to escape");
    }
  }

  @FXML
  private void onExitGameClicked(ActionEvent event) {
    // Exit the application
    System.exit(0);
  }

  @FXML
  private void mute() {
    // Handle click on mute
    GameState.mute();
  }

  @FXML
  private void onReplayGameClicked(ActionEvent event) {
    // Check game difficulty and time limit, then return to the corridor
    try {
      App.resetToDefault();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Inventory.clearInventory();
    StartScreenController.getInstance()
        .checkDifficultyAndTimeLimit(GameState.gameTime, GameState.difficultyLevel);
    App.returnToCorridor();
    App.resetPlayerImage();
  }

  @FXML
  private void onNewGameClicked(ActionEvent event) {
    // Go back to the start screen
    Music.playBackgroundMusic();
    App.goToStartScreen();
    Inventory.clearInventory();
    App.resetPlayerImage();
    try {
      App.resetToDefault();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  public double getWinLossHeight() {
    // Get the preferred height of the winlossPane
    return winlossPane.getPrefHeight();
  }

  @FXML
  public double getWinLossWidth() {
    // Get the preferred width of the winlossPane
    return winlossPane.getPrefWidth();
  }

  @Override
  public void updateInventory() {
    // Implementation not provided, but should update the inventory
  }

  /** Updates the mute button to reflect the current mute status of the game. */
  @FXML
  public void updateMute() {
    if (!GameState.isMuted) {
      soundToggle.setImage(new ImageView("images/sound/audioOn.png").getImage());
      return;
    }
    soundToggle.setImage(new ImageView("images/sound/audioOff.png").getImage());
  }

  @Override
  public void updateObjective() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'updateObjective'");
  }
}
