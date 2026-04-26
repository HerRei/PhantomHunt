package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.PlayerState;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.Position;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class responsible for translating a visual map image (PNG/JPG) into a 2D array of logical
 * TileTypes for collision detection.
 */
public final class MapLogic {

  private static final Logger LOGGER = LogManager.getLogger(MapLogic.class);
  private static MapLogic instance;
  private static final String tileImage = "/assets/floor-test.png"; // path to tileImg
  private int tileSize;
  private ArrayList<int[]> possibleSpawnPoints;
  private Boolean[][] walkingMap;
  private int recursionDepth = 0;

  public MapLogic(String[][] map) {
    this.walkingMap = loadMapFromString(map);
    resetSpawnPoints();
    try { // 1. Load the image file
      Image imageFile = new Image(getClass().getResourceAsStream(tileImage));
      this.tileSize = (int) imageFile.getHeight();
      instance = this;
    } catch (Exception e) {
      LOGGER.error("Couldn't read the TileImage");
    }
  }

  public static MapLogic getInstance() {
    if (instance == null) {
      return null;
    }
    return instance;
  }

  /** Identifies all walkable spawn points (represented by spaces). */
  public void resetSpawnPoints() {
    possibleSpawnPoints = new ArrayList<int[]>();
    for (int r = 0; r < walkingMap.length; r++) {
      for (int c = 0; c < walkingMap[r].length; c++) {
        if (walkingMap[r][c]) {
          possibleSpawnPoints.add(new int[] {r, c});
        }
      }
    }
  }

  /**
   * Converts a 2D String array representation of a map into a 2D Boolean array. A cell is marked as
   * walkable (true) if it contains a space character, otherwise it is considered an obstacle
   * (false).
   *
   * @param mapImage The 2D String array representing the map layout.
   * @return A 2D Boolean array where true indicates a walkable path.
   */
  public Boolean[][] loadMapFromString(String[][] mapImage) {
    if (mapImage == null) {
      return new Boolean[0][0];
    }

    int rows = mapImage.length;
    int cols = mapImage[0].length;
    Boolean[][] walkableMap = new Boolean[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (!"X".equals(mapImage[i][j])) {
          walkableMap[i][j] = true;
        } else {
          walkableMap[i][j] = false;
        }
      }
    }

    return walkableMap;
  }

  /**
   * Selects a random spawn point, removes it from the list to prevent reuse, and returns it.
   *
   * @return The selected int[] coordinates, or null if no spawn points are left.
   */
  public int[] useRandomSpawnPoint() {
    if (this.possibleSpawnPoints == null || this.possibleSpawnPoints.isEmpty()) {
      return null;
    }

    Random rand = new Random();
    int index = rand.nextInt(this.possibleSpawnPoints.size());
    return this.possibleSpawnPoints.remove(index);
  }

  /**
   * calculates the distance in pixels between a tile and a x/y-coordinate
   *
   * @param tile
   * @param xPosition
   * @param yPosition
   * @return the distance in pixels
   */
  public double calcDistance(int[] tile, double xPosition, double yPosition) {
    double targetX = tileToPixelPosition(tile[1], tile[0])[1];
    double targetY = tileToPixelPosition(tile[1], tile[0])[0];
    double distance =
        Math.sqrt(Math.pow(targetX - xPosition, 2) + Math.pow(targetY - yPosition, 2));
    return distance;
  }

  /**
   * sets a new position in tile[] for a player and makes sure that the point is @distance entitys
   * apart from any other players.
   *
   * @param oldPoint
   * @param players
   * @param distance
   * @return
   */
  public int[] setRandomPosition(int[] oldPoint, List<PlayerState> players, double distance) {
    if (!(oldPoint == null)) possibleSpawnPoints.add(oldPoint);
    int[] possibleSpawnPoint = useRandomSpawnPoint();
    for (PlayerState player : players) {
      if (calcDistance(possibleSpawnPoint, player.getPosition().getX(), player.getPosition().getY())
          < distance) {
        possibleSpawnPoint = setRandomPosition(possibleSpawnPoint, players, distance);
      }
    }
    return possibleSpawnPoint;
  }

  /**
   * creates Spawnpoints and makes sure that they are @distance of entitys apart.
   *
   * @param len
   * @param spawns
   * @param distance
   * @return
   */
  public List<Position> getRandomSpawns(int len, List<Position> spawns, double distance) {
    recursionDepth++;
    if (recursionDepth > 1000) {
      recursionDepth = 0;
      return spawns;
    }

    Position possiblePos = new Position(useRandomSpawnPoint(), this);
    if (len <= 0) {
      recursionDepth = 0;
      return spawns;
    }

    for (Position pos : spawns) {
      if (calcDistance(pos.getLastSpawn(), possiblePos.getX(), possiblePos.getY()) < distance) {
        possibleSpawnPoints.add(possiblePos.getLastSpawn());
        return (getRandomSpawns(len, spawns, distance));
      }
    }
    spawns.add(possiblePos);
    return getRandomSpawns(len - 1, spawns, distance);
  }

  /**
   * calculates tile-Position to pixel-Position
   *
   * @param xTile
   * @param yTile
   * @return
   */
  public double[] tileToPixelPosition(int xTile, int yTile) {
    int y = yTile * tileSize;
    int x = xTile * tileSize;
    return new double[] {y, x};
  }

  /**
   * calculates pixel-Posistion to tile-Position
   *
   * @param x
   * @param y
   * @return
   */
  public int[] pixelToTilePosition(double x, double y) {
    int yTile = (int) y / tileSize;
    int xTile = (int) x / tileSize;
    return new int[] {yTile, xTile};
  }

  public int getPixelWidth(String[][] mapImage) {
    return mapImage[0].length * tileSize;
  }

  public int getPixelHeight(String[][] mapImage) {
    return mapImage[0].length * tileSize;
  }

  public int getTileSize() {
    return tileSize;
  }

  public Boolean[][] getMap() {
    return walkingMap;
  }

  /**
   * Generates an example 19x20 map string array. 'X' represents a wall, and ' ' represents a
   * walkable path.
   */
  /*
  "Q", quadra.
  "L", top_left
  "V", vertical
  "D", down_left
  "R", top_right
  "A", down_right
  "H", horizontal
  "T", triple_top
  "B", triple_down
  "C", triple_left
  "E", triple_right
   */
  public static String[][] generateExampleMap() {
    return new String[][] {
      {
        "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X",
        "X"
      },
      {
        "X", "L", "H", "H", "T", "H", "H", "H", "R", "X", "L", "H", "H", "H", "T", "H", "H", "R",
        "X"
      },
      {
        "X", "V", "X", "X", "V", "X", "X", "X", "V", "X", "V", "X", "X", "X", "V", "X", "X", "V",
        "X"
      },
      {
        "X", "C", "H", "H", "Q", "H", "T", "H", "B", "H", "B", "H", "T", "H", "Q", "H", "H", "E",
        "X"
      },
      {
        "X", "V", "X", "X", "V", "X", "V", "X", "X", "X", "X", "X", "V", "X", "V", "X", "X", "V",
        "X"
      },
      {
        "X", "D", "H", "H", "E", "X", "D", "H", "R", "X", "L", "H", "A", "X", "C", "H", "H", "A",
        "X"
      },
      {
        "X", "X", "X", "X", "V", "X", "X", "X", "V", "X", "V", "X", "X", "X", "V", "X", "X", "X",
        "X"
      },
      {
        "X", "L", "H", "H", "E", "X", "L", "H", "B", "T", "B", "H", "R", "X", "C", "H", "H", "R",
        "X"
      },
      {
        "X", "V", "X", "X", "C", "H", "E", "X", "X", "V", "X", "X", "C", "H", "E", "X", "X", "V",
        "X"
      },
      {
        "X", "D", "R", "X", "V", "X", "V", "X", "X", "V", "X", "X", "V", "X", "V", "X", "L", "A",
        "X"
      },
      {
        "X", "X", "V", "X", "V", "X", "C", "H", "H", "Q", "H", "H", "E", "X", "V", "X", "V", "X",
        "X"
      },
      {
        "X", "L", "B", "H", "E", "X", "V", "X", "X", "V", "X", "X", "V", "X", "C", "H", "B", "R",
        "X"
      },
      {
        "X", "V", "X", "X", "C", "H", "B", "H", "T", "B", "T", "H", "B", "H", "E", "X", "X", "V",
        "X"
      },
      {
        "X", "D", "R", "X", "V", "X", "X", "X", "V", "X", "V", "X", "X", "X", "V", "X", "L", "A",
        "X"
      },
      {
        "X", "X", "V", "X", "V", "X", "L", "H", "B", "H", "B", "H", "R", "X", "V", "X", "V", "X",
        "X"
      },
      {
        "X", "L", "B", "H", "A", "X", "V", "X", "X", "X", "X", "X", "V", "X", "D", "H", "B", "R",
        "X"
      },
      {
        "X", "V", "X", "X", "X", "X", "D", "H", "R", "X", "L", "H", "A", "X", "X", "X", "X", "V",
        "X"
      },
      {
        "X", "V", "X", "X", "X", "X", "X", "X", "V", "X", "V", "X", "X", "X", "X", "X", "X", "V",
        "X"
      },
      {
        "X", "D", "H", "H", "H", "H", "H", "H", "B", "H", "B", "H", "H", "H", "H", "H", "H", "A",
        "X"
      },
      {
        "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X", "X",
        "X"
      }
    };
  }
}
