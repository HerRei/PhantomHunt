package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

/**
 * Represents the current movement intentions of a player based on their client input.
 */
public final class InputState {
  private final boolean up;
  private final boolean down;
  private final boolean left;
  private final boolean right;

  /**
   * Constructs a new InputState with the specified directional intentions.
   *
   * @param up    True if player moves upwards, false otherwise.
   * @param down  True if player moves downwards, false otherwise.
   * @param left  True if player moves left, false otherwise.
   * @param right True if player moves right, false otherwise.
   */
  public InputState(boolean up, boolean down, boolean left, boolean right) {
    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
  }

  /**
   * Creates a copy of this InputState.
   *
   * @return A new InputState object with the same values.
   */
  public InputState copy() {
    return new InputState(up, down, left, right);
  }

  public boolean isUp() {
    return up;
  }

  public boolean isDown() {
    return down;
  }

  public boolean isLeft() {
    return left;
  }

  public boolean isRight() {
    return right;
  }

  @Override
  public String toString() {
    return "InputState{up=" + up + ", down=" + down + ", left=" + left + ", right=" + right + "}";
  }
}
