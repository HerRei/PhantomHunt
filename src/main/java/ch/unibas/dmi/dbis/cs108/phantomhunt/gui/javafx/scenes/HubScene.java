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
import javafx.scene.layout.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** The primary hub scene displaying the global chat, profile options, and lobby navigation. */
public class HubScene implements SceneInterface {

  private ListView<String> chatDisplay;
  private ListView<String> playerListDisplay; // New: Display for all players
  private TextField chatInput;
  private TextField whisperTargetInput;
  private ComboBox<String> chatMode;
  private Label nicknameLabel;
  private Label wisdomLabel;
  private Scene localScene;

  private static final String QOTD_HOST = "djxmmx.net";
  private static final int QOTD_PORT = 17;
  private static final String FALLBACK_QUOTE =
      "\"Your mind is like water, my friend. When it is agitated, it becomes difficult to see. But if you allow it to settle, the answer becomes clear.\" \n - Master Oogway";

  public HubScene() {
    createScene();
  }

  /** Constructs the UI layout for the hub. */
  public void createScene() {
    GameModel model = GameModel.getInstance();

    // --- LEFT SIDE: Navigation, Profile & Player List ---
    VBox leftMenu = new VBox(15); // Slightly tighter spacing
    leftMenu.setPadding(new Insets(25));
    leftMenu.setAlignment(Pos.TOP_CENTER);
    leftMenu.setPrefWidth(350);

    // Profile Section
    VBox profileBox = new VBox(8);
    profileBox.setAlignment(Pos.CENTER);
    Label headLabel = new Label("Logged in as:");
    headLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

    nicknameLabel = new Label();
    nicknameLabel.textProperty().bind(Bindings.concat(model.getName()));
    nicknameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    profileBox.getChildren().addAll(headLabel, nicknameLabel);

    // Buttons
    Button btnNickname = new Button("Change Nickname");
    Button btnJoin = new Button("Join Lobby");
    Button btnCreate = new Button("Create Lobby");
    Button btnHighscore = new Button("Show Highscores");
    Button btnKeyBinding = new Button("Key Binding");
    Button btnWisdom = new Button("Get Wisdom");

    btnJoin.setMaxWidth(Double.MAX_VALUE);
    btnNickname.setMaxWidth(Double.MAX_VALUE);
    btnKeyBinding.setMaxWidth(Double.MAX_VALUE);
    btnCreate.setMaxWidth(Double.MAX_VALUE);
    btnHighscore.setMaxWidth(Double.MAX_VALUE);
    btnWisdom.setMaxWidth(Double.MAX_VALUE);
    btnJoin.setPrefHeight(40);
    btnCreate.setPrefHeight(40);
    btnKeyBinding.setPrefHeight(40);
    btnHighscore.setPrefHeight(40);
    btnWisdom.setPrefHeight(40);

    wisdomLabel = new Label(FALLBACK_QUOTE);
    wisdomLabel.setWrapText(true);
    wisdomLabel.setMaxWidth(Double.MAX_VALUE);

    // Player List Section (replacing Volume)
    Label onlineLabel = new Label("Players Online:");
    onlineLabel.setStyle("-fx-font-weight: bold;");

    playerListDisplay = new ListView<>();
    // BINDING: Connect to the observable list in GameModel
    playerListDisplay.setItems(model.players);
    VBox.setVgrow(playerListDisplay, Priority.ALWAYS); // Let the list take up remaining space

    leftMenu
        .getChildren()
        .addAll(
            profileBox,
            btnNickname,
            new Separator(),
            btnJoin,
            btnCreate,
            btnKeyBinding,
            btnHighscore,
            btnWisdom,
            wisdomLabel,
            new Separator(),
            onlineLabel,
            playerListDisplay);

    // --- RIGHT SIDE: Chat System ---
    VBox chatBox = new VBox(10);
    chatBox.setPadding(new Insets(15));
    HBox.setHgrow(chatBox, Priority.ALWAYS);

    chatDisplay = new ListView<>();
    chatDisplay.setItems(model.chatMessagesProperty());
    VBox.setVgrow(chatDisplay, Priority.ALWAYS);

    model
        .chatMessagesProperty()
        .addListener(
            (ListChangeListener<String>)
                c -> {
                  Platform.runLater(() -> chatDisplay.scrollTo(chatDisplay.getItems().size() - 1));
                });

    HBox inputArea = new HBox(8);
    chatInput = new TextField();
    chatInput.setPromptText("Type a message...");
    HBox.setHgrow(chatInput, Priority.ALWAYS);

    chatMode = new ComboBox<>();
    chatMode.getItems().addAll("Global", "Whisper");
    chatMode.setValue("Global");
    chatMode.setPrefWidth(100);

    whisperTargetInput = new TextField();
    whisperTargetInput.setPromptText("To whom?");
    whisperTargetInput.setPrefWidth(100);
    whisperTargetInput.visibleProperty().bind(Bindings.equal("Whisper", chatMode.valueProperty()));
    whisperTargetInput.managedProperty().bind(whisperTargetInput.visibleProperty());

    Button btnSend = new Button("Send");
    btnSend.setPrefWidth(80);

    // --- Actions ---
    btnSend.setOnAction(e -> handleSendMessage());
    btnHighscore.setOnAction(
        e -> {
          SceneManager.getInstance().showScene(SceneProtocol.HIGHSCORE);
          EventHandlers.getInstance().updateHighscore();
        });
    btnKeyBinding.setOnAction(e ->SceneManager.getInstance().showScene(SceneProtocol.KEY_BINDING));
    btnWisdom.setOnAction(e -> loadWisdom());
    btnNickname.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.NICKNAME));
    btnJoin.setOnAction(
        e -> {
          EventHandlers.getInstance().updateLists();
          SceneManager.getInstance().showScene(SceneProtocol.JOINLOBBY);
        });
    btnCreate.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.CREATELOBBY));
    chatInput.setOnAction(e -> handleSendMessage());

    inputArea.getChildren().addAll(chatMode, whisperTargetInput, chatInput, btnSend);
    chatBox.getChildren().addAll(new Label("Server Chat"), chatDisplay, inputArea);

    // --- MAIN LAYOUT ---
    HBox mainLayout = new HBox();
    mainLayout
        .getChildren()
        .addAll(leftMenu, new Separator(javafx.geometry.Orientation.VERTICAL), chatBox);

    localScene = new Scene(mainLayout, 900, 600); // Increased window size slightly for the list
  }

  private void handleSendMessage() {
    String message = chatInput.getText().trim();
    String mode = chatMode.getValue();

    if (!message.isEmpty()) {
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
  }

  private void loadWisdom() {
    wisdomLabel.setText("Loading wisdom...");
    new Thread(
            () -> {
              String quote = FALLBACK_QUOTE;
              try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(QOTD_HOST, QOTD_PORT), 3000);
                socket.setSoTimeout(3000);
                BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder wisdom = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                  if (!line.isBlank()) {
                    if (wisdom.length() > 0) {
                      wisdom.append(" ");
                    }
                    wisdom.append(line.trim());
                  }
                }
                if (wisdom.length() > 0) {
                  quote = wisdom.toString();
                }
              } catch (Exception ignored) {
                quote = FALLBACK_QUOTE;
              }
              String finalQuote = quote;
              Platform.runLater(() -> wisdomLabel.setText(finalQuote));
            })
        .start();
  }

  @Override
  public Scene getScene() {
    return localScene;
  }
}
