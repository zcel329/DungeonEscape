package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Chat;
import nz.ac.auckland.se206.Controller;
import nz.ac.auckland.se206.DungeonMaster;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.Music;
import nz.ac.auckland.se206.TimerCounter;
import nz.ac.auckland.se206.Utility;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Drag the anchors around to change a polygon's points. */
// see https://stackoverflow.com/questions/13056795/cubiccurve-javafx
// and https://stackoverflow.com/questions/15981274/javafx-modify-polygons
public class UntangleRoomController implements Controller {

  // a draggable anchor displayed around a point.
  class Anchor extends Circle {

    // records relative x and y co-ordinates.
    private class Delta {
      private double horizontal;
      private double vertical;
    }

    private final DoubleProperty horizontal;
    private final DoubleProperty vertical;

    Anchor(Color color, DoubleProperty x, DoubleProperty y) {
      super(x.get(), y.get(), 10);
      setFill(color.deriveColor(1, 1, 1, 0.5));
      setStroke(color);
      setStrokeWidth(2);
      setStrokeType(StrokeType.OUTSIDE);

      this.horizontal = x;
      this.vertical = y;

      x.bind(centerXProperty());
      y.bind(centerYProperty());
      enableDrag();
    }

    // make a node movable by dragging it around with the mouse.
    private void enableDrag() {
      final Delta dragDelta = new Delta();
      // record a delta distance for the drag and drop operation.
      setOnMousePressed(
          new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
              // record a delta distance for the drag and drop operation.
              dragDelta.horizontal = getCenterX() - mouseEvent.getX();
              dragDelta.vertical = getCenterY() - mouseEvent.getY();
              getScene().setCursor(Cursor.MOVE);
            }
          });
      // move a node around, when scene is dragged.
      setOnMouseReleased(
          new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
              isIntersecting((Polygon) getParent().getChildrenUnmodifiable().get(0));
              getScene().setCursor(Cursor.HAND);
            }
          });
      /// move a node around, when scene is dragged.
      setOnMouseDragged(
          new EventHandler<MouseEvent>() {
            @Override
            // move a node around, when scene is dragged.
            public void handle(MouseEvent mouseEvent) {
              // move a node around, when scene is dragged.
              double newX = mouseEvent.getX() + dragDelta.horizontal;
              if (newX > 0 && newX < getScene().getWidth()) {
                setCenterX(newX);
              }
              // move a node around, when scene is dragged.
              double newY = mouseEvent.getY() + dragDelta.vertical;
              if (newY > 0 && newY < getScene().getHeight()) {
                setCenterY(newY);
              }
            }
          });
      // change the cursor when it is over nodes
      setOnMouseEntered(
          new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
              if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
              }
            }
          });
      // change the cursor back to normal when it is exited
      setOnMouseExited(
          new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
              if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
              }
            }
          });
    }
  }

  private static UntangleRoomController instance;

  public static UntangleRoomController getInstance() {
    return instance;
  }

  @FXML private AnchorPane untangleRoomAnchorPane;
  @FXML private Pane pane;
  @FXML private Pane cursorPane;
  @FXML private Pane popUp;
  @FXML private Pane visualDungeonMaster;
  @FXML private ImageView exclamationMark;
  @FXML private ImageView key2;
  @FXML private ImageView soundToggle;
  @FXML private Label lblTime;
  @FXML private ImageView hand;
  @FXML private TextArea textArea;
  @FXML private TextField inputText;
  @FXML private Button showButton;
  @FXML private Button closeButton;
  @FXML private Button sendButton;
  @FXML private ImageView chatBackground;
  @FXML private Button switchButton;
  @FXML private Label hintField;
  @FXML private Label lblObjectiveMarker;

  @FXML private VBox inventoryKey1;
  @FXML private VBox inventoryKey2;
  @FXML private VBox inventoryKey3;

  private HintNode hintNode;
  private Chat.AppUi appUi;

  private boolean isSolved = false;

  private DungeonMaster callDungeonMaster;

  /**
   * Initializes the PuzzleController. This method is automatically called after the FXML file has
   * been loaded.
   */
  public void initialize() {
    TimerCounter.addTimerLabel(lblTime);
    // set the instance
    instance = this;

    callDungeonMaster = new DungeonMaster();

    popUp.toBack();

    visualDungeonMaster.visibleProperty().set(false);
    visualDungeonMaster.mouseTransparentProperty().set(true);

    TranslateTransition translateTransition = GameState.translate(exclamationMark);
    translateTransition.play();

    // set the inventory choice box
    Polygon polygon = createStartingTriangle();

    // add the polygon to the pane
    Group root = new Group();
    root.getChildren().add(polygon);
    root.getChildren().addAll(createControlAnchorsFor(polygon.getPoints()));
    pane.getChildren().add(root);
  }

  /** Animates the cursor to move to the puzzle room. */
  public void animateCursor() {
    Duration duration = Duration.millis(1500);
    // Create new translate transition
    TranslateTransition transition = new TranslateTransition(duration, hand);

    // Play the delay first

    // Move in X axis by +200

    transition.setByX(100);
    // Move in Y axis by +100
    transition.setByY(-80);
    // Go back to previous position after 2.5 seconds
    transition.setAutoReverse(true);
    // Repeat animation twice
    transition.setCycleCount(2);
    // Delay for .5 seconds

    // Change the image after the delay
    PauseTransition delay = new PauseTransition(Duration.millis(500)); // Delay for 2.5 seconds

    // Change the image after the delay
    delay.setOnFinished(
        event -> {
          Image newImage = new Image("images/hand2.png"); // Load the new image
          hand.setImage(newImage);
          transition.play();
        });

    // Play the delay, and when it's finished, start the translation animation

    // Play the delay first
    delay.play();
    // Play the delay, and when it's finished, start the translation animation
    transition.setOnFinished(
        event -> {
          untangleRoomAnchorPane.getChildren().remove(cursorPane);
        });
  }

  // creates a triangle.
  private Polygon createStartingTriangle() {
    //  create a triangle
    Polygon polygon = new Polygon();

    // set the points of the
    ObservableList<Double> polygon1 =
        FXCollections.observableArrayList(300d, 60d, 390d, 410d, 90d, 160d, 485d, 160d, 130d, 400d);
    ObservableList<Double> polygon2 =
        FXCollections.observableArrayList(175d, 90d, 270d, 355d, 140d, 320d, 400d, 90d, 380d, 340d);
    ObservableList<Double> polygon3 =
        FXCollections.observableArrayList(
            480d, 140d, 150d, 350d, 305d, 190d, 470d, 350d, 115d, 140d);

    int random = (int) (Math.random() * 3);
    System.out.println(random + "random");
    if (random == 1) {
      polygon.getPoints().setAll(polygon1);
    } else if (random == 2) {
      polygon.getPoints().setAll(polygon2);
    } else {
      polygon.getPoints().setAll(polygon3);
    }

    //  set the style of the triangle
    polygon.setStroke(Color.rgb(210, 15, 57, 1));
    polygon.setStrokeWidth(4);
    polygon.setStrokeLineCap(StrokeLineCap.ROUND);
    polygon.setFill(Color.rgb(230, 69, 83, 0.4));

    return polygon;
  }

  @FXML
  private void shrinkItem(MouseEvent event) {
    shrink((ImageView) event.getSource());
  }

  @FXML
  private void enlargeItem(MouseEvent event) {
    enlarge((ImageView) event.getSource());
  }

  @FXML
  private void shrink(ImageView image) {
    image.setScaleX(1.0);
    image.setScaleY(1.0);
  }

  @FXML
  private void enlarge(ImageView image) {
    image.setScaleX(1.5);
    image.setScaleY(1.5);
  }

  private void isIntersecting(Polygon polygon) {
    // Untangle lines to solve the puzzle
    // for debug
    if (pane.getChildren().size() > 1) {
      pane.getChildren().remove(1);
    }
    System.out.println(polygon.getPoints());

    List<Line> lines = new ArrayList<>();
    for (int i = 0; i < polygon.getPoints().size() - 2; i += 2) {
      double x1 = polygon.getPoints().get(i);
      double y1 = polygon.getPoints().get(i + 1);
      double x2 = polygon.getPoints().get(i + 2);
      double y2 = polygon.getPoints().get(i + 3);

      Line line = new Line(x1, y1, x2, y2);
      line.setScaleX(0.9);
      line.setScaleY(0.9);
      line.setStroke(Color.GREEN);
      lines.add(line);
    }
    double x1 = polygon.getPoints().get(polygon.getPoints().size() - 2);
    double y1 = polygon.getPoints().get(polygon.getPoints().size() - 1);
    double x2 = polygon.getPoints().get(0);
    double y2 = polygon.getPoints().get(1);
    Line line = new Line(x1, y1, x2, y2);
    line.setScaleX(0.9);
    line.setScaleY(0.9);
    line.setStroke(Color.BLACK);
    lines.add(line);

    for (int i = 0; i < lines.size(); i++) {
      for (int j = i + 1; j < lines.size(); j++) {
        Shape intersection = Shape.intersect(lines.get(i), lines.get(j));
        if (intersection.getBoundsInLocal().getWidth() != -1) {
          System.out.println(
              "Lines "
                  + i
                  + " and "
                  + j
                  + " intersect"
                  + intersection.getBoundsInLocal().getWidth());
          return;
        }
      }
    }

    puzzleSolved();
  }

  // check if the puzzle is solved
  private void puzzleSolved() {
    if (isSolved) {
      return;
    }
    isSolved = true;
    // If it's solved, then the player can go back to the corridor
    key2.setVisible(true);
    key2.setDisable(false);
    // update the game state
    visualDungeonMaster.visibleProperty().set(true);
    visualDungeonMaster.mouseTransparentProperty().set(false);
  }

  @FXML
  public double getUntangleRoomWidth() {

    return untangleRoomAnchorPane.getPrefWidth();
  }

  @FXML
  public double getUntangleRoomHeight() {

    return untangleRoomAnchorPane.getPrefHeight();
  }

  // creates a draggable anchor displayed around a point.
  private ObservableList<Anchor> createControlAnchorsFor(final ObservableList<Double> points) {
    ObservableList<Anchor> anchors = FXCollections.observableArrayList();

    for (int i = 0; i < points.size(); i += 2) {
      final int idx = i;

      DoubleProperty xval = new SimpleDoubleProperty(points.get(i));
      DoubleProperty yval = new SimpleDoubleProperty(points.get(i + 1));

      // add a listener to the xval property
      xval.addListener(
          new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldX, Number x) {
              points.set(idx, (double) x);
            }
          });
      // add a listener to the yval property
      yval.addListener(
          new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldY, Number y) {
              points.set(idx + 1, (double) y);
            }
          });
      // add the anchor to the list of anchors
      anchors.add(new Anchor(Color.rgb(230, 69, 83, 0.8), xval, yval));
    }

    return anchors;
  }

  @FXML
  private void onKey2Clicked(MouseEvent event) {
    Music.playEquipSound();
    GameState.hasKeyTwo = true;
    // make key invisible and unclickable
    key2.setVisible(false);
    key2.setDisable(true);
    GameState.isKey2Collected = true;
    Inventory.update();

    // depending on the past keys collected update the objective marker
    if (GameState.isKey1Collected == false && GameState.isKey3Collected == false) {
      ObjectiveMarker.setObjective("Find the other keys");
    } else if (GameState.isKey1Collected == true && GameState.isKey3Collected == false) {
      ObjectiveMarker.setObjective("Find key 3");
    } else if (GameState.isKey3Collected == true && GameState.isKey1Collected == false) {
      ObjectiveMarker.setObjective("Find key 1");
    } else {
      ObjectiveMarker.setObjective("Return to the corridor ");
    }
    ObjectiveMarker.update();
  }

  @FXML
  private void onReturnToCorridorClicked(ActionEvent event) {
    // if all the keys are collected and the chest isnt opened update the objective marker
    if (GameState.isKey1Collected == true
        && GameState.isKey2Collected == true
        && GameState.isKey3Collected == true
        && GameState.isChestOpened == false) {

      ObjectiveMarker.setObjective("Open the Chest");

      ObjectiveMarker.update();
    }
    App.returnToCorridor();
    GameState.currentRoom = GameState.State.CHEST;
  }

  /**
   * Updates the inventory UI by setting items to the inventory choice box and applying a temporary
   * shadow effect.
   */
  public void updateInventory() {

    // set key visibility
    GameState.setKeys(inventoryKey1, inventoryKey2, inventoryKey3);
  }

  @FXML
  private void mute() {
    // Handle click on mute
    GameState.mute();
  }

  /**
   * Updates the mute button's image based on the current mute state of the game. If the game is not
   * muted, sets the button image to audio on; otherwise, sets it to audio off.
   */
  @FXML
  public void updateMute() {
    if (!GameState.isMuted) {
      soundToggle.setImage(new ImageView("images/sound/audioOn.png").getImage());
      return;
    }
    soundToggle.setImage(new ImageView("images/sound/audioOff.png").getImage());
  }

  @FXML
  private void clickExit(MouseEvent event) {
    // Handle click on exit
    Utility.exitGame();
  }

  @FXML
  private void onKeyPressed(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER) {
      onSendMessage(null);
    }
  }

  private void enforceTextRetrieval() {
    try {
      GameState.chat.onSendMessage(inputText.getText(), appUi);
    } catch (Exception e) {
      e.printStackTrace();
    }
    inputText.clear();
  }

  @FXML
  private void onSendMessage(ActionEvent event) {
    enforceTextRetrieval();
  }

  @FXML
  private void onShowChat(ActionEvent event) {
    GameState.chat.massEnable(appUi);
  }

  @FXML
  private void onCloseChat(ActionEvent event) {
    GameState.chat.massDisable(appUi);
  }

  /**
   * Handles the event when the player interacts with the AI button, displaying a popup to
   * communicate with the Dungeon Master.
   *
   * @param event The MouseEvent representing the player's interaction with the AI button.
   */
  @FXML
  public void getAi(MouseEvent event) {
    popUp.visibleProperty().set(false);
    callDungeonMaster.createPopUp(popUp);
    String context = DungeonMaster.getDungeonMasterComment();
    callDungeonMaster.getText("user", context);
    // sets popup styling and formatting
    popUp.getStyleClass().add("popUp");
    GameState.popUpShow(popUp, visualDungeonMaster);
  }

  /**
   * Enables the chat functionality for the untangle puzzle room. Initializes the chat interface and
   * sets the UI components for chat interactions.
   */
  public void enableClassAction() {
    // Set the instance
    appUi = Chat.AppUi.UNTANGLE;
    // Create a CompletableFuture for the background task
    hintNode =
        new HintNode(
            textArea,
            inputText,
            showButton,
            closeButton,
            sendButton,
            chatBackground,
            switchButton,
            hintField);
    GameState.chat.addToMap(appUi, hintNode);
    // Set the current application UI to UNTANGLE
    GameState.chat.massDisable(appUi);
    GameState.chat.addChat(textArea);
  }

  @FXML
  private void onSwitchChatView(ActionEvent event) {
    GameState.chat.lastHintToggle();
  }

  @Override
  public void updateObjective() {
    lblObjectiveMarker.setText(ObjectiveMarker.getObjective());
  }
}
