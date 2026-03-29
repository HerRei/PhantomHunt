package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import javafx.beans.binding.Bindings;

public class NicknameScene extends AbstractInputScene {
    public NicknameScene(){
        super();
    }


    @Override
    protected void setupTexts() {
        descriptionLabel.textProperty().bind(Bindings.concat("Your name: ", GameModel.getInstance().getName()));
        inputField.setPromptText("Enter your new nickname...");
        confirmButton.setText("Update Name");
    }

    @Override
    protected void setupEvents() {
        // Handle the confirmation logic
        confirmButton.setOnAction(e -> {
            EventHandlers.getInstance().handleNicknameUpdate(inputField.getText());
            inputField.clear();
        });

        // Handle the back button logic
        backButton.setOnAction(e -> {
            SceneManager.getInstance().showScene(SceneProtocol.HOME);
        });
    }
}
