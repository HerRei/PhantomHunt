package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.Map;

/**
 * Represents a 2D spatial coordinate on the game map.
 */
public final class Position {
  private double x;
  private double y;
  private int[] goal_tile; //[y_tile, x_tile]


  public Position(int[] tile, Map map) {
    this.goal_tile = tile;
    this.y = map.tileToPixelPosition(tile[0], tile[1])[0];
    this.x = map.tileToPixelPosition(tile[0], tile[1])[1];
  }

  /**
   * Updates position towards the goal tile.
   * If horizontal movement: aligns Y to tile center. If vertical: aligns X.
   */
  public void updatePosition(InputState input, double speed, double deltaTime, Map map) {
    double[] targetPixel = map.tileToPixelPosition(goal_tile[1], goal_tile[0]);
    double targetY = targetPixel[0];
    double targetX = targetPixel[1];

    if (input.isLeft() || input.isRight()) {
      this.y = targetY; // Align to row
      if (Math.abs(targetX - x) > 3) {
        x += (targetX > x ? 1 : -1) * speed * deltaTime;
      } else {
        x = targetX;
      }
    } else if (input.isUp() || input.isDown()) {
      this.x = targetX; // Align to column
      if (Math.abs(targetY - y) > 3) {
        y += (targetY > y ? 1 : -1) * speed * deltaTime;
      } else {
        y = targetY;
      }
    }
  }

  /**
   * Checks if player is close enough to current tile center to change direction.
   * If valid, updates goal_tile.
   */
  public boolean checkValidInput(InputState nextInput, Map map) {
    double[] currentTilePixel = map.tileToPixelPosition(goal_tile[1], goal_tile[0]);
    double dist = Math.sqrt(Math.pow(currentTilePixel[1] - x, 2) + Math.pow(currentTilePixel[0] - y, 2));

    if (dist < 5) {
      int nextY = goal_tile[0];
      int nextX = goal_tile[1];

      if (nextInput.isUp()) nextY--;
      if (nextInput.isDown()) nextY++;
      if (nextInput.isLeft()) nextX--;
      if (nextInput.isRight()) nextX++;

      Boolean[][] walkingMap = map.getMap();
      // Boundary check and wall check
      if (nextY >= 0 && nextY < walkingMap.length && nextX >= 0 && nextX < walkingMap[0].length) {
        if (walkingMap[nextY][nextX]) {
          this.goal_tile = new int[]{nextY, nextX};
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Creates a copy of this position.
   *
   * @return A new Position object with the same values.
   */
  public Position copy() {
    return new Position(goal_tile, Map.getInstance());
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
