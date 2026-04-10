package ch.unibas.dmi.dbis.cs108.example.server.game.util;

import ch.unibas.dmi.dbis.cs108.example.server.game.state.TileType;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MapLoader {

  private static final Logger LOGGER = LogManager.getLogger(MapLoader.class);
  private static final int BLACK_RGB = Color.BLACK.getRGB();

  private MapLoader() {
    // Utility class should not be instantiated
  }

  public static TileType[][] loadMapFromImage(String resourcePath) {
    try (InputStream stream = MapLoader.class.getResourceAsStream(resourcePath)) {
      Objects.requireNonNull(stream, "Map resource not found: " + resourcePath);
      BufferedImage image = ImageIO.read(stream);

      int width = image.getWidth();
      int height = image.getHeight();
      TileType[][] map = new TileType[height][width];

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          map[y][x] = (image.getRGB(x, y) == BLACK_RGB) ? TileType.WALL : TileType.FLOOR;
        }
      }
      LOGGER.info("Successfully loaded map '{}' ({}x{})", resourcePath, width, height);
      return map;
    } catch (IOException e) {
      LOGGER.error("Failed to load map from resource: {}", resourcePath, e);
      throw new RuntimeException("Could not load map resource", e);
    }
  }
}