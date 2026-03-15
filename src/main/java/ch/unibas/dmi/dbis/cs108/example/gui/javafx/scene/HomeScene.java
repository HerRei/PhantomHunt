package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Builds the home screen of the client.
 */
public class HomeScene {

  /**
   * Creates the home scene.
   *
   * @return the home scene
   */
  public Scene createScene(String nickname) {
    Label titleLabel = new Label("Phantom Hunt - Home");
    titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

    Label infoLabel = new Label("Choose an action:");
    infoLabel.setStyle("-fx-font-size: 16px;");

    Label nameLabel = new Label("Hello player "+nickname);
    nameLabel.setStyle("-fx-font-size: 16px;");


    Button nicknameButton = new Button("Change Nickname");
    Button createLobbyButton = new Button("Create Lobby");
    Button joinLobbyButton = new Button("Join Lobby");
    Button globalChatButton = new Button("Global Chat");
    Button lobbyChatButton = new Button("Lobby Chat");
    Button whisperButton = new Button("Whisper");

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
        lobbyChatButton,
        whisperButton
    );
    return new Scene(layout, 1280, 960);
  }
}