package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import ch.unibas.dmi.dbis.cs108.example.Main;
import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.*;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.*;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.sound.SoundManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main Entry Point for the JavaFX Application.
 * Manages the lifecycle and links Singletons.
 */
public class GUI extends Application {

  /**
   * Initializes the primary stage, sets up the scene infrastructure, and launches the UI.
   *
   * @param primaryStage the primary stage for this application, onto which
   *                     the application scene can be set.
   */
  @Override
  public void start(Stage primaryStage) {
    // Initialize Core Singletons
    GameModel.getInstance();
    EventHandlers.getInstance();
    SoundManager soundManager = SoundManager.getInstance();
    soundManager.initialize();

    // Setup Scene Infrastructure
    SceneManager manager = SceneManager.getInstance();
    manager.setStage(primaryStage);

    // Register Scenes (Scenes fetch Singletons internally)
    manager.addScene(SceneProtocol.HOME, new HubScene());
    manager.addScene(SceneProtocol.NICKNAME, new NicknameScene());
    manager.addScene(SceneProtocol.CREATELOBBY, new CreateLobbyScene());
    manager.addScene(SceneProtocol.JOINLOBBY, new JoinLobbyScene());
    manager.addScene(SceneProtocol.GAME, new GameScene());
    manager.addScene(SceneProtocol.LOBBY, new LobbyScene());

    // Configure Window and Launch
    primaryStage.setTitle("Phantom Hunt");
    primaryStage.setOnCloseRequest(event -> soundManager.shutdown());
    manager.showScene(SceneProtocol.HOME);

    // GUI is now completely initialized
    // We take address form Main and start Client
    if (Main.getTargetHost() != null) {
      new TcpClient(Main.getTargetHost(), Main.getTargetPort());
    }
  }

  /**
   * Fallback entry point for the JavaFX application.
   * This is primarily used to launch the app from IDEs that don't fully support JavaFX natively.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}