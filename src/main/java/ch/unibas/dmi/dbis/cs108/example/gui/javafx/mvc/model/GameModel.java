package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameModel {
    private static final Logger LOGGER = LogManager.getLogger(GameModel.class);
    private static GameModel instance;
    private final ObservableList<Player> players = FXCollections.observableArrayList();
    private final StringProperty playerName = new SimpleStringProperty();
    private final ObservableList<String> chatMessages = FXCollections.observableArrayList(); //Alle sachen die im Chat angezeigt werden sollen.

    private GameModel() {} // Private constructor

    public static synchronized GameModel getInstance() {
        if (instance == null) {
            instance = new GameModel();
        }
        return instance;
    }

    public ObservableList<Player> getPlayers() {
        return players;
    }

    /**
     * Adds a message to the list with all ChatMessages
     * @param msg
     */
    public void addChatMessage(String msg) {
        Platform.runLater(() -> chatMessages.add(msg));
    }

    public ObservableList<String> chatMessagesProperty() {
        return chatMessages; //for property Binding
    }

    /**
     * Clears all messages from the chat.
     * Also wrapped in Platform.runLater to prevent 'Not on FX application thread' exceptions.
     */
    public void clearChat() {
        Platform.runLater(chatMessages::clear);
    }


    // ---GETTERS---
    public StringProperty getName() {
        return playerName;
    }

    // ---SETTERS---
    public void setName(String name){
        playerName.set(name);
    }
}