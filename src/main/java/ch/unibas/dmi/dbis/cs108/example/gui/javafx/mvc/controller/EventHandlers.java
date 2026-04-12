package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.example.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.SceneProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHandlers {
    private static final Logger LOGGER = LogManager.getLogger(EventHandlers.class);
    private static EventHandlers instance;
    private GameModel model;
    private ServerHandler serverHandler;

    private EventHandlers() {
        model = GameModel.getInstance();
    }// Private constructor

    public static synchronized EventHandlers getInstance() {
        if (instance == null) {
            instance = new EventHandlers();
        }
        return instance;
    }


    public void updateDatas() {
        //überschreibt model datas für spiel
        //model.players = xx
    }

    /**
     * Sends the current movement input state to the server.
     * Format: "INPUT u,d,l,r" where 1 is pressed and 0 is released.
     */
    public void sendInputs(boolean up, boolean down, boolean left, boolean right) {
        if (serverHandler == null) return;

        String payload = String.format("%d %d %d %d",
                up ? 1 : 0, down ? 1 : 0, left ? 1 : 0, right ? 1 : 0);

        // Sending as a dedicated INPUT command
        serverHandler.sendMessage(Packet.of(Command.INPUT, payload));
    }

    public void setSH(ServerHandler sh) {
        // sh.getName() could be null here. idk if this would create a crash
        GameModel.getInstance().setName(sh.getName());
        serverHandler = sh;
    }


    //Handle-methods (eventFunctions)
    public void resetAndBackToHub() {
        GameModel.getInstance().resetModel();
        SceneManager.getInstance().showScene(SceneProtocol.HOME);
    }

    public void handleStartGame() {
        SceneManager.getInstance().showScene(SceneProtocol.GAME);
    }

    public void quitLobby(String id){
        serverHandler.sendMessage(Packet.of(Command.LOGOUT_LOBBY, id));
    }

    public void sendMessage(Command cmd, String... args){
        // Null-Check
        if (serverHandler == null) {
            LOGGER.warn("Cannot send message: Not connected to server.");
            return;
        }


        //Sendet Nachricht an Server
        serverHandler.sendMessage(Packet.of(cmd, args));
    }

    public void handleNicknameUpdate(String name) {
        name = name.trim();
        if (!name.isEmpty()) {
            // Null-Check
            if (serverHandler != null) {
                serverHandler.sendMessage(Packet.of(Command.NICK, name));
            } else {
                LOGGER.warn("Cannot send nickname: Not connected to server.");
            }
        }
        else{
            LOGGER.info("Nickname is empty.");
        }
    }
}
