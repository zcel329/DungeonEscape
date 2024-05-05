package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Represents a HintNode containing various UI components for displaying hints and managing user
 * interactions.
 */
public class HintNode {
  @FXML private TextArea textArea;
  @FXML private TextField inputText;
  @FXML private Button showButton;
  @FXML private Button closeButton;
  @FXML private Button sendButton;
  @FXML private ImageView chatBackground;
  @FXML private Button switchButton;
  @FXML private Label hintField;

  private List<Node> nodeList;

  /**
   * Constructs a HintNode object with the specified UI components. Initializes the UI components
   * and creates a nodeList containing the components for easy manipulation.
   *
   * @param textArea The TextArea for displaying chat messages.
   * @param inputText The TextField for user input.
   * @param showButton The Button for toggling show/hide of chat history.
   * @param closeButton The Button for closing the chat interface.
   * @param sendButton The Button for sending user messages.
   * @param chatBackground The ImageView representing the chat interface background.
   * @param switchButton The Button for switching chat modes (hint vs. message).
   * @param hintField The Label for displaying remaining hints.
   */
  public HintNode(
      TextArea textArea,
      TextField inputText,
      Button showButton,
      Button closeButton,
      Button sendButton,
      ImageView chatBackground,
      Button switchButton,
      Label hintField) {
    this.textArea = textArea;
    this.inputText = inputText;
    this.showButton = showButton;
    this.sendButton = sendButton;
    this.closeButton = closeButton;
    this.chatBackground = chatBackground;
    this.switchButton = switchButton;
    this.hintField = hintField;

    nodeList = new ArrayList<>();
    nodeList.add(textArea);
    nodeList.add(inputText);
    nodeList.add(closeButton);
    nodeList.add(chatBackground);
    nodeList.add(sendButton);
    nodeList.add(switchButton);
  }

  public List<Node> getNodeList() {
    return nodeList;
  }

  public Button getShowButton() {
    return showButton;
  }

  public Label getHintField() {
    return hintField;
  }

  public Button getCloseButton() {
    return closeButton;
  }

  public Button getSendButton() {
    return sendButton;
  }

  public Button getSwiButton() {
    return switchButton;
  }
}
