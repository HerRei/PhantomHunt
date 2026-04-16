package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.Player;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
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
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the map screen where the game is played.
 * Handles the display of the map, players, game chat, and pixel-based collision detection.
 */
public class GameScene implements SceneInterface {
  private final Scene scene;
  private final Pane gamePane = new Pane();
  private final Map<Player, Rectangle> playerShapes = new HashMap<>();
  private final TextArea chatArea = new TextArea();
  private final TextField chatInput = new TextField();
  private boolean w;
  private boolean a;
  private boolean s;
  private boolean d;
  private final double mapScale;

  /**
   * Initializes the game scene, fetching the map images and scaling them to the screen.
   */
  public GameScene() {
    GameModel model = GameModel.getInstance();
    Image mapImage = model.getGameMap();
    ImageView mapView = new ImageView(mapImage);
    mapView.setPreserveRatio(true);
    mapView.setFitHeight(720);

    this.mapScale = 720.0 / mapImage.getHeight();

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

  private VBox createSidebar(GameModel model) {
    VBox box = new VBox(15);
    box.setPadding(new Insets(15));
    box.setPrefWidth(250);
    box.setPrefHeight(720);
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

    box.getChildren().addAll(statusBox, new Separator(), scoreBox, tableLabel, table, chatLabel, chatArea, chatInput);
    VBox.setVgrow(chatArea, Priority.ALWAYS);

    return box;
  }

  private void setupChatBinding() {
    GameModel.getInstance().lobbyChatMessagesProperty().addListener((ListChangeListener<String>) c -> {
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
    if (msg.isEmpty()) {
      return;
    }
    EventHandlers.getInstance().sendMessage(Command.YAP, msg);
    chatInput.clear();
  }

  private void setupControls() {
    scene.setOnKeyPressed(e -> handleKey(e.getCode()));
  }

  private void handleKey(KeyCode code) {
    if (chatInput.isFocused()) {
      return;
    }
    int horizontal = 0, vertical = 0;
    boolean changed = false;
    switch (code) {
      case W -> {
        if (w != true) {
          w = true;
          a = false;
          s = false;
          d = false;
          horizontal = 0;
          vertical = 1;
          changed = true;
        }
      }
      case S -> {
        if (s != true) {
          w = false;
          a = false;
          s = true;
          d = false;
          horizontal = 0;
          vertical = 1;
          changed = true;
        }
      }
      case A -> {
        if (a != true) {
          w = false;
          a = false;
          s = false;
          d = false;
          horizontal = -1;
          vertical = 0;
          changed = true;
        }
      }
      case D -> {
        if (d != true) {
          w = false;
          a = false;
          s = false;
          d = true;
          horizontal = 1;
          vertical = 0;
          changed = true;
        }
      }
      default -> {
      }
    }
    if (changed) {
      EventHandlers.getInstance().sendInputs(vertical, horizontal);
    }
  }

  private void setupPlayerTracking() {
    GameModel.getInstance().getPlayers().addListener((ListChangeListener<Player>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(this::addPlayer);
        }
        if (c.wasRemoved()) {
          c.getRemoved().forEach(this::removePlayer);
        }
      }
    });
    GameModel.getInstance().getPlayers().forEach(this::addPlayer);
  }

  private void addPlayer(Player player) {
    Rectangle shape = new Rectangle(20, 20);
    double playerHalfWidth = shape.getWidth() / 2;
    double playerHalfHeight = shape.getHeight() / 2;

    player.skinProperty().addListener((obs, oldSkin, newSkin) -> updatePlayerColor(shape, newSkin));
    updatePlayerColor(shape, player.skinProperty().get());

    shape.layoutXProperty().bind(player.xPosition().multiply(mapScale).subtract(playerHalfWidth));
    shape.layoutYProperty().bind(player.yPosition().multiply(mapScale).subtract(playerHalfHeight));

    playerShapes.put(player, shape);
    gamePane.getChildren().add(shape);
  }

  private void updatePlayerColor(Rectangle shape, String skin) {
    if ("HUMAN".equalsIgnoreCase(skin)) {
      shape.setFill(Color.RED);
    } else {
      shape.setFill(Color.WHITE);
    }
  }

  private void removePlayer(Player player) {
    Rectangle shape = playerShapes.remove(player);
    if (shape != null) {
      gamePane.getChildren().remove(shape);
    }
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
