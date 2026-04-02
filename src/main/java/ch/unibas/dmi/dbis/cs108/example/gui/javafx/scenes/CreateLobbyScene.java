package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.SceneManager;

public class CreateLobbyScene extends AbstractInputScene{
    public CreateLobbyScene(){
        super();
    }


    @Override
    protected void setupTexts() {
        descriptionLabel.setText("Create a new Lobby");
        inputField.setPromptText("Enter LobbyID...");
        confirmButton.setText("Create Lobby");
    }

    @Override
    protected void setupEvents() {
        // Handle the confirmation logic
        confirmButton.setOnAction(e -> {
            //Create Lobby-event
        });

        // Handle the back button logic
        backButton.setOnAction(e -> {
            SceneManager.getInstance().showScene(SceneProtocol.HOME);
        });
    }
}
