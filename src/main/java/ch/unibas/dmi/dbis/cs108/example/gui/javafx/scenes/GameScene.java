package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen; // Import for screen dimensions

/**
 * Represents the map screen where the game is played.
 * Handles the display of the map and provides pixel-based collision detection.
 */
public class GameScene implements SceneInterface {

  private Scene scene;
  private Image collisionMap;
  private PixelReader pixelReader;

  /**
   * Initializes the game scene, fetching the map images and scaling them to the screen.
   */
  public GameScene() {
    // Get the model instance
    GameModel model = GameModel.getInstance();

    // Get the map images from the model
    Image mapImage = model.getGameMap();
    ImageView mapView = new ImageView(mapImage);

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
    double scaleFactor = Math.min(scaleFactorWidth, scaleFactorHeight);

    double scaledWidth = mapOriginalWidth * scaleFactor * 0.95; // reduce dimensions to
    double scaledHeight = mapOriginalHeight * scaleFactor * 0.95; // account for taskbar

    mapView.setFitWidth(scaledWidth);
    mapView.setFitHeight(scaledHeight);
    mapView.setPreserveRatio(true); // Maintain aspect ratio

    // Get the collision map from the model
    collisionMap = model.getCollisionMap();
    pixelReader = collisionMap.getPixelReader();

    // Create a layout and add the visual map
    StackPane root = new StackPane();
    root.getChildren().add(mapView);

    // Set the background of the root pane to black
    root.setStyle("-fx-background-color: black;");

    // Create the scene with the scaled dimensions
    this.scene = new Scene(root, scaledWidth, scaledHeight);
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

  @Override
  public Scene getScene() {
    return scene;
  }
}