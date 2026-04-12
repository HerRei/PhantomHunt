package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.example.client.ClientApp;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.SceneProtocol;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.SceneInterface;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/** Singleton manager responsible for handling and switching between JavaFX scenes.*/
public class SceneManager {
    private static final Logger LOGGER = LogManager.getLogger(SceneManager.class);
    private static SceneManager instance;
    private Stage stageRef;
    private final Map<SceneProtocol, SceneInterface> scenes = new HashMap<>();

    private SceneManager() {} // Private constructor

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
    public void setStage(Stage stage){
        this.stageRef = stage;
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
     * Switches the active scene on the primary stage.
     * Automatically executes on the JavaFX Application Thread.
     *
     * @param type the protocol enum identifying the scene to show
     */
    public void showScene(SceneProtocol type){
        SceneInterface myScene = scenes.get(type);
        if (myScene != null) {
            Platform.runLater(() -> {
                if (stageRef != null) {
                    stageRef.setScene(myScene.getScene());
                    stageRef.show();
                } else {
                    LOGGER.error("Stage reference is null. Did you forget to call setStage()?");
                }
            });

        } else{
            LOGGER.error("Tried to show a scene that doesn't exist: {}", type);
        }
    }
}