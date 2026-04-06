package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds all the important data for the game.
 * This includes the player list, chat messages, and game maps.
 * It's a Singleton, meaning there is only one instance of this class
 * for the entire application.
 */
@SuppressWarnings("java:S6548")
public class GameModel {
  private static final Logger LOGGER = LogManager.getLogger(GameModel.class);
  private static GameModel instance;
  private final ObservableList<Player> players = FXCollections.observableArrayList();
  private final StringProperty playerName = new SimpleStringProperty();
  private final ObservableList<String> chatMessages = FXCollections.observableArrayList();

  // Map properties
  private final ObjectProperty<Image> gameMap = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> collisionMap = new SimpleObjectProperty<>();

  private GameModel() { // NOSONAR
    loadMaps();
  }

  /**
   * Gets the single instance of the GameModel.
   * This is used by other classes to access the game's data.
   *
   * @return The one and only instance of GameModel.
   */
  // The Singleton pattern is used intentionally here.
  public static synchronized GameModel getInstance() { // NOSONAR
    if (instance == null) {
      instance = new GameModel();
    }
    return instance;
  }

  private void loadMaps() {
    try {
      Image gameMapImage = new Image(getClass().getResourceAsStream("/assets/map_concept.png"));
      setGameMap(gameMapImage);
      Image collisionMapImage = new Image(getClass().getResourceAsStream("/assets/map_collision_concept.png"));
      setCollisionMap(collisionMapImage);
      LOGGER.info("Maps loaded successfully.");
    } catch (Exception e) {
      LOGGER.error("Failed to load maps.", e);
    }
  }

  /**
   * @return The list of all players in the game.
   */
  public ObservableList<Player> getPlayers() {
    return players;
  }

  /**
   * Adds a message to the chat.
   *
   * @param msg The message to add.
   */
  public void addChatMessage(String msg) {
    Platform.runLater(() -> chatMessages.add(msg));
  }

  /**
   * @return The list of chat messages.
   */
  public ObservableList<String> chatMessagesProperty() {
    return chatMessages;
  }

  /**
   * Clears all messages from the chat.
   */
  public void clearChat() {
    Platform.runLater(chatMessages::clear);
  }

  // ---GETTERS---

  public StringProperty getName() {
    return playerName;
  }

  public Image getGameMap() {
    return gameMap.get();
  }

  public ObjectProperty<Image> gameMapProperty() {
    return gameMap;
  }

  public Image getCollisionMap() {
    return collisionMap.get();
  }

  public ObjectProperty<Image> collisionMapProperty() {
    return collisionMap;
  }

  // ---SETTERS---
  public void setName(String name) {
    playerName.set(name);
  }

  public void setGameMap(Image gameMap) {
    this.gameMap.set(gameMap);
  }

  public void setCollisionMap(Image collisionMap) {
    this.collisionMap.set(collisionMap);
  }
}