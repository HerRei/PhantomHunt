package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Represents the lobby waiting area, displaying connected players and a lobby chat.
 */
public class LobbyScene implements SceneInterface {

  private Scene scene;
  private VBox root;
  private Label lobbyIdLabel;
  private ListView<String> playerList;
  private Button startGameButton;
  private Button backButton;
  private String id;
  private ListView<String> chatArea;
  private TextField chatInput;

  public LobbyScene() {
    root = new VBox(10);
    root.setAlignment(Pos.CENTER);
    lobbyIdLabel = new Label("Lobby ID: ");
    playerList = new ListView<>();
    startGameButton = new Button("Start Game");
    backButton = new Button("Leave Lobby");
    chatArea = new ListView<>();
    chatInput = new TextField();
    chatInput.setPromptText("Type your message here");

    root.getChildren().addAll(lobbyIdLabel, playerList, startGameButton, backButton, chatArea, chatInput);

    setupEvents();
    bindChat();

    this.scene = new Scene(root, 400, 500);
  }

  private void setupEvents() {
    startGameButton.setOnAction(e -> {
      // Logic to start the game
      EventHandlers.getInstance().sendMessage(Command.START, "");
    });

    backButton.setOnAction(e -> {
      // Logic to leave the lobby
      EventHandlers.getInstance().quitLobby(id);
      SceneManager.getInstance().showScene(SceneProtocol.HOME);
    });

    chatInput.setOnAction(e -> {
      String message = chatInput.getText();
      if (message != null && !message.isEmpty()) {
        EventHandlers.getInstance().sendMessage(Command.YAP, message);
        chatInput.clear();
      }
    });
  }

  private void bindChat() {
    chatArea.setItems(GameModel.getInstance().lobbyChatMessagesProperty());
    GameModel.getInstance().lobbyChatMessagesProperty().addListener((ListChangeListener<String>) c -> {
      Platform.runLater(() -> chatArea.scrollTo(chatArea.getItems().size() - 1));
    });
  }

  /**
   * Updatess the UI with the latest lobby state from the server.
   *
   * @param lobbyId The current lobby ID.
   * @param players Array of player names currently in the lobby.
   */
  public void updateLobbyInfo(String lobbyId, String[] players) {
    id = lobbyId;
    lobbyIdLabel.setText("Lobby ID: " + id);
    playerList.getItems().setAll(players);
    // Show/hide start button based on whether the current player is the host
    String currentPlayerName = GameModel.getInstance().getName().get();
    if (players.length > 0 && players[0].equals(currentPlayerName)) {
      startGameButton.setVisible(true);
    } else {
      startGameButton.setVisible(false);
    }
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
