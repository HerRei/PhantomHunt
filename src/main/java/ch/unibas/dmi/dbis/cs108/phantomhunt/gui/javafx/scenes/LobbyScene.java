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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

/** Represents the lobby waiting area, displaying connected players and a lobby chat. */
public class LobbyScene implements SceneInterface {

  private static final String DARK_BG =
      "-fx-background-color: #2b2b2b;";
  private static final String BUTTON_STYLE =
      "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-font-weight: bold; -fx-padding: 8 22; -fx-background-radius: 6;";
  private static final String INPUT_STYLE =
      "-fx-background-color: #3c3f41; -fx-text-fill: white; -fx-prompt-text-fill: #888;";

  private Scene scene;
  private Label lobbyIdLabel;
  private ListView<String> playerList;
  private Button startGameButton;
  private Button backButton;
  private String id;
  private ListView<String> chatArea;
  private TextField chatInput;
  private Label gameSettingsLabel;
  private TextField gameSettingsInput;

  /** Constructs a new LobbyScene. Initializes UI components, binds the chat to the game model. */
  public LobbyScene() {
    // ── Player list (left panel) ────────────────────────────────────────────
    lobbyIdLabel = new Label("Lobby ID: —");
    lobbyIdLabel.setStyle(
        "-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold;");

    Label playersTitle = new Label("Players in Lobby");
    playersTitle.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-weight: bold;");

    playerList = new ListView<>();
    playerList.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
    VBox.setVgrow(playerList, Priority.ALWAYS);

    startGameButton = new Button("▶  Start Game");
    startGameButton.setStyle(
        "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 14px; "
            + "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 6;");
    startGameButton.setMaxWidth(Double.MAX_VALUE);

    gameSettingsLabel = new Label("Game Settings");
    gameSettingsLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-weight: bold;");

    gameSettingsInput = new TextField(GameRules.defaultRules().toPayload());
    gameSettingsInput.setPromptText("rounds duration radius speed humanScore humanWin phantomCatch humanCatch abilities phantomWin");
    gameSettingsInput.setStyle(INPUT_STYLE);
    gameSettingsInput.setMaxWidth(Double.MAX_VALUE);

    backButton = new Button("Leave Lobby");
    backButton.setStyle(BUTTON_STYLE);
    backButton.setMaxWidth(Double.MAX_VALUE);

    VBox leftPanel =
        new VBox(12, lobbyIdLabel, new Separator(), playersTitle, playerList,
            gameSettingsLabel, gameSettingsInput, startGameButton, backButton);
    leftPanel.setPadding(new Insets(25));
    leftPanel.setAlignment(Pos.TOP_CENTER);
    leftPanel.setPrefWidth(300);
    leftPanel.setStyle("-fx-background-color: #313335;");

    // ── Chat (right panel) ─────────────────────────────────────────────────
    Label chatTitle = new Label("Lobby Chat");
    chatTitle.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

    chatArea = new ListView<>();
    chatArea.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    chatInput = new TextField();
    chatInput.setPromptText("Type your message here...");
    chatInput.setStyle(INPUT_STYLE);

    VBox chatPanel = new VBox(12, chatTitle, chatArea, chatInput);
    chatPanel.setPadding(new Insets(25));
    chatPanel.setStyle(DARK_BG);
    HBox.setHgrow(chatPanel, Priority.ALWAYS);

    // ── Main layout ─────────────────────────────────────────────────────────
    HBox root =
        new HBox(leftPanel, new Separator(javafx.geometry.Orientation.VERTICAL), chatPanel);

    this.scene = new Scene(root, 900, 640);

    setupEvents();
    bindChat();

    // F11 toggles fullscreen
    scene.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.F11) {
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
            if (stage != null) stage.setFullScreen(!stage.isFullScreen());
          }
        });
  }

  private void setupEvents() {
    startGameButton.setOnAction(
        e -> {
          EventHandlers.getInstance().sendMessage(Command.GAME_SETTINGS, gameSettingsInput.getText().trim());
          EventHandlers.getInstance().sendMessage(Command.START);
        });

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
    gameSettingsLabel.setVisible(isHost);
    gameSettingsLabel.setManaged(isHost);
    gameSettingsInput.setVisible(isHost);
    gameSettingsInput.setManaged(isHost);
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
