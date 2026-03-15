package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import java.util.function.BiConsumer;

/**
 * Builds the home screen of the client.
 */
public class HomeScene {

  private TextArea globalChatArea;
  private TextArea whisperArea;

  /**
   * Creates the home scene.
   *
   * @return the home scene
   */
  public Scene createScene(
      String nickname,
      Runnable onChangeNickname,
      Consumer<String> onSendGlobalMessage,
      BiConsumer<String, String> onSendWisper //1. Reciver name, 2. wisper message
  ) {
    Label titleLabel = new Label("Phantom Hunt - Home");
    titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

    Label infoLabel = new Label("Choose an action:");
    infoLabel.setStyle("-fx-font-size: 16px;");


    Label nameLabel = new Label("Hello player "+ nickname);
    nameLabel.setStyle("-fx-font-size: 16px;");


    Button nicknameButton = new Button("Change Nickname");
    nicknameButton.setOnAction(event -> onChangeNickname.run());
    Button createLobbyButton = new Button("Create Lobby");
    Button joinLobbyButton = new Button("Join Lobby");
    Button globalChatButton = new Button("Global Chat");
    TextField globalChatField = new TextField();
    globalChatField.setPromptText("Enter global message here");
    globalChatField.setMaxWidth(Double.MAX_VALUE);
    globalChatField.setVisible(false);
    globalChatField.setManaged(false);

    /**
     * Starts Layout for Global Chat button
     */

    Button sendGlobalButton = new Button("Send Global Message");
    sendGlobalButton.setMaxWidth(Double.MAX_VALUE);
    sendGlobalButton.setVisible(false);
    sendGlobalButton.setManaged(false);
    globalChatArea = new TextArea();
    globalChatArea.setEditable(false);
    globalChatArea.setWrapText(true);
    globalChatArea.setPrefRowCount(10);
    globalChatArea.setVisible(false);
    globalChatArea.setManaged(false);


    Button lobbyChatButton = new Button("Lobby Chat");

    /**
     * Starts Layout for Wisper function
     */
    Button whisperButton = new Button("Whisper");
    TextField whisperTargetField = new TextField();
    whisperTargetField.setPromptText("Enter target nickname");
    whisperTargetField.setMaxWidth(Double.MAX_VALUE);
    whisperTargetField.setVisible(false);
    whisperTargetField.setManaged(false);

    TextField whisperMessageField = new TextField();
    whisperMessageField.setPromptText("Enter whisper message here");
    whisperMessageField.setMaxWidth(Double.MAX_VALUE);
    whisperMessageField.setVisible(false);
    whisperMessageField.setManaged(false);

    Button sendWhisperButton = new Button("Send Whisper");
    sendWhisperButton.setMaxWidth(Double.MAX_VALUE);
    sendWhisperButton.setVisible(false);
    sendWhisperButton.setManaged(false);

    whisperArea = new TextArea();
    whisperArea.setEditable(false);
    whisperArea.setWrapText(true);
    whisperArea.setPrefRowCount(8);
    whisperArea.setVisible(false);
    whisperArea.setManaged(false);

    //Effect for click on globalchat button
    globalChatButton.setOnAction(event -> { //buttons erst sichtbar bei cklick
      globalChatField.setVisible(true);
      globalChatField.setManaged(true);
      sendGlobalButton.setVisible(true);
      sendGlobalButton.setManaged(true);
      globalChatArea.setVisible(true);
      globalChatArea.setManaged(true);
      globalChatField.requestFocus();
    });

    //Effect for cklick on wisper Button
    whisperButton.setOnAction(event -> {
      whisperTargetField.setVisible(true);
      whisperTargetField.setManaged(true);
      whisperMessageField.setVisible(true);
      whisperMessageField.setManaged(true);
      sendWhisperButton.setVisible(true);
      sendWhisperButton.setManaged(true);
      whisperArea.setVisible(true);
      whisperArea.setManaged(true);
      whisperTargetField.requestFocus();
    });

    // kontrolle und sernder und nachricht lesen
    sendGlobalButton.setOnAction(event -> { //After click, contorlling not empty, andd trimming
      String message = globalChatField.getText().trim();
      if (!message.isEmpty()) {
        System.out.println("Homescreen: sending gloobal Message: "+message); //Debugging
        onSendGlobalMessage.accept(message);
        globalChatField.clear();
      }
    });


    //Kontrolle, und sender und nachricht lesen
    sendWhisperButton.setOnAction(event -> {
      String target = whisperTargetField.getText().trim();
      String message = whisperMessageField.getText().trim();

      if (!target.isEmpty() && !message.isEmpty()) {
        onSendWisper.accept(target, message);
        whisperMessageField.clear();
      }
    });


    nicknameButton.setMaxWidth(Double.MAX_VALUE);
    createLobbyButton.setMaxWidth(Double.MAX_VALUE);
    joinLobbyButton.setMaxWidth(Double.MAX_VALUE);
    globalChatButton.setMaxWidth(Double.MAX_VALUE);
    lobbyChatButton.setMaxWidth(Double.MAX_VALUE);
    whisperButton.setMaxWidth(Double.MAX_VALUE);

    VBox layout = new VBox();
    layout.setAlignment(Pos.CENTER);
    layout.setSpacing(15);
    layout.setPadding(new Insets(30));
    layout.getChildren().addAll(
        titleLabel,
        infoLabel,
        nameLabel,
        nicknameButton,
        createLobbyButton,
        joinLobbyButton,
        globalChatButton,
        globalChatField,
        sendGlobalButton,
        globalChatArea,
        lobbyChatButton,
        whisperButton,
        whisperTargetField,
        whisperMessageField,
        sendWhisperButton,
        whisperArea
    );
    return new Scene(layout, 1280, 960);
  }

  public void appendGlobalMessage(String message) { //for formating and displaying of chat
    if (globalChatArea == null || message == null || message.isBlank()) {
      return;
    }

    if (!globalChatArea.getText().isEmpty()) {
      globalChatArea.appendText("\n");
    }

    globalChatArea.appendText(message);
  }

  public void appendWhisperMessage(String message) {
    if (whisperArea == null || message == null || message.isBlank()) {
      return;
    }

    if (!whisperArea.getText().isEmpty()) {
      whisperArea.appendText("\n");
    }

    whisperArea.appendText(message);
  }
}