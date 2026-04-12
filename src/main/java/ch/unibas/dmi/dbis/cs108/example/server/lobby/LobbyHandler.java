package ch.unibas.dmi.dbis.cs108.example.server.lobby;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.server.game.GameFactory;
import ch.unibas.dmi.dbis.cs108.example.server.game.GameHandler;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.GameState;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.TileType;
import ch.unibas.dmi.dbis.cs108.example.server.game.util.MapLoader;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyHandler {

  private static final Logger LOGGER = LogManager.getLogger(LobbyHandler.class);

  private final Vector<Lobby> waitingLobbies = new Vector<>();
  private final Vector<Lobby> playingLobbies = new Vector<>();
  private final Vector<Lobby> finishedLobbies = new Vector<>();
  private final AtomicInteger lobbyCounter = new AtomicInteger(1);
  private final GameFactory gameFactory = new GameFactory();

  // ---------------------------------------------------------------------------------------------
  // Getters & Setters
  // ---------------------------------------------------------------------------------------------

  public Optional<Vector<Lobby>> getWaitingLobbies() {
    return Optional.ofNullable(waitingLobbies);
  }

  public Optional<Vector<Lobby>> getPlayingLobbies() {
    return Optional.ofNullable(playingLobbies);
  }

  public Optional<Vector<Lobby>> getFinishedLobbies() {
    return Optional.ofNullable(finishedLobbies);
  }

  public String getLobbies() {
    StringBuilder sb = new StringBuilder();
    for (Lobby lobby : waitingLobbies) {
      sb.append(lobby.getId()).append(", ");
    }
    for (Lobby lobby : playingLobbies) {
      sb.append(lobby.getId()).append(", ");
    }
    for (Lobby lobby : finishedLobbies) {
      sb.append(lobby.getId()).append(", ");
    }
    // Remove the trailing comma and space if the list is not empty
    if (sb.length() > 0) {
      sb.setLength(sb.length() - 2);
    }
    return sb.toString();
  }

  // ---------------------------------------------------------------------------------------------
  // Lobby Management & Methods
  // ---------------------------------------------------------------------------------------------
  public void startGame(String id, ClientHandler requester) {
    Optional<Lobby> lobbyOpt = findLobbyById(id, waitingLobbies);
    if (lobbyOpt.isEmpty()) {
      requester.sendMessage(Packet.of(Command.REJECT, "Lobby not found or has already started: " + id));
      return;
    }

    Lobby lobby = lobbyOpt.get();
    if (lobby.getHost() != requester) {
      requester.sendMessage(Packet.of(Command.REJECT, "You are not the host of this lobby"));
      return;
    }

    Vector<ClientHandler> players = lobby.getPlayers().orElseThrow();
    if (players.size() != GameState.REQUIRED_PLAYER_COUNT) {
      requester.sendMessage(
              Packet.of(
                      Command.REJECT,
                      "Not the right amount of players. Is: "
                              + players.size()
                              + ", required: "
                              + GameState.REQUIRED_PLAYER_COUNT));
      return;
    }

    List<GameState.PlayerSeed> seeds = new LinkedList<>();
    for (ClientHandler player : players) {
      seeds.add(new GameState.PlayerSeed(player.getName(), player.getName()));
    }

    TileType[][] map = MapLoader.loadMapFromImage("/assets/map_collision_concept.png");
    GameState gameState = gameFactory.createWithDefaultRules(lobby.getId(), seeds, map);
    GameHandler gameHandler = new GameHandler(gameState, this, lobby);

    lobby.attachGame(gameHandler);
    waitingLobbies.remove(lobby);
    playingLobbies.add(lobby);
    lobby.getActiveGame().get().startGameLoop();
    lobby.broadcast(Packet.of(Command.INFO, "GAME_STARTED"));

    gameHandler.startMatch(System.currentTimeMillis());
    lobby.broadcastGameStart();
  }

  public Lobby createLobby(String name, ClientHandler host) {
    String id = "lobby" + lobbyCounter.getAndIncrement();

    Lobby lobby = new Lobby(id, name, host);
    waitingLobbies.add(lobby);
    host.setCurrentLobby(lobby);
    lobby.broadcastLobbyInfo();

    LOGGER.info("Lobby {} ({}) created by {}", name, id, host.getName());
    return lobby;
  }

  public void joinLobby(String id, ClientHandler player) {
    Optional<Lobby> lobbyOpt = findLobbyById(id, waitingLobbies);
    if (lobbyOpt.isEmpty()) {
      player.sendMessage(Packet.of(Command.REJECT, "Lobby not found or has already started: " + id));
      return;
    }

    Lobby lobby = lobbyOpt.get();
    if (lobby.addPlayer(player)) {
      player.setCurrentLobby(lobby);
    }
  }

  public void spectateLobby(String id, ClientHandler player) {
    Optional<Lobby> lobbyOpt = findLobbyById(id);
    if (lobbyOpt.isEmpty()) {
      player.sendMessage(Packet.of(Command.REJECT, "Lobby not found: " + id));
      return;
    }

    Lobby lobby = lobbyOpt.get();
    if (lobby.addSpectator(player)) {
      player.setCurrentLobby(lobby);
    }
  }

  public void leaveLobby(String id, ClientHandler player) {
    Optional<Lobby> lobbyOpt = findLobbyById(id);
    if (lobbyOpt.isEmpty()) return;

    Lobby lobby = lobbyOpt.get();
    if (lobby.removePlayer(player) || lobby.removeSpectator(player)) {
      player.setCurrentLobby(null);
      if (lobby.getPlayers().isPresent() && lobby.getPlayers().get().isEmpty() && lobby.getSpectators().isPresent() && lobby.getSpectators().get().isEmpty()) {
        waitingLobbies.remove(lobby);
        playingLobbies.remove(lobby);
        finishedLobbies.remove(lobby);
        LOGGER.info("Empty lobby {} removed.", id);
      }
    }
  }

  public void finishLobby(String id) {
    Optional<Lobby> lobbyOpt = findLobbyById(id, playingLobbies);
    if (lobbyOpt.isEmpty()) {
      LOGGER.warn("Could not find lobby to finish: {}", id);
      return;
    }
    Lobby lobby = lobbyOpt.get();
    playingLobbies.remove(lobby);
    finishedLobbies.add(lobby);
    LOGGER.info("Lobby {} finished.", id);
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  private Optional<Lobby> findLobbyById(String id) {
    Optional<Lobby> lobby = findLobbyById(id, waitingLobbies);
    if (lobby.isPresent()) {
      return lobby;
    }
    lobby = findLobbyById(id, playingLobbies);
    if (lobby.isPresent()) {
      return lobby;
    }
    return findLobbyById(id, finishedLobbies);
  }

  private Optional<Lobby> findLobbyById(String id, Vector<Lobby> lobbyList) {
    for (Lobby lobby : lobbyList) {
      if (lobby.getId().equals(id)) {
        return Optional.of(lobby);
      }
    }
    return Optional.empty();
  }
}
