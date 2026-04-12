package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;

/**
 * Scene allowing the user to join an existing multiplayer lobby via ID.
 */
public class JoinLobbyScene extends AbstractInputScene {
  public JoinLobbyScene() {
    super();
  }


  @Override
  protected void setupTexts() {
    descriptionLabel.setText("Join an existing Lobby");
    inputField.setPromptText("Enter LobbyID to join...");
    confirmButton.setText("Join Lobby");
  }

  @Override
  protected void setupEvents() {
    // Handle the confirmation logic
    confirmButton.setOnAction(e -> {
      //join lobby
      String lobbyId = inputField.getText();
      EventHandlers.getInstance().sendMessage(Command.CHECKIN, lobbyId);
    });

    // Handle the back button logic
    backButton.setOnAction(e -> {
      SceneManager.getInstance().showScene(SceneProtocol.HOME);
    });
  }
}
