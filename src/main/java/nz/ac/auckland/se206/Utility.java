package nz.ac.auckland.se206;

/** Utility class containing common helper methods for the game application. */
public class Utility {

  /** Exits the game by terminating the application. */
  public static void exitGame() {
    Music.stop();
    System.exit(0);
  }
}
