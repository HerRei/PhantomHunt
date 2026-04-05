package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.*;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.*;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.sound.SoundManager;
import javafx.application.Application;
import javafx.stage.Stage;

/*
 * Main Entry Point for the JavaFX Application.
 * Manages the lifecycle and links Singletons.
 */
public class GUI extends Application {

  @Override
  public void start(Stage primaryStage) {
    // Create the model instance. It is no longer a Singleton.
    GameModel model = new GameModel();

    // Initialize other Singletons (can be refactored later if needed)
    EventHandlers.getInstance();
    SoundManager soundManager = SoundManager.getInstance();
    soundManager.initialize();

    // Setup Scene Infrastructure
    SceneManager manager = SceneManager.getInstance();
    manager.setStage(primaryStage);

    // Register Scenes, injecting the model where needed
    manager.addScene(SceneProtocol.HOME, new HubScene());
    manager.addScene(SceneProtocol.NICKNAME, new NicknameScene());
    manager.addScene(SceneProtocol.CREATELOBBY, new CreateLobbyScene());
    manager.addScene(SceneProtocol.JOINLOBBY, new JoinLobbyScene());
    manager.addScene(SceneProtocol.GAME, new GameScene(model));

    // Configure Window and Launch
    primaryStage.setTitle("Phantom Hunt");
    primaryStage.setOnCloseRequest(event -> soundManager.shutdown());
    manager.showScene(SceneProtocol.NICKNAME);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
