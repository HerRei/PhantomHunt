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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

/** Scene displaying the global highscore leaderboard. */
public class HighscoreScene implements SceneInterface {

  private Scene localScene;

  public HighscoreScene() {
    createScene();
  }

  /** Builds or rebuilds the highscore scene (also called on refresh). */
  public void createScene() {
    TableView<Map.Entry<String, Integer>> tableView = new TableView<>();
    tableView.setStyle(SceneStyle.TABLE);

    TableColumn<Map.Entry<String, Integer>, String> nameColumn = new TableColumn<>("Player");
    nameColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getKey()));

    TableColumn<Map.Entry<String, Integer>, Integer> scoreColumn = new TableColumn<>("Score");
    scoreColumn.setCellValueFactory(
        data -> new SimpleIntegerProperty(data.getValue().getValue()).asObject());

    tableView.getColumns().addAll(nameColumn, scoreColumn);

    ObservableList<Map.Entry<String, Integer>> entries =
        FXCollections.observableArrayList(GameModel.getInstance().getHighscores().entrySet());
    tableView.setItems(entries);
    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    Button btnBack = new Button("Back");
    btnBack.setStyle(SceneStyle.BUTTON);
    btnBack.setPrefWidth(130);
    btnBack.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.HOME));

    Button btnRefresh = new Button("Refresh");
    btnRefresh.setStyle(SceneStyle.BUTTON);
    btnRefresh.setPrefWidth(130);
    btnRefresh.setOnAction(
        e -> {
          createScene();
          SceneManager.getInstance().showScene(SceneProtocol.HIGHSCORE);
        });

    HBox buttonBar = new HBox(15, btnBack, btnRefresh);
    buttonBar.setAlignment(Pos.CENTER);

    Label title = new Label("Highscores");
    title.setStyle(SceneStyle.TITLE_LARGE);

    Separator sep = new Separator();

    VBox layout = new VBox(18, title, sep, tableView, buttonBar);
    layout.setPadding(new Insets(40));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle(SceneStyle.DARK_BACKGROUND);
    VBox.setVgrow(tableView, Priority.ALWAYS);

    SceneManager sceneManager = SceneManager.getInstance();
    localScene = new Scene(layout, sceneManager.getWidth(), sceneManager.getHeight());
  }

  @Override
  public Scene getScene() {
    return localScene;
  }
}
