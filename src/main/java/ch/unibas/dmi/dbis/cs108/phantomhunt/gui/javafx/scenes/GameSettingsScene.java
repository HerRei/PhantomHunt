package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameRules;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/** Scene where the lobby host can configure game rules before starting the match. */
public class GameSettingsScene implements SceneInterface {

  private static final double FIXED_PLAYER_RADIUS = 6.0;

  private final Scene scene;
  private final Map<String, TextField> fields = new LinkedHashMap<>();
  private final Label errorLabel;
  private GameRules currentRules = GameRules.defaultRules();

  /** Builds the game settings scene. */
  public GameSettingsScene() {
    VBox root = new VBox(18);
    root.setPadding(new Insets(30));
    root.setAlignment(Pos.TOP_CENTER);
    root.setStyle(SceneStyle.DARK_BACKGROUND);

    Label title = new Label("Game Settings");
    title.setStyle(SceneStyle.TITLE);

    GridPane grid = buildSettingsGrid();

    errorLabel = new Label();
    errorLabel.setStyle(SceneStyle.ERROR_TEXT);
    errorLabel.setMinHeight(20);

    Button saveButton = new Button("Save Settings");
    saveButton.setStyle(SceneStyle.BUTTON_PRIMARY);
    saveButton.setOnAction(e -> saveSettings());

    Button resetButton = new Button("Reset Defaults");
    resetButton.setStyle(SceneStyle.BUTTON);
    resetButton.setOnAction(e -> resetDefaults());

    Button backButton = new Button("Back to Lobby");
    backButton.setStyle(SceneStyle.BUTTON);
    backButton.setOnAction(
        e -> {
          populateFields(currentRules);
          errorLabel.setText("");
          SceneManager.getInstance().showScene(SceneProtocol.LOBBY);
        });

    HBox buttons = new HBox(10, saveButton, resetButton, backButton);
    buttons.setAlignment(Pos.CENTER);

    root.getChildren().addAll(title, grid, errorLabel, buttons);

    SceneManager sceneManager = SceneManager.getInstance();
    this.scene = new Scene(root, sceneManager.getWidth(), sceneManager.getHeight());
    populateFields(currentRules);
  }

  private GridPane buildSettingsGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(18);
    grid.setVgap(12);
    grid.setAlignment(Pos.CENTER);
    grid.setMaxWidth(620);

    addIntegerField(grid, 0, "Total Rounds", "totalRounds");
    addIntegerField(grid, 1, "Round Duration Seconds", "roundDurationSeconds");
    addIntegerField(grid, 2, "Move Speed Per Second", "moveSpeedPerSecond");
    addIntegerField(grid, 3, "Human Points Per Second", "humanPointsPerSecond");
    addIntegerField(grid, 4, "Human Round Win Bonus", "humanRoundWinBonus");
    addIntegerField(grid, 5, "Phantom Catch Bonus", "phantomCatchBonus");
    addIntegerField(grid, 6, "Human Catch Bonus", "humanCatchBonus");
    addIntegerField(grid, 7, "Human Abilities", "humanAbilitys");
    addIntegerField(grid, 8, "Phantom Round Win Bonus", "phantomRoundWinBonus");
    return grid;
  }

  private void addIntegerField(GridPane grid, int row, String label, String key) {
    addField(grid, row, label, key, change -> change.getControlNewText().matches("\\d*"));
  }

  private void addField(
      GridPane grid, int row, String label, String key, Predicate<TextFormatter.Change> filter) {
    Label fieldLabel = new Label(label);
    fieldLabel.setStyle(SceneStyle.BODY_TEXT);
    fieldLabel.setPrefWidth(210);

    TextField field = new TextField();
    field.setTextFormatter(new TextFormatter<>(change -> filter.test(change) ? change : null));
    field.setStyle(SceneStyle.INPUT);
    field.setPrefWidth(220);
    fields.put(key, field);

    grid.add(fieldLabel, 0, row);
    grid.add(field, 1, row);
  }

  private void saveSettings() {
    try {
      currentRules =
          new GameRules(
              parseInt("totalRounds"),
              secondsToMillis(parseInt("roundDurationSeconds")),
              FIXED_PLAYER_RADIUS,
              parseInt("moveSpeedPerSecond"),
              parseInt("humanPointsPerSecond"),
              parseInt("humanRoundWinBonus"),
              parseInt("phantomCatchBonus"),
              parseInt("humanCatchBonus"),
              parseInt("humanAbilitys"),
              parseInt("phantomRoundWinBonus"));
      errorLabel.setText("");
      SceneManager.getInstance().showScene(SceneProtocol.LOBBY);
    } catch (IllegalArgumentException e) {
      errorLabel.setText(e.getMessage());
    }
  }

  private int parseInt(String key) {
    String value = fields.get(key).getText();
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("All settings must contain numbers.");
    }
    return Integer.parseInt(value.trim());
  }

  private long secondsToMillis(int seconds) {
    return seconds * 1000L;
  }

  private void resetDefaults() {
    currentRules = GameRules.defaultRules();
    populateFields(currentRules);
    errorLabel.setText("");
  }

  private void populateFields(GameRules rules) {
    fields.get("totalRounds").setText(Integer.toString(rules.totalRounds()));
    fields.get("roundDurationSeconds").setText(Long.toString(rules.roundDurationMillis() / 1000));
    fields.get("moveSpeedPerSecond").setText(Integer.toString((int) rules.moveSpeedPerSecond()));
    fields.get("humanPointsPerSecond").setText(Integer.toString(rules.humanPointsPerSecond()));
    fields.get("humanRoundWinBonus").setText(Integer.toString(rules.humanRoundWinBonus()));
    fields.get("phantomCatchBonus").setText(Integer.toString(rules.phantomCatchBonus()));
    fields.get("humanCatchBonus").setText(Integer.toString(rules.humanCatchBonus()));
    fields.get("humanAbilitys").setText(Integer.toString(rules.humanAbilitys()));
    fields.get("phantomRoundWinBonus").setText(Integer.toString(rules.phantomRoundWinBonus()));
  }

  /** Returns the saved game settings as the GAME_SETTINGS protocol payload. */
  public String getSettingsPayload() {
    return currentRules.toPayload();
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
