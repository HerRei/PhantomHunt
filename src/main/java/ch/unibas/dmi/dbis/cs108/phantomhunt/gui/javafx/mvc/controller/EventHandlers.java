package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.phantomhunt.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.SceneProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton controller that handles UI events and translates them into
 * network commands or scene changes.
 */
public class EventHandlers {
  private static final Logger LOGGER = LogManager.getLogger(EventHandlers.class);
  private static EventHandlers instance;
  private ServerHandler serverHandler;

  private EventHandlers() {}

  /**
   * Retrieves the singleton instance of EventHandlers.
   *
   * @return the singleton instance
   */
  public static synchronized EventHandlers getInstance() {
    if (instance == null) {
      instance = new EventHandlers();
    }
    return instance;
  }

  /**
   * Sends the current movement input state to the server.
   * Format: "INPUT vertical horizontal", where 1 is pressed and 0 is released.
   */
  public void sendInputs(int vertical, int horizontal) {
    if (serverHandler == null) {
      LOGGER.warn("Cannot send inputs: Not connected to server.");
      return;
    }

    String payload = String.format("%d %d",
        vertical, horizontal);

    serverHandler.sendMessage(Packet.of(Command.INPUT, payload));
  }

  public void sendAbility(){
    if (serverHandler == null) {
      LOGGER.warn("Cannot send inputs: Not connected to server.");
      return;
    }
    serverHandler.sendMessage(Packet.of(Command.ABILITY));
  }

  /**
   * Sets the ServerHandler used for network communication.
   * Synchronizes the GameModel nickname if one is already set.
   *
   * @param sh the active ServerHandler
   */
  public void setSH(ServerHandler sh) {
    this.serverHandler = sh;
    if (sh != null && sh.getName() != null) {
      GameModel.getInstance().setName(sh.getName());
    }
  }

  /**
   * Resets game state and navigates back to the hub (HOME scene).
   */
  public void resetAndBackToHub() {
    if (serverHandler == null) {
      LOGGER.warn("resetAndBackToHub: not connected, skipping server message.");
      GameModel.getInstance().resetModel();
      SceneManager.getInstance().showScene(SceneProtocol.HOME);
      return;
    }
    serverHandler.sendMessage(Packet.of(Command.LOGOUT_LOBBY));

    GameModel.getInstance().resetModel();
    SceneManager.getInstance().showScene(SceneProtocol.HOME);
    LOGGER.info("resetAndBackToHub: left lobby, navigated to HOME.");
  }

  public void backToLobby(){
      SceneManager.getInstance().showScene(SceneProtocol.LOBBY);
      serverHandler.sendMessage(Packet.of(Command.GAME_FINISH));
  }

  /**
   * Switches the UI to the main game scene.
   */
  public void handleStartGame() {
    SceneManager.getInstance().showScene(SceneProtocol.GAME);
  }

  /**
   * Sends a request to the server to leave the specified lobby.
   *
   * @param id the ID of the lobby to leave
   */
  public void quitLobby(String id) {
    sendMessage(Command.LOGOUT_LOBBY, id);
  }

  /**
   * Sends a request to the server to join the specified lobby.
   *
   * @param id the ID of the lobby to join
   */
  public void joinLobby(String id) {
    sendMessage(Command.CHECKIN, id);
  }

  /**
   * Sends a generic network packet to the server if connected.
   *
   * @param cmd the protocol command
   * @param args optional arguments for the command
   */
  public void sendMessage(Command cmd, String... args) {
    if (serverHandler == null) {
      LOGGER.warn("Cannot send message: Not connected to server.");
      return;
    }
    serverHandler.sendMessage(Packet.of(cmd, args));
  }

  /**
   * Sends a nickname change request to the server.
   *
   * @param name the requested nickname
   */
  public void handleNicknameUpdate(String name) {
    if (name == null || name.trim().isEmpty()) {
      LOGGER.warn("Nickname update rejected: Input is empty.");
      return;
    }
    sendMessage(Command.NICK, name.trim());
  }

  /**
   * sends request for lobby-lists
   */
  public void updateLists() {
    serverHandler.sendMessage(Packet.of(Command.LIST_LOBBY));
  }
}
