package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

/**
 * Represents the current movement intentions of a player based on their client input.
 */
public final class InputState {
  private final int vertical;
  private final int horizontal;

  /**
   * Constructs a new InputState with the specified directional intentions.
   *
   * @param vertical    -1 if player moves upwards, 1 upwards, 0 otherwise.
   * @param horizontal  -1 if player moves to the left, 1 to the right, 0 otherwise.
   */
  public InputState(int vertical, int horizontal) {
    this.vertical = vertical;
    this.horizontal = horizontal;
  }

  /**
   * Creates a copy of this InputState.
   *
   * @return A new InputState object with the same values.
   */
  public InputState copy() {
    return new InputState(vertical, horizontal);
  }

  public boolean isUp() {
    return vertical == -1;
  }

  public boolean isDown() {
    return vertical == 1;
  }

  public boolean isLeft() {
    return horizontal == -1;
  }

  public boolean isRight() {
    return horizontal == 1;
  }

  @Override
  public String toString() {
    return "InputState{vertical=" + vertical + ", horizontal=" + horizontal +"}";
  }
}
