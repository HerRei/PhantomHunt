package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class GameScene implements SceneInterface {

  private Scene scene;
  private Image collisionMap;
  private PixelReader pixelReader;

  public GameScene(GameModel model) {
    // Get the map images from the model
    Image mapImage = model.getGameMap();
    ImageView mapView = new ImageView(mapImage);

    // Get the collision map from the model
    collisionMap = model.getCollisionMap();
    pixelReader = collisionMap.getPixelReader();

    // Create a layout and add the visual map
    StackPane root = new StackPane();
    root.getChildren().add(mapView);

    // Set the background of the root pane to black
    root.setStyle("-fx-background-color: black;");

    // Create the scene
    this.scene = new Scene(root, mapImage.getWidth(), mapImage.getHeight());
  }

  /**
   * Checks if a given coordinate is walkable.
   *
   * @param x The x-coordinate to check.
   * @param y The y-coordinate to check.
   * @return {@code true} if the pixel at the given coordinates on the collision map is not black, {@code false} otherwise.
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