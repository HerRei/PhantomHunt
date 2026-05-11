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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** The primary hub scene displaying the global chat, profile options, and lobby navigation. */
public class HubScene implements SceneInterface {

  private ListView<String> chatDisplay;
  private ListView<String> playerListDisplay;
  private TextField chatInput;
  private ComboBox<String> whisperTargetSelector;
  private ComboBox<String> chatMode;
  private Label nicknameLabel;
  private Scene localScene;

  public HubScene() {
    createScene();
  }

  /** Constructs the UI layout for the hub. */
  public void createScene() {
    GameModel model = GameModel.getInstance();

    VBox leftMenu = new VBox(12);
    leftMenu.setPadding(new Insets(25));
    leftMenu.setAlignment(Pos.TOP_CENTER);
    leftMenu.setPrefWidth(290);
    leftMenu.setStyle(SceneStyle.PANEL_BACKGROUND);

    // Profile
    Label headLabel = new Label("Logged in as:");
    headLabel.setStyle(SceneStyle.SUBTLE_TEXT);
    nicknameLabel = new Label();
    nicknameLabel.textProperty().bind(Bindings.concat(model.getName()));
    nicknameLabel.setStyle(SceneStyle.PROFILE_NAME);
    VBox profileBox = new VBox(4, headLabel, nicknameLabel);
    profileBox.setAlignment(Pos.CENTER);

    // Nav buttons
    Button btnNickname = navButton("Change Nickname");
    Button btnJoin = navButton("Join Lobby");
    Button btnCreate = navButton("Create Lobby");
    Button btnHighscore = navButton("Show Highscores");
    Button btnKeyBinding = navButton("Key Bindings");
    Button btnWisdom = navButton("Get Wisdom");

    Label onlineLabel = new Label("Players Online");
    onlineLabel.setStyle(SceneStyle.SECTION_LABEL_SMALL);

    playerListDisplay = new ListView<>();
    playerListDisplay.setItems(model.players);
    playerListDisplay.setStyle(SceneStyle.LIST);
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

    VBox chatBox = new VBox(10);
    chatBox.setPadding(new Insets(20));
    chatBox.setStyle(SceneStyle.DARK_BACKGROUND);
    HBox.setHgrow(chatBox, Priority.ALWAYS);

    Label chatTitle = new Label("Server Chat");
    chatTitle.setStyle(SceneStyle.PANEL_TITLE);

    chatDisplay = new ListView<>();
    chatDisplay.setItems(model.chatMessagesProperty());
    chatDisplay.setStyle(SceneStyle.LIST);
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
    chatMode.setStyle(SceneStyle.INPUT);
    styleComboBox(chatMode);

    whisperTargetSelector = new ComboBox<>();
    whisperTargetSelector.setItems(model.players);
    whisperTargetSelector.setPromptText("To whom?");
    whisperTargetSelector.setPrefWidth(140);
    whisperTargetSelector.setStyle(SceneStyle.INPUT);
    styleComboBox(whisperTargetSelector);
    whisperTargetSelector
        .visibleProperty()
        .bind(Bindings.equal("Whisper", chatMode.valueProperty()));
    whisperTargetSelector.managedProperty().bind(whisperTargetSelector.visibleProperty());

    chatInput = new TextField();
    chatInput.setPromptText("Type a message...");
    chatInput.setStyle(SceneStyle.INPUT);
    HBox.setHgrow(chatInput, Priority.ALWAYS);

    Button btnSend = new Button("Send");
    btnSend.setStyle(SceneStyle.BUTTON);
    btnSend.setPrefWidth(80);

    HBox inputArea = new HBox(8, chatMode, whisperTargetSelector, chatInput, btnSend);
    inputArea.setAlignment(Pos.CENTER_LEFT);

    chatBox.getChildren().addAll(chatTitle, chatDisplay, inputArea);

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

    HBox mainLayout = new HBox(
        leftMenu, new Separator(javafx.geometry.Orientation.VERTICAL), chatBox);

    SceneManager sceneManager = SceneManager.getInstance();
    localScene = new Scene(mainLayout, sceneManager.getWidth(), sceneManager.getHeight());
  }

  private Button navButton(String text) {
    Button b = new Button(text);
    b.setMaxWidth(Double.MAX_VALUE);
    b.setPrefHeight(36);
    b.setStyle(SceneStyle.BUTTON_COMPACT);
    return b;
  }

  private void styleComboBox(ComboBox<String> comboBox) {
    comboBox.setButtonCell(createComboBoxButtonCell(comboBox));
    comboBox.setCellFactory(listView -> createComboBoxListCell());
  }

  private ListCell<String> createComboBoxButtonCell(ComboBox<String> comboBox) {
    return new ListCell<String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? comboBox.getPromptText() : item);
        setStyle(SceneStyle.comboBoxCell(empty));
      }
    };
  }

  private ListCell<String> createComboBoxListCell() {
    return new ListCell<String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        setStyle(SceneStyle.LIST);
      }
    };
  }

  private void handleSendMessage() {
    String message = chatInput.getText().trim();
    String mode = chatMode.getValue();
    if (message.isEmpty()) return;

    if ("Global".equals(mode)) {
      EventHandlers.getInstance().sendMessage(Command.UNICOM, message);
      chatInput.clear();
    } else {
      String target = whisperTargetSelector.getValue();
      if (target == null || target.isBlank()) {
        GameModel.getInstance().addChatMessage("SYSTEM: You need to enter a target");
        return;
      }
      EventHandlers.getInstance().sendMessage(Command.WHISPER, target.trim(), message);
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
