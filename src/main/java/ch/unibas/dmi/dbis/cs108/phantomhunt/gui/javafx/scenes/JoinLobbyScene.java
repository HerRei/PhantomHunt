package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

/**
 * Enhanced JoinLobbyScene that displays available and running lobbies from the GameModel.
 * Allows joining via list selection or manual ID entry.
 */
public class JoinLobbyScene extends AbstractInputScene {

  private ListView<String> waitingListView;
  private ListView<String> runningListView;

  public JoinLobbyScene() {
    super();
    setupLobbyLists();
  }

  /**
   * Initializes the ListViews and binds them to the GameModel's observable lists.
   */
  private void setupLobbyLists() {
    GameModel model = GameModel.getInstance();

    // Initialize ListViews
    waitingListView = new ListView<>();
    runningListView = new ListView<>();

    // Bind data from model
    waitingListView.setItems(model.getAvailableLobbies());
    runningListView.setItems(model.getRunningLobbies());

    // Configure UI appearance
    waitingListView.setPrefHeight(150);
    runningListView.setPrefHeight(100);

    // Running lobbies are usually not joinable
    runningListView.setDisable(true);

    // Inject lists into the existing VBox layout (from AbstractInputScene)
    VBox root = (VBox) scene.getRoot();

    // Inserting labels and lists before the input area
    root.getChildren().add(1, new Label("Joinable Lobbies:"));
    root.getChildren().add(2, waitingListView);
    root.getChildren().add(3, new Label("Running Games:"));
    root.getChildren().add(4, runningListView);
  }

  @Override
  protected void setupTexts() {
    descriptionLabel.setText("Lobby Browser");
    inputField.setPromptText("Enter LobbyName...");
    confirmButton.setText("Join Lobby");
  }

  @Override
  protected void setupEvents() {
    // Join logic
    confirmButton.setOnAction(e -> {
      String lobbyId = inputField.getText().trim();
      if (!lobbyId.isEmpty()) {
        EventHandlers.getInstance().sendMessage(Command.CHECKIN, lobbyId);
      }
    });

    // Navigation back
    backButton.setOnAction(e -> {
      SceneManager.getInstance().showScene(SceneProtocol.HOME);
    });
  }
}