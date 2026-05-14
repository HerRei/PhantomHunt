package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.PlayerRole;
import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.SoundEffect;
import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.SoundManager;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all the important data for the game. This includes the player list, chat messages, and game
 * maps. Operates as a Singleton.
 */
@SuppressWarnings("java:S6548")
public class GameModel {

  private static final Logger LOGGER = LogManager.getLogger(GameModel.class);
  private static GameModel instance;

  private Map<String, Integer> highscores = new LinkedHashMap<>();
  private final ObservableList<Player> lobbyPlayers = FXCollections.observableArrayList();
  /** Flat list of online player name strings for hub display. */
  public final ObservableList<String> players = FXCollections.observableArrayList();
  private final BooleanProperty humanAbility = new SimpleBooleanProperty();
  private final StringProperty playerName = new SimpleStringProperty();
  private final StringProperty playerRole = new SimpleStringProperty();
  private final IntegerProperty playerScore = new SimpleIntegerProperty();
  private final IntegerProperty remainingTime = new SimpleIntegerProperty();
  private final IntegerProperty round = new SimpleIntegerProperty();
  private final BooleanProperty wisdomBonusReady = new SimpleBooleanProperty(false);
  private final BooleanProperty wisdomBlessingAvailable = new SimpleBooleanProperty(false);
  private final BooleanProperty wisdomBlindnessActive = new SimpleBooleanProperty(false);
  private final StringProperty wisdomStatus = new SimpleStringProperty("");
  private final ObservableList<String> chatMessages = FXCollections.observableArrayList();
  private final ObservableList<String> availableLobbies = FXCollections.observableArrayList();
  private final ObservableList<String> runningLobbies = FXCollections.observableArrayList();
  private final ObservableList<String> lobbyChatMessages = FXCollections.observableArrayList();
  private final Ability ability = new Ability(100, 100);
  private final BooleanProperty isAbilityVisible = new SimpleBooleanProperty(false);

  // Map properties
  private final ObjectProperty<Image> gameMap = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> collisionMap = new SimpleObjectProperty<>();

  // Key bindings
  public static final String KEY_UP = "up";
  public static final String KEY_DOWN = "down";
  public static final String KEY_LEFT = "left";
  public static final String KEY_RIGHT = "right";
  private final Map<String, KeyCode> keyBindings = new LinkedHashMap<>();

  private GameModel() { // NOSONAR
    humanAbility.set(false);
    playerScore.set(0);
    initDefaultKeyBindings();
  }

  /** Initialises WASD as the default key bindings. */
  private void initDefaultKeyBindings() {
    keyBindings.put(KEY_UP, KeyCode.W);
    keyBindings.put(KEY_DOWN, KeyCode.S);
    keyBindings.put(KEY_LEFT, KeyCode.A);
    keyBindings.put(KEY_RIGHT, KeyCode.D);
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
   * Returns the current key bindings map. Keys are action names (see {@code KEY_*} constants),
   * values are the assigned {@link KeyCode}s.
   *
   * @return Mutable map of action → KeyCode.
   */
  public Map<String, KeyCode> getKeyBindings() {
    return keyBindings;
  }

  /**
   * Returns the {@link KeyCode} bound to the given action.
   *
   * @param action One of the {@code KEY_*} constants.
   * @return The assigned KeyCode, or {@code null} if the action is unknown.
   */
  public KeyCode getKeyBinding(String action) {
    return keyBindings.get(action);
  }

  /**
   * Binds a new key to the given action. The previous binding is overwritten.
   *
   * @param action One of the {@code KEY_*} constants.
   * @param code   The new {@link KeyCode} to assign.
   */
  public void setKeyBinding(String action, KeyCode code) {
    keyBindings.put(action, code);
  }

  /** Resets all key bindings to the default WASD layout. */
  public void resetKeyBindings() {
    initDefaultKeyBindings();
  }

  /**
   * Updates the local game state based on the server's broadcast.
   *
   * @param payload The raw string from the GSU packet.
   */
  public void updatePlayersFromServer(String payload) {
    // Split top-level components: Round Time Players abilityX abilityY abilityVisible
    String[] sections = payload.split(" ");
    if (sections.length < 6) return;

    int currentRound = Integer.parseInt(sections[0]);
    int timeRemaining = Integer.parseInt(sections[1]);
    remainingTime.set(timeRemaining / 1000);

    int previousRound = round.get();
    if (currentRound > 0 && currentRound != previousRound) {
      // A new round must start with clear vision, even if an old overlay timer is still running.
      wisdomBlindnessActive.set(false);
      SoundManager.getInstance().stop(SoundEffect.RUNNING_ON_FLOOR);
      SoundManager.getInstance().stop(SoundEffect.DRAGGING_CHAIN);
      SoundManager.getInstance().stop(SoundEffect.THE_VILLAINS_MIDNIGHT_WALTZ);
      SoundManager.getInstance().play(SoundEffect.THE_VILLAINS_MIDNIGHT_WALTZ);
    }
    round.set(currentRound);

    // Format per entry: "Name:Role:X:Y:Score"
    String[] playerEntries = sections[2].split(";");
    for (String entry : playerEntries) {
      String[] data = entry.split(":");
      if (data.length < 5) continue;

      String name  = data[0];
      String role  = data[1];
      double x     = Double.parseDouble(data[2]);
      double y     = Double.parseDouble(data[3]);
      int    score = Integer.parseInt(data[4]);

      String currentName = playerName.getValue();
      if (currentName != null && name.endsWith(currentName)) {
        playerScore.set(score);
        playerRole.set(role);
      }
      updateOrAddPlayer(name, role, x, y, score);
    }

    ability.xPosition().set(Double.parseDouble(sections[3]));
    ability.yPosition().set(Double.parseDouble(sections[4]));
    isAbilityVisible.set(Boolean.parseBoolean(sections[5]));
  }

  /** Updates the local list of lobbies when the server sends new data. */
  public void updateLobbyList(List<String> runningLobbys, List<String> waitingLobbys) {
    this.availableLobbies.setAll(waitingLobbys);
    this.runningLobbies.setAll(runningLobbys);
  }

  /**
   * Finds an existing player by name and updates their state, or creates a new one.
   * Player slot number (and therefore color) is based on insertion order.
   */
  private void updateOrAddPlayer(String name, String role, double x, double y, int score) {
    // Search for player by nickname
    Player player =
        lobbyPlayers.stream()
            .filter(p -> p.nameProperty().get().equals(name))
            .findFirst()
            .orElse(null);

    if (player != null) {
      // sound logic - this should not be here - but i dont see why this becomes an issue...
      boolean wasMoving = player.getMoved();
      boolean moved =
          Double.compare(player.getXPosition(), x) != 0
              || Double.compare(player.getYPosition(), y) != 0;

      String currentName = playerName.get();
      boolean isLocalPlayer = currentName != null && name.equals(currentName);

      if (isLocalPlayer) {
        PlayerRole currentRole = PlayerRole.valueOf(role);
        if (moved && !wasMoving) {
          if (currentRole == PlayerRole.HUMAN) {
            SoundManager.getInstance().play(SoundEffect.RUNNING_ON_FLOOR);
          } else if (currentRole == PlayerRole.PHANTOM) {
            SoundManager.getInstance().play(SoundEffect.DRAGGING_CHAIN);
          }
        } else if (!moved && wasMoving) {
          if (currentRole == PlayerRole.HUMAN) {
            SoundManager.getInstance().stop(SoundEffect.RUNNING_ON_FLOOR);
          } else if (currentRole == PlayerRole.PHANTOM) {
            SoundManager.getInstance().stop(SoundEffect.DRAGGING_CHAIN);
          }
        }
      }

      player.xPosition().set(x);
      player.yPosition().set(y);
      player.setScore(score);
      player.setRole(role);
      player.setSkin(role);
      player.setMoved(moved);
    } else {
      // Slot = insertion order (1-based); color is assigned inside the Player constructor.
      int playerNumber = lobbyPlayers.size() + 1;
      lobbyPlayers.add(new Player(name, role, role, score, x, y, playerNumber, "front"));
    }
  }

  /** @return the ability object */
  public Ability getAbility() {
    return ability;
  }

  /** @return The list of all players in the game. */
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
   * Adds a message to the lobby chat.
   *
   * @param msg the message to add.
   */
  public void addLobbyChatMessage(String msg) {
    Platform.runLater(() -> lobbyChatMessages.add(msg));
  }

  /** @return The list of chat messages. */
  public ObservableList<String> chatMessagesProperty() {
    return chatMessages;
  }

  /** @return The list of lobby chat messages. */
  public ObservableList<String> lobbyChatMessagesProperty() {
    return lobbyChatMessages;
  }

  /** Clears all messages from the chat. */
  public void clearChat() {
    Platform.runLater(chatMessages::clear);
  }

  /** Clears all messages from the lobby chat. */
  public void clearLobbyChat() {
    Platform.runLater(lobbyChatMessages::clear);
  }

  // ---GETTERS---

  /**
   * Determines the winner(s) by highest score.
   *
   * @return comma-separated name(s) of the winner(s)
   */
  public String getWinner() {
    LOGGER.info(lobbyPlayers);
    int max = -1;
    StringBuilder winnerName = new StringBuilder(); // stringbuilder instead of string-concatenation

    for (Player p : getPlayers()) {
      if (p.getScore() > max) {
        winnerName = new StringBuilder(p.getName());
        max = p.getScore();
      } else if (p.getScore() == max) {
        if (!winnerName.isEmpty()) {
          winnerName.append(", ");
        }
        winnerName.append(p.getName());
      }
    }
    return winnerName.toString();
  }

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

  public Map<String, Integer> getHighscores() {
    return highscores;
  }

  public BooleanProperty humanAbilityProperty() {
    return humanAbility;
  }

  public BooleanProperty isAbilityVisibleProperty() {
    return isAbilityVisible;
  }

  public BooleanProperty wisdomBonusReadyProperty() {
    return wisdomBonusReady;
  }

  /**
   * Exposes whether this client can still use its once-per-match Wisdom Blessing.
   *
   * @return the local Wisdom Blessing availability property
   */
  public BooleanProperty wisdomBlessingAvailableProperty() {
    return wisdomBlessingAvailable;
  }

  /**
   * Exposes whether the local map view should be covered by the Wisdom Blessing overlay.
   *
   * @return the local Wisdom Blessing blindness property
   */
  public BooleanProperty wisdomBlindnessActiveProperty() {
    return wisdomBlindnessActive;
  }

  public StringProperty wisdomStatusProperty() {return wisdomStatus;}


  // ---SETTERS---

  public void setName(String name) {
    playerName.set(name);
  }

  public void setAbility(Boolean value) {
    humanAbility.set(value);
  }

  public void setWisdomBonusReady(boolean value) {wisdomBonusReady.set(value);}

  public void setWisdomBlessingAvailable(boolean value) {wisdomBlessingAvailable.set(value);}

  public void setWisdomBlindnessActive(boolean value) {wisdomBlindnessActive.set(value);}

  public void setWisdomStatus(String value) {wisdomStatus.set(value == null ? "" : value);}


  public void setGameMap(Image gameMap) {
    this.gameMap.set(gameMap);
  }

  public void setCollisionMap(Image collisionMap) {
    this.collisionMap.set(collisionMap);
  }

  public void setHighscores(Map<String, Integer> highscores) {
    this.highscores = highscores;
  }

  /**
   * Updates the facing direction of the local player.
   *
   * @param direction the direction to set (e.g. "front")
   */
  public void setMyPlayerDirection(String direction) {
    String myPlayerName = playerName.get();
    if (myPlayerName == null) {
      return;
    }
    for (Player p : lobbyPlayers) {
      if (p.getName().equals(myPlayerName)) {
        p.setPlayerDirection(direction);
      }
    }
  }

  /** Resets player list, score, and chat for a fresh game session. */
  public void resetModel() {
    this.lobbyPlayers.clear();
    this.playerScore.set(0);
    this.wisdomBlessingAvailable.set(false);
    this.wisdomBlindnessActive.set(false);
    this.clearChat();
    this.clearLobbyChat();
  }
}
