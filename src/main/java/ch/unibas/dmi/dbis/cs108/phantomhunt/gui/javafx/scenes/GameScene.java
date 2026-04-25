package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.Player;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the map screen where the game is played. Handles the display of the map, players, game
 * chat, and tile-based map rendering.
 */
public class GameScene implements SceneInterface {

  private static final int TILE_SIZE = 32;

  private final Scene scene;
  private final Pane gamePane = new Pane();
  private final Map<Player, ImageView> playerSprites = new HashMap<>();
  private final TextArea chatArea = new TextArea();
  private final TextField chatInput = new TextField();
  private boolean w, a, s, d; // INPUTS
  private final double mapScale;
  private final Map<String, Image> floorImageMap = new HashMap<>();
  private final Map<String, Image[]> humanSprites = new HashMap<>();
  private final Map<String, Image> phantomSprites = new HashMap<>();
  private final Map<String, Image> phantomGlitchedSprites = new HashMap<>();
  private final Map<Player, Integer> frameIndex = new HashMap<>();
  private final Map<Player, Long> lastFrameUpdate = new HashMap<>();
  private ImageView abilitySprite;


  /** Initializes the game scene, building the map from tiles and scaling to fit the screen. */
  public GameScene() {
    // Load floor tiles
    floorImageMap.put("Q", new Image(getClass().getResourceAsStream("/assets/floors/quadra.png")));
    floorImageMap.put(
        "L", new Image(getClass().getResourceAsStream("/assets/floors/top_left.png")));
    floorImageMap.put(
        "V", new Image(getClass().getResourceAsStream("/assets/floors/vertical.png")));
    floorImageMap.put(
        "D", new Image(getClass().getResourceAsStream("/assets/floors/down_left.png")));
    floorImageMap.put(
        "R", new Image(getClass().getResourceAsStream("/assets/floors/top_right.png")));
    floorImageMap.put(
        "A", new Image(getClass().getResourceAsStream("/assets/floors/down_right.png")));
    floorImageMap.put(
        "H", new Image(getClass().getResourceAsStream("/assets/floors/horizontal.png")));
    floorImageMap.put(
        "T", new Image(getClass().getResourceAsStream("/assets/floors/triple_top.png")));
    floorImageMap.put(
        "B", new Image(getClass().getResourceAsStream("/assets/floors/triple_down.png")));
    floorImageMap.put(
        "C", new Image(getClass().getResourceAsStream("/assets/floors/triple_left.png")));
    floorImageMap.put(
        "E", new Image(getClass().getResourceAsStream("/assets/floors/triple_right.png")));

    // Load player images
    loadSprites();

    GameModel model = GameModel.getInstance();
    String[][] mapData = MapLogic.generateExampleMap(); // see note below

    int mapRows = mapData.length;
    int mapCols = mapData[0].length;

    // Total pixel size of the unscaled map
    double mapPixelHeight = mapRows * TILE_SIZE;
    double mapPixelWidth = mapCols * TILE_SIZE;

    this.mapScale = 640.0 / mapPixelHeight;

    // Build the tile layer
    Pane tilePane = buildTilePane(mapData);

    // Scale the tile pane using a Group so layout is based on scaled bounds
    javafx.scene.Group scaledTiles = new javafx.scene.Group(tilePane);
    scaledTiles.setScaleX(mapScale);
    scaledTiles.setScaleY(mapScale);
    // Group scales around its center — shift it back so top-left stays at (0,0)
    scaledTiles.setTranslateX((mapPixelWidth * mapScale - mapPixelWidth) / 2);
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
    setupAbility();

    model.humanAbilityProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal) {
        playerSprites.forEach((player, sprite) ->
            updatePlayerSprite(player, sprite)
        );
      }
    });
  }

  private void loadSprites() {
    // Load human sprites (4 players, 4 directions, 2 frames each)
    String[] directions = {"front", "back", "left", "right"};
    for (int i = 1; i <= 4; i++) {
      for (String direction : directions) {
        String key = "p" + i + "_" + direction;
        Image[] frames = new Image[2];
        frames[0] = new Image(getClass().getResourceAsStream("/assets/humans/" + key + "1.png"));
        frames[1] = new Image(getClass().getResourceAsStream("/assets/humans/" + key + "2.png"));
        humanSprites.put(key, frames);
      }
    }

    // Load ghost sprites (4 players, 4 directions)
    String[] phantomColors = {"b", "g", "r", "v"};
    String[] phantomDirections = {"d", "u", "l", "r"};
    for (String color : phantomColors) {
      for (String direction : phantomDirections) {
        String key = color + direction + "_ghost";
        phantomSprites.put(
            key, new Image(getClass().getResourceAsStream("/assets/ghosts/" + key + ".png")));
        String glitchKey = color + direction + "_glitch";
        phantomGlitchedSprites.put(
            glitchKey, new Image(getClass().getResourceAsStream("/assets/ghosts/" + glitchKey + ".png")));
      }
    }
  }

  /**
   * Builds a Pane populated with 32×32 tile ImageViews. Walls ("X") are left as transparent (black
   * background shows through). Others are walkable tiles
   */
  private Pane buildTilePane(String[][] mapData) {
    Pane pane = new Pane();

    for (int row = 0; row < mapData.length; row++) {
      for (int col = 0; col < mapData[row].length; col++) {
        String tileType = mapData[row][col];
        if (!"X".equals(tileType) && floorImageMap.containsKey(tileType)) {
          Image floorImage = this.floorImageMap.get(tileType);
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

    box.getChildren()
        .addAll(
            statusBox,
            new Separator(),
            scoreBox,
            tableLabel,
            table,
            chatLabel,
            chatArea,
            chatInput);
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    return box;
  }

  // ── Chat ───────────────────────────────────────────────────────────────────

  private void setupChatBinding() {
    GameModel.getInstance()
        .lobbyChatMessagesProperty()
        .addListener(
            (ListChangeListener<String>)
                c -> {
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
    scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
    scene.setOnKeyReleased(e -> handleKeyReleased(e.getCode()));
  }

  private void handleKeyPress(KeyCode code) {
    if (chatInput.isFocused()) return;

    int horizontal = 0, vertical = 0;
    boolean changed = false;

    switch (code) {
      case W -> {
        if (!w) {
          w = true;
          a = false;
          s = false;
          d = false;
          vertical = -1;
          changed = true;
        }
      }
      case S -> {
        if (!s) {
          w = false;
          a = false;
          s = true;
          d = false;
          vertical = 1;
          changed = true;
        }
      }
      case A -> {
        if (!a) {
          w = false;
          a = true;
          s = false;
          d = false;
          horizontal = -1;
          changed = true;
        }
      }
      case D -> {
        if (!d) {
          w = false;
          a = false;
          s = false;
          d = true;
          horizontal = 1;
          changed = true;
        }
      }
      default -> {}
    }

    if (changed) {
      EventHandlers.getInstance().sendInputs(vertical, horizontal);
    }
  }

  private void handleKeyReleased(KeyCode code) {
    // In case we implement a key released feature
  }

  // ── Player tracking ────────────────────────────────────────────────────────

  private void setupPlayerTracking() {
    GameModel.getInstance()
        .getPlayers()
        .addListener(
            (ListChangeListener<Player>)
                c -> {
                  while (c.next()) {
                    if (c.wasAdded()) c.getAddedSubList().forEach(this::addPlayer);
                    if (c.wasRemoved()) c.getRemoved().forEach(this::removePlayer);
                  }
                });
    GameModel.getInstance().getPlayers().forEach(this::addPlayer);
  }

  private void addPlayer(Player player) {
    double size = TILE_SIZE * mapScale;
    ImageView sprite = new ImageView();
    sprite.setFitWidth(size);
    sprite.setFitHeight(size);

    // Listen for position changes to update direction and animation
    player
        .xPosition()
        .addListener(
            (obs, oldX, newX) -> {
              double deltaX = newX.doubleValue() - oldX.doubleValue();
              if (deltaX == 0) return;

              if (deltaX > 0) {
                GameModel.getInstance().setMyPlayerDirection("right");
              } else {
                GameModel.getInstance().setMyPlayerDirection("left");
              }
              handlePositionChange(player, sprite);
            });

    player
        .yPosition()
        .addListener(
            (obs, oldY, newY) -> {
              double deltaY = newY.doubleValue() - oldY.doubleValue();
              if (deltaY == 0) return;

              if (deltaY > 0) {
                GameModel.getInstance().setMyPlayerDirection("front");
              } else {
                GameModel.getInstance().setMyPlayerDirection("back");
              }
              handlePositionChange(player, sprite);
            });

    player
        .skinProperty()
        .addListener((obs, oldSkin, newSkin) -> updatePlayerSprite(player, sprite));
    updatePlayerSprite(player, sprite);

    sprite.layoutXProperty().bind(player.xPosition().multiply(mapScale));
    sprite.layoutYProperty().bind(player.yPosition().multiply(mapScale));

    playerSprites.put(player, sprite);
    gamePane.getChildren().add(sprite);
  }

  private void handlePositionChange(Player player, ImageView sprite) {
    long now = System.currentTimeMillis();
    long lastUpdate = lastFrameUpdate.getOrDefault(player, 0L);

    // Cooldown to prevent double-update from x/y listeners for a single move
    if (now - lastUpdate > 100) {
      int frame = frameIndex.getOrDefault(player, 0);
      frameIndex.put(player, 1 - frame);
      lastFrameUpdate.put(player, now);
    }
    updatePlayerSprite(player, sprite);
  }

  private void updatePlayerSprite(Player player, ImageView sprite) {
    String skin = player.getSkin();
    String direction = player.getPlayerDirection();
    String[] phantomColors = {"b", "g", "r", "v"};
    Map<String, String> phantomDirections =
        Map.of("front", "d", "back", "u", "left", "l", "right", "r");

    int frame = frameIndex.getOrDefault(player, 0);

    if ("HUMAN".equals(skin)) {
      String key = "p" + player.getPlayerNumber() + "_" + direction;
      Image[] frames = humanSprites.get(key);
      if (frames == null) {
        return;
      }

      sprite.setImage(frames[frame]);

    } else { // ghost
      String color = phantomColors[player.getPlayerNumber() - 1];
      String dir = phantomDirections.get(direction);
      String baseKey = color + dir;

      boolean abilityActive = GameModel.getInstance().humanAbilityProperty().get();

      if (abilityActive && frame == 1) {
        sprite.setImage(phantomGlitchedSprites.get(baseKey + "_glitch"));
      } else {
        sprite.setImage(phantomSprites.get(baseKey + "_ghost"));
      }
    }
  }

  private void removePlayer(Player player) {
    ImageView sprite = playerSprites.remove(player);
    if (sprite != null) gamePane.getChildren().remove(sprite);
  }

  private void setupAbility() {
    Image image = new Image(getClass().getResourceAsStream("/assets/abilities/ability.png"));
    abilitySprite = new ImageView(image);
    abilitySprite.setFitWidth(TILE_SIZE * mapScale);
    abilitySprite.setFitHeight(TILE_SIZE * mapScale);
    abilitySprite.layoutXProperty().bind(GameModel.getInstance().getAbility().xPosition().multiply(mapScale));
    abilitySprite.layoutYProperty().bind(GameModel.getInstance().getAbility().yPosition().multiply(mapScale));
    abilitySprite.visibleProperty().bind(GameModel.getInstance().isAbilityVisibleProperty());
    gamePane.getChildren().add(abilitySprite);
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
