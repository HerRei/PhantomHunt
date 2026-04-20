package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Holds all the important data for the game.
 * This includes the player list, chat messages, and game maps.
 * Operates as a Singleton
 */
@SuppressWarnings("java:S6548")
public class GameModel {
  private static final Logger LOGGER = LogManager.getLogger(GameModel.class);
  private static GameModel instance;
  private final ObservableList<Player> lobbyPlayers = FXCollections.observableArrayList();
  public final ObservableList<String> players = FXCollections.observableArrayList();
  private final BooleanProperty humanAbility = new SimpleBooleanProperty();
  private final StringProperty playerName = new SimpleStringProperty();
  private final StringProperty playerRole = new SimpleStringProperty();
  private final IntegerProperty playerScore = new SimpleIntegerProperty();
  private final IntegerProperty remainingTime = new SimpleIntegerProperty();
  private final IntegerProperty round = new SimpleIntegerProperty();
  private final ObservableList<String> chatMessages = FXCollections.observableArrayList();
  private final ObservableList<String> availableLobbies = FXCollections.observableArrayList();
  private final ObservableList<String> runningLobbies = FXCollections.observableArrayList();
  private final ObservableList<String> lobbyChatMessages = FXCollections.observableArrayList();
  private boolean host = false;

  // Map properties
  private final ObjectProperty<Image> gameMap = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> collisionMap = new SimpleObjectProperty<>();

  private GameModel() { // NOSONAR
    loadMaps();
    humanAbility.set(false);
    playerScore.set(0);
  }

  /**
   * Gets the single instance of the GameModel.
   *
   * @return The one and only instance of GameModel.
   */
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
    remainingTime.set(timeRemaining/1000);
    round.set(currentRound);
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
      if (name.endsWith(playerName.getValue())) {
        playerScore.set(score);
        playerRole.set(role);
      }

      // 3. Find existing player in our list or create a new one
      updateOrAddPlayer(name, role, x, y, score);
    }
  }

  /**
   * Updates the local list of lobbies when the server sends new data
   */
  public void updateLobbyList(List<String> runningLobbys, List<String> waitingLobbys) {
    this.availableLobbies.setAll(waitingLobbys);
    this.runningLobbies.setAll(runningLobbys);
  }

  private void updateOrAddPlayer(String name, String role, double x, double y, int score) {
    // Search for player by nickname
    Player player = lobbyPlayers.stream()
            .filter(p -> p.nameProperty().get().equals(name))
            .findFirst()
            .orElse(null);

    if (player != null) {
      player.xPosition().set(x);
      player.yPosition().set(y);
      player.setScore(score);
      player.setSkin(role);
    } else {
      lobbyPlayers.add(new Player(name, role, score, x, y));
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
   * Adds a message to the lobby chat
   *
   * @param msg the message to add.
   */
  public void addLobbyChatMessage(String msg) {
    Platform.runLater(() -> lobbyChatMessages.add(msg));
  }

  /**
   * @return The list of chat messages.
   */
  public ObservableList<String> chatMessagesProperty() {
    return chatMessages;
  }


  /**
   * @return The list of lobby chat messages.
   */
  public ObservableList<String> lobbyChatMessagesProperty() {
    return lobbyChatMessages;
  }

  /**
   * Clears all messages from the chat.
   */
  public void clearChat() {
    Platform.runLater(chatMessages::clear);
  }

  /**
   * Clears all messages from the lobby chat.
   */
  public void clearLobbyChat() {
    Platform.runLater(lobbyChatMessages::clear);
  }

  // ---GETTERS---

  public StringProperty getName() {
    return playerName;
  }

  public StringProperty getRole() {
    return playerRole;
  }

  public IntegerProperty getRound() {
    return round;
  }

  public IntegerProperty getTime() {
    return remainingTime;
  }

  public boolean isHost() { return host; }

  public void setHost(boolean host) { this.host = host; }

  public IntegerProperty getScore() {
    return playerScore;
  }

  public Image getGameMap() {
    return gameMap.get();
  }

  public ObservableList<String> getAvailableLobbies() {
    return availableLobbies;
  }

  public ObservableList<String> getRunningLobbies() {
    return runningLobbies;
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

  public void setAbility(Boolean value){ humanAbility.set(value);}

  public void setGameMap(Image gameMap) {
    this.gameMap.set(gameMap);
  }

  public void setCollisionMap(Image collisionMap) {
    this.collisionMap.set(collisionMap);
  }

  public void resetModel() {
    this.lobbyPlayers.clear();
    this.playerScore.set(0);
    this.clearChat();
    this.clearLobbyChat();
  }
}
