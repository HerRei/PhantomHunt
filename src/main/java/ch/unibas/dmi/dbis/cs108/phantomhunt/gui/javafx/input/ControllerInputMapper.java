package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.input;

/** Converts raw controller axes and buttons into the game's vertical/horizontal movement format. */
public final class ControllerInputMapper {

  public static final float DEFAULT_DEADZONE = 0.45F;
  public static final MovementInput IDLE = new MovementInput(0, 0);

  private ControllerInputMapper() {}

  /**
   * Converts the left stick and D-pad into one cardinal movement direction.
   *
   * <p>The server movement logic expects the same one-direction input used by the keyboard controls,
   * so diagonal controller input is reduced to its dominant axis.
   */
  public static MovementInput map(
      float leftX,
      float leftY,
      boolean dpadUp,
      boolean dpadDown,
      boolean dpadLeft,
      boolean dpadRight) {
    MovementInput dpadInput = mapDpad(dpadUp, dpadDown, dpadLeft, dpadRight);
    if (dpadInput.isMoving()) {
      return dpadInput;
    }

    float absX = Math.abs(leftX);
    float absY = Math.abs(leftY);
    if (absX < DEFAULT_DEADZONE && absY < DEFAULT_DEADZONE) {
      return IDLE;
    }

    if (absX > absY) {
      return new MovementInput(0, leftX < 0 ? -1 : 1);
    }
    return new MovementInput(leftY < 0 ? -1 : 1, 0);
  }

  private static MovementInput mapDpad(
      boolean dpadUp, boolean dpadDown, boolean dpadLeft, boolean dpadRight) {
    int vertical = 0;
    int horizontal = 0;

    if (dpadUp != dpadDown) {
      vertical = dpadUp ? -1 : 1;
    }
    if (dpadLeft != dpadRight) {
      horizontal = dpadLeft ? -1 : 1;
    }

    if (vertical != 0) {
      return new MovementInput(vertical, 0);
    }
    if (horizontal != 0) {
      return new MovementInput(0, horizontal);
    }
    return IDLE;
  }

  public record MovementInput(int vertical, int horizontal) {
    public boolean isMoving() {
      return vertical != 0 || horizontal != 0;
    }
  }
}
