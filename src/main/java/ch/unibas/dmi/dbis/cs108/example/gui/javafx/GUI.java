package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene.LoadingScene;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene.EnterNicknameScene;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene.HomeScene;
import ch.unibas.dmi.dbis.cs108.example.client.ClientApp;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.util.function.BiConsumer; //für funnktionen mit zwei eingaben



/**
 * Main JavaFX application class.
 * It owns the main stage and switches between the different client scenes.
 */
public class GUI extends Application {
  private Stage stage;
  private ClientApp clientApp;
  //private String nickname;
  private HomeScene currentHomeScene;

  /**
   * Starts the JavaFX application from a regular main method.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Initializes the primary stage, creates the client connection and shows the first scene.
   *
   * @param stage the primary JavaFX stage
   */
  @Override
  public void start(Stage stage) {
    this.stage = stage;
    this.stage.setTitle("Phantom Hunt");
    this.clientApp = new ClientApp();
    showLoadingScene();
    this.stage.show();

  }

  /**
   * Creates and displays the loading scene.
   */
  public void showLoadingScene() {
    LoadingScene loadingScene = new LoadingScene();
    Scene scene = loadingScene.createScene(this::showEnterNicknameScene);
    stage.setScene(scene);
  }

  /**
   * Creates and displays the nickname entry scene.
   * Existing chat listeners are removed before switching away from the home scene.
   */
  public void showEnterNicknameScene() {
    ClientApp.setGlobalMessageListener(null); //nicht mehr zu message listerner wenn bei change nickname
    ClientApp.setWhisperMessageListener(null); //gilt auch für wisper
    EnterNicknameScene enterNicknameScene = new EnterNicknameScene();
    String currentNickname = ClientApp.getConfirmedNickname();
    Scene scene = enterNicknameScene.createScene(this::handleNicknameEntered, currentNickname); //hier nächste action in this:: something, für nächse action
    stage.setScene(scene);
  }

  /**
   * Sends the entered nickname to the client logic and waits briefly for confirmation.
   * Once a nickname is available, the home scene is shown.
   *
   * @param nickname the nickname entered by the user
   */
  public void handleNicknameEntered(String nickname) { //ersatzt
    ClientApp.setConfirmedNickname(null); //takes old name out of cache
    boolean success = clientApp.setNickname(nickname);

    if (!success) {
      return;
    }

    new Thread(() -> {
      for (int i = 0; i < 20; i++) {
        String confirmedNickname = ClientApp.getConfirmedNickname();
        if (confirmedNickname != null && !confirmedNickname.isBlank()) {
          Platform.runLater(() -> showHomeScene(confirmedNickname));
          return;
        }
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      Platform.runLater(() -> showHomeScene(nickname));
    }).start();
  }

  /**
   * Creates and displays the home scene and installs the message listeners used by the UI.
   *
   * @param nickname the nickname currently shown in the home scene
   */
  public void showHomeScene(String nickname) {
    //aktuelle home sc
    currentHomeScene = new HomeScene();
    //hier nächste action in this:: something, für nächse action
    //aktuelle homescene wird gespeichert
    Scene scene = currentHomeScene.createScene(
        nickname,
        this::showEnterNicknameScene,
        clientApp::sendGlobalMessage,
        clientApp::sendWhisper
    );

    ClientApp.setGlobalMessageListener(message ->
        Platform.runLater(() -> currentHomeScene.appendGlobalMessage(message))
    );

    ClientApp.setWhisperMessageListener(message ->
    Platform.runLater(() -> currentHomeScene.appendWhisperMessage(message))
    );

    stage.setScene(scene);
  }
}
