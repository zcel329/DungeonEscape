package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import nz.ac.auckland.se206.controllers.HintNode;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/**
 * This class represents the chat interface and handles interactions with GPT-3 for generating
 * responses. It manages various chat screens, message processing, and UI updates.
 */
public class Chat {
  /**
   * Enumeration representing different states of the application user interface. The states include
   * the different rooms, chat interface, puzzle screens, and game outcome screens.
   */
  public enum AppUi {
    START,
    FIRST_ROOM,
    CORRIDOR,
    PUZZLEROOM,
    PUZZLE,
    CHAT,
    UNTANGLE,
    LEADERBOARD,
    CHEST,
    WINLOSS
  }

  private static Chat instance;

  public static Chat getInstance() {
    return instance;
  }

  @FXML private TextArea chatTextArea;
  @FXML private TextArea lastHintArea;

  private ChatCompletionRequest chatCompletionRequest;
  private List<TextArea> variousChatScreens;
  private boolean isThinking;
  private boolean showLastHintOnly;
  private Map<AppUi, HintNode> nodeMap;
  private Set<HintNode> nodeList;

  /**
   * Constructs a Chat object. Initializes data structures and properties for chat functionality.
   */
  public Chat() {
    nodeList = new HashSet<>();
    nodeMap = new HashMap<>();
    isThinking = true;
    instance = this;
    showLastHintOnly = true;
    chatTextArea = new TextArea();
    lastHintArea = new TextArea();
    variousChatScreens = new ArrayList<>();
  }

  public void addChat(TextArea chat) {
    variousChatScreens.add(chat);
  }

  /**
   * Initializes chat functionality after the application starts. Configures the chat completion
   * request and runs GPT-3 with an initial hint request in a background task using
   * CompletableFuture.
   *
   * @throws ApiProxyException If there is an error with the API proxy.
   */
  public void initialiseAfterStart() throws ApiProxyException {

    // Create a CompletableFuture for the background task

    CompletableFuture.runAsync(
        () -> {
          // Configure the chat completion request
          chatCompletionRequest =
              new ChatCompletionRequest()
                  .setN(1)
                  .setTemperature(0.7)
                  .setTopP(0.8)
                  .setMaxTokens(200);

          // Run GPT-3 with an initial hint request
          runGpt(new ChatMessage("user", GptPromptEngineering.getHint()));
          isThinking = false;
          Platform.runLater(
              () -> {
                enableIfDisable();
              });
        });
  }

  /** Enables the switch button and send button if they are disabled in any HintNode. */
  private void enableIfDisable() {
    // Iterate through the list of HintNode groups and check if the switch button or send button is
    // disabled
    boolean bool = false;
    for (HintNode hintNode : nodeList) {
      if (!bool && hintNode.getShowButton().isDisabled()) {
        bool = true;
      }
    }
    // If either button is disabled, enable both buttons in all HintNode groups
    if (bool) {
      for (HintNode hintNode : nodeList) {
        enableNode(hintNode.getSwiButton());
        enableNode(hintNode.getSendButton());
      }
    }
  }

  private void appendChatMessage(ChatMessage msg, String role) {

    String message = role + ": " + msg.getContent() + "\n\n";

    lastHintArea.appendText(message);
    chatTextArea.appendText(message);
    updateChats();
  }

  private ChatMessage runGpt(ChatMessage msg) {
    chatCompletionRequest.addMessage(msg);
    try {
      // Execute the chat completion request with GPT-3
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());

      // Append the GPT-3 response to the chat interface
      appendChatMessage(result.getChatMessage(), "Dungeon Master");

      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Handles sending a message in the chat interface based on the current application UI state.
   *
   * @param message The message to be sent.
   * @param appUi The current application UI state.
   * @throws ApiProxyException If there is an error with the API proxy.
   * @throws IOException If there is an I/O error.
   */
  public void onSendMessage(String message, AppUi appUi) throws ApiProxyException, IOException {

    HintNode hintNode = nodeMap.get(appUi);

    if (isThinking) {
      return;
    }

    Button sendButton = hintNode.getSendButton();
    Button switchButton = hintNode.getSwiButton();

    // If the message is empty, return early
    if (message.trim().isEmpty()) {
      return;
    }

    disableNode(sendButton);
    disableNode(switchButton);
    lastHintArea.clear();
    // Append the fake message to the chat interface
    appendChatMessage(new ChatMessage("user", message), "Player");

    CompletableFuture.runAsync(
        () -> {
          isThinking = true;

          // Clear the input field and create actual and fake chat messages
          String hint = "";
          if (GameState.currentRoom == GameState.State.CHEST) {
            if (GameState.isKey1Collected
                && GameState.isKey2Collected
                && GameState.isKey3Collected) {
              hint = "\"The answers are hiddne within the riddle. Pay attention to the order.\"";
            } else {
              hint = "\"Search the dungeon for three keys\"";
            }
          } else if (GameState.currentRoom == GameState.State.MARCELLIN) {
            hint = "\"Move the points of the shape such that no lines between points overlap.\"";
          } else if (GameState.currentRoom == GameState.State.ZACH) {
            hint = "\"Investigate the door to find a sliding puzzle\"";
          } else if (GameState.currentRoom == GameState.State.RUSIRU) {
            hint = "\"See if you can craft a potion using the cauldron\"";
          }

          String contextMsg;
          if (GameState.currentDifficulty == GameState.Difficulty.HARD) {

            contextMsg = message + "(If this message is a question, coldly rebuke me.)";
          } else if (GameState.currentDifficulty == GameState.Difficulty.EASY) {
            contextMsg = GptPromptEngineering.hintPrompt(message, hint);
          } else {
            if (GameState.hintsGiven < 5) {
              contextMsg = GptPromptEngineering.hintPrompt(message, hint);
            } else {
              contextMsg = GptPromptEngineering.noHintPrompt(message);
            }
          }

          ChatMessage actualMessage = new ChatMessage("user", contextMsg);

          if (runGpt(actualMessage).getContent().toLowerCase().contains("hint")) {
            GameState.hintsGiven++;
            System.out.println("HINT DETECTED!");
          }
          isThinking = false;
          // Ensure that the UI updates on the JavaFX application thread
          Platform.runLater(
              () -> {
                if (hintNode.getCloseButton().isVisible()) {
                  enableNode(switchButton);
                  enableNode(sendButton);
                }

                enableIfDisable();
                int hintsLeft = 5 - GameState.hintsGiven;
                if (hintsLeft < 0) {
                  hintsLeft = 0;
                }
                for (HintNode node : nodeList) {
                  node.getHintField().setText(hintsLeft + " Hints(s) Remaining");
                }
              });
        });
  }

  /**
   * Updates chat messages on various chat screens. It sets the text on each TextArea based on the
   * current chat display mode, either showing the last hint only or the entire chat history.
   */
  public void updateChats() {

    for (TextArea textArea : variousChatScreens) {
      if (showLastHintOnly) {
        textArea.setText(lastHintArea.getText());
      } else {
        textArea.setText(chatTextArea.getText());
      }
    }
  }

  /** Toggles the display of last hint only. Updates chat messages accordingly. */
  public void lastHintToggle() {
    // Toggle the boolean variable
    showLastHintOnly = !showLastHintOnly;
    // Update the chat messages
    updateChats();
    // Update the text on the switch button
    for (HintNode hintNode : nodeList) {
      if (showLastHintOnly) {
        hintNode.getSwiButton().setText("Expand");
      } else {
        hintNode.getSwiButton().setText("Shrink");
      }
    }
  }

  private void enableNode(Object node) {
    Node actualNode = (Node) node;
    actualNode.setVisible(true);
    actualNode.setDisable(false);
  }

  private void disableNode(Object node) {
    Node actualNode = (Node) node;
    actualNode.setVisible(false);
    actualNode.setDisable(true);
  }

  /**
   * Enables a group of UI components associated with the given AppUi instance. It enables all nodes
   * in the HintNode's nodeList, disables the show button, and enables the hint field.
   *
   * @param appUi The AppUi instance whose associated UI components need to be enabled.
   */
  public void massEnable(AppUi appUi) {
    // Get the corresponding HintNode from the nodeMap using the provided AppUi object.
    HintNode hintNode = nodeMap.get(appUi);

    // Iterate through the NodeList of the retrieved HintNode and enable each node.
    for (Node node : hintNode.getNodeList()) {
      enableNode(node);
    }

    // Disable the 'Show' button associated with the HintNode.
    disableNode(hintNode.getShowButton());

    // Enable the hint field associated with the HintNode.
    enableHintField(hintNode.getHintField());

    // If the system is currently in a 'thinking' state (boolean variable isThinking is true),
    // disable the 'SWI' button and 'Send' button associated with the HintNode.
    if (isThinking) {
      disableNode(hintNode.getSwiButton());
      disableNode(hintNode.getSendButton());
    }
  }

  /**
   * Disables a group of UI components associated with the given AppUi instance. It disables all
   * nodes in the HintNode's nodeList, enabling the show button, and disabling the hint field.
   *
   * @param appUi The AppUi instance whose associated UI components need to be disabled.
   */
  public void massDisable(AppUi appUi) {

    HintNode hintNode = nodeMap.get(appUi);

    for (Node node : hintNode.getNodeList()) {
      disableNode(node);
    }

    enableNode(hintNode.getShowButton());
    disableNode(hintNode.getHintField());
  }

  private void enableHintField(Label hintField) {
    if (GameState.currentDifficulty == GameState.Difficulty.MEDIUM) {
      enableNode(hintField);
    }
  }

  public void addToMap(AppUi appUi, HintNode hintNode) {
    nodeMap.put(appUi, hintNode);
    nodeList.add(hintNode);
  }

  /** Method to disable all nodes in the list of HintNode groups. */
  public void disableAll() {
    for (HintNode group : nodeList) {
      for (Node hint : group.getNodeList()) {
        disableNode(hint);
      }
      enableNode(group.getShowButton());
      disableNode(group.getHintField());
    }
  }
}
