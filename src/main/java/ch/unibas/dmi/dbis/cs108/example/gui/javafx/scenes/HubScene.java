package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * The primary hub scene displaying the global chat, profile options, and lobby navigation.
 */
public class HubScene implements SceneInterface {

    private ListView<String> chatDisplay;
    private ListView<String> playerListDisplay; // New: Display for all players
    private TextField chatInput;
    private TextField whisperTargetInput;
    private ComboBox<String> chatMode;
    private Label nicknameLabel;
    private Scene localScene;

    public HubScene(){
        createScene();
    }

    /**
     * Constructs the UI layout for the hub.
     */
    public void createScene() {
        GameModel model = GameModel.getInstance();

        // --- LEFT SIDE: Navigation, Profile & Player List ---
        VBox leftMenu = new VBox(15); // Slightly tighter spacing
        leftMenu.setPadding(new Insets(25));
        leftMenu.setAlignment(Pos.TOP_CENTER);
        leftMenu.setPrefWidth(350);

        // Profile Section
        VBox profileBox = new VBox(8);
        profileBox.setAlignment(Pos.CENTER);
        Label headLabel = new Label("Logged in as:");
        headLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        nicknameLabel = new Label();
        nicknameLabel.textProperty().bind(Bindings.concat( model.getName()));
        nicknameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        profileBox.getChildren().addAll(headLabel, nicknameLabel);

        // Buttons
        Button btnNickname = new Button("Change Nickname");
        Button btnJoin = new Button("Join Lobby");
        Button btnCreate = new Button("Create Lobby");

        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnNickname.setMaxWidth(Double.MAX_VALUE);
        btnCreate.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setPrefHeight(40);
        btnCreate.setPrefHeight(40);

        // NEW: Player List Section (replacing Volume)
        Label onlineLabel = new Label("Players Online:");
        onlineLabel.setStyle("-fx-font-weight: bold;");

        playerListDisplay = new ListView<>();
        // BINDING: Connect to the observable list in GameModel
        playerListDisplay.setItems(model.players);
        VBox.setVgrow(playerListDisplay, Priority.ALWAYS); // Let the list take up remaining space

        leftMenu.getChildren().addAll(profileBox, btnNickname, new Separator(), btnJoin, btnCreate, new Separator(), onlineLabel, playerListDisplay);

        // --- RIGHT SIDE: Chat System ---
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(15));
        HBox.setHgrow(chatBox, Priority.ALWAYS);

        chatDisplay = new ListView<>();
        chatDisplay.setItems(model.chatMessagesProperty());
        VBox.setVgrow(chatDisplay, Priority.ALWAYS);

        model.chatMessagesProperty().addListener((ListChangeListener<String>) c -> {
            Platform.runLater(() -> chatDisplay.scrollTo(chatDisplay.getItems().size() - 1));
        });

        HBox inputArea = new HBox(8);
        chatInput = new TextField();
        chatInput.setPromptText("Type a message...");
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        chatMode = new ComboBox<>();
        chatMode.getItems().addAll("Global", "Whisper");
        chatMode.setValue("Global");
        chatMode.setPrefWidth(100);

        whisperTargetInput = new TextField();
        whisperTargetInput.setPromptText("To whom?");
        whisperTargetInput.setPrefWidth(100);
        whisperTargetInput.visibleProperty().bind(Bindings.equal("Whisper", chatMode.valueProperty()));
        whisperTargetInput.managedProperty().bind(whisperTargetInput.visibleProperty());

        Button btnSend = new Button("Send");
        btnSend.setPrefWidth(80);

        //--- Actions ---
        btnSend.setOnAction(e -> handleSendMessage());
        btnNickname.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.NICKNAME));
        btnJoin.setOnAction(e -> {
            EventHandlers.getInstance().updateLists();
            SceneManager.getInstance().showScene(SceneProtocol.JOINLOBBY);
        });
        btnCreate.setOnAction(e -> SceneManager.getInstance().showScene(SceneProtocol.CREATELOBBY));
        chatInput.setOnAction(e -> handleSendMessage());

        inputArea.getChildren().addAll(chatMode, whisperTargetInput, chatInput, btnSend);
        chatBox.getChildren().addAll(new Label("Server Chat"), chatDisplay, inputArea);

        // --- MAIN LAYOUT ---
        HBox mainLayout = new HBox();
        mainLayout.getChildren().addAll(leftMenu, new Separator(javafx.geometry.Orientation.VERTICAL), chatBox);

        localScene = new Scene(mainLayout, 900, 600); // Increased window size slightly for the list
    }

    private void handleSendMessage() {
        String message = chatInput.getText().trim();
        String mode = chatMode.getValue();

        if (!message.isEmpty()) {
            if ("Global".equals(mode)) {
                EventHandlers.getInstance().sendMessage(Command.UNICOM, message);
                chatInput.clear();
            } else {
                String target = whisperTargetInput.getText().trim();
                if (target.isEmpty()) {
                    GameModel.getInstance().addChatMessage("SYSTEM: You need to enter a target");
                    return;
                }
                EventHandlers.getInstance().sendMessage(Command.WHISPER, target, message);
                chatInput.clear();
            }
        }
    }

    @Override
    public Scene getScene() {
        return localScene;
    }
}
