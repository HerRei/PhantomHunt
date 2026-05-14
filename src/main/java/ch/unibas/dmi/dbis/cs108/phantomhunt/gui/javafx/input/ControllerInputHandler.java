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
}