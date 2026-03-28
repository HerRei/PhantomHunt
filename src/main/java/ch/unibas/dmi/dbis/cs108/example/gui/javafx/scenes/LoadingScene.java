package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Builds the initial loading scene of the JavaFX client.
 */
public class LoadingScene implements SceneInterface{

  private Button continueButton;
  private Scene localScene;

  /**
   * Creates the loading scene and connects the continue button to the next action.
   *
   * @param onContinue action that is executed when the user presses the continue button
   * @return the loading scene
   */
  public void createScene(Runnable onContinue) {
    Label titleLabel = new Label("Phantom Hunt");
    titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

    Label subtitleLabel = new Label("Loading client...");
    subtitleLabel.setStyle("-fx-font-size: 16px;");

    continueButton = new Button("Proceed");
    continueButton.setOnAction(event -> onContinue.run()); //sets action to clicking of button

    VBox layout = new VBox();
    layout.setAlignment(Pos.CENTER);
    layout.setSpacing(15);
    layout.getChildren().add(titleLabel);
    layout.getChildren().add(subtitleLabel);
    layout.getChildren().add(continueButton);

    localScene = new Scene(layout, 1280, 960);
  }

  @Override
  public Scene getScene(){
    return localScene;
  }

  /**
   * Returns the continue button of this scene.
   *
   * @return the continue button
   */
  public Button getContinueButton() {
    return continueButton;
  }
}
