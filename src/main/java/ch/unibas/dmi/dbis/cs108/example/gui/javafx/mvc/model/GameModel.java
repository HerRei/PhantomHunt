package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameModel {
  private static final Logger LOGGER = LogManager.getLogger(GameModel.class);
  private final ObservableList<Player> players = FXCollections.observableArrayList();
  private final StringProperty playerName = new SimpleStringProperty();
  private final ObservableList<String> chatMessages = FXCollections.observableArrayList(); //Alle sachen die im Chat angezeigt werden sollen.

  // Map properties
  private final ObjectProperty<Image> gameMap = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> collisionMap = new SimpleObjectProperty<>();

  public GameModel() {
    loadMaps();
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

  public ObservableList<Player> getPlayers() {
    return players;
  }

  /**
   * Adds a message to the list with all ChatMessages
   *
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