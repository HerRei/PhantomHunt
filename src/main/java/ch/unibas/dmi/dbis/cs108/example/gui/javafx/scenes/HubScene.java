package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel; // Angenommen, das Model liegt hier
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class HubScene implements SceneInterface {

    private TextArea chatDisplay;
    private TextField chatInput;
    private ComboBox<String> chatMode;
    private Slider volumeSlider;
    private Label nicknameLabel; // Anzeige für den aktuellen Namen
    private Scene localScene;

    public HubScene(){
        createScene();
    }

    public void createScene() {
        // --- LEFT SIDE: Navigation & Profile (Wider side) ---
        VBox leftMenu = new VBox(20);
        leftMenu.setPadding(new Insets(25));
        leftMenu.setAlignment(Pos.TOP_CENTER);
        leftMenu.setPrefWidth(350); // Increased width as requested

        // Nickname Section
        VBox profileBox = new VBox(8);
        profileBox.setAlignment(Pos.CENTER);
        Label headLabel = new Label("Logged in as:");
        headLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        // Initialer Name aus dem Model
        nicknameLabel = new Label();
        nicknameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button btnChangeName = new Button("Change Nickname");
        btnChangeName.setMaxWidth(150);
        btnChangeName.setOnAction(e -> handleChangeNickname());

        profileBox.getChildren().addAll(headLabel, nicknameLabel, btnChangeName);

        // Action Buttons
        Button btnJoin = new Button("Join Lobby");
        Button btnCreate = new Button("Create Lobby");
        Button btnSettings = new Button("Settings");

        // Make buttons larger and fill width
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnCreate.setMaxWidth(Double.MAX_VALUE);
        btnSettings.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setPrefHeight(40);
        btnCreate.setPrefHeight(40);

        Label volLabel = new Label("Music Volume:");
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setShowTickLabels(true);

        leftMenu.getChildren().addAll(profileBox, new Separator(), btnJoin, btnCreate, btnSettings, new Separator(), volLabel, volumeSlider);

        // --- RIGHT SIDE: Chat System (Relatively smaller) ---
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(15));
        HBox.setHgrow(chatBox, Priority.ALWAYS);

        chatDisplay = new TextArea();
        chatDisplay.setEditable(false);
        chatDisplay.setWrapText(true);
        VBox.setVgrow(chatDisplay, Priority.ALWAYS);

        HBox inputArea = new HBox(8);
        chatInput = new TextField();
        chatInput.setPromptText("Type a message...");
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        chatMode = new ComboBox<>();
        chatMode.getItems().addAll("Global", "Whisper");
        chatMode.setValue("Global");
        chatMode.setPrefWidth(100);

        Button btnSend = new Button("Send");
        btnSend.setPrefWidth(80);
        btnSend.setOnAction(e -> handleSendMessage());

        chatInput.setOnAction(e -> handleSendMessage());

        inputArea.getChildren().addAll(chatMode, chatInput, btnSend);
        chatBox.getChildren().addAll(new Label("Server Chat"), chatDisplay, inputArea);

        // --- MAIN LAYOUT ---
        HBox mainLayout = new HBox();
        mainLayout.getChildren().addAll(leftMenu, new Separator(javafx.geometry.Orientation.VERTICAL), chatBox);

        localScene = new Scene(mainLayout, 800, 450);
    }

    /**
     * Dialog to change Nickname
     */
    private void handleChangeNickname() {
        TextInputDialog dialog = new TextInputDialog(nicknameLabel.getText());
        dialog.setTitle("Change Nickname");
        dialog.setHeaderText("New Nickname:");
        dialog.setContentText("Please enter your name:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                // Sende Befehl an Server via Handler
                EventHandlers.getInstance().handleNicknameUpdate(newName.trim());
                // Update das Label (wird später idealerweise übers Model/Binding gemacht)
                nicknameLabel.setText(newName.trim());
            }
        });
    }

    public void addToChat(String msg) {
        chatDisplay.appendText(msg + "\n");
    }

    private void handleSendMessage() {
        String message = chatInput.getText().trim();
        String mode = chatMode.getValue();

        if (!message.isEmpty()) {
            // Wichtig: Strings vergleicht man in Java mit .equals()!
            if ("Global".equals(mode)) {
                EventHandlers.getInstance().sendMessage(Command.UNICOM, message);
            } else {
                EventHandlers.getInstance().sendMessage(Command.WHISPER, message);
            }
            chatInput.clear();
        }
    }

    @Override
    public Scene getScene() {
        return localScene;
    }
}