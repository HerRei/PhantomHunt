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

public class SceneManager {
    private static final Logger LOGGER = LogManager.getLogger(SceneManager.class);
    private static SceneManager instance;
    private Stage stageRef;
    public final Map<SceneProtocol, SceneInterface> scenes = new HashMap<>();

    private SceneManager() {} // Private constructor

    /** Get the single instance of the manager */
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setStage(Stage stage){
        stageRef = stage;
    }

    public void addScene(SceneProtocol type, SceneInterface scene) {
        scenes.put(type, scene);
    }

    public void showScene(SceneProtocol type){
        SceneInterface myScene = scenes.get(type);
        if (myScene != null) {
            // JavaFX-Thread-Rule (Platform.runLater)
            Platform.runLater(() -> {
                if (stageRef != null) {
                    stageRef.setScene(myScene.getScene());
                    stageRef.show();
                } else {
                    LOGGER.error("Stage reference is null. Did you forget to call setStage()?");
                }
            });

        }
        else{
            LOGGER.error("Tried to show a scene that doesn't exist");
        }
    }
}