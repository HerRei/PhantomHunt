package ch.unibas.dmi.dbis.cs108.example.server.lobby;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.server.game.GameHandler;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

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

  public boolean addSpectator(ClientHandler spectator) {
    if (spectators.contains(spectator)) {
      LOGGER.warn("Spectator {} is already in lobby {}", spectator.getName(), this.id);
      return false;
    }
    spectators.add(spectator);
    LOGGER.info("Spectator {} joined lobby {}", spectator.getName(), this.name);
    return true;
  }

  public boolean removeSpectator(ClientHandler spectator) {
    if (!spectators.contains(spectator)) {
      LOGGER.warn("Spectator {} is not in lobby {}", spectator.getName(), this.id);
      return false;
    }
    spectators.remove(spectator);
    LOGGER.info("Spectator {} left lobby {}", spectator.getName(), this.id);
    return true;
  }

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

  public void broadcastGameStart() {
    Packet packet = Packet.of(Command.GAME_START);
    for (ClientHandler player : players) {
      player.sendMessage(packet);
    }
  }

  /**
   * Sends a packet to everyone in this lobby.
   */
  public void broadcast(Packet packet) {
    // Send to active players
    for (ClientHandler player : players) {
      player.sendMessage(packet);
    }
    // Send to spectators
    for (ClientHandler spectator : spectators) {
      spectator.sendMessage(packet);
    }
  }
}
