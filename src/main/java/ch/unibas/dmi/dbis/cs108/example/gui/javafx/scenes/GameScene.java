package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.Player;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * Scene displaying the game world map and a sidebar with game state info.
 */
public class GameScene implements SceneInterface {
  private final Scene scene;
  private final Pane gamePane = new Pane();
  private final Map<Player, Rectangle> playerShapes = new HashMap<>();
  private final TextArea chatArea = new TextArea();
  private final TextField chatInput = new TextField();
  private boolean w, a, s, d;
  private final double mapScale;

  public GameScene() {
    GameModel model = GameModel.getInstance();
    ImageView mapView = new ImageView(model.getGameMap());
    mapView.setPreserveRatio(true);
    mapView.setFitHeight(720); // Fixed map display height

    // Scale logic: displayed height / original image height (512)
    this.mapScale = 720.0 / 512.0;

    StackPane gameStack = new StackPane(mapView, gamePane);
    gameStack.setStyle("-fx-background-color: black;");

    VBox sidebar = createSidebar(model);

    BorderPane root = new BorderPane();
    root.setCenter(gameStack);
    root.setRight(sidebar);

    this.scene = new Scene(root);
    setupControls();
    setupPlayerTracking();
    setupChatBinding();
  }

  /**
   * Builds the sidebar with game status, scores, and chat.
   */
  private VBox createSidebar(GameModel model) {
    VBox box = new VBox(15);
    box.setPadding(new Insets(15));
    box.setPrefWidth(250);
    box.setPrefHeight(720);// Fixed sidebar width
    box.setStyle("-fx-background-color: #333; -fx-text-fill: white;");

    // --- 1. Game Status (Role, Round, Time) ---
    VBox statusBox = new VBox(5);

    Label roleLabel = new Label();
    roleLabel.setStyle("-fx-text-fill: #00BFFF; -fx-font-weight: bold; -fx-font-size: 14px;");
    roleLabel.textProperty().bind(model.getRole().concat(" Role"));

    Label roundLabel = new Label();
    roundLabel.textProperty().bind(model.getRound().asString("Round: %d"));

    Label timeLabel = new Label();
    timeLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
    timeLabel.textProperty().bind(model.getTime().asString("Time: %d s"));

    statusBox.getChildren().addAll(roleLabel, roundLabel, timeLabel);

    // --- 2. Personal Score ---
    Label scoreTitle = new Label("Your Score:");
    scoreTitle.setStyle("-fx-font-weight: bold;");

    Label scoreValue = new Label();
    scoreValue.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 22px; -fx-font-weight: bold;");
    scoreValue.textProperty().bind(model.getScore().asString());

    VBox scoreBox = new VBox(2, scoreTitle, scoreValue);

    // --- 3. Player List ---
    TableView<Player> table = new TableView<>(model.getPlayers());
    table.setPrefHeight(180);

    TableColumn<Player, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    TableColumn<Player, Integer> scCol = new TableColumn<>("Score");
    scCol.setCellValueFactory(new PropertyValueFactory<>("score"));

    table.getColumns().addAll(nameCol, scCol);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // --- 4. Chat ---
    chatArea.setEditable(false);
    chatArea.setWrapText(true);
    chatArea.setPrefHeight(250);
    chatInput.setPromptText("Send message...");
    chatInput.setOnAction(e -> sendMessage());

    box.getChildren().addAll(statusBox, new Separator(), scoreBox, new Label("Players:"), table, new Label("Chat:"), chatArea, chatInput);
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    return box;
  }

  /**
   * Updates chat area when new messages arrive in model.
   */
  private void setupChatBinding() {
    GameModel.getInstance().chatMessagesProperty().addListener((ListChangeListener<String>) c -> {
      StringBuilder sb = new StringBuilder();
      for (String msg : GameModel.getInstance().chatMessagesProperty()) {
        sb.append(msg).append("\n");
      }
      chatArea.setText(sb.toString());
      chatArea.setScrollTop(Double.MAX_VALUE);
    });
  }

  private void sendMessage() {
    String msg = chatInput.getText().trim();
    if (!msg.isEmpty()) EventHandlers.getInstance().sendMessage(Command.YAP, msg);
    chatInput.clear();
  }

  private void setupControls() {
    scene.setOnKeyPressed(e -> handleKey(e.getCode(), true));
    scene.setOnKeyReleased(e -> handleKey(e.getCode(), false));
  }

  private void handleKey(javafx.scene.input.KeyCode code, boolean pressed) {
    if (chatInput.isFocused()) return;
    boolean changed = false;
    switch (code) {
      case W -> { if (w != pressed) { w = pressed; changed = true; } }
      case S -> { if (s != pressed) { s = pressed; changed = true; } }
      case A -> { if (a != pressed) { a = pressed; changed = true; } }
      case D -> { if (d != pressed) { d = pressed; changed = true; } }
    }
    if (changed) EventHandlers.getInstance().sendInputs(w, s, a, d);
  }

  private void setupPlayerTracking() {
    GameModel.getInstance().getPlayers().addListener((ListChangeListener<Player>) c -> {
      while (c.next()) {
        if (c.wasAdded()) c.getAddedSubList().forEach(this::addPlayer);
        if (c.wasRemoved()) c.getRemoved().forEach(this::removePlayer);
      }
    });
    GameModel.getInstance().getPlayers().forEach(this::addPlayer);
  }

  /**
   * Adds and binds a player rectangle to the map.
   */
  private void addPlayer(Player p) {
    Rectangle r = new Rectangle(20, 20);

    // Skin-based color listener
    p.skinProperty().addListener((obs, old, newSkin) ->
            r.setFill("HUMAN".equalsIgnoreCase(newSkin) ? Color.RED : Color.WHITE));
    r.setFill("HUMAN".equalsIgnoreCase(p.skinProperty().get()) ? Color.RED : Color.WHITE);

    // Position binding with map scaling
    r.layoutXProperty().bind(p.xPosition().multiply(mapScale).subtract(10));
    r.layoutYProperty().bind(p.yPosition().multiply(mapScale).subtract(10));

    playerShapes.put(p, r);
    gamePane.getChildren().add(r);
  }

  private void removePlayer(Player p) {
    Rectangle r = playerShapes.remove(p);
    if (r != null) gamePane.getChildren().remove(r);
  }

  @Override public Scene getScene() { return scene; }
}