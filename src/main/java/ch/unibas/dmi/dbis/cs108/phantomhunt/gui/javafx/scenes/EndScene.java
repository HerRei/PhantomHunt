package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.Player;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;

/** Scene shown when a game ends, displaying the winner and final rankings. */
public class EndScene implements SceneInterface {

  private static final String DARK_BG =
      "-fx-background-color: #2b2b2b;";
  private static final String BUTTON_STYLE =
      "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 14px; "
          + "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 6;";

  private final Scene scene;
  private Button lobbyButton;
  private Label winnerText;
  TableView<Player> rankingTable;

  public EndScene() {
    GameModel model = GameModel.getInstance();

    // ── Title ──────────────────────────────────────────────────────────────
    Label titleLabel = new Label("Game Over");
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

    Separator sep1 = new Separator();
    sep1.setMaxWidth(400);

    // ── Winner ─────────────────────────────────────────────────────────────
    this.winnerText = new Label("The Winner is: ...");
    winnerText.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 22px; -fx-font-weight: bold;");

    // ── Score ──────────────────────────────────────────────────────────────
    Label yourScoreText = new Label("Your Final Score:");
    yourScoreText.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 15px;");

    Label finalScoreLabel = new Label("0");
    finalScoreLabel.setStyle(
        "-fx-text-fill: #00FF00; -fx-font-size: 48px; -fx-font-weight: bold;");
    if (model.getName() != null) {
      finalScoreLabel.textProperty().bind(model.getScore().asString("%d"));
    }

    Separator sep2 = new Separator();
    sep2.setMaxWidth(400);

    // ── Ranking table ──────────────────────────────────────────────────────
    Label rankLabel = new Label("Final Rankings");
    rankLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px; -fx-font-weight: bold;");

    this.rankingTable = new TableView<>(model.getPlayers());
    rankingTable.setPrefHeight(180);
    rankingTable.setMaxWidth(340);
    rankingTable.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");

    TableColumn<Player, String> nameCol = new TableColumn<>("Player");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    TableColumn<Player, Integer> scoreCol = new TableColumn<>("Final Score");
    scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
    rankingTable.getColumns().addAll(nameCol, scoreCol);
    rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    scoreCol.setSortType(TableColumn.SortType.DESCENDING);
    rankingTable.getSortOrder().add(scoreCol);

    // ── Buttons ────────────────────────────────────────────────────────────
    lobbyButton = new Button("Back to Lobby");
    lobbyButton.setStyle(BUTTON_STYLE);
    lobbyButton.setVisible(false);

    Button hubButton = new Button("Back to Hub");
    hubButton.setStyle(BUTTON_STYLE);

    HBox buttonBox = new HBox(20, lobbyButton, hubButton);
    buttonBox.setAlignment(Pos.CENTER);

    // ── Layout ─────────────────────────────────────────────────────────────
    VBox root =
        new VBox(
            18,
            titleLabel, sep1,
            winnerText,
            yourScoreText, finalScoreLabel,
            sep2,
            rankLabel, rankingTable,
            buttonBox);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(40));
    root.setStyle(DARK_BG);

    this.scene = new Scene(root, 900, 640);

    lobbyButton.setOnAction(e -> EventHandlers.getInstance().backToLobby());
    hubButton.setOnAction(e -> EventHandlers.getInstance().resetAndBackToHub());

    // F11 toggles fullscreen
    scene.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.F11) {
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
            if (stage != null) stage.setFullScreen(!stage.isFullScreen());
          }
        });
  }

  /**
   * Shows or hides the lobby button based on whether the local player is still in the lobby.
   *
   * @param lobbyId the current lobby ID
   * @param players the current list of players in the lobby
   */
  public void updateLobbyInfo(String lobbyId, String[] players) {
    String currentPlayerName = GameModel.getInstance().getName().get();
    boolean isStillInLobby = false;
    for (String player : players) {
      if (player.equals(currentPlayerName)) {
        isStillInLobby = true;
        break;
      }
    }
    lobbyButton.setVisible(isStillInLobby);
  }

  /** Refreshes the winner label and sorts the ranking table by score descending. */
  public void updateWinner() {
    String winner = GameModel.getInstance().getWinner();
    rankingTable = new TableView<>(GameModel.getInstance().getPlayers());
    Platform.runLater(
        () -> {
          winnerText.setText("The Winner is: " + (winner.isBlank() ? "Nobody" : winner));
          GameModel.getInstance()
              .getPlayers()
              .sort(Comparator.comparingInt(Player::getScore).reversed());
        });
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
