package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

/**
 * Represents a 2D spatial coordinate on the game map.
 */
public final class Position {
  private double x;
  private double y;

  public Position(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Position copy() {
    return new Position(x, y);
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
