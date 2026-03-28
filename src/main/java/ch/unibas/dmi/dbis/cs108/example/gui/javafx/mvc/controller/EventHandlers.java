package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.example.client.ClientApp;
import ch.unibas.dmi.dbis.cs108.example.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.SceneProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHandlers {
    private static final Logger LOGGER = LogManager.getLogger(ClientApp.class);
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

    public void setSH(ServerHandler sh) {
        serverHandler = sh;
    }


    //Handle-methods (eventFunctions)
    public void handleStartGame() {
        SceneManager.getInstance().showScene(SceneProtocol.GAME);
    }
    public void sendMessage(Command cmd, String msg){
        //Sendet Nachricht an Server
        if (cmd == Command.WHISPER){
            serverHandler.sendMessage(Packet.of(cmd, msg.split(" ", 2)));
        }
        else{
            serverHandler.sendMessage(Packet.of(cmd, msg));
        }
    }

    public void handleNicknameUpdate(String name) {
        serverHandler.sendMessage(Packet.of(Command.NICK, name));
    }
}
