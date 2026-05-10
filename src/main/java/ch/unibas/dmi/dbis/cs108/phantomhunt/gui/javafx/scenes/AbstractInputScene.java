package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
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

  /** Initializes the UI components and calls the template setup methods. */
  public AbstractInputScene() {
    buildBaseLayout();
    setupTexts();
    setupEvents();
  }

  /** Creates the shared dark-themed layout structure. */
  private void buildBaseLayout() {
    descriptionLabel = new Label("Description Placeholder");
    descriptionLabel.setStyle(SceneStyle.TITLE);
    descriptionLabel.setWrapText(true);
    descriptionLabel.setMaxWidth(420);
    descriptionLabel.setAlignment(Pos.CENTER);

    inputField = new TextField();
    inputField.setMaxWidth(380);
    inputField.setPrefHeight(38);
    inputField.setStyle(SceneStyle.INPUT_LARGE);

    backButton = new Button("Back");
    confirmButton = new Button("Confirm");
    backButton.setStyle(SceneStyle.BUTTON_LARGE);
    confirmButton.setStyle(SceneStyle.BUTTON_PRIMARY_SMALL);
    backButton.setPrefWidth(120);
    confirmButton.setPrefWidth(160);

    HBox buttonBox = new HBox(16, backButton, confirmButton);
    buttonBox.setAlignment(Pos.CENTER);

    VBox root = new VBox(24, descriptionLabel, inputField, buttonBox);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(60));
    root.setStyle(SceneStyle.DARK_BACKGROUND);

    SceneManager sceneManager = SceneManager.getInstance();
    scene = new Scene(root, sceneManager.getWidth(), sceneManager.getHeight());
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
