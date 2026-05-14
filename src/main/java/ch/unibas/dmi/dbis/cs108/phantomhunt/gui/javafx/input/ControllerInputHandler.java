package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.input;

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
    if (!unavailable && pollingTimeline.getStatus() != Timeline.Status.RUNNING) {
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


}