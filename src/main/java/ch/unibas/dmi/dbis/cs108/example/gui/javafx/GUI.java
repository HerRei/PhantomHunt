package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene.LoadingScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;




/**
 * This is a first JavaFX-Application, for the first Scene: LoadingScene
 */
public class GUI extends Application {
  private Stage stage;

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
    showLoadingScene();
    this.stage.show();
  }

  /**
   * Shows the loading scene.
   */
  public void showLoadingScene() {
    LoadingScene loadingScene = new LoadingScene();
    Scene scene = loadingScene.createScene();
    stage.setScene(scene);
  }
}
