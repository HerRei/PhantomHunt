package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.Player;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class EndScene implements SceneInterface {

    private final Scene scene;

    public EndScene() {
        GameModel model = GameModel.getInstance();

        // Titel
        Label titleLabel = new Label("Game Over");
        titleLabel.setFont(new Font("Arial", 40));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // final score
        Label yourScoreText = new Label("Your Final Score:");
        yourScoreText.setStyle("-fx-text-fill: #ccc; -fx-font-size: 18px;");

        Label finalScoreLabel = new Label("0");
        finalScoreLabel.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 48px; -fx-font-weight: bold;");
        if (model.getName() != null) {
            finalScoreLabel.textProperty().bind(model.getScore().asString("%d"));
        }

        // Table with all scores
        TableView<Player> rankingTable = new TableView<>(model.getPlayers());
        rankingTable.setPrefHeight(250);
        rankingTable.setMaxWidth(300);

        TableColumn<Player, String> nameCol = new TableColumn<>("Player");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Player, Integer> scoreCol = new TableColumn<>("Final Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        rankingTable.getColumns().addAll(nameCol, scoreCol);
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Back to Hub Button
        Button backToHubBtn = new Button("Back to Hub");
        backToHubBtn.setPrefSize(200, 50);
        backToHubBtn.setStyle("-fx-font-size: 16px; -fx-base: #444; -fx-text-fill: white;");
        backToHubBtn.setOnAction(e -> {
            EventHandlers.getInstance().resetAndBackToHub();
        });

        // Layout
        VBox root = new VBox(20, titleLabel, yourScoreText, finalScoreLabel, rankingTable, backToHubBtn);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #222;");

        this.scene = new Scene(root, 800, 600);
    }

    @Override
    public Scene getScene() {
        return scene;
    }
}