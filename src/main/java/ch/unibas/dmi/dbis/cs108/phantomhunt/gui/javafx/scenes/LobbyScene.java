package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameRules;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Represents the lobby waiting area, displaying connected players and a lobby chat. */
public class LobbyScene implements SceneInterface {

  private Scene scene;
  private Label lobbyIdLabel;
  private ListView<String> playerList;
  private Button startGameButton;
  private Button gameSettingsButton;
  private Button backButton;
  private String id;
  private ListView<String> chatArea;
  private TextField chatInput;

  /** Constructs a new LobbyScene. Initializes UI components, binds the chat to the game model. */
  public LobbyScene() {
    lobbyIdLabel = new Label("Lobby ID: -");
    lobbyIdLabel.setStyle(SceneStyle.GOLD_LABEL);

    Label playersTitle = new Label("Players in Lobby");
    playersTitle.setStyle(SceneStyle.SECTION_LABEL);

    playerList = new ListView<>();
    playerList.setStyle(SceneStyle.LIST);
    VBox.setVgrow(playerList, Priority.ALWAYS);

    startGameButton = new Button("Start Game");
    startGameButton.setStyle(SceneStyle.BUTTON_PRIMARY);
    startGameButton.setMaxWidth(Double.MAX_VALUE);

    gameSettingsButton = new Button("Game Settings");
    gameSettingsButton.setStyle(SceneStyle.BUTTON);
    gameSettingsButton.setMaxWidth(Double.MAX_VALUE);

    backButton = new Button("Leave Lobby");
    backButton.setStyle(SceneStyle.BUTTON);
    backButton.setMaxWidth(Double.MAX_VALUE);

    VBox leftPanel =
        new VBox(12, lobbyIdLabel, new Separator(), playersTitle, playerList,
            gameSettingsButton, startGameButton, backButton);
    leftPanel.setPadding(new Insets(25));
    leftPanel.setAlignment(Pos.TOP_CENTER);
    leftPanel.setPrefWidth(300);
    leftPanel.setStyle(SceneStyle.PANEL_BACKGROUND);

    Label chatTitle = new Label("Lobby Chat");
    chatTitle.setStyle(SceneStyle.PANEL_TITLE);

    chatArea = new ListView<>();
    chatArea.setStyle(SceneStyle.LIST);
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    chatInput = new TextField();
    chatInput.setPromptText("Type your message here...");
    chatInput.setStyle(SceneStyle.INPUT);

    VBox chatPanel = new VBox(12, chatTitle, chatArea, chatInput);
    chatPanel.setPadding(new Insets(25));
    chatPanel.setStyle(SceneStyle.DARK_BACKGROUND);
    HBox.setHgrow(chatPanel, Priority.ALWAYS);

    HBox root =
        new HBox(leftPanel, new Separator(javafx.geometry.Orientation.VERTICAL), chatPanel);

    SceneManager sceneManager = SceneManager.getInstance();
    this.scene = new Scene(root, sceneManager.getWidth(), sceneManager.getHeight());

    setupEvents();
    bindChat();
  }

  private void setupEvents() {
    startGameButton.setOnAction(
        e -> {
          EventHandlers.getInstance().sendMessage(Command.GAME_SETTINGS, getGameSettingsPayload());
          EventHandlers.getInstance().sendMessage(Command.START);
        });

    gameSettingsButton.setOnAction(
        e -> SceneManager.getInstance().showScene(SceneProtocol.GAME_SETTINGS));

    backButton.setOnAction(
        e -> {
          EventHandlers.getInstance().quitLobby(id);
          SceneManager.getInstance().showScene(SceneProtocol.HOME);
        });

    chatInput.setOnAction(
        e -> {
          String message = chatInput.getText();
          if (message != null && !message.isEmpty()) {
            EventHandlers.getInstance().sendMessage(Command.YAP, message);
            chatInput.clear();
          }
        });
  }

  private void bindChat() {
    chatArea.setItems(GameModel.getInstance().lobbyChatMessagesProperty());
    GameModel.getInstance()
        .lobbyChatMessagesProperty()
        .addListener(
            (ListChangeListener<String>)
                c ->
                    Platform.runLater(
                        () -> chatArea.scrollTo(chatArea.getItems().size() - 1)));
  }

  /**
   * Updates the UI with the latest lobby state from the server.
   *
   * @param lobbyId The current lobby ID.
   * @param players Array of player names currently in the lobby.
   */
  public void updateLobbyInfo(String lobbyId, String[] players) {
    id = lobbyId;
    lobbyIdLabel.setText("Lobby ID: " + id);
    playerList.getItems().setAll(players);
    String currentPlayerName = GameModel.getInstance().getName().get();
    boolean isHost = players.length > 0 && players[0].equals(currentPlayerName);
    startGameButton.setVisible(isHost);
    startGameButton.setManaged(isHost);
    gameSettingsButton.setVisible(isHost);
    gameSettingsButton.setManaged(isHost);
  }

  private String getGameSettingsPayload() {
    SceneInterface settingsScene = SceneManager.getInstance().getScene(SceneProtocol.GAME_SETTINGS);
    if (settingsScene instanceof GameSettingsScene gameSettingsScene) {
      return gameSettingsScene.getSettingsPayload();
    }
    return GameRules.defaultRules().toPayload();
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
