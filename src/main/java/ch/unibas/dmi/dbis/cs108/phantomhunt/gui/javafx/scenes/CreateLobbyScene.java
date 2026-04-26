package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;

/** Scene allowing the user to create a new multiplayer lobby. */
public class CreateLobbyScene extends AbstractInputScene {
  public CreateLobbyScene() {
    super();
  }

  @Override
  protected void setupTexts() {
    descriptionLabel.setText("Create a new Lobby");
    inputField.setPromptText("Enter Lobby ID...");
    confirmButton.setText("Create Lobby");
  }

  @Override
  protected void setupEvents() {
    // Handle the confirmation logic
    confirmButton.setOnAction(
        e -> {
          String lobbyName = inputField.getText();
          EventHandlers.getInstance().sendMessage(Command.MKL, lobbyName);
        });

    // Handle the back button logic
    backButton.setOnAction(
        e -> {
          SceneManager.getInstance().showScene(SceneProtocol.HOME);
        });
  }
}
