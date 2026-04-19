package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a 2D spatial coordinate on the game map.
 */
public class Position {
  private static final Logger LOGGER = LogManager.getLogger(Position.class);
  private static final int REACTIONNUMBER = 4;
  private double x;
  private double y;
  private int[] goal_tile; //[y_tile, x_tile]


  public Position(int[] tile, Map map) {
    this.goal_tile = tile;
    this.y = map.tileToPixelPosition(tile[1], tile[0])[0];
    this.x = map.tileToPixelPosition(tile[1], tile[0])[1];
  }

  /**
   * Updates position towards the goal tile.
   */
  public void updatePosition(InputState input, double speed, double deltaTime, Map map) {
    double[] targetPixel = map.tileToPixelPosition(goal_tile[1], goal_tile[0]);
    double targetY = targetPixel[0];
    double targetX = targetPixel[1];

    if (input.getHorizontal() != 0) {
      this.y = targetY;
      double step = input.getHorizontal() * speed * deltaTime;

      if (Math.abs(targetX - x) < Math.abs(step)) {
        x = targetX;
      } else {
        x += step;
      }
    }
    else if (input.getVertical() != 0) {
      this.x = targetX;
      double step = input.getVertical() * speed * deltaTime;

      if (Math.abs(targetY - y) < Math.abs(step)) {
        y = targetY;
      } else {
        y += step;
      }
    }
  }

  /**
   * Validates and updates the goal tile based on input, implementing Pac-Man style movement.
   * Reversing or continuing on the same axis is permitted immediately.
   * Turns (90-degree changes) are buffered until the player is near the current tile center.
   *
   * @param nextInput The desired input state (direction) from the player.
   * @param map The game map for collision checking.
   * @return true if the goal tile was updated to a new walkable neighbor.
   */
  public boolean checkValidInput(InputState old,InputState nextInput, Map map) {
    if (nextInput == null || !nextInput.isMoving()) return false;

    // Determine current alignment to the goal tile center in pixels
    double[] targetPixel = map.tileToPixelPosition(goal_tile[1], goal_tile[0]);

    // Check if we are currently aligned with the horizontal or vertical axis of the goal
    int currentlyHorizontal = old.getHorizontal();
    int currentlyVertical = old.getVertical();

    int inputIsHorizontal = nextInput.getHorizontal();
    int inputIsVertical = nextInput.getVertical();

    // 1. Same-axis logic: Always allow moving/reversing on the current axis of travel
    if ((currentlyHorizontal + inputIsHorizontal == 0) &&  (currentlyVertical + inputIsVertical == 0) && map.calcDistance(goal_tile, x, y) < (map.getTileSize())/2d) {
     return tryUpdateGoal(nextInput, map);
    }

    // 2. Turn logic (Pac-Man style): Only allow 90-degree turns near the tile center
    double distance = map.calcDistance(goal_tile, x, y);
    if (distance < REACTIONNUMBER) {
      return tryUpdateGoal(nextInput, map);
    }

    return false;
  }

  /**
   * Internal helper to check walkability and update the goal_tile coordinates.
   */
  private boolean tryUpdateGoal(InputState input, Map map) {
    // Fix: Corrected indices for horizontal/vertical update
    int nextY = goal_tile[0] + input.getVertical();
    int nextX = goal_tile[1] + input.getHorizontal();

    try {
      // Check the walking map for obstacles
      if (map.getMap()[nextY][nextX]) {
        this.goal_tile[0] = nextY;
        this.goal_tile[1] = nextX;
        return true;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      LOGGER.trace("Map boundary reached at [{}, {}]", nextY, nextX);
    }
    return false;
  }

  /**
   * Creates a copy of this position.
   *
   * @return A new Position object with the same values.
   */
  public Position copy() {
    return new Position(new int[]{goal_tile[0], goal_tile[1]}, Map.getInstance());
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }

  @Override
  public String toString() {
    return "Position{x=" + x + ", y=" + y + "}";
  }
}
