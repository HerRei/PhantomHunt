package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class JoinLobbyScene extends AbstractInputScene {

  private ListView<String> waitingView;
  private ListView<String> runningView;

  public JoinLobbyScene() {
    super();
    addLobbyLists();
  }

  /**
   * Injects ListView components into the existing layout
   */
  private void addLobbyLists() {
    GameModel model = GameModel.getInstance();

    // Setup list for joinable lobbies
    waitingView = new ListView<>();
    waitingView.setItems(model.getAvailableLobbies());
    waitingView.setPrefHeight(150);

    // Setup list for active games (non-joinable)
    runningView = new ListView<>();
    runningView.setItems(model.getRunningLobbies());
    runningView.setPrefHeight(100);
    runningView.setDisable(true); // visual cue: cannot join running games

    // Add to the root VBox (accessed via scene.getRoot())
    VBox root = (VBox) scene.getRoot();

    // Insert lists between description and input field
    root.getChildren().add(1, new Label("Waiting Lobbies (Joinable):"));
    root.getChildren().add(2, waitingView);
    root.getChildren().add(3, new Label("Running Lobbies:"));
    root.getChildren().add(4, runningView);
  }

  @Override
  protected void setupTexts() {
    descriptionLabel.setText("Lobby Browser");
    inputField.setPromptText("Enter LobbyID or select from list...");
    confirmButton.setText("Join Lobby");
  }

  @Override
  protected void setupEvents() {
    // Sync selection from list to text field
    waitingView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) inputField.setText(newVal);
    });

    // Handle join logic
    confirmButton.setOnAction(e -> {
      String lobbyId = inputField.getText().trim();
      if (!lobbyId.isEmpty()) {
        EventHandlers.getInstance().sendMessage(Command.CHECKIN, lobbyId);
      }
    });

    backButton.setOnAction(e ->
            SceneManager.getInstance().showScene(SceneProtocol.HOME)
    );
  }
}