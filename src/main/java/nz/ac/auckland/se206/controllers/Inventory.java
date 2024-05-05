package nz.ac.auckland.se206.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import nz.ac.auckland.se206.Controller;

/** Represents the inventory system in the game, managing collected items and their availability. */
public class Inventory {
  @FXML private static ObservableList<String> inventory = FXCollections.observableArrayList();

  public static void addToInventory(String string) {
    inventory.add(string);
    update();
  }

  public static ObservableList<String> getInventory() {
    return inventory;
  }

  public static void removeFromInventory(String string) {
    inventory.remove(string);
    update();
  }

  public static void clearInventory() {
    inventory.clear();
    update();
  }

  public static boolean contains(String string) {
    return inventory.contains(string);
  }

  /** Updates the inventory for all controllers except the WinLossController. */
  public static void update() {
    for (Controller controller : SceneManager.getControllers()) {
      if (controller.equals(WinLossController.getInstance())) {
        continue;
      }
      controller.updateInventory();
    }
  }
}
