package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class responsible for translating a visual map image (PNG/JPG)
 * into a 2D array of logical TileTypes for collision detection.
 */
public final class Map {

  private static final Logger LOGGER = LogManager.getLogger(Map.class);
  private static Map instance;
  private static final String tileImage = "path to image"; //path to tileImg
  private int tileSize;
  private ArrayList<int[]> possibleSpawnPoints;
  private Boolean[][] walkingMap;

  public Map(String[][] map) {
    this.walkingMap = loadMapFromString(map);
    this.possibleSpawnPoints = getSpawnPoints(map);
    try{// 1. Load the image file
      File imageFile = new File(tileImage);
      BufferedImage image = ImageIO.read(imageFile);
      this.tileSize = image.getHeight();
      instance = this;}
    catch (Exception e) {
      LOGGER.error("Couldn't read the TileImage");
    }
  }

  public static Map getInstance() {
    if(instance == null){
      return null;
    }
    return instance;
  }

  /**
   * Identifies all walkable spawn points (represented by spaces).
   * @param mapImage The 2D map source.
   * @return Array of coordinates [row, col] for each spawn point.
   */
  public static ArrayList<int[]> getSpawnPoints(String[][] mapImage) {
    if (mapImage == null) return null;

    ArrayList<int[]> points = new ArrayList<>();

    for (int r = 0; r < mapImage.length; r++) {
      for (int c = 0; c < mapImage[r].length; c++) {
        if (" ".equals(mapImage[r][c])) {
          points.add(new int[]{r, c});
        }
      }
    }

    return points;
  }

  /**
   * Converts a 2D String array representation of a map into a 2D Boolean array.
   * A cell is marked as walkable (true) if it contains a space character,
   * otherwise it is considered an obstacle (false).
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
        // Check if the current string is a space to determine walkability
        if (" ".equals(mapImage[i][j])) {
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
   * @return The selected int[] coordinates, or null if no spawn points are left.
   */
  public int[] useRandomSpawnPoint() {
    if (this.possibleSpawnPoints == null || this.possibleSpawnPoints.isEmpty()) {
      return null;
    }

    Random rand = new Random();
    // Select a random index and remove the element at that position
    int index = rand.nextInt(this.possibleSpawnPoints.size());
    return this.possibleSpawnPoints.remove(index);
  }

  public double calcDistance(int[] tile, double xPosition, double yPosition){
    double targetX = tileToPixelPosition(tile[1], tile[0])[1];
    double targetY = tileToPixelPosition(tile[1], tile[0])[0];
    return Math.sqrt(Math.pow(targetX- xPosition, 2) + Math.pow(targetY- yPosition, 2));
  }

  public double[] tileToPixelPosition(int xTile, int yTile){
    int y = yTile * tileSize;
    int x = xTile * tileSize;
    return new double[]{y, x};
  }

  public int[] pixelToTilePosition(double x, double y){
    int yTile = (int)y/tileSize;
    int xTile = (int)x/tileSize;
    return new int[]{yTile, xTile};
  }

  public int getPixelWidth(String[][] mapImage){return mapImage[0].length * tileSize;}

  public int getPixelHeight(String[][] mapImage){ return mapImage[0].length * tileSize;}

  public Boolean[][] getMap() {
    return walkingMap;
  }


}