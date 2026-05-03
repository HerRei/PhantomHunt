package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** The primary hub scene displaying the global chat, profile options, and lobby navigation. */
public class HubScene implements SceneInterface {

  private static final String DARK_BG = "-fx-background-color: #2b2b2b;";
  private static final String PANEL_BG = "-fx-background-color: #313335;";
  private static final String BUTTON_STYLE =
      "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 6;";
  private static final String INPUT_STYLE =
      "-fx-background-color: #3c3f41; -fx-text-fill: white; -fx-prompt-text-fill: #888;";

  private static final String QOTD_HOST = "djxmmx.net";
  private static final int QOTD_PORT = 17;
  private static final String FALLBACK_QUOTE =
      "\"Your mind is like water, my friend. When it is agitated, it becomes difficult to see. "
          + "But if you allow it to settle, the answer becomes clear.\"\n — Master Oogway";

  private ListView<String> chatDisplay;
  private ListView<String> playerListDisplay;
  private TextField chatInput;
  private TextField whisperTargetInput;
  private ComboBox<String> chatMode;
  private Label nicknameLabel;
  private Scene localScene;

  public HubScene() {
    createScene();
  }

  /** Constructs the UI layout for the hub. */
  public void createScene() {
    GameModel model = GameModel.getInstance();

    // ── LEFT PANEL ─────────────────────────────────────────────────────────
    VBox leftMenu = new VBox(12);
    leftMenu.setPadding(new Insets(25));
    leftMenu.setAlignment(Pos.TOP_CENTER);
    leftMenu.setPrefWidth(290);
    leftMenu.setStyle(PANEL_BG);

    // Profile
    Label headLabel = new Label("Logged in as:");
    headLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
    nicknameLabel = new Label();
    nicknameLabel.textProperty().bind(Bindings.concat(model.getName()));
    nicknameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
    VBox profileBox = new VBox(4, headLabel, nicknameLabel);
    profileBox.setAlignment(Pos.CENTER);

    // Nav buttons
    Button btnNickname = navButton("Change Nickname");
    Button btnJoin = navButton("Join Lobby");
    Button btnCreate = navButton("Create Lobby");
    Button btnHighscore = navButton("Show Highscores");
    Button btnKeyBinding = navButton("Key Bindings");
    Button btnWisdom = navButton("Get Wisdom 🔮");

    Label onlineLabel = new Label("Players Online");
    onlineLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px; -fx-font-weight: bold;");

    playerListDisplay = new ListView<>();
    playerListDisplay.setItems(model.players);
    playerListDisplay.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
    VBox.setVgrow(playerListDisplay, Priority.ALWAYS);

    leftMenu
        .getChildren()
        .addAll(
            profileBox,
            new Separator(),
            btnNickname, btnJoin, btnCreate, btnKeyBinding, btnHighscore,
            new Separator(),
            btnWisdom,
            new Separator(),
            onlineLabel, playerListDisplay);

    // ── RIGHT PANEL: Chat ──────────────────────────────────────────────────
    VBox chatBox = new VBox(10);
    chatBox.setPadding(new Insets(20));
    chatBox.setStyle(DARK_BG);
    HBox.setHgrow(chatBox, Priority.ALWAYS);

    Label chatTitle = new Label("Server Chat");
    chatTitle.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

    chatDisplay = new ListView<>();
    chatDisplay.setItems(model.chatMessagesProperty());
    chatDisplay.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
    VBox.setVgrow(chatDisplay, Priority.ALWAYS);

    model
        .chatMessagesProperty()
        .addListener(
            (ListChangeListener<String>)
                c ->
                    Platform.runLater(
                        () -> chatDisplay.scrollTo(chatDisplay.getItems().size() - 1)));

    chatMode = new ComboBox<>();
    chatMode.getItems().addAll("Global", "Whisper");
    chatMode.setValue("Global");
    chatMode.setPrefWidth(110);
    chatMode.setStyle(INPUT_STYLE);
    chatMode.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        setStyle("-fx-text-fill: white;");
      }
    });

    whisperTargetInput = new TextField();
    whisperTargetInput.setPromptText("To whom?");
    whisperTargetInput.setPrefWidth(110);
    whisperTargetInput.setStyle(INPUT_STYLE);
    whisperTargetInput
        .visibleProperty()
        .bind(Bindings.equal("Whisper", chatMode.valueProperty()));
    whisperTargetInput.managedProperty().bind(whisperTargetInput.visibleProperty());

    chatInput = new TextField();
    chatInput.setPromptText("Type a message...");
    chatInput.setStyle(INPUT_STYLE);
    HBox.setHgrow(chatInput, Priority.ALWAYS);

    Button btnSend = new Button("Send");
    btnSend.setStyle(BUTTON_STYLE);
    btnSend.setPrefWidth(80);

    HBox inputArea = new HBox(8, chatMode, whisperTargetInput, chatInput, btnSend);
    inputArea.setAlignment(Pos.CENTER_LEFT);

    chatBox.getChildren().addAll(chatTitle, chatDisplay, inputArea);

    // ── Events ─────────────────────────────────────────────────────────────
    btnSend.setOnAction(e -> handleSendMessage());
    chatInput.setOnAction(e -> handleSendMessage());
    btnNickname.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.NICKNAME));
    btnJoin.setOnAction(
        e -> {
          EventHandlers.getInstance().updateLists();
          SceneManager.getInstance().showScene(SceneProtocol.JOINLOBBY);
        });
    btnCreate.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.CREATELOBBY));
    btnKeyBinding.setOnAction(
        e -> SceneManager.getInstance().showScene(SceneProtocol.KEY_BINDING));
    btnHighscore.setOnAction(
        e -> {
          SceneManager.getInstance().showScene(SceneProtocol.HIGHSCORE);
          EventHandlers.getInstance().updateHighscore();
        });
    btnWisdom.setOnAction(e -> openWisdom(SceneProtocol.HOME));

    // ── Main layout ────────────────────────────────────────────────────────
    HBox mainLayout = new HBox(
        leftMenu, new Separator(javafx.geometry.Orientation.VERTICAL), chatBox);

    localScene = new Scene(mainLayout, 900, 640);

    // F11 toggles fullscreen
    localScene.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.F11) {
            javafx.stage.Stage stage = (javafx.stage.Stage) localScene.getWindow();
            if (stage != null) stage.setFullScreen(!stage.isFullScreen());
          }
        });
  }

  private Button navButton(String text) {
    Button b = new Button(text);
    b.setMaxWidth(Double.MAX_VALUE);
    b.setPrefHeight(36);
    b.setStyle(
        "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 13px; "
            + "-fx-font-weight: bold; -fx-background-radius: 6;");
    return b;
  }

  private void handleSendMessage() {
    String message = chatInput.getText().trim();
    String mode = chatMode.getValue();
    if (message.isEmpty()) return;

    if ("Global".equals(mode)) {
      EventHandlers.getInstance().sendMessage(Command.UNICOM, message);
      chatInput.clear();
    } else {
      String target = whisperTargetInput.getText().trim();
      if (target.isEmpty()) {
        GameModel.getInstance().addChatMessage("SYSTEM: You need to enter a target");
        return;
      }
      EventHandlers.getInstance().sendMessage(Command.WHISPER, target, message);
      chatInput.clear();
    }
  }

  private void openWisdom(SceneProtocol returnScene) {
    WisdomScene wisdomScene = (WisdomScene) SceneManager.getInstance().getScene(SceneProtocol.WISDOM);
    if (wisdomScene != null) {
      wisdomScene.openFrom(returnScene);
      SceneManager.getInstance().showScene(SceneProtocol.WISDOM);
    }
  }

  @Override
  public Scene getScene() {
    return localScene;
  }
}
