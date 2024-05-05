package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import nz.ac.auckland.se206.Controller;

/** This class is used to update the objective for all controllers except the WinLossController. */
public class ObjectiveMarker {
  @FXML private static String objective = "placeholder";

  public static void setObjective(String string) {
    objective = string;
  }

  public static String getObjective() {
    return objective;
  }

  /** Updates the objective for all controllers except the WinLossController. */
  public static void update() {
    for (Controller controller : SceneManager.getControllers()) {
      if (controller.equals(WinLossController.getInstance())) {
        continue;
      }
      controller.updateObjective();
    }
  }
}
