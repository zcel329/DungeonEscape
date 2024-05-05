package nz.ac.auckland.se206.controllers;

import java.util.Comparator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.ScoreEntry;

/** Controller class for managing the leaderboard functionality in the game. */
public class LeaderboardController {

  private static LeaderboardController instance;

  public static LeaderboardController getInstance() {
    return instance;
  }

  @FXML private StackPane graph;
  @FXML private ScrollPane scrollPane;
  @FXML private Label curretProfile;
  @FXML private Label labelGames;
  @FXML private Label labelPlayTime;
  @FXML private Label labelHintsUsed;
  @FXML private VBox leaderboard;
  @FXML private VBox leaderboardContainer;

  /** Initializes the leaderboard by adding sample scores and sorting them. */
  public void initialize() {
    instance = this;
  }

  /**
   * Adds a time entry to the leaderboard with the specified difficulty, time, position, and final
   * status.
   *
   * @param difficultyString The difficulty the player was on.
   * @param time The time achieved by the player.
   * @param position The position of the player in the leaderboard.
   * @param isFinal Indicates whether the entry is for the final leaderboard.
   */
  public void addTime(String difficultyString, String time, int position, boolean isFinal) {
    // Create a horizontal box to represent the leaderboard entry
    HBox entry = new HBox();
    entry.setPrefHeight(60);

    // Create two horizontal boxes to divide the entry into two halves
    HBox firstHalf = new HBox();
    firstHalf.setPrefWidth(250);
    firstHalf.setAlignment(Pos.CENTER_LEFT);

    HBox secondHalf = new HBox();
    secondHalf.setPrefWidth(250);
    secondHalf.setAlignment(Pos.CENTER_RIGHT);

    // Determine the background color based on the position
    String hexcode = getColour(position);

    // Set the style for the entry, including background color and padding
    entry.setStyle("-fx-background-color: " + hexcode + "; -fx-padding: 15;");
    if (isFinal) {
      entry.setStyle("-fx-background-color: #d5b85a; -fx-padding: 15;");
    }
    entry.setAlignment(Pos.CENTER);

    // Create a stack pane to display the position number as a circle with a label
    StackPane pos = new StackPane();
    Circle circle = new Circle(15);
    circle.setFill(Color.WHITE);
    Label posLabel = new Label(Integer.toString(position + 1));
    posLabel.setStyle("-fx-text-fill: " + hexcode + "; -fx-font-size: 20; -fx-font-weight: bold;");

    pos.getChildren().addAll(circle, posLabel);
    pos.setPadding(new Insets(0, 20, 0, 0));
    pos.getStyleClass().add("white-text-small");

    // Add the position display to the first half
    firstHalf.getChildren().add(pos);

    // Check if the time is -1 (indicating no time set)
    if (time == "") {
      firstHalf.getChildren().add(new Label("No time set"));
      leaderboard.getChildren().add(entry);
      return;
    }

    // Create labels for the player's name and time
    Label difficultyLabel = new Label(difficultyString);
    difficultyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-padding: 0 0 0 20;");
    difficultyLabel.getStyleClass().add("white-text-small");
    firstHalf.getChildren().add(difficultyLabel);

    Label timeLabel = new Label("Time Left: " + time);
    timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-padding: 0 15 0 10;");
    timeLabel.getStyleClass().add("white-text-small");
    secondHalf.getChildren().add(timeLabel);

    // Add the two halves to the entry
    entry.getChildren().addAll(firstHalf, secondHalf);

    // Depending on whether it's a final entry, add to the appropriate container
    if (isFinal) {
      leaderboardContainer.getChildren().add(entry);
    } else {
      leaderboard.getChildren().add(entry);
    }
  }

  /** Sorts the scores list by time and updates the leaderboard UI. */
  public void sortScores() {
    // sets up updated leaderboard
    leaderboard.getChildren().clear();
    if (leaderboardContainer.getChildren().size() > 2) {
      leaderboardContainer.getChildren().remove(2);
    }
    labelGames.setText(GameState.gamesWon + " Games Won");
    String formatTime =
        String.format("%02d:%02d", GameState.totalTime / 60, GameState.totalTime % 60);
    labelPlayTime.setText(formatTime + " Time Spent");
    labelHintsUsed.setText(GameState.hintsUsed + " Hints Used");
    if (GameState.scores.size() == 0) {
      return;
    }
    // Store the last (highest) score entry temporarily
    ScoreEntry temp = GameState.scores.get(GameState.scores.size() - 1);

    // Sort the scores list based on time using a comparator
    GameState.scores.sort(Comparator.comparing(ScoreEntry::getTime).reversed());

    // Iterate through the sorted scores list and update leaderboard positions
    for (int i = 0; i < GameState.scores.size(); i++) {

      String score = GameState.scores.get(i).getTime();

      // Set the leaderboard position for each score entry
      GameState.scores.get(i).setLeaderboardPos(i);

      // Retrieve the name of the player
      String diffuclty = GameState.scores.get(i).getDifficulty();

      // Update the leaderboard UI with the player's name, score, and position
      addTime(diffuclty, score, i, false);
    }

    // Retrieve the time and name of the latest score entry
    String time = temp.getTime();
    String difficulty = temp.getDifficulty();

    // Update the leaderboard UI with the highest score as the final entry
    addTime(difficulty, time, GameState.scores.indexOf(temp), true);
  }

  // depending on integer input, return a colour for the leaderboard
  private String getColour(int i) {
    switch (i % 10) {
      case 0:
        // red
        return "#E2CE31";

      case 1:
        // orange
        return "#E3C631";

      case 2:
        // yellow
        return "#E3BF31";

      case 3:
        // green
        return "#E4B731";
      case 4:
        // blue
        return "#E4B031";

      case 5:
        // purple
        return "#E5A832";

      case 6:
        // pink
        return "#E5A132";

      case 7:
        // brown
        return "#E69932";

      case 8:
        // grey
        return "#E69232";

      case 9:
        // black
        return "#E78A32";

      default:
        // white
        return "#ffffff";
    }
  }

  /**
   * Handles the event when the user clicks the button to return to the start screen.
   *
   * @param event The MouseEvent representing the click event.
   */
  @FXML
  public void toStartScreen(MouseEvent event) {

    if (App.oldScene.equals(SceneManager.AppUi.START)) {
      App.goToStartScreen();
    } else if (App.oldScene.equals(SceneManager.AppUi.WINLOSS)) {
      App.goToWinLoss();
    }
  }
}
