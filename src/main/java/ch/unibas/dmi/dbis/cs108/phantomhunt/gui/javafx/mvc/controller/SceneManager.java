package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.SceneInterface;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.SceneProtocol;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/** Singleton manager responsible for handling and switching between JavaFX scenes. */
public class SceneManager {
  private static final Logger LOGGER = LogManager.getLogger(SceneManager.class);
  private static SceneManager instance;
  private double width = 900;
  private double height = 640;
  private boolean fullscreen;
  private Stage stageRef;
  private SceneProtocol currentScene;
  private final Map<SceneProtocol, SceneInterface> scenes = new HashMap<>();
  private final Set<Scene> managedScenes = Collections.newSetFromMap(new IdentityHashMap<>());

  private SceneManager() {}

  /**
   * Retrieves the singleton instance of the SceneManager.
   *
   * @return the singleton instance
   */
  public static synchronized SceneManager getInstance() {
    if (instance == null) {
      instance = new SceneManager();
    }
    return instance;
  }

  /**
   * Sets the primary stage of the JavaFX application.
   *
   * @param stage the primary stage
   */
  public void setStage(Stage stage) {
    this.stageRef = stage;
    stage.setWidth(width);
    stage.setHeight(height);
    stage.setFullScreenExitHint("");
    stage.widthProperty().addListener((observable, oldValue, newValue) -> updateWidth(newValue));
    stage.heightProperty().addListener((observable, oldValue, newValue) -> updateHeight(newValue));
    stage.fullScreenProperty()
        .addListener((observable, oldValue, newValue) -> fullscreen = newValue);
  }

  /**
   * Registers a new scene with the manager.
   *
   * @param type the protocol enum identifying the scene
   * @param scene the scene interface implementation
   */
  public void addScene(SceneProtocol type, SceneInterface scene) {
    scenes.put(type, scene);
  }

  /**
   * Switches the active scene on the primary stage. Automatically executes on the JavaFX
   * Application Thread.
   *
   * @param type the protocol enum identifying the scene to show
   */
  public void showScene(SceneProtocol type) {
    SceneInterface myScene = scenes.get(type);
    if (myScene != null) {
      this.currentScene = type;
      MenuMusicController.getInstance().onSceneChanged(type);
      Platform.runLater(
          () -> {
            if (stageRef != null) {
              boolean keepFullscreen = fullscreen || stageRef.isFullScreen();
              saveStageSize();
              Scene scene = myScene.getScene();
              registerFullscreenShortcut(scene);
              stageRef.setScene(scene);
              stageRef.setWidth(width);
              stageRef.setHeight(height);
              fullscreen = keepFullscreen;
              stageRef.setFullScreen(keepFullscreen);
              stageRef.show();
              if (keepFullscreen) {
                Platform.runLater(
                    () -> {
                      fullscreen = true;
                      stageRef.setFullScreen(true);
                    });
              }
            } else {
              LOGGER.error("Stage reference is null. Did you forget to call setStage()?");
            }
          });

    } else {
      LOGGER.error("Tried to show a scene that doesn't exist: {}", type);
    }
  }

  public SceneProtocol getCurrentScene() {
    return currentScene;
  }

  public SceneInterface getScene(SceneProtocol type) {
    return scenes.get(type);
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  public boolean isFullscreen() {
    return fullscreen;
  }

  public void toggleFullscreen() {
    if (stageRef != null) {
      fullscreen = !stageRef.isFullScreen();
      stageRef.setFullScreen(fullscreen);
    }
  }

  public void setFullscreen(boolean value) {
    if (stageRef != null) {
      fullscreen = value;
      stageRef.setFullScreen(value);
    }
  }

  public void registerFullscreenShortcut(Scene scene) {
    if (scene == null || !managedScenes.add(scene)) {
      return;
    }

    scene.addEventFilter(
            javafx.scene.input.KeyEvent.KEY_PRESSED,
            event -> {
              boolean windowsFullscreen =
                      event.getCode() == KeyCode.F11;
              boolean macFullscreen =
                      event.getCode() == KeyCode.F
                              && event.isControlDown()
                              && event.isMetaDown();

              if (windowsFullscreen || macFullscreen) {
                toggleFullscreen();
                event.consume();
              }
            });
  }

  private void updateWidth(Number value) {
    if (!fullscreen && value.doubleValue() > 0) {
      width = value.doubleValue();
    }
  }

  private void updateHeight(Number value) {
    if (!fullscreen && value.doubleValue() > 0) {
      height = value.doubleValue();
    }
  }

  private void saveStageSize() {
    if (stageRef != null && !stageRef.isFullScreen()) {
      width = stageRef.getWidth();
      height = stageRef.getHeight();
    }
  }
}
