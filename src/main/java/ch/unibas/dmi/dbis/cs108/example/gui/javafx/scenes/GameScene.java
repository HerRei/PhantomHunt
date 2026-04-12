package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.Player;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen; // Import for screen dimensions
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the map screen where the game is played.
 * This class is responsible for displaying the map and providing
 * collision detection logic.
 */
public class GameScene implements SceneInterface {

  private static final Logger LOGGER = LogManager.getLogger(GameScene.class);
  private Scene scene;
  private Image collisionMap;
  private final Pane gamePane;
  private PixelReader pixelReader;
  private final Map<Player, Rectangle> playerShapes = new HashMap<>();
  private boolean w, a, s, d;
  private final double scaleFactor;

  /**
   * Creates a new GameScene.
   * It fetches the necessary map images from the GameModel and sets up
   * the visual components of the scene. The map is scaled to fit the user's screen.
   */
  public GameScene() {
    // Get the model instance
    GameModel model = GameModel.getInstance();

    // Get the map images from the model
    Image mapImage = model.getGameMap();
    ImageView mapView = new ImageView(mapImage);
    gamePane = new Pane();

    // --- Scale the map based on screen size ---
    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    double screenWidth = primaryScreenBounds.getWidth();
    double screenHeight = primaryScreenBounds.getHeight();

    double mapOriginalWidth = mapImage.getWidth();
    double mapOriginalHeight = mapImage.getHeight();

    // Calculate scale factors for width and height
    double scaleFactorWidth = screenWidth / mapOriginalWidth;
    double scaleFactorHeight = screenHeight / mapOriginalHeight;

    // Use the smaller scale factor to ensure the entire map fits on screen
    this.scaleFactor = Math.min(scaleFactorWidth, scaleFactorHeight) * 0.95;

    double scaledWidth = mapOriginalWidth * scaleFactor; 
    double scaledHeight = mapOriginalHeight * scaleFactor;

    mapView.setFitWidth(scaledWidth);
    mapView.setFitHeight(scaledHeight);
    mapView.setPreserveRatio(true); // Maintain aspect ratio

    // Get the collision map from the model
    collisionMap = model.getCollisionMap();
    pixelReader = collisionMap.getPixelReader();

    // Create a layout and add the visual map
    StackPane root = new StackPane();
    root.getChildren().addAll(mapView, gamePane);

    // Set the background of the root pane to black
    root.setStyle("-fx-background-color: black;");

    // Create the scene with the scaled dimensions
    this.scene = new Scene(root, scaledWidth, scaledHeight);

    setupControls();
    setupPlayerTracking();
  }

  /**
   * Checks if a specific coordinate on the map is walkable.
   * It does this by checking the color of the pixel on the collision map.
   * Black pixels are considered walls (not walkable).
   *
   * @param x The x-coordinate to check.
   * @param y The y-coordinate to check.
   * @return true if the coordinate is walkable, false otherwise.
   */
  public boolean isWalkable(int x, int y) {
    // Ensure the coordinates are within the map boundaries
    if (x < 0 || x >= collisionMap.getWidth() || y < 0 || y >= collisionMap.getHeight()) {
      return false; // Out of bounds is not walkable
    }

    // Get the color of the pixel at the given coordinates from the collision map
    Color color = pixelReader.getColor(x, y);

    // Assume black pixels are walls and anything else is walkable.
    return !Color.BLACK.equals(color);
  }

  /**
   * Registers key listeners for WASD movement.
   */
  private void setupControls() {
    scene.setOnKeyPressed(e -> handleKeyEvent(e.getCode(), true));
    scene.setOnKeyReleased(e -> handleKeyEvent(e.getCode(), false));
  }

  private void handleKeyEvent(javafx.scene.input.KeyCode code, boolean pressed) {
    boolean changed = false;
    switch (code) {
      case W -> { if (w != pressed) { w = pressed; changed = true; } }
      case A -> { if (a != pressed) { a = pressed; changed = true; } }
      case S -> { if (s != pressed) { s = pressed; changed = true; } }
      case D -> { if (d != pressed) { d = pressed; changed = true; } }
      default -> {}
    }
    // Only send to server if the state actually changed to save bandwidth
    if (changed) {
      EventHandlers.getInstance().sendInputs(w, s, a, d);
    }
  }
  /**
   * Observes the player list and creates/removes rectangles accordingly.
   */
  private void setupPlayerTracking() {
    GameModel model = GameModel.getInstance();
    model.getPlayers().addListener((ListChangeListener<Player>) change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          for (Player p : change.getAddedSubList()) {
            addPlayerShape(p);
          }
        }
        if (change.wasRemoved()) {
          for (Player p : change.getRemoved()) {
            removePlayerShape(p);
          }
        }
      }
    });

    // Add initially existing players
    for (Player p : model.getPlayers()) {
      addPlayerShape(p);
    }
  }

  /**
   * Creates a rectangle for a player and binds its position.
   */
  private void addPlayerShape(Player player) {
    Rectangle rect = new Rectangle(20, 20); // Width and Height of the player

    // Choose color based on role (skin property)
    // Assume "HUMAN" = Red, others (Phantoms) = White
    if ("HUMAN".equalsIgnoreCase(player.skinProperty().get())) {
      rect.setFill(Color.RED);
    } else {
      rect.setFill(Color.WHITE);
    }
    rect.layoutXProperty().bind(player.xPosition().multiply(scaleFactor));
    rect.layoutYProperty().bind(player.yPosition().multiply(scaleFactor));

    playerShapes.put(player, rect);
    gamePane.getChildren().add(rect);
  }

  private void removePlayerShape(Player player) {
    Rectangle rect = playerShapes.remove(player);
    if (rect != null) {
      gamePane.getChildren().remove(rect);
    }
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}