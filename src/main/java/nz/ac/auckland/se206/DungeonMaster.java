package nz.ac.auckland.se206;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/**
 * The DungeonMaster class represents the character in the game who provides guidance and challenges
 * to the player. It handles dialogues, hints, and interactions with the player.
 */
public class DungeonMaster {
  private static boolean commentStrength = false;

  /**
   * Creates and returns a string for context to be sent back to the dungeon master with a question.
   *
   * @return returns the context for the dungeon master to use when generating a response.
   */
  public static String getDungeonMasterComment() {
    // comment on the players progress within the rooms of the dungeon so far
    String currentRoom = "";
    switch (GameState.currentRoom) {
      case MARCELLIN:
        currentRoom = "untangle room";
        break;
      case RUSIRU:
        currentRoom = "potion room and increased their strength";
        break;
      case ZACH:
        currentRoom = "sliding puzzle room";
        break;
      case CHEST:
        currentRoom = "chest";
        break;
      default:
        break;
    }

    // gets a string of the current key status
    String keyStatus = "The player needs to get the keys to the following rooms ";

    // if they have already finished everything
    if (GameState.isChestOpened) {
      keyStatus =
          "The player has all the keys to the rooms and needs to get the sword and shield to defeat"
              + " the dungeon master. ";
    }

    // individual key status messages
    if (!GameState.isKey1Collected) {
      keyStatus += "potion room and key 1, ";
    }
    if (!GameState.isKey2Collected) {
      keyStatus += "untangle room and key 2, ";
    }
    if (!GameState.isKey3Collected) {
      keyStatus += "sliding puzzle room and key 3, ";
    }

    if (GameState.isKey1Collected && GameState.isKey2Collected && GameState.isKey3Collected) {
      keyStatus =
          "The player has all the keys to the rooms and needs to get the sword and shield"
              + " to defeat the dungeon master. ";
    }

    // full context string to be returned
    String context =
        "You are the master of a dungeon which I the player am trying to escape from. Comment on"
            + " the players progress within the dungeon so far. The player needs to go to the"
            + " following rooms "
            + keyStatus
            + ". The player has just completed the "
            + currentRoom
            + ". Do not go over 50 words.";

    System.out.println(context);

    return context;
  }

  /**
   * Creates and returns a string for the dungeon master to use when generating a response.
   *
   * @return returns the context for the dungeon master to use when generating a response.
   */
  public static String getDungeonMasterResponse() {
    // depending on the state of the game modify the message sent to the dungeon master
    String response =
        "You are the master of a dungeon which I the player am trying to escape from. If the player"
            + " has all three keys then they should go to the chest and solve the final puzzle. ";

    // if the player has not collected any keys yet
    if (!GameState.isKey1Collected && !GameState.isKey2Collected && !GameState.isKey3Collected) {
      response += "The player has not collected any keys yet. ";
    } else {
      // if the player has not collected all the keys yet
      if (!GameState.isKey1Collected) {
        response += "The player needs to get key 1 from the potion room. ";
      }
      if (!GameState.isKey2Collected) {
        response += "The player needs to get key 2 from the untangle room. ";
      }
      if (!GameState.isKey3Collected) {
        response += "The player needs to get key 3 from the sliding puzzle room. ";
      }
    }

    // if the player has crafted the potion and got the key
    if (GameState.isKey1Collected) {
      if (!commentStrength) {
        response += "Comment on the increased strength of the player from the potion they crafted";
        commentStrength = true;
      }
    }

    // if the player has collected all the keys
    if (GameState.isKey1Collected && GameState.isKey2Collected && GameState.isKey3Collected) {
      response +=
          "The player has all the keys to the rooms and needs to get the sword and shield"
              + " to defeat the dungeon master. ";
    }

    // if the player has opened the chest
    if (GameState.isChestOpened) {
      response =
          "You are the master of a dungeon which the player is trying to escape from. The player"
              + " has solved all the puzzles in the dungeon and is preparing to fight you. Goad the"
              + " player into fighting you. ";
    }

    // to ensure that the response is not too long
    response += "Do not go over 50 words.";

    System.out.println(response);

    return response;
  }

  private boolean isSpeaking = false;
  private boolean messageFinished = false;

  private int messageIndex = 0;

  private String message;
  private String[] messages;

  private Pane popUp;

  private ChatCompletionRequest chatCompletionRequest =
      new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(400);

  /**
   * Creates a pop-up dialog with the Dungeon Master's image, name, and dialogue.
   *
   * @param popUp The pane representing the pop-up dialog.
   */
  public void createPopUp(Pane popUp) {
    System.out.println("creating pop up");
    this.popUp = popUp;
    HBox popUpBox = new HBox();

    // DUNGEON MASTER IMAGE
    StackPane dungeonMasterStack = new StackPane();
    Rectangle dungeonMasterRectangle = new Rectangle();
    dungeonMasterRectangle.setWidth(60);
    dungeonMasterRectangle.setHeight(60);
    dungeonMasterRectangle.setStyle("-fx-fill: #ffffff");
    dungeonMasterRectangle.setOpacity(0.25);
    ImageView dungeonMasterImage = new ImageView("images/dungeonMasterImageCrop.png");
    dungeonMasterImage.scaleXProperty().set(-1);
    dungeonMasterImage.setFitHeight(60);
    dungeonMasterImage.setFitWidth(60);

    // sets the thought bubble
    ImageView dungeonMasterThought = new ImageView("images/thought.png");
    dungeonMasterThought.setScaleX(-1);
    dungeonMasterThought.setFitHeight(30);
    dungeonMasterThought.setFitWidth(30);
    dungeonMasterThought.setTranslateY(50);
    dungeonMasterThought.setTranslateX(35);
    TranslateTransition transition = GameState.translate(dungeonMasterThought);
    transition.play();

    dungeonMasterStack
        .getChildren()
        .addAll(dungeonMasterRectangle, dungeonMasterImage, dungeonMasterThought);

    // DIALOG BOX
    VBox dialogueBox = new VBox();

    // NAME
    Label name = new Label("Dungeon Master");

    // DIALOG
    Text dialogue = new Text("...");
    dialogueBox.getChildren().addAll(name);
    dialogueBox.getChildren().addAll(dialogue);
    dialogue.setWrappingWidth(430);

    // DIALOG NEXT BUTTON
    ImageView nextButton = new ImageView("images/down.png");
    nextButton.setFitHeight(20);
    nextButton.setFitWidth(20);
    nextButton.visibleProperty().set(false);

    TranslateTransition translateTransition = new TranslateTransition();
    translateTransition.setDuration(Duration.millis(500));
    translateTransition.setNode(nextButton);
    translateTransition.setToY(5);
    translateTransition.setAutoReverse(true);
    translateTransition.setCycleCount(Animation.INDEFINITE);
    translateTransition.play();

    ImageView quitButton = new ImageView();
    quitButton.setImage(new Image("images/close.png"));
    quitButton.setFitHeight(20);
    quitButton.setFitWidth(20);
    quitButton.setStyle("-fx-fill: #f38ba8");
    quitButton.setOnMouseClicked(
        e -> {
          GameState.tts.terminate();
          popUp.visibleProperty().set(false);
          System.out.println("quit");
        });

    StackPane dialogueContainer = new StackPane();
    dialogueContainer.getChildren().addAll(dialogueBox, nextButton, quitButton);
    nextButton.setTranslateY(25);
    nextButton.setTranslateX(220);
    quitButton.setTranslateY(-20);
    quitButton.setTranslateX(220);

    // ADDING TO POP UP
    popUpBox.getChildren().addAll(dungeonMasterStack);
    popUpBox.getChildren().addAll(dialogueContainer);
    popUp.getChildren().add(popUpBox);
    paneFormat(popUp);
  }

  /**
   * Retrieves a response from the GPT model based on the given role and message.
   *
   * @param role The role of the message sender (e.g., "user", "assistant").
   * @param message The message sent to the GPT model for generating a response.
   */
  public void getText(String role, String message) {
    Music.playThink();
    System.out.println("getting text");
    messages = null;
    messageIndex = 0;
    // create a chat message
    ChatMessage chatMessage = new ChatMessage(role, message);
    chatCompletionRequest.addMessage(chatMessage);
    // create a task to get the response
    Task<Void> gptTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              // get the response
              ChatCompletionResult chatCompetionResult = chatCompletionRequest.execute();
              Choice result = chatCompetionResult.getChoices().iterator().next();
              // append the response to the chat
              chatCompletionRequest.addMessage(result.getChatMessage());
              // get the message for the popup
              appendChatMessage(result.getChatMessage());
              return null;
            } catch (ApiProxyException e) {
              e.printStackTrace();
              return null;
            }
          }
        };

    gptTask.setOnSucceeded(
        e -> {
          System.out.println("gpt task succeeded");
          nextMessage();
        });
    gptTask.setOnFailed(
        e -> {
          System.out.println("gpt task failed");
        });
    gptTask.setOnCancelled(
        e -> {
          System.out.println("gpt task cancelled");
        });

    System.out.print(messages);
    Thread thread = new Thread(gptTask);
    thread.setDaemon(true);
    thread.start();
  }

  // returns the riddle
  public String getRiddle() {
    return message;
  }

  public void appendChatMessage(ChatMessage msg) {
    message = msg.getContent();
    messages = message.split("(?<=[a-z])\\.\\s+");
  }

  /**
   * Proceeds to the next message in the dialogue with the Dungeon Master, updating the pop-up
   * dialog. If there are more messages, it displays the next message; if not, it hides the "Next"
   * button.
   */
  public void nextMessage() {
    System.out.println("next message");
    // ensures thoughtbubble doesnt popup after the first message
    ImageView thoughtBubble =
        (ImageView)
            ((StackPane) ((HBox) popUp.getChildren().get(0)).getChildren().get(0))
                .getChildren()
                .get(2);
    thoughtBubble.visibleProperty().set(false);
    // popup -> hbox -> dialog container -> dialog box -> text
    Text dialogue =
        (Text)
            ((VBox)
                    ((StackPane) ((HBox) popUp.getChildren().get(0)).getChildren().get(1))
                        .getChildren()
                        .get(0))
                .getChildren()
                .get(1);
    // popup -> hbox -> dialog container -> next button
    ImageView nextButton =
        (ImageView)
            ((StackPane) ((HBox) popUp.getChildren().get(0)).getChildren().get(1))
                .getChildren()
                .get(1);
    System.out.println("mss " + messages.length + " " + messageIndex);
    isSpeaking = true;
    // if there are more messages
    if (messageIndex < messages.length) {
      if (messages[messageIndex] == "") {
        messageIndex++;
      }
      // if there are more messages
      System.out.println("next message: " + messages[messageIndex]);
      nextButton.visibleProperty().set(false);
      dialogue.setText(messages[messageIndex]);
      // create a task to speak the message
      Task<Void> speakTask =
          new Task<Void>() {
            @Override
            protected Void call() {
              GameState.tts.speak(messages[messageIndex]);
              return null;
            }
          };
      // when the task is done
      speakTask.setOnSucceeded(
          e -> {
            System.out.println("speak task succeeded");
            isSpeaking = false;
            if (messageIndex < messages.length - 1) {
              nextButton.visibleProperty().set(true);
            }
          });

      Thread thread = new Thread(speakTask);
      thread.setDaemon(true);
      thread.start();
    }
  }

  /**
   * Formats the dialogue pane and attaches event handlers to its elements.
   *
   * @param dialogue The dialogue pane to be formatted.
   */
  public void paneFormat(Pane dialogue) {
    // Retrieve and configure UI elements within the dialogue pane
    ImageView exitButton =
        (ImageView)
            ((StackPane) ((HBox) dialogue.getChildren().get(0)).getChildren().get(1))
                .getChildren()
                .get(2);
    Text dialogueText =
        (Text)
            ((VBox)
                    ((StackPane) ((HBox) dialogue.getChildren().get(0)).getChildren().get(1))
                        .getChildren()
                        .get(0))
                .getChildren()
                .get(1);
    ImageView nextButton =
        (ImageView)
            ((StackPane) ((HBox) dialogue.getChildren().get(0)).getChildren().get(1))
                .getChildren()
                .get(1);

    // Attach an event handler to the exit button to close the dialogue
    exitButton.setOnMouseClicked(
        event1 -> {
          GameState.tts.cancel();
          popUp.getChildren().clear();
          popUp.visibleProperty().set(false);
          popUp.mouseTransparentProperty().set(true);
          messageFinished = true;
        });

    // Attach an event handler to the dialogue text to allow advancing the dialogue
    dialogueText.setOnMouseClicked(
        event1 -> {
          if (!isSpeaking()) {
            messageIndex++;
            nextMessage();
          }
        });

    // Attach an event handler to the next button to allow advancing the dialogue
    nextButton.setOnMouseClicked(
        event1 -> {
          if (!isSpeaking()) {
            messageIndex++;
            nextMessage();
          }
        });
  }

  public boolean isSpeaking() {
    return isSpeaking;
  }

  public String[] getMessages() {
    return messages;
  }

  public Pane getPopUp() {
    return popUp;
  }

  public boolean isMessageFinished() {
    return messageFinished;
  }
}
