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
 * This is a first JavaFX-Application, for the first Scene: LoadingScene
 */
public class GUI extends Application {
  private Stage stage;
  private ClientApp clientApp;
  //private String nickname;
  private HomeScene currentHomeScene;

  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Sets up and opens the main game vidow
   *
   * @param stage
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
   * Shows the loading scene.
   */
  public void showLoadingScene() {
    LoadingScene loadingScene = new LoadingScene();
    Scene scene = loadingScene.createScene(this::showEnterNicknameScene);
    stage.setScene(scene);
  }

  public void showEnterNicknameScene() {
    ClientApp.setGlobalMessageListener(null); //nicht mehr zu message listerner wenn bei change nickname
    ClientApp.setWhisperMessageListener(null); //gilt auch für wisper
    EnterNicknameScene enterNicknameScene = new EnterNicknameScene();
    String currentNickname = ClientApp.getConfirmedNickname();
    Scene scene = enterNicknameScene.createScene(this::handleNicknameEntered, currentNickname); //hier nächste action in this:: something, für nächse action
    stage.setScene(scene);
  }

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
   * Shows the home scene.
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
