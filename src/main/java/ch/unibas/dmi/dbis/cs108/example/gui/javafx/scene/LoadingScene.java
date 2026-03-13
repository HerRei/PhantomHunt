package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Builds a simple loading scene for the JavaFX client.
 */
public class LoadingScene {

  private Button continueButton;

  /**
   * Creates the JavaFX scene.
   *
   * @return the loading scene
   */
  public Scene createScene() {
    Label titleLabel = new Label("Phantom Hunt");
    titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

    Label subtitleLabel = new Label("Loading client...");
    subtitleLabel.setStyle("-fx-font-size: 16px;");

    continueButton = new Button("Continue");

    VBox layout = new VBox();
    layout.setAlignment(Pos.CENTER);
    layout.setSpacing(15);
    layout.getChildren().add(titleLabel);
    layout.getChildren().add(subtitleLabel);
    layout.getChildren().add(continueButton);

    return new Scene(layout, 640, 480);
  }

  /**
   * Returns the continue button so other classes can attach actions if needed.
   *
   * @return the continue button
   */
  public Button getContinueButton() {
    return continueButton;
  }
}
