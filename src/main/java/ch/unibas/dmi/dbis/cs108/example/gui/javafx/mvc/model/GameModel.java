package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model;

import ch.unibas.dmi.dbis.cs108.example.client.ClientApp;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameModel {
    private static final Logger LOGGER = LogManager.getLogger(ClientApp.class);
    private static GameModel instance;
    private final ObservableList<Player> players = FXCollections.observableArrayList();
    public final StringProperty playerName = new SimpleStringProperty();

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


    public String getName() {
        return playerName.get();
    }
}