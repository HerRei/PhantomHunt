package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.*;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.*;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Logger;

/*
 * Main Entry Point for the JavaFX Application.
 * Manages the lifecycle and links Singletons.
 */
public class GUI extends Application {

  @Override
  public void start(Stage primaryStage) {
    // Initialize Core Singletons
    GameModel model = GameModel.getInstance();
    EventHandlers handlers = EventHandlers.getInstance();

    // Setup Scene Infrastructure
    SceneManager manager = SceneManager.getInstance();
    manager.setStage(primaryStage);

    // Register Scenes (Scenes fetch Singletons internally)
    manager.addScene(SceneProtocol.HOME, new HubScene());
    manager.addScene(SceneProtocol.NICKNAME, new NicknameScene());
    manager.addScene(SceneProtocol.CREATELOBBY, new CreateLobbyScene());
    manager.addScene(SceneProtocol.JOINLOBBY, new JoinLobbyScene());

    // Configure Window and Launch
    primaryStage.setTitle("Phantom Hunt");
    manager.showScene(SceneProtocol.NICKNAME);
  }
  public static void main(String[] args) {
    launch(args);
  }
}