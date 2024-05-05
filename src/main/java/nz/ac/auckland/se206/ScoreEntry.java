package nz.ac.auckland.se206;

/**
 * The ScoreEntry class represents a score entry in the leaderboard. It includes information such as
 * player name, score, and movements made during the game.
 */
public class ScoreEntry {
  /** Enum representing different types of movements. */
  public enum Movement {
    UP,
    DOWN,
    NONE
  }

  private Movement movement;

  private String difficulty;
  private int leaderboardPos;
  private String time;

  /**
   * Constructor for creating a ScoreEntry instance with specified difficulty, leaderboard position,
   * and time.
   *
   * @param difficulty The difficulty level of the game.
   * @param leaderboardPos The position of the player on the leaderboard.
   * @param time The time taken to complete the game.
   */
  public ScoreEntry(String difficulty, int leaderboardPos, String time) {
    this.difficulty = difficulty;
    this.leaderboardPos = leaderboardPos;
    this.time = time;
    movement = Movement.NONE; // Set the initial movement to NONE
  }

  public String getDifficulty() {
    return difficulty;
  }

  public String getTime() {
    return time;
  }

  public int getLeaderboardPos() {
    return leaderboardPos;
  }

  public Movement getMovement() {
    return movement;
  }

  public void setLeaderboardPos(int leaderboardPos) {
    this.leaderboardPos = leaderboardPos;
  }

  public void setMovement(Movement movement) {
    this.movement = movement;
  }
}
