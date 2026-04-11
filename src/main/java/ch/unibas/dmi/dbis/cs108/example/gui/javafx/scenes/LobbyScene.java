package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class LobbyScene implements SceneInterface {

    private Scene scene;
    private VBox root;
    private Label lobbyIdLabel;
    private ListView<String> playerList;
    private Button startGameButton;
    private Button backButton;
    private String id;

    public LobbyScene() {
        root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        lobbyIdLabel = new Label("Lobby ID: ");
        playerList = new ListView<>();
        startGameButton = new Button("Start Game");
        backButton = new Button("Leave Lobby");

        root.getChildren().addAll(lobbyIdLabel, playerList, startGameButton, backButton);

        setupEvents();

        this.scene = new Scene(root, 400, 300);
    }

    private void setupEvents() {
        startGameButton.setOnAction(e -> {
            // Logic to start the game
            EventHandlers.getInstance().sendMessage(Command.START, "");
        });

        backButton.setOnAction(e -> {
            // Logic to leave the lobby
            EventHandlers.getInstance().quitLobby(id);
            SceneManager.getInstance().showScene(SceneProtocol.HOME);
        });
    }

    public void updateLobbyInfo(String lobbyId, String[] players) {
        id = lobbyId;
        lobbyIdLabel.setText("Lobby ID: " + id);
        playerList.getItems().setAll(players);
        // Show/hide start button based on whether the current player is the host
        String currentPlayerName = GameModel.getInstance().getName().get();
        if (players.length > 0 && players[0].equals(currentPlayerName)) {
            startGameButton.setVisible(true);
        } else {
            startGameButton.setVisible(false);
        }
    }

    @Override
    public Scene getScene() {
        return scene;
    }
}
