package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.Player;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;

/** Scene shown when a game ends, displaying the winner and final rankings. */
public class EndScene implements SceneInterface {

  private final Scene scene;
  private Button lobbyButton;
  private Label winnerText;
  TableView<Player> rankingTable;

  public EndScene() {
    GameModel model = GameModel.getInstance();

    Label titleLabel = new Label("Game Over");
    titleLabel.setStyle(SceneStyle.TITLE_HERO);

    Separator sep1 = new Separator();
    sep1.setMaxWidth(400);

    this.winnerText = new Label("The Winner is: ...");
    winnerText.setStyle(SceneStyle.GOLD_TEXT);

    Label yourScoreText = new Label("Your Final Score:");
    yourScoreText.setStyle(SceneStyle.SUBTLE_TEXT);

    Label finalScoreLabel = new Label("0");
    finalScoreLabel.setStyle(SceneStyle.SCORE_TEXT);
    if (model.getName() != null) {
      finalScoreLabel.textProperty().bind(model.getScore().asString("%d"));
    }

    Separator sep2 = new Separator();
    sep2.setMaxWidth(400);

    Label rankLabel = new Label("Final Rankings");
    rankLabel.setStyle(SceneStyle.SECTION_LABEL);

    this.rankingTable = new TableView<>(model.getPlayers());
    rankingTable.setPrefHeight(180);
    rankingTable.setMaxWidth(340);
    rankingTable.setStyle(SceneStyle.TABLE);

    TableColumn<Player, String> nameCol = new TableColumn<>("Player");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    TableColumn<Player, Integer> scoreCol = new TableColumn<>("Final Score");
    scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
    rankingTable.getColumns().addAll(nameCol, scoreCol);
    rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    scoreCol.setSortType(TableColumn.SortType.DESCENDING);
    rankingTable.getSortOrder().add(scoreCol);

    lobbyButton = new Button("Back to Lobby");
    lobbyButton.setStyle(SceneStyle.BUTTON_LARGE);
    lobbyButton.setVisible(false);

    Button hubButton = new Button("Back to Hub");
    hubButton.setStyle(SceneStyle.BUTTON_LARGE);

    HBox buttonBox = new HBox(20, lobbyButton, hubButton);
    buttonBox.setAlignment(Pos.CENTER);

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
    root.setStyle(SceneStyle.DARK_BACKGROUND);

    SceneManager sceneManager = SceneManager.getInstance();
    this.scene = new Scene(root, sceneManager.getWidth(), sceneManager.getHeight());

    lobbyButton.setOnAction(e -> EventHandlers.getInstance().backToLobby());
    hubButton.setOnAction(e -> EventHandlers.getInstance().resetAndBackToHub());
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
