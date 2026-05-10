package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
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

  private void setupLobbyLists() {
    GameModel model = GameModel.getInstance();

    waitingListView = new ListView<>();
    runningListView = new ListView<>();

    waitingListView.setItems(model.getAvailableLobbies());
    runningListView.setItems(model.getRunningLobbies());

    waitingListView.setStyle(SceneStyle.LIST);
    runningListView.setStyle(SceneStyle.LIST);

    waitingListView.setPrefHeight(160);
    runningListView.setPrefHeight(100);

    VBox root = (VBox) scene.getRoot();

    Label joinableLabel = new Label("Joinable Lobbies:");
    joinableLabel.setStyle(SceneStyle.SECTION_LABEL);
    Label runningLabel = new Label("Running Games (Spectate):");
    runningLabel.setStyle(SceneStyle.SECTION_LABEL);

    root.getChildren().add(1, joinableLabel);
    root.getChildren().add(2, waitingListView);
    root.getChildren().add(3, runningLabel);
    root.getChildren().add(4, runningListView);

    waitingListView.setOnMouseClicked(this::handleLobbyClick);
    runningListView.setOnMouseClicked(this::handleRunningLobbyClick);
  }

  private void handleLobbyClick(MouseEvent event) {
    if (event.getClickCount() != 2) return;
    String selectedLobby = waitingListView.getSelectionModel().getSelectedItem();
    if (selectedLobby == null || selectedLobby.isEmpty()) return;
    inputField.setText(extractLobbyId(selectedLobby));
    confirmButton.fire();
  }

  private void handleRunningLobbyClick(MouseEvent event) {
    if (event.getClickCount() != 2) return;
    String selectedLobby = runningListView.getSelectionModel().getSelectedItem();
    if (selectedLobby == null || selectedLobby.isEmpty()) return;
    EventHandlers.getInstance().sendMessage(Command.SPEC, extractLobbyId(selectedLobby));
  }

  @Override
  protected void setupTexts() {
    descriptionLabel.setText("Lobby Browser");
    inputField.setPromptText("Double-click a lobby or enter its name");
    confirmButton.setText("Join Lobby");
  }

  @Override
  protected void setupEvents() {
    confirmButton.setOnAction(e -> {
      String lobbyId = extractLobbyId(inputField.getText().trim());
      if (!lobbyId.isEmpty()) {
        EventHandlers.getInstance().sendMessage(Command.CHECKIN, lobbyId);
      }
    });

    backButton.setOnAction(
        e -> SceneManager.getInstance().showScene(SceneProtocol.HOME));
  }

  private String extractLobbyId(String lobbyEntry) {
    int countSuffixStart = lobbyEntry.lastIndexOf(" (");
    if (countSuffixStart < 0) {
      return lobbyEntry;
    }
    return lobbyEntry.substring(0, countSuffixStart);
  }
}
