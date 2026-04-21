package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.Player;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Comparator;

public class EndScene implements SceneInterface {

    private final Scene scene;
    private Button lobbyButton;
    private Label winnerText;
    private String winnerName;

    public EndScene() {
        GameModel model = GameModel.getInstance();

        Label titleLabel = new Label("Game Over");
        titleLabel.setFont(new Font("Arial", 40));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Leer initialisieren — wird via updateWinner() gesetzt
        this.winnerText = new Label("The Winner is: ...");
        winnerText.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label yourScoreText = new Label("Your Final Score:");
        yourScoreText.setStyle("-fx-text-fill: #ccc; -fx-font-size: 18px;");

        Label finalScoreLabel = new Label("0");
        finalScoreLabel.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 48px; -fx-font-weight: bold;");
        if (model.getName() != null) {
            finalScoreLabel.textProperty().bind(model.getScore().asString("%d"));
        }

        // Tabelle — direkt an ObservableList gebunden, aktualisiert sich automatisch
        TableView<Player> rankingTable = new TableView<>(model.getPlayers());
        rankingTable.setPrefHeight(200);
        rankingTable.setMaxWidth(300);

        TableColumn<Player, String> nameCol = new TableColumn<>("Player");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Player, Integer> scoreCol = new TableColumn<>("Final Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        rankingTable.getColumns().addAll(nameCol, scoreCol);
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Tabelle nach Score sortieren
        scoreCol.setSortType(TableColumn.SortType.DESCENDING);
        rankingTable.getSortOrder().add(scoreCol);

        Button lobbyButton = new Button("Lobby");
        this.lobbyButton = lobbyButton;
        Button hubButton = new Button("Hub");

        String buttonStyle = "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;";
        lobbyButton.setStyle(buttonStyle);
        hubButton.setStyle(buttonStyle);
        lobbyButton.setVisible(false); // standardmässig versteckt, bis LOBBY_INFO kommt

        HBox buttonBox = new HBox(20, lobbyButton, hubButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, titleLabel, winnerText, yourScoreText, finalScoreLabel, rankingTable, buttonBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #222;");

        this.scene = new Scene(root, 800, 600);

        lobbyButton.setOnAction(e -> EventHandlers.getInstance().backToLobby());
        hubButton.setOnAction(e -> EventHandlers.getInstance().resetAndBackToHub());
    }

    public void updateLobbyInfo(String lobbyId, String[] players) {
        String currentPlayerName = GameModel.getInstance().getName().get();
        if (players.length > 0 && players[0].equals(currentPlayerName)) {
            lobbyButton.setVisible(true);
        } else {
            lobbyButton.setVisible(false);
        }
    }

    public void updateWinner() {
        String winner = GameModel.getInstance().getWinner();
        Platform.runLater(() -> {
            winnerText.setText("The Winner is: " + (winner.isBlank() ? "Nobody" : winner));
            GameModel.getInstance().getPlayers()
                    .sort(Comparator.comparingInt(Player::getScore).reversed());
        });
    }

    @Override
    public Scene getScene() {
        return scene;
    }
}