package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.example.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.SceneProtocol;
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

    // no usage
    /*public void updateDatas() {
        //überschreibt model datas für spiel
        //model.players = xx
    }*/

    /**
     * Sets the ServerHandler used for network communication
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
    public void quitLobby(String id){
        // DRY: Reusing the safe sendMessage method
        sendMessage(Command.LOGOUT_LOBBY, id);
    }

    /**
     * Sends a generic network packet to the server if connected.
     *
     * @param cmd the protocol command
     * @param args optional arguments for the command
     */
    public void sendMessage(Command cmd, String... args){
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
            if (serverHandler != null) {
                LOGGER.warn("Nickname update rejected: Input is empty.");
                return;
            }
        }
        // DRY
        sendMessage(Command.NICK, name.trim());
    }
}
