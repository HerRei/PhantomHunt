package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Abstract base class for scenes with a description, an input field, and confirmation/back buttons.
 */
public abstract class AbstractInputScene implements SceneInterface {

  protected Label descriptionLabel;
  protected TextField inputField;
  protected Button confirmButton;
  protected Button backButton;
  protected Scene scene;

  /** Initializes the UI components and calls the setup methods. */
  public AbstractInputScene() {
    buildBaseLayout();
    setupTexts();
    setupEvents();
  }

  /** Creates the general layout structure. */
  private void buildBaseLayout() {
    // Initialize the description label (above the input field)
    descriptionLabel = new Label("Description Placeholder");
    descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
    descriptionLabel.setWrapText(true);
    descriptionLabel.setMaxWidth(400); // Ensures text wraps if too long
    descriptionLabel.setAlignment(Pos.CENTER);

    // Initialize the central input field
    inputField = new TextField();
    inputField.setMaxWidth(300);
    inputField.setPrefHeight(40);
    inputField.setStyle("-fx-font-size: 16px;");

    // Initialize buttons
    backButton = new Button("Back");
    confirmButton = new Button("Confirm");

    backButton.setPrefWidth(120);
    confirmButton.setPrefWidth(120);

    // Horizontal box for the buttons
    HBox buttonBox = new HBox(20, backButton, confirmButton);
    buttonBox.setAlignment(Pos.CENTER);

    // Main vertical layout (Description -> Input -> Buttons)
    VBox root = new VBox(20, descriptionLabel, inputField, buttonBox);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(50));

    // Create the scene
    scene = new Scene(root, 800, 450);
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
