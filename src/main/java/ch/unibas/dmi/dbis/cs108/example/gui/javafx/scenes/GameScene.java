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

public class GameScene implements SceneInterface {
  private final Scene scene;
  private final Pane gamePane = new Pane();
  private final Map<Player, Rectangle> playerShapes = new HashMap<>();
  private final TextArea chatArea = new TextArea();
  private final TextField chatInput = new TextField();
  private boolean w, a, s, d;

  public GameScene() {
    GameModel model = GameModel.getInstance();
    ImageView mapView = new ImageView(model.getGameMap());
    mapView.setPreserveRatio(true);
    mapView.setFitHeight(720);

    StackPane gameStack = new StackPane(mapView, gamePane);
    gameStack.setStyle("-fx-background-color: black;");

    // Sidebar erstellen
    VBox sidebar = createSidebar(model);

    BorderPane root = new BorderPane();
    root.setCenter(gameStack);
    root.setRight(sidebar);

    this.scene = new Scene(root);
    setupControls();
    setupPlayerTracking();
    setupChatBinding(); // Chat-Bindung initialisieren
  }

  private VBox createSidebar(GameModel model) {
    VBox box = new VBox(15); // Etwas mehr Abstand zwischen den Elementen
    box.setPadding(new Insets(15));
    box.setPrefWidth(250);
    box.setStyle("-fx-background-color: #333; -fx-text-fill: white;");

    // 1. Dein Score (Ganz oben)
    Label yourScoreTitle = new Label("Your Score:");
    yourScoreTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

    Label scoreLabel = new Label("0");
    scoreLabel.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 24px; -fx-font-weight: bold;");

    // Bindet den Score des lokalen Spielers (angenommen model.getLocalPlayer() existiert)
    scoreLabel.textProperty().bind(GameModel.getInstance().getScore().asString());

    VBox scoreContainer = new VBox(5, yourScoreTitle, scoreLabel);
    scoreContainer.setPadding(new Insets(0, 0, 10, 0));

    // 2. Scoreboard (Alle Spieler)
    Label tableLabel = new Label("Players");
    tableLabel.setStyle("-fx-text-fill: white;");

    TableView<Player> table = new TableView<>(model.getPlayers());
    table.setPrefHeight(200);

    TableColumn<Player, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

    TableColumn<Player, Integer> scoreCol = new TableColumn<>("Score");
    scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

    table.getColumns().addAll(nameCol, scoreCol);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // 3. Chat (Ganz unten)
    Label chatLabel = new Label("Chat");
    chatLabel.setStyle("-fx-text-fill: white;");

    chatArea.setEditable(false);
    chatArea.setWrapText(true);
    chatArea.setPrefHeight(300);

    chatInput.setPromptText("Type a message...");
    chatInput.setOnAction(e -> sendMessage());

    // Alles zur Sidebar hinzufügen
    box.getChildren().addAll(scoreContainer, tableLabel, table, chatLabel, chatArea, chatInput);

    // Damit der Chat-Bereich den restlichen Platz einnimmt, falls nötig
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    return box;
  }

  /**
   * Bindet das TextArea an die Chat-Liste im Model.
   * Jedes Mal, wenn eine neue Nachricht kommt, wird der Text aktualisiert.
   */
  private void setupChatBinding() {
    GameModel.getInstance().chatMessagesProperty().addListener((ListChangeListener<String>) c -> {
      StringBuilder sb = new StringBuilder();
      for (String msg : GameModel.getInstance().chatMessagesProperty()) {
        sb.append(msg).append("\n");
      }
      chatArea.setText(sb.toString());
      chatArea.setScrollTop(Double.MAX_VALUE); // Auto-Scroll nach unten
    });
  }

  private void sendMessage() {
    String msg = chatInput.getText().trim();
    if (msg.isEmpty()) return;
    EventHandlers.getInstance().sendMessage(Command.YAP, msg);
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

  private void addPlayer(Player p) {
    Rectangle r = new Rectangle(20, 20);
    r.setFill("HUMAN".equalsIgnoreCase(p.skinProperty().get()) ? Color.RED : Color.WHITE);
    r.layoutXProperty().bind(p.xPosition());
    r.layoutYProperty().bind(p.yPosition());
    playerShapes.put(p, r);
    gamePane.getChildren().add(r);
  }

  private void removePlayer(Player p) {
    Rectangle r = playerShapes.remove(p);
    if (r != null) gamePane.getChildren().remove(r);
  }

  @Override public Scene getScene() { return scene; }
}