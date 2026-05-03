package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Abstract base class for scenes with a description, an input field, and confirmation/back buttons.
 * Applies the dark UI theme and exposes F11 fullscreen toggling.
 */
public abstract class AbstractInputScene implements SceneInterface {

  private static final String DARK_BG =
      "-fx-background-color: #2b2b2b;";
  private static final String BUTTON_STYLE =
      "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 6;";
  private static final String CONFIRM_STYLE =
      "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 6;";
  private static final String INPUT_STYLE =
      "-fx-background-color: #3c3f41; -fx-text-fill: white; "
          + "-fx-prompt-text-fill: #888; -fx-font-size: 14px;";

  protected Label descriptionLabel;
  protected TextField inputField;
  protected Button confirmButton;
  protected Button backButton;
  protected Scene scene;

  /** Initializes the UI components and calls the template setup methods. */
  public AbstractInputScene() {
    buildBaseLayout();
    setupTexts();
    setupEvents();
  }

  /** Creates the shared dark-themed layout structure. */
  private void buildBaseLayout() {
    descriptionLabel = new Label("Description Placeholder");
    descriptionLabel.setStyle(
        "-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
    descriptionLabel.setWrapText(true);
    descriptionLabel.setMaxWidth(420);
    descriptionLabel.setAlignment(Pos.CENTER);

    inputField = new TextField();
    inputField.setMaxWidth(380);
    inputField.setPrefHeight(38);
    inputField.setStyle(INPUT_STYLE);

    backButton = new Button("← Back");
    confirmButton = new Button("Confirm");
    backButton.setStyle(BUTTON_STYLE);
    confirmButton.setStyle(CONFIRM_STYLE);
    backButton.setPrefWidth(120);
    confirmButton.setPrefWidth(160);

    HBox buttonBox = new HBox(16, backButton, confirmButton);
    buttonBox.setAlignment(Pos.CENTER);

    VBox root = new VBox(24, descriptionLabel, inputField, buttonBox);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(60));
    root.setStyle(DARK_BG);

    scene = new Scene(root, 900, 640);

    // F11 toggles fullscreen
    scene.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.F11) {
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
            if (stage != null) stage.setFullScreen(!stage.isFullScreen());
          }
        });
  }

  /** Configures the specific text values for the UI components. */
  protected abstract void setupTexts();

  /** Configures the event handlers for user interaction. */
  protected abstract void setupEvents();

  @Override
  public Scene getScene() {
    return scene;
  }
}
