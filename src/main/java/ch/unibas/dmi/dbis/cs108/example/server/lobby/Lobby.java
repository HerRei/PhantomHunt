package ch.unibas.dmi.dbis.cs108.example.server.lobby;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.server.game.GameHandler;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Represents a lobby where players can gather before starting a game.
 * A lobby has a unique ID, a name, a host, and a list of players and spectators.
 * It also manages the game associated with the lobby.
 */
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Represents a lobby where players can gather before starting a game.
 * A lobby has a unique ID, a name, a host, and a list of players and spectators.
 * It also manages the game associated with the lobby.
 */
public class Lobby {

  private static final Logger LOGGER = LogManager.getLogger(Lobby.class);


  private final Vector<ClientHandler> players = new Vector<>();
  private final Vector<ClientHandler> spectators = new Vector<>();
  private final String id;
  private final String name;
  private ClientHandler host;
  private GameHandler activeGame;
  // ---------------------------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------------------------

  /**
   * Creates a new lobby with the given name and host.
   *
   * @param id   the unique ID of the lobby
   * @param name the name of the lobby
   * @param host the host of the lobby
   */
  public Lobby(String id, String name, ClientHandler host) {
    this.id = id;
    this.name = name;
    this.host = host;
    this.players.add(host);
    this.activeGame = null;
    LOGGER.info("Lobby {} ({}) created by {}", name, id, host.getName());
  }

  // ---------------------------------------------------------------------------------------------
  // Getters & Setters
  // ---------------------------------------------------------------------------------------------

  public boolean isTheGameRunning() { //this was here for debugging but should not be used as its just a redirecrted api call
    return activeGame != null && activeGame.gameIsRunning();
  }


  public void attachGame(GameHandler gameHandler) {
    this.activeGame = gameHandler;
  }

  public Optional<GameHandler> getActiveGame() {
    return Optional.ofNullable(activeGame);
  }

  public boolean hasActiveGame() {
    return activeGame != null;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ClientHandler getHost() {
    return host;
  }

  public Optional<Vector<ClientHandler>> getPlayers() {
    return Optional.of(players);
  }

  public Optional<Vector<ClientHandler>> getSpectators() {
    return Optional.of(spectators);
  }

  // ---------------------------------------------------------------------------------------------
  // Player Management
  // ---------------------------------------------------------------------------------------------

  /**
   * Adds a player to the lobby
   *
   * @param player the player to add
   * @return true if the player was added, false otherwise
   */
  public boolean addPlayer(ClientHandler player) {
    if (hasActiveGame()) {
      player.sendMessage(Packet.of(Command.REJECT, "Game is already running."));
      return false;
    }
    if (players.contains(player)) {
      LOGGER.warn("Player {} is already in lobby {}", player.getName(), this.id);
      return false;
    }
    if (players.size() >= 4) {
      LOGGER.warn("This lobby is already full");
      return false;
    }

    players.add(player);
    LOGGER.info("Player {} joined lobby {}", player.getName(), this.name);
    broadcastLobbyInfo();
    return true;
  }

  /**
   * Removes a player from the lobby. If the host leaves, assigns a new host.
   *
   * @param player the player to remove
   * @return true if the player was removed, false otherwise
   */
  public boolean removePlayer(ClientHandler player) {
    if (hasActiveGame()) {
      player.sendMessage(Packet.of(Command.REJECT, "Game is already running."));
      return false;
    }
    if (!players.contains(player)) {
      LOGGER.warn("Player {} is not in lobby {}", player.getName(), this.id);
      return false;
    }

    players.remove(player);
    LOGGER.info("Player {} left lobby {}", player.getName(), this.id);
    if (player == host && !players.isEmpty()) {
      host = players.get(0);
      LOGGER.info("Host left, new host is {}", host.getName());
    }
    broadcastLobbyInfo();
    return true;
  }

  /**
   * Adds a spectator to the lobby.
   *
   * @param spectator the spectator to add
   * @return true if the spectator was added, false otherwise
   */
  public boolean addSpectator(ClientHandler spectator) {
    if (spectators.contains(spectator)) {
      LOGGER.warn("Spectator {} is already in lobby {}", spectator.getName(), this.id);
      return false;
    }
    spectators.add(spectator);
    LOGGER.info("Spectator {} joined lobby {}", spectator.getName(), this.name);
    return true;
  }

  /**
   * Removes a spectator from the lobby.
   *
   * @param spectator the spectator to remove
   * @return true if the spectator was removed, false otherwise
   */
  public boolean removeSpectator(ClientHandler spectator) {
    if (!spectators.contains(spectator)) {
      LOGGER.warn("Spectator {} is not in lobby {}", spectator.getName(), this.id);
      return false;
    }
    spectators.remove(spectator);
    LOGGER.info("Spectator {} left lobby {}", spectator.getName(), this.id);
    return true;
  }

  /**
   * Broadcasts the current lobby information to all players in the lobby.
   */
  public void broadcastLobbyInfo() {
    String playerNames = players.stream()
            .map(ClientHandler::getName)
            .collect(Collectors.joining(" "));
    String packetText = id + " " + playerNames;
    Packet packet = Packet.of(Command.LOBBY_INFO, packetText);
    for (ClientHandler player : players) {
      player.sendMessage(packet);
    }
  }

  /**
   * Broadcasts a game start message to all players in the lobby.
   */
  public void broadcastGameStart() {
    Packet packet = Packet.of(Command.GAME_START);
    for (ClientHandler player : players) {
      player.sendMessage(packet);
    }
  }
}
