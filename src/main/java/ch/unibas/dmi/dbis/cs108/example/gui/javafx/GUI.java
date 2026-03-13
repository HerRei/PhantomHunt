package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * This is a first JavaFX-Application, for the
 */

/**
 * This is an example JavaFX-Application.
 */
public class GUI extends Application {

  /**
   * Launching this method will not work on some platforms.
   * What you should do is to create a separate main class and launch the GUI class from there as is done in {@link Main}
   */
  public static void main(String[] args) {

    launch(args);
  }

  /**
   * Sets up and opens the main game window.
   *
   * @param stage
   */
  @Override
  public void start(Stage stage) {
    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
    Scene scene = new Scene(new StackPane(l), 640, 480);
    stage.setScene(scene);
    stage.show();
  }

}
