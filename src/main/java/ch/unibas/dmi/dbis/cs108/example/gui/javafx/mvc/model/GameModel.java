package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.GameScene;
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
  private final ObservableList<Player> lobbyPlayers = FXCollections.observableArrayList();
  public final ObservableList<String> players = FXCollections.observableArrayList();
  private final StringProperty playerName = new SimpleStringProperty();
  private final IntegerProperty playerScore = new SimpleIntegerProperty();
  private final ObservableList<String> chatMessages = FXCollections.observableArrayList();

  // Map properties
  private final ObjectProperty<Image> gameMap = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> collisionMap = new SimpleObjectProperty<>();

  private GameModel() { // NOSONAR
    loadMaps();
    playerScore.set(0);
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

  /**
   * Updates the local game state based on the server's broadcast.
   * @param payload The raw string from the GSU packet
   */
  public void updatePlayersFromServer(String payload) {
    // 1. Split top-level components (Round, Time, Players)
    String[] sections = payload.split(" ");
    if (sections.length < 3) return;
    int currentRound = Integer.parseInt(sections[0]);
    int timeRemaining = Integer.parseInt(sections[1]);
    String playersData = sections[2];
    String[] playerEntries = playersData.split(";");

    for (String entry : playerEntries) {
      // Format: "Name:Role:X:Y:Score"
      String[] data = entry.split(":");
      if (data.length < 5) continue;

      String name = data[0];
      String role = data[1];
      double x = Double.parseDouble(data[2]);
      double y = Double.parseDouble(data[3]);
      int score = Integer.parseInt(data[4]);
      if (name == playerName.get()){
        playerScore.set(score);
      }

      // 3. Find existing player in our list or create a new one
      updateOrAddPlayer(name, role, x, y, score);
    }
  }

  private void updateOrAddPlayer(String name, String role, double x, double y, int score) {
    // Search for player by nickname
    Player player = lobbyPlayers.stream()
            .filter(p -> p.nameProperty().get().equals(name))
            .findFirst()
            .orElse(null);

    if (player != null) {
      player.xPosition().set((int) x); // or double if you change your Player class
      player.yPosition().set((int) y);
      player.setScore(score);
      // Note: You could also add a roleProperty to the Player class
    } else {
      lobbyPlayers.add(new Player(name, role, score, (int) x, (int) y));
    }
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
    return lobbyPlayers;
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

  public IntegerProperty getScore() {
    return playerScore;
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

  public void resetModel() {
    this.players.clear();
    this.playerScore.set(0);
    this.clearChat();
  }
}