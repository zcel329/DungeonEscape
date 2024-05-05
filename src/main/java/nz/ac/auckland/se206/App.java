package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nz.ac.auckland.se206.GameState.State;
import nz.ac.auckland.se206.controllers.ChestController;
import nz.ac.auckland.se206.controllers.CorridorController;
import nz.ac.auckland.se206.controllers.PuzzleController;
import nz.ac.auckland.se206.controllers.PuzzleRoomController;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.controllers.SceneManager;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;
import nz.ac.auckland.se206.controllers.UntangleRoomController;
import nz.ac.auckland.se206.controllers.WinLossController;

/**
 * This is the entry point of the JavaFX application, while you can change this class, it should
 * remain as the class that runs the JavaFX application.
 */
public class App extends Application {

  public static SceneManager.AppUi oldScene = null;
  public static SceneManager.AppUi newScene = AppUi.START;

  private static Scene scene;

  private static Parent root;

  public static void main(final String[] args) {
    launch();
  }

  /**
   * Sets the root of the scene to the specified AppUi.
   *
   * @param appUi The AppUi enum representing the new scene to be set.
   * @throws IOException If there is an error loading the FXML file for the specified AppUi.
   */
  public static void setRoot(SceneManager.AppUi appUi) throws IOException {
    oldScene = newScene;
    newScene = appUi;

    scene.setRoot(SceneManager.getUiRoot(appUi));

    root.requestFocus();
  }

  /**
   * Returns the node associated to the input file. The method expects that the file is located in
   * "src/main/resources/fxml".
   *
   * @param fxml The name of the FXML file (without extension).
   * @return The node of the input file.
   * @throws IOException If the file is not found.
   */
  private static Parent loadFxml(final String fxml) throws IOException {

    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "Canvas" scene.
   *
   * @param stage The primary stage of the application.
   * @throws IOException If "src/main/resources/fxml/canvas.fxml" is not found.
   */
  @Override
  public void start(final Stage stage) throws IOException {

    SceneManager.addUi(AppUi.UNTANGLE, loadFxml("untangleRoom"));
    SceneManager.addUi(AppUi.WINLOSS, loadFxml("winloss"));
    SceneManager.addUi(AppUi.LEADERBOARD, loadFxml("leaderboard"));
    SceneManager.addUi(AppUi.FIRST_ROOM, loadFxml("room"));
    SceneManager.addUi(AppUi.CORRIDOR, loadFxml("corridor"));
    SceneManager.addUi(AppUi.START, loadFxml("startScreen"));
    SceneManager.addUi(AppUi.PUZZLE, loadFxml("puzzle"));
    SceneManager.addUi(AppUi.PUZZLEROOM, loadFxml("puzzleroom"));
    SceneManager.addUi(AppUi.CHEST, loadFxml("chest"));

    SceneManager.addController(PuzzleRoomController.getInstance());
    SceneManager.addController(WinLossController.getInstance());
    SceneManager.addController(RoomController.getInstance());
    SceneManager.addController(CorridorController.getInstance());
    SceneManager.addController(PuzzleController.getInstance());
    SceneManager.addController(UntangleRoomController.getInstance());
    SceneManager.addController(ChestController.getInstance());

    root = SceneManager.getUiRoot(AppUi.START);
    scene = new Scene(root, 800, 730);
    stage.setScene(scene);
    stage.show();
    focus();
    Music.playBackgroundMusic();
  }

  public static void focus() {
    root.requestFocus();
  }

  /**
   * Resets the game to its default state, including UI components and controllers.
   *
   * @throws IOException If there are issues loading FXML files.
   */
  public static void resetToDefault() throws IOException {
    // Reset the game state
    GameState.reset();
    GameState.currentRoom = State.CHEST;

    SceneManager.addUi(AppUi.PUZZLE, loadFxml("puzzle"));
    SceneManager.addUi(AppUi.PUZZLEROOM, loadFxml("puzzleroom"));
    SceneManager.addUi(AppUi.CHEST, loadFxml("chest"));
    SceneManager.addUi(AppUi.FIRST_ROOM, loadFxml("room"));
    SceneManager.addUi(AppUi.CORRIDOR, loadFxml("corridor"));
    SceneManager.addUi(AppUi.UNTANGLE, loadFxml("untangleRoom"));
    SceneManager.addUi(AppUi.WINLOSS, loadFxml("winloss"));

    SceneManager.clearControllers();

    SceneManager.addController(PuzzleRoomController.getInstance());
    SceneManager.addController(WinLossController.getInstance());
    SceneManager.addController(RoomController.getInstance());
    SceneManager.addController(CorridorController.getInstance());
    SceneManager.addController(PuzzleController.getInstance());
    SceneManager.addController(UntangleRoomController.getInstance());
    SceneManager.addController(ChestController.getInstance());
  }

  /**
   * Return to the corridor and navigate to the corridor view. Adjust the stage size to fit the
   * corridor dimensions.
   */
  public static void setStageToSize(double width, double height) {
    Stage primaryStage = (Stage) scene.getWindow();
    primaryStage.setWidth(width);
    primaryStage.setHeight(height);
  }

  /** Redirects the user to the chest view. */
  public static void goToChest() {
    try {
      App.setRoot(AppUi.CHEST);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Redirects the user to the puzzle view. */
  public static void goToPuzzle() {
    try {
      App.setRoot(AppUi.PUZZLE);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Switches the application UI to the chat interface. */
  public static void goToChat() {
    try {
      App.setRoot(AppUi.CHAT);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Redirects the user back to the corridor view. */
  public static void returnToCorridor() {
    Chat.getInstance().disableAll();
    try {
      // Set the root view to the corridor
      App.setRoot(AppUi.CORRIDOR);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method navigates the application to the leaderboard view.
   *
   * @throws IOException if there is an error loading the leaderboard view.
   */
  public static void goToLeaderboard() {
    try {
      // Set the root view to the leaderboard
      App.setRoot(AppUi.LEADERBOARD);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void makeSwordAndShieldAppear() {
    CorridorController corridorController = CorridorController.getInstance();
    corridorController.onTreasureChestUnlocked();
  }

  /**
   * Navigate to Door 1 and enter the first room. Adjust the stage size to fit the first room
   * dimensions.
   */
  public static void resetPlayerImage() {
    CorridorController corridorController = CorridorController.getInstance();
    corridorController.resetPlayerImage();
  }

  /** Redirects the user to the first room (door 1) view. */
  public static void goToDoor1() {

    try {
      // Set the root view to the first room
      App.setRoot(AppUi.FIRST_ROOM);

      // Get the first room controller instance

    } catch (IOException e) {
      e.printStackTrace();
    }

    // Focus on the first room
    focus();
  }

  /** Navigate to Door 2 and sets the scene to be the untangle room. */
  public static void goToDoor2() {

    try {
      // Set the root view to the Untangle room
      App.setRoot(AppUi.UNTANGLE);
      // get untangle room controller
      UntangleRoomController untangleController = UntangleRoomController.getInstance();
      untangleController.animateCursor();
      // Get the Untangle room controller instance

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Navigate to Door 3 and enter the Puzzle room. Adjust the stage size to fit the Puzzle room
   * dimensions.
   */
  public static void goToDoor3() {

    try {
      // Set the root view to the Puzzle room
      App.setRoot(AppUi.PUZZLEROOM);

      // Get the Puzzle room controller instance

    } catch (IOException e) {

      e.printStackTrace();
    }

    // Focus on the new room
    focus();
  }

  /**
   * Switches the application UI to the Win/Loss screen. Adjusts the primary stage size to fit the
   * Win/Loss screen content.
   */
  public static void goToWinLoss() {
    try {
      // Set the root of the application to the Win/Loss screen.
      App.setRoot(AppUi.WINLOSS);

    } catch (IOException e) {
      // Handle any IOException that might occur during the switch.
      e.printStackTrace();
    }

    // Set the focus to an unspecified method.
    focus();
  }

  /**
   * Switches the application UI to the Start Screen. Adjusts the primary stage size to fit the
   * Start Screen content.
   */
  public static void goToStartScreen() {
    try {
      // Set the root of the application to the Start Screen.
      App.setRoot(AppUi.START);

      // Get the instance of the StartScreenController.

    } catch (IOException e) {
      // Handle any IOException that might occur during the switch.
      e.printStackTrace();
    }

    // Set the focus to an unspecified method.
    focus();
  }

  /**
   * Navigate to Door 2 and enter the Untangle room. Adjust the stage size to fit the Untangle room
   * dimensions.
   */
  @Override
  public void stop() {
    // Stop the timer when the application is closing
    Utility.exitGame();
  }
}
