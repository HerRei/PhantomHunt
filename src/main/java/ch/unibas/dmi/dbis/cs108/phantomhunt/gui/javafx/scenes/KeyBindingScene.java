package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Scene that lets the player remap the four movement keys (up / down / left / right).
 *
 * <p>Click a button to start listening, then press any key to assign it. The new bindings are
 * written directly to {@link GameModel} and take effect immediately in {@link GameScene}.
 */
public class KeyBindingScene implements SceneInterface {

  private final Scene scene;

  // Maps action name to its "assign" button so we can update the label after rebinding
  private final Map<String, Button> bindingButtons = new LinkedHashMap<>();

  // The action currently waiting for a key press, or null if not in listen mode
  private String listeningAction = null;

  /** Builds the key-binding configuration scene. */
  public KeyBindingScene() {
    VBox root = new VBox(20);
    root.setPadding(new Insets(30));
    root.setAlignment(Pos.CENTER);
    root.setStyle("-fx-background-color: #2b2b2b;");

    Label title = new Label("Key Bindings");
    title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

    Label hint = new Label("Click a button, then press the desired key.");
    hint.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

    GridPane grid = buildBindingGrid();

    Button resetButton = new Button("Reset to Defaults");
    Button hubButton = new Button("Back to Lobby");
    resetButton.setStyle(
        "-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 13px;");
    hubButton.setStyle(
            "-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 13px;");
    hubButton.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.HOME));
    resetButton.setOnAction(e -> resetToDefaults());

    root.getChildren().addAll(title, hint, grid, resetButton, hubButton);

    this.scene = new Scene(root, 400, 350);

    // Global key listener: only active while a button is in listen mode
    scene.setOnKeyPressed(
        e -> {
          if (listeningAction != null) {
            assignKey(listeningAction, e.getCode());
            listeningAction = null;
          }
        });
  }

  /**
   * Builds the grid with one row per action. Each row shows the action label and the current
   * key-binding button.
   */
  private GridPane buildBindingGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(12);
    grid.setAlignment(Pos.CENTER);

    // Display names for each action constant
    Map<String, String> actionLabels = new LinkedHashMap<>();
    actionLabels.put(GameModel.KEY_UP, "Move Up");
    actionLabels.put(GameModel.KEY_DOWN, "Move Down");
    actionLabels.put(GameModel.KEY_LEFT, "Move Left");
    actionLabels.put(GameModel.KEY_RIGHT, "Move Right");

    int row = 0;
    for (Map.Entry<String, String> entry : actionLabels.entrySet()) {
      String action = entry.getKey();
      String displayName = entry.getValue();

      Label actionLabel = new Label(displayName);
      actionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
      actionLabel.setPrefWidth(100);

      Button keyButton = createKeyButton(action);
      bindingButtons.put(action, keyButton);

      grid.add(actionLabel, 0, row);
      grid.add(keyButton, 1, row);
      row++;
    }
    return grid;
  }

  /**
   * Creates a button that displays the current key code for {@code action}. Clicking it enters
   * listen mode for that action.
   *
   * @param action One of the {@code GameModel.KEY_*} constants.
   * @return The configured button.
   */
  private Button createKeyButton(String action) {
    KeyCode current = GameModel.getInstance().getKeyBinding(action);
    Button button = new Button(current != null ? current.getName() : "?");
    button.setPrefWidth(120);
    button.setStyle(normalStyle());
    button.setOnAction(
        e -> {
          // Cancel any previous listen mode
          if (listeningAction != null) {
            Button prev = bindingButtons.get(listeningAction);
            if (prev != null) {
              prev.setStyle(normalStyle());
              prev.setText(keyLabel(listeningAction));
            }
          }
          // Enter listen mode for this action
          listeningAction = action;
          button.setStyle(listeningStyle());
          button.setText("Press a key…");
        });
    return button;
  }

  /**
   * Writes the chosen key to the model and updates the button label.
   *
   * @param action The action being rebound.
   * @param code The newly pressed key.
   */
  private void assignKey(String action, KeyCode code) {
    GameModel.getInstance().setKeyBinding(action, code);
    Button button = bindingButtons.get(action);
    if (button != null) {
      button.setText(code.getName());
      button.setStyle(normalStyle());
    }
  }

  /** Resets all bindings to WASD defaults and refreshes button labels. */
  private void resetToDefaults() {
    GameModel.getInstance().resetKeyBindings();
    listeningAction = null;
    bindingButtons.forEach(
        (action, button) -> {
          button.setText(keyLabel(action));
          button.setStyle(normalStyle());
        });
  }

  /** Returns the display name of the key currently bound to {@code action}. */
  private String keyLabel(String action) {
    KeyCode code = GameModel.getInstance().getKeyBinding(action);
    return code != null ? code.getName() : "?";
  }

  // ── Styles ────────────────────────────────────────────────────────────────

  private String normalStyle() {
    return "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 13px;";
  }

  private String listeningStyle() {
    return "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 13px;";
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
