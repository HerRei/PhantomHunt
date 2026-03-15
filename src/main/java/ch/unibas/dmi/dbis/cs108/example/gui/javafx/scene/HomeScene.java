package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene;


import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import javafx.scene.control.TextField;

/**
 * Builds the home screen of the client.
 */
public class HomeScene {

  /**
   * Creates the home scene.
   *
   * @return the home scene
   */
  public Scene createScene(String nickname, Runnable onChangeNickname, Consumer<String> onSendGlobalMessage) {
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

    Button sendGlobalButton = new Button("Send Global Message");
    sendGlobalButton.setMaxWidth(Double.MAX_VALUE);
    sendGlobalButton.setVisible(false);
    sendGlobalButton.setManaged(false);
    Button lobbyChatButton = new Button("Lobby Chat");
    Button whisperButton = new Button("Whisper");
    globalChatButton.setOnAction(event -> { //buttons erst sichtbar bei cklick
      globalChatField.setVisible(true);
      globalChatField.setManaged(true);
      sendGlobalButton.setVisible(true);
      sendGlobalButton.setManaged(true);
      globalChatField.requestFocus();
    });

    sendGlobalButton.setOnAction(event -> { //After click, contorlling not empty, andd trimming
      String message = globalChatField.getText().trim();
      if (!message.isEmpty()) {
        onSendGlobalMessage.accept(message);
        globalChatField.clear();
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
        lobbyChatButton,
        whisperButton
    );
    return new Scene(layout, 1280, 960);
  }
}