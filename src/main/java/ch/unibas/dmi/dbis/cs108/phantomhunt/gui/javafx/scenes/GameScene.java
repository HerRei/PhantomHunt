package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.input.ControllerInputHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.Player;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the map screen where the game is played.
 *
 * <p>Supports fullscreen mode: press F11 to switch. In fullscreen the game area fills as much space
 * as possible while keeping the original tile aspect ratio. The sidebar stays at a fixed width.
 */
public class GameScene implements SceneInterface {

  private static final int TILE_SIZE = 32;
  private static final int SIDEBAR_WIDTH = 250;
  private static final int WISDOM_BLINDNESS_SECONDS = 8;
  private static final String WISDOM_BLINDNESS_QUOTE =
      "\"A wise player can still run into a wall. The wall remains unimpressed.\"";

  private final Scene scene;
  private final Pane gamePane = new Pane();
  private final TextArea chatArea = new TextArea();
  private final TextField chatInput = new TextField();
  private final ControllerInputHandler controllerInputHandler =
      new ControllerInputHandler(this::sendMovement, this::handleControllerPrimaryAction);

  // Sprite / animation state
  private final Map<Player, ImageView> playerSprites = new HashMap<>();
  private final Map<String, Image> floorImageMap = new HashMap<>();
  private final Map<String, Image[]> humanSprites = new HashMap<>();
  private final Map<String, Image> phantomSprites = new HashMap<>();
  private final Map<String, Image> phantomGlitchedSprites = new HashMap<>();
  private final Map<Player, Integer> frameIndex = new HashMap<>();
  private final Map<Player, Long> lastFrameUpdate = new HashMap<>();
  private ImageView abilitySprite;

  // Movement flags
  private boolean up, down, left, right;

  // Unscaled pixel dimensions of the map
  private final double mapPixelWidth;
  private final double mapPixelHeight;
  private final double baseScale;

  private final StackPane gameStack;
  private final javafx.scene.Group scaledTiles;
  private final Pane tilePane;

  // Status labels updated dynamically when player data arrives
  private final Label nameLabel = new Label();
  private final Label roleLabel = new Label();
  private final Label wisdomBlessingLabel = new Label("Wisdom Blessing ready: Press R");
  private ProgressBar wisdomBlindProgress;
  private StackPane wisdomBlindOverlay;
  private Timeline wisdomBlindTimeline;

  /** Initializes the game scene, building the map from tiles and scaling to fit the screen. */
  public GameScene() {
    loadFloorTiles();
    loadSprites();

    GameModel model = GameModel.getInstance();
    String[][] mapData = MapLogic.generateExampleMap();

    int mapRows = mapData.length;
    int mapCols = mapData[0].length;

    mapPixelHeight = mapRows * TILE_SIZE;
    mapPixelWidth = mapCols * TILE_SIZE;

    SceneManager sceneManager = SceneManager.getInstance();
    baseScale = sceneManager.getHeight() / mapPixelHeight;

    // Tile layer (unscaled)
    tilePane = buildTilePane(mapData);

    // Scaled group: scales around center, then translated to top-left
    scaledTiles = new javafx.scene.Group(tilePane);
    applyScale(baseScale);

    // Game stack: tiles + player sprites
    gameStack = new StackPane(scaledTiles, gamePane);
    gameStack.setAlignment(Pos.TOP_LEFT);
    gameStack.setStyle(SceneStyle.GAME_BACKGROUND);
    applyGameStackSize(baseScale);
    setupWisdomBlindnessOverlay();

    VBox sidebar = createSidebar(model);

    BorderPane root = new BorderPane();
    root.setStyle(SceneStyle.DARK_BACKGROUND);
    root.setCenter(gameStack);
    root.setRight(sidebar);

    this.scene = new Scene(root, sceneManager.getWidth(), sceneManager.getHeight());

    setupPlayerTracking();
    setupChatBinding();
    setupAbility();

    model.humanAbilityProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal) playerSprites.forEach(this::updatePlayerSprite);
    });

    // Refresh status labels whenever role or name changes
    model.getRole()
        .addListener(
            (obs, o, n) ->
                Platform.runLater(
                    () -> {
                      refreshStatusLabels();
                      refreshWisdomBlessingLabel();
                    }));
    model.getName().addListener((obs, o, n) -> Platform.runLater(this::refreshStatusLabels));
    model.wisdomBlessingAvailableProperty()
        .addListener((obs, oldValue, newValue) -> Platform.runLater(this::refreshWisdomBlessingLabel));
    model.wisdomBlindnessActiveProperty()
        .addListener(
            (obs, oldValue, active) ->
                Platform.runLater(
                    () -> {
                      if (active) {
                        startWisdomBlindnessOverlay();
                      } else {
                        hideWisdomBlindnessOverlay();
                      }
                    }));

    // Resize listener for fullscreen scaling
    scene.widthProperty().addListener((obs, o, n) -> onSceneResized());
    scene.heightProperty().addListener((obs, o, n) -> onSceneResized());

    scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
    scene.setOnKeyReleased(e -> handleKeyReleased(e.getCode()));
    setupControllerInputLifecycle();
  }

  /**
   * Called whenever the scene dimensions change (window resize or fullscreen toggle). Recomputes
   * the dynamic scale so the map fills as much space as possible while keeping the tile aspect
   * ratio intact.
   */
  private void onSceneResized() {
    double availW = scene.getWidth() - SIDEBAR_WIDTH;
    double availH = scene.getHeight();
    if (availW <= 0 || availH <= 0) return;

    // Fit by height first, then clamp to available width
    double dynScale = Math.min(availH / mapPixelHeight, availW / mapPixelWidth);

    applyScale(dynScale);
    applyGameStackSize(dynScale);
    updateAllPlayerSpritePositions(dynScale);

    if (abilitySprite != null) {
      abilitySprite.setFitWidth(TILE_SIZE * dynScale);
      abilitySprite.setFitHeight(TILE_SIZE * dynScale);
      abilitySprite.layoutXProperty().unbind();
      abilitySprite.layoutYProperty().unbind();
      abilitySprite.layoutXProperty()
          .bind(GameModel.getInstance().getAbility().xPosition().multiply(dynScale));
      abilitySprite.layoutYProperty()
          .bind(GameModel.getInstance().getAbility().yPosition().multiply(dynScale));
    }
  }

  private void applyScale(double scale) {
    scaledTiles.setScaleX(scale);
    scaledTiles.setScaleY(scale);
    // Group scales around its center and is shifted so top-left stays at (0,0).
    scaledTiles.setTranslateX((mapPixelWidth * scale - mapPixelWidth) / 2.0);
    scaledTiles.setTranslateY((mapPixelHeight * scale - mapPixelHeight) / 2.0);
  }

  private void applyGameStackSize(double scale) {
    double w = mapPixelWidth * scale;
    double h = mapPixelHeight * scale;
    gameStack.setPrefSize(w, h);
    gameStack.setMinSize(w, h);
    gameStack.setMaxSize(w, h);
  }

  /** Re-binds all player sprite positions and sizes for a new dynamic scale. */
  private void updateAllPlayerSpritePositions(double scale) {
    double size = TILE_SIZE * scale;
    playerSprites.forEach((player, sprite) -> {
      sprite.setFitWidth(size);
      sprite.setFitHeight(size);
      sprite.layoutXProperty().unbind();
      sprite.layoutYProperty().unbind();
      sprite.layoutXProperty().bind(player.xPosition().multiply(scale));
      sprite.layoutYProperty().bind(player.yPosition().multiply(scale));
    });
  }

  private void loadFloorTiles() {
    String[] keys = {"Q", "L", "V", "D", "R", "A", "H", "T", "B", "C", "E"};
    String[] names = {
      "quadra", "top_left", "vertical", "down_left", "top_right",
      "down_right", "horizontal", "triple_top", "triple_down", "triple_left", "triple_right"
    };
    for (int i = 0; i < keys.length; i++) {
      floorImageMap.put(
          keys[i], new Image(getClass().getResourceAsStream("/assets/floors/" + names[i] + ".png")));
    }
  }

  private void loadSprites() {
    String[] directions = {"front", "back", "left", "right"};
    for (int i = 1; i <= 4; i++) {
      for (String dir : directions) {
        String key = "p" + i + "_" + dir;
        humanSprites.put(
            key,
            new Image[] {
              new Image(getClass().getResourceAsStream("/assets/humans/" + key + "1.png")),
              new Image(getClass().getResourceAsStream("/assets/humans/" + key + "2.png"))
            });
      }
    }

    String[] phantomColors = {"b", "g", "r", "v"};
    String[] phantomDirections = {"d", "u", "l", "r"};
    for (String color : phantomColors) {
      for (String dir : phantomDirections) {
        String key = color + dir + "_ghost";
        String gKey = color + dir + "_glitch";
        phantomSprites.put(
            key, new Image(getClass().getResourceAsStream("/assets/ghosts/" + key + ".png")));
        phantomGlitchedSprites.put(
            gKey, new Image(getClass().getResourceAsStream("/assets/ghosts/" + gKey + ".png")));
      }
    }
  }

  /**
   * Builds a Pane populated with 32x32 tile ImageViews. Walls ("X") are left as transparent (black
   * background shows through). Others are walkable tiles.
   */
  private Pane buildTilePane(String[][] mapData) {
    Pane pane = new Pane();
    for (int row = 0; row < mapData.length; row++) {
      for (int col = 0; col < mapData[row].length; col++) {
        String tileType = mapData[row][col];
        if (!"X".equals(tileType) && floorImageMap.containsKey(tileType)) {
          ImageView tile = new ImageView(floorImageMap.get(tileType));
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

  private VBox createSidebar(GameModel model) {
    VBox box = new VBox(15);
    box.setPadding(new Insets(15));
    box.setPrefWidth(SIDEBAR_WIDTH);
    box.setStyle(SceneStyle.PANEL_BACKGROUND);

    // Own name shown in black on a white badge
    nameLabel.setStyle(SceneStyle.NAME_BADGE);

    // Role label color is applied dynamically in refreshStatusLabels()
    roleLabel.setStyle(SceneStyle.ROLE_LABEL);
    wisdomBlessingLabel.setStyle(SceneStyle.TIME_TEXT);
    wisdomBlessingLabel.setVisible(false);
    wisdomBlessingLabel.setManaged(false);

    Label roundLabel = new Label();
    roundLabel.setStyle(SceneStyle.SUBTLE_TEXT);
    roundLabel.textProperty().bind(model.getRound().asString("Round: %d"));

    Label timeLabel = new Label();
    timeLabel.setStyle(SceneStyle.TIME_TEXT);
    timeLabel.textProperty().bind(model.getTime().asString("Time: %d s"));

    VBox statusBox = new VBox(5, nameLabel, roleLabel, wisdomBlessingLabel, roundLabel, timeLabel);

    // Score
    Label scoreTitle = new Label("Your Score:");
    scoreTitle.setStyle(SceneStyle.SECTION_LABEL);
    Label scoreValue = new Label();
    scoreValue.setStyle(SceneStyle.SCORE_TEXT_SMALL);
    scoreValue.textProperty().bind(model.getScore().asString());
    VBox scoreBox = new VBox(2, scoreTitle, scoreValue);

    // Players table
    Label tableLabel = new Label("Players");
    tableLabel.setStyle(SceneStyle.SECTION_LABEL);

    TableView<Player> table = new TableView<>(model.getPlayers());
    table.setPrefHeight(160);
    table.setStyle(SceneStyle.TABLE);
    TableColumn<Player, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    TableColumn<Player, Integer> scCol = new TableColumn<>("Score");
    scCol.setCellValueFactory(new PropertyValueFactory<>("score"));
    table.getColumns().addAll(nameCol, scCol);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Chat
    Label chatLabel = new Label("Chat");
    chatLabel.setStyle(SceneStyle.SECTION_LABEL);

    chatArea.setEditable(false);
    chatArea.setWrapText(true);
    chatArea.setStyle(SceneStyle.TEXT_AREA);
    chatArea.setPrefHeight(200);
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    chatInput.setPromptText("Send message...");
    chatInput.setStyle(SceneStyle.INPUT);
    chatInput.setOnAction(e -> sendMessage());

    Label f11Hint = new Label("F11 - Toggle Fullscreen");
    f11Hint.setStyle(SceneStyle.HINT_TEXT);

    box.getChildren()
        .addAll(
            statusBox, new Separator(),
            scoreBox,
            tableLabel, table,
            chatLabel, chatArea, chatInput,
            new Separator(), f11Hint);

    return box;
  }

  /**
   * Updates the name badge and the role label. The role label reads "You are [Color] and are
   * [Role]" and is rendered in the player's assigned slot color.
   */
  private void refreshStatusLabels() {
    GameModel model = GameModel.getInstance();
    String myName = model.getName().get();
    String role = model.getRole().get();

    // Own name badge (black text on white)
    nameLabel.setText(myName != null ? myName : "");

    if (role == null || role.isBlank()) {
      roleLabel.setText("Waiting for role...");
      roleLabel.setStyle(SceneStyle.ROLE_WAITING);
      return;
    }

    // Find own player entry to retrieve the assigned color
    String hex = "#00BFFF"; // fallback
    String colorName = "Unknown";
    if (myName != null) {
      for (Player p : model.getPlayers()) {
        if (p.getName().equals(myName)) {
          hex = p.getColor();
          colorName = p.getColorName();
          break;
        }
      }
    }

    roleLabel.setText("You are " + colorName + " and are " + role);
    roleLabel.setStyle(SceneStyle.roleColor(hex));
  }

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
                  Platform.runLater(
                      () -> {
                        chatArea.setText(sb.toString());
                        chatArea.setScrollTop(Double.MAX_VALUE);
                      });
                });
  }

  private void sendMessage() {
    String msg = chatInput.getText().trim();
    if (msg.isEmpty()) return;
    EventHandlers.getInstance().sendMessage(Command.YAP, msg);
    chatInput.clear();
  }

  /**
   * Handles a key-press event by comparing {@code code} against the current key bindings stored in
   * {@link GameModel}. Only one direction is active at a time; pressing a new direction cancels the
   * previous one.
   *
   * @param code The key that was pressed.
   */
  private void handleKeyPress(KeyCode code) {
    if (chatInput.isFocused()) return;

    GameModel model = GameModel.getInstance();
    if (code == KeyCode.R && shouldShowWisdomBlessingHint()) {
      EventHandlers.getInstance().sendMessage(Command.WISDOM, "BLESSING");
      return;
    }

    int horizontal = 0, vertical = 0;
    boolean changed = false;

    if (code.equals(model.getKeyBinding(GameModel.KEY_UP))) {
      if (!up) {
        up = true; down = false; left = false; right = false; vertical = -1; changed = true;
      }
    } else if (code.equals(model.getKeyBinding(GameModel.KEY_DOWN))) {
      if (!down) {
        up = false; down = true; left = false; right = false; vertical = 1; changed = true;
      }
    } else if (code.equals(model.getKeyBinding(GameModel.KEY_LEFT))) {
      if (!left) {
        up = false; down = false; left = true; right = false; horizontal = -1; changed = true;
      }
    } else if (code.equals(model.getKeyBinding(GameModel.KEY_RIGHT))) {
      if (!right) {
        up = false; down = false; left = false; right = true; horizontal = 1; changed = true;
      }
    }

    if (changed) sendMovement(vertical, horizontal);
  }

  private void handleKeyReleased(KeyCode code) {
    // Reserved for future use
  }

  private void sendMovement(int vertical, int horizontal) {
    EventHandlers.getInstance().sendInputs(vertical, horizontal);
  }

  private void handleControllerPrimaryAction() {
    if (shouldShowWisdomBlessingHint()) {
      EventHandlers.getInstance().sendMessage(Command.WISDOM, "BLESSING");
    }
  }

  private void setupControllerInputLifecycle() {
    scene
        .windowProperty()
        .addListener(
            (observable, oldWindow, newWindow) -> {
              if (newWindow == null) {
                controllerInputHandler.stop();
              } else {
                controllerInputHandler.start();
              }
            });
  }

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

  /** Returns the current dynamic scale derived from the game stack's preferred width. */
  private double currentScale() {
    double w = gameStack.getPrefWidth();
    return (w > 0) ? w / mapPixelWidth : baseScale;
  }

  private void addPlayer(Player player) {
    double scale = currentScale();
    double size = TILE_SIZE * scale;

    ImageView sprite = new ImageView();
    sprite.setFitWidth(size);
    sprite.setFitHeight(size);

    // Listen for position changes to update direction and animation
    player
        .xPosition()
        .addListener(
            (obs, oldX, newX) -> {
              double dx = newX.doubleValue() - oldX.doubleValue();
              if (dx == 0) return;
              player.setPlayerDirection(dx > 0 ? "right" : "left");
              handlePositionChange(player, sprite);
            });

    player
        .yPosition()
        .addListener(
            (obs, oldY, newY) -> {
              double dy = newY.doubleValue() - oldY.doubleValue();
              if (dy == 0) return;
              player.setPlayerDirection(dy > 0 ? "front" : "back");
              handlePositionChange(player, sprite);
            });

    player.skinProperty().addListener((obs, oldSkin, newSkin) -> updatePlayerSprite(player, sprite));
    updatePlayerSprite(player, sprite);

    sprite.layoutXProperty().bind(player.xPosition().multiply(scale));
    sprite.layoutYProperty().bind(player.yPosition().multiply(scale));

    playerSprites.put(player, sprite);
    gamePane.getChildren().add(sprite);

    // Refresh status labels now that the player's color is available
    Platform.runLater(this::refreshStatusLabels);
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
    int frame = frameIndex.getOrDefault(player, 0);

    Map<String, String> phantomDirMap = Map.of("front", "d", "back", "u", "left", "l", "right", "r");
    String[] phantomColors = {"b", "g", "r", "v"};

    if ("HUMAN".equals(skin)) {
      String key = "p" + player.getPlayerNumber() + "_" + direction;
      Image[] frames = humanSprites.get(key);
      if (frames != null) sprite.setImage(frames[frame]);
    } else {
      String color = phantomColors[player.getPlayerNumber() - 1];
      String dir = phantomDirMap.get(direction);
      String baseKey = color + dir;
      boolean abilityActive = GameModel.getInstance().humanAbilityProperty().get();
      sprite.setImage(
          abilityActive && frame == 1
              ? phantomGlitchedSprites.get(baseKey + "_glitch")
              : phantomSprites.get(baseKey + "_ghost"));
    }
  }

  private void removePlayer(Player player) {
    ImageView sprite = playerSprites.remove(player);
    if (sprite != null) gamePane.getChildren().remove(sprite);
  }

  private void setupAbility() {
    Image image = new Image(getClass().getResourceAsStream("/assets/abilities/ability.png"));
    abilitySprite = new ImageView(image);
    abilitySprite.setFitWidth(TILE_SIZE * baseScale);
    abilitySprite.setFitHeight(TILE_SIZE * baseScale);
    abilitySprite
        .layoutXProperty()
        .bind(GameModel.getInstance().getAbility().xPosition().multiply(baseScale));
    abilitySprite
        .layoutYProperty()
        .bind(GameModel.getInstance().getAbility().yPosition().multiply(baseScale));
    abilitySprite.visibleProperty().bind(GameModel.getInstance().isAbilityVisibleProperty());
    gamePane.getChildren().add(abilitySprite);
  }

  private void setupWisdomBlindnessOverlay() {
    Label title = new Label("Daily Wisdom");
    title.setStyle(SceneStyle.WISDOM_TITLE);

    Label quote = new Label(WISDOM_BLINDNESS_QUOTE);
    quote.setWrapText(true);
    quote.setMaxWidth(560);
    quote.setStyle(SceneStyle.WISDOM_QUOTE);

    wisdomBlindProgress = new ProgressBar(0.0D);
    wisdomBlindProgress.setMaxWidth(420);

    VBox content = new VBox(18, title, quote, wisdomBlindProgress);
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(40));

    wisdomBlindOverlay = new StackPane(content);
    wisdomBlindOverlay.setAlignment(Pos.CENTER);
    wisdomBlindOverlay.setStyle("-fx-background-color: rgba(43, 43, 43, 0.97);");
    // The overlay hides the map, but it must not steal clicks or keyboard input.
    wisdomBlindOverlay.setMouseTransparent(true);
    wisdomBlindOverlay.setVisible(false);
    gameStack.getChildren().add(wisdomBlindOverlay);
  }

  private void startWisdomBlindnessOverlay() {
    if (wisdomBlindTimeline != null) {
      wisdomBlindTimeline.stop();
    }

    long startedAtMillis = System.currentTimeMillis();
    wisdomBlindOverlay.setVisible(true);
    wisdomBlindProgress.setProgress(0.0D);

    wisdomBlindTimeline =
        new Timeline(
            new KeyFrame(
                Duration.millis(100),
                e -> {
                  double elapsed =
                      (System.currentTimeMillis() - startedAtMillis)
                          / (WISDOM_BLINDNESS_SECONDS * 1000.0D);
                  wisdomBlindProgress.setProgress(Math.min(1.0D, elapsed));
                  if (elapsed >= 1.0D) {
                    GameModel.getInstance().setWisdomBlindnessActive(false);
                  }
                }));
    wisdomBlindTimeline.setCycleCount(Timeline.INDEFINITE);
    wisdomBlindTimeline.play();
  }

  private void hideWisdomBlindnessOverlay() {
    if (wisdomBlindTimeline != null) {
      wisdomBlindTimeline.stop();
    }
    wisdomBlindOverlay.setVisible(false);
    wisdomBlindProgress.setProgress(0.0D);
  }

  private boolean shouldShowWisdomBlessingHint() {
    GameModel model = GameModel.getInstance();
    // The server still owns the 15-second rule; the client only shows local eligibility.
    return model.wisdomBlessingAvailableProperty().get()
        && "HUMAN".equals(model.getRole().get());
  }

  private void refreshWisdomBlessingLabel() {
    boolean visible = shouldShowWisdomBlessingHint();
    wisdomBlessingLabel.setVisible(visible);
    wisdomBlessingLabel.setManaged(visible);
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
