package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

public class HighscoreScene implements SceneInterface {

  private Scene localScene;

  public HighscoreScene() {
    createScene();
  }

  public void createScene() {
    // --- Tabelle ---
    TableView<Map.Entry<String, Integer>> tableView = new TableView<>();

    TableColumn<Map.Entry<String, Integer>, String> nameColumn = new TableColumn<>("Player");
    nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));

    TableColumn<Map.Entry<String, Integer>, Integer> scoreColumn = new TableColumn<>("Score");
    scoreColumn.setCellValueFactory(
            data -> new SimpleIntegerProperty(data.getValue().getValue()).asObject());

    tableView.getColumns().addAll(nameColumn, scoreColumn);

    ObservableList<Map.Entry<String, Integer>> entries =
            FXCollections.observableArrayList(GameModel.getInstance().getHighscores().entrySet());
    tableView.setItems(entries);
    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // --- Buttons ---
    Button btnBack = new Button("Back");
    btnBack.setPrefWidth(120);
    btnBack.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.HOME));

    Button btnRefresh = new Button("🔄 Refresh");
    btnRefresh.setPrefWidth(120);
    btnRefresh.setOnAction(e -> {
      createScene();
      SceneManager.getInstance().showScene(SceneProtocol.HIGHSCORE);
    });

    HBox buttonBar = new HBox(10, btnBack, btnRefresh);
    buttonBar.setAlignment(Pos.CENTER);

    // --- Layout ---
    VBox layout = new VBox(15);
    layout.setPadding(new Insets(25));
    layout.setAlignment(Pos.TOP_CENTER);

    Label title = new Label("Highscores");
    title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

    VBox.setVgrow(tableView, Priority.ALWAYS);
    layout.getChildren().addAll(title, tableView, buttonBar);

    localScene = new Scene(layout, 500, 400);
  }

  @Override
  public Scene getScene() {
    return localScene;
  }
}