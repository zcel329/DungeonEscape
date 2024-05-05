package nz.ac.auckland.se206;

/** Marker interface for controllers in the application. */
public interface Controller {
  public static Controller getInstance() {
    return null;
  }

  public void updateMute();

  public void updateInventory();

  public void updateObjective();
}
