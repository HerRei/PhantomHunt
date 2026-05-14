package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.input;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.util.Objects;
import java.util.function.BiConsumer;

/** Polls a connected gamepad and forwards movement changes to the existing input pipeline. */
public final class ControllerInputHandler {

  private static final Logger LOGGER = LogManager.getLogger(ControllerInputHandler.class);
  private static final Duration POLL_INTERVAL = Duration.millis(50);

  private final BiConsumer<Integer, Integer> movementConsumer;
  private final Runnable primaryAction;
  private final Timeline pollingTimeline;
  private final GLFWGamepadState gamepadState = GLFWGamepadState.create();

  private ControllerInputMapper.MovementInput lastMovement = ControllerInputMapper.IDLE;
  private boolean lastPrimaryPressed;
  private boolean glfwInitialized;
  private boolean unavailable;
  private int activeJoystick = -1;

  public ControllerInputHandler(
      BiConsumer<Integer, Integer> movementConsumer, Runnable primaryAction) {
    this.movementConsumer = Objects.requireNonNull(movementConsumer);
    this.primaryAction = Objects.requireNonNull(primaryAction);
    this.pollingTimeline = new Timeline(new KeyFrame(POLL_INTERVAL, event -> pollController()));
    this.pollingTimeline.setCycleCount(Timeline.INDEFINITE);
  }

  public void start() {
    if (!unavailable && pollingTimeline.getStatus() != Animation.Status.RUNNING) {
      pollingTimeline.play();
    }
  }

  public void stop() {
    pollingTimeline.stop();
    activeJoystick = -1;
    lastMovement = ControllerInputMapper.IDLE;
    lastPrimaryPressed = false;
  }

  private void pollController() {
    if (!ensureGlfwInitialized()) {
      return;
    }

    int joystick = findActiveJoystick();
    if (joystick == -1) {
      return;
    }

    try {
      if (!GLFW.glfwGetGamepadState(joystick, gamepadState)) {
        disconnectActiveJoystick();
        return;
      }

      handleMovement(gamepadState);
      handlePrimaryAction(gamepadState);
    } catch (Throwable e) {
      unavailable = true;
      pollingTimeline.stop();
      LOGGER.warn("Controller support was disabled after a polling error.", e);
    }
  }

  private boolean ensureGlfwInitialized() {
    if (glfwInitialized) {
      return true;
    }
    if (unavailable) {
      return false;
    }

    try {
      glfwInitialized = GLFW.glfwInit();
      if (!glfwInitialized) {
        unavailable = true;
        pollingTimeline.stop();
        LOGGER.warn("Controller support is unavailable because GLFW could not be initialized.");
      }
      return glfwInitialized;
    } catch (Throwable e) {
      unavailable = true;
      pollingTimeline.stop();
      LOGGER.warn("Controller support is unavailable on this system.", e);
      return false;
    }
  }

  private int findActiveJoystick() {
    if (activeJoystick != -1 && isUsableGamepad(activeJoystick)) {
      return activeJoystick;
    }

    if (activeJoystick != -1) {
      disconnectActiveJoystick();
    }

    for (int joystick = GLFW.GLFW_JOYSTICK_1; joystick <= GLFW.GLFW_JOYSTICK_LAST; joystick++) {
      if (isUsableGamepad(joystick)) {
        activeJoystick = joystick;
        LOGGER.info("Controller connected: {}", GLFW.glfwGetGamepadName(joystick));
        return activeJoystick;
      }
    }

    return -1;
  }

  private boolean isUsableGamepad(int joystick) {
    return GLFW.glfwJoystickPresent(joystick) && GLFW.glfwJoystickIsGamepad(joystick);
  }

  private void disconnectActiveJoystick() {
    LOGGER.info("Controller disconnected.");
    activeJoystick = -1;
    lastMovement = ControllerInputMapper.IDLE;
    lastPrimaryPressed = false;
  }

  private void handleMovement(GLFWGamepadState state) {
    ControllerInputMapper.MovementInput movement =
        ControllerInputMapper.map(
            state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X),
            state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y),
            isPressed(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP),
            isPressed(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN),
            isPressed(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT),
            isPressed(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT));

    if (movement.equals(lastMovement)) {
      return;
    }

    lastMovement = movement;
    if (movement.isMoving()) {
      movementConsumer.accept(movement.vertical(), movement.horizontal());
    }
  }

  private void handlePrimaryAction(GLFWGamepadState state) {
    boolean primaryPressed = isPressed(state, GLFW.GLFW_GAMEPAD_BUTTON_A);
    if (primaryPressed && !lastPrimaryPressed) {
      primaryAction.run();
    }
    lastPrimaryPressed = primaryPressed;
  }

  private boolean isPressed(GLFWGamepadState state, int button) {
    return state.buttons(button) == GLFW.GLFW_PRESS;
  }
}
