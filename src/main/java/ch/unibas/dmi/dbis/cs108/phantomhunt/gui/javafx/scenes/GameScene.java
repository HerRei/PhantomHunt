package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.Player;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.LobbyHandler;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the map screen where the game is played.
 * Handles the display of the map, players, game chat, and tile-based map rendering.
 */
public class GameScene implements SceneInterface {

  private static final int TILE_SIZE = 32;
  private static final String TILE_IMAGE_PATH = "/assets/floor-test.png";

  private final Scene scene;
  private final Pane gamePane = new Pane();
  private final Map<Player, Rectangle> playerShapes = new HashMap<>();
  private final TextArea chatArea = new TextArea();
  private final TextField chatInput = new TextField();
  private boolean w, a, s, d;
  private final double mapScale;

  /**
   * Initializes the game scene, building the map from tiles and scaling to fit the screen.
   */
  public GameScene() {
    GameModel model = GameModel.getInstance();
    String[][] mapData = LobbyHandler.generateExampleMap(); // see note below

    int mapRows = mapData.length;
    int mapCols = mapData[0].length;

    // Total pixel size of the unscaled map
    double mapPixelHeight = mapRows * TILE_SIZE;
    double mapPixelWidth  = mapCols * TILE_SIZE;

    this.mapScale = 512.0 / mapPixelHeight;

    // Build the tile layer
    Pane tilePane = buildTilePane(mapData);

    // Scale the tile pane using a Group so layout is based on scaled bounds
    javafx.scene.Group scaledTiles = new javafx.scene.Group(tilePane);
    scaledTiles.setScaleX(mapScale);
    scaledTiles.setScaleY(mapScale);
    // Group scales around its center — shift it back so top-left stays at (0,0)
    scaledTiles.setTranslateX((mapPixelWidth  * mapScale - mapPixelWidth)  / 2);
    scaledTiles.setTranslateY((mapPixelHeight * mapScale - mapPixelHeight) / 2);

    // Player shapes sit on top, already in scaled coordinates via property bindings
    // Use TOP_LEFT alignment so StackPane does not center the children
    StackPane gameStack = new StackPane(scaledTiles, gamePane);
    gameStack.setAlignment(Pos.TOP_LEFT);
    gameStack.setPrefSize(mapPixelWidth * mapScale, mapPixelHeight * mapScale);
    gameStack.setMinSize(mapPixelWidth * mapScale, mapPixelHeight * mapScale);
    gameStack.setMaxSize(mapPixelWidth * mapScale, mapPixelHeight * mapScale);
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
   * Builds a Pane populated with 32×32 tile ImageViews.
   * Walls ("X") are left as transparent (black background shows through).
   * Walkable tiles (" ") get the floor image.
   */
  private Pane buildTilePane(String[][] mapData) {
    Pane pane = new Pane();
    Image floorImage = new Image(getClass().getResourceAsStream(TILE_IMAGE_PATH));

    for (int row = 0; row < mapData.length; row++) {
      for (int col = 0; col < mapData[row].length; col++) {
        if (" ".equals(mapData[row][col])) {
          ImageView tile = new ImageView(floorImage);
          tile.setFitWidth(TILE_SIZE);
          tile.setFitHeight(TILE_SIZE);
          tile.setPreserveRatio(false);
          tile.setLayoutX(col * TILE_SIZE);
          tile.setLayoutY(row * TILE_SIZE);
          pane.getChildren().add(tile);
        }
      }
    }
    return pane;
  }

  // ── Sidebar ────────────────────────────────────────────────────────────────

  private VBox createSidebar(GameModel model) {
    VBox box = new VBox(15);
    box.setPadding(new Insets(15));
    box.setPrefWidth(250);
    box.setPrefHeight(512);
    box.setStyle("-fx-background-color: #333; -fx-text-fill: white;");

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

    Label scoreTitle = new Label("Your Score:");
    scoreTitle.setStyle("-fx-font-weight: bold;");

    Label scoreValue = new Label();
    scoreValue.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 22px; -fx-font-weight: bold;");
    scoreValue.textProperty().bind(model.getScore().asString());

    VBox scoreBox = new VBox(2, scoreTitle, scoreValue);

    Label tableLabel = new Label("Players");
    tableLabel.setStyle("-fx-text-fill: white;");

    TableView<Player> table = new TableView<>(model.getPlayers());
    table.setPrefHeight(180);

    TableColumn<Player, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

    TableColumn<Player, Integer> scCol = new TableColumn<>("Score");
    scCol.setCellValueFactory(new PropertyValueFactory<>("score"));

    table.getColumns().addAll(nameCol, scCol);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    Label chatLabel = new Label("Chat");
    chatLabel.setStyle("-fx-text-fill: white;");

    chatArea.setEditable(false);
    chatArea.setWrapText(true);
    chatArea.setPrefHeight(250);

    chatInput.setPromptText("Send message...");
    chatInput.setOnAction(e -> sendMessage());

    box.getChildren().addAll(statusBox, new Separator(), scoreBox, tableLabel, table,
            chatLabel, chatArea, chatInput);
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    return box;
  }

  // ── Chat ───────────────────────────────────────────────────────────────────

  private void setupChatBinding() {
    GameModel.getInstance().lobbyChatMessagesProperty()
            .addListener((ListChangeListener<String>) c -> {
              StringBuilder sb = new StringBuilder();
              for (String msg : GameModel.getInstance().lobbyChatMessagesProperty()) {
                sb.append(msg).append("\n");
              }
              chatArea.setText(sb.toString());
              chatArea.setScrollTop(Double.MAX_VALUE);
            });
  }

  private void sendMessage() {
    String msg = chatInput.getText().trim();
    if (msg.isEmpty()) return;
    EventHandlers.getInstance().sendMessage(Command.YAP, msg);
    chatInput.clear();
  }

  // ── Input ──────────────────────────────────────────────────────────────────

  private void setupControls() {
    scene.setOnKeyPressed(e -> handleKey(e.getCode()));
  }

  private void handleKey(KeyCode code) {
    if (chatInput.isFocused()) return;

    int horizontal = 0, vertical = 0;
    boolean changed = false;

    switch (code) {
      case W -> { if (!w) { w=true; a=false; s=false; d=false; vertical=-1; changed=true; } }
      case S -> { if (!s) { w=false; a=false; s=true;  d=false; vertical= 1; changed=true; } }
      case A -> { if (!a) { w=false; a=true;  s=false; d=false; horizontal=-1; changed=true; } }
      case D -> { if (!d) { w=false; a=false; s=false; d=true;  horizontal= 1; changed=true; } }
      default -> {}
    }

    if (changed) {
      EventHandlers.getInstance().sendInputs(vertical, horizontal);
    }
  }

  // ── Player tracking ────────────────────────────────────────────────────────

  private void setupPlayerTracking() {
    GameModel.getInstance().getPlayers().addListener((ListChangeListener<Player>) c -> {
      while (c.next()) {
        if (c.wasAdded())   c.getAddedSubList().forEach(this::addPlayer);
        if (c.wasRemoved()) c.getRemoved().forEach(this::removePlayer);
      }
    });
    GameModel.getInstance().getPlayers().forEach(this::addPlayer);
  }

  private void addPlayer(Player player) {
    Rectangle shape = new Rectangle(32, 32);

    player.skinProperty().addListener((obs, oldSkin, newSkin) -> updatePlayerColor(shape, newSkin));
    updatePlayerColor(shape, player.skinProperty().get());

    shape.layoutXProperty().bind(player.xPosition().multiply(mapScale));
    shape.layoutYProperty().bind(player.yPosition().multiply(mapScale));

    playerShapes.put(player, shape);
    gamePane.getChildren().add(shape);
  }

  private void updatePlayerColor(Rectangle shape, String skin) {
    shape.setFill("HUMAN".equalsIgnoreCase(skin) ? Color.RED : Color.WHITE);
  }

  private void removePlayer(Player player) {
    Rectangle shape = playerShapes.remove(player);
    if (shape != null) gamePane.getChildren().remove(shape);
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}