package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.control.Label;
import nz.ac.auckland.se206.controllers.LeaderboardController;
import nz.ac.auckland.se206.controllers.WinLossController;

/** Utility class for managing game timers and updating timer labels across multiple screens. */
public class TimerCounter {

  private static List<Label> timerLabels = new ArrayList<>();

  /**
   * Adds a timer label to the list of labels to be updated.
   *
   * @param label The label to be added for timer updates.
   */
  public static void addTimerLabel(Label label) {
    timerLabels.add(label);
  }

  /**
   * Starts the game timer with the specified initial time.
   *
   * @param time The initial time for the timer in seconds.
   */
  public void timerStart(int time) {
    final int[] timeCounter = new int[1];
    System.out.println("Timer started");
    timeCounter[0] = time;

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                timeCounter[0]--;

                // Formatting the seconds to be in a presentable/readable format
                int min = timeCounter[0] / 60;
                int sec = timeCounter[0] - min * 60;
                String formattedTime = min + ":" + String.format("%02d", sec);

                Platform.runLater(
                    () -> {

                      // Updating the timer counter across the multiple screens
                      updateTimers(formattedTime);

                      // Game over condition
                      if (timeCounter[0] == 0) {
                        Music.playLossMusic();

                        this.cancel();
                        System.out.println("Timer stopped");
                        gameOver();
                      }

                      // Check if the game is won
                      if (GameState.isGameWon) {
                        Music.playWinMusic();

                        System.out.println("Timer stopped");
                        this.cancel();
                      }
                    });
              }
            },
            0,
            1000);
  }

  /**
   * Handles the game over scenario, navigates to the Win/Loss screen, and checks the game status.
   */
  private void gameOver() {
    // updates scoreboard
    System.out.println("Game over");
    GameState.tts.cancel();
    if (GameState.currentTimeLimit == GameState.TimeLimit.TWO_MINUTES) {
      GameState.totalTime += 120;
    } else if (GameState.currentTimeLimit == GameState.TimeLimit.FOUR_MINUTES) {
      GameState.totalTime += 240;
    } else {
      GameState.totalTime += 360;
    }
    GameState.hintsUsed += GameState.hintsGiven;
    LeaderboardController.getInstance().sortScores();
    App.goToWinLoss();
    WinLossController.getInstance().checkGameStatus();
  }

  /**
   * Updates all registered timer labels with the given formatted time string.
   *
   * @param formattedTime The formatted time string to be displayed on the timer labels.
   */
  private void updateTimers(String formattedTime) {
    for (Label label : timerLabels) {
      label.setText(formattedTime);
    }
  }
}
