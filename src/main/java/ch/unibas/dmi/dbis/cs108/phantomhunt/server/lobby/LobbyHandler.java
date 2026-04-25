package ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.GameFactory;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.GameHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameState;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages all lobbies on the server including creating, joining, leaving, spectating lobbies, and
 * transitioning to active games.
 */
public class LobbyHandler {

  private static final Logger LOGGER = LogManager.getLogger(LobbyHandler.class);
  private static LobbyHandler instance;

  private final Vector<Lobby> waitingLobbies = new Vector<>();
  private final Vector<Lobby> playingLobbies = new Vector<>();
  private final Vector<Lobby> finishedLobbies = new Vector<>();
  private final AtomicInteger lobbyCounter = new AtomicInteger(1);
  private final GameFactory gameFactory = new GameFactory();

  /**
   * Retrieves the singleton instance of LobbyHandler.
   *
   * @return the singleton instance
   */
  public static synchronized LobbyHandler getInstance() {
    if (instance == null) {
      instance = new LobbyHandler();
    }
    return instance;
  }

  // ---------------------------------------------------------------------------------------------
  // Getters & Setters
  // ---------------------------------------------------------------------------------------------

  public Optional<Vector<Lobby>> getWaitingLobbies() {
    return Optional.ofNullable(waitingLobbies);
  }

  /**
   * Returning a comma-separated string with the IDs of the lobbies across all states.
   *
   * @return a String with the formatted lobbies
   */
  public String getLobbies() {
    // Collect all names of waiting lobbies
    List<String> waitingNames = new ArrayList<>();
    for (Lobby lobby : waitingLobbies) {
      waitingNames.add(lobby.getName());
    }

    // Collect all names of playing lobbies
    List<String> playingNames = new ArrayList<>();
    for (Lobby lobby : playingLobbies) {
      playingNames.add(lobby.getName());
    }

    // put together
    return String.join(":", waitingNames) + ";" + String.join(":", playingNames);
  }

  // ---------------------------------------------------------------------------------------------
  // Lobby Management & Methods
  // ---------------------------------------------------------------------------------------------

  /**
   * Starts a game in the given lobby.
   *
   * @param id the ID of the lobby to start the game in
   * @param requester the client requesting the game start
   */
  public void startGame(String id, ClientHandler requester) {
    Optional<Lobby> lobbyOpt = findLobbyById(id, waitingLobbies);
    if (lobbyOpt.isEmpty()) {
      requester.sendMessage(
          Packet.of(Command.REJECT, "Lobby not found or has already started: " + id));
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

    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameState gs = gameFactory.createWithDefaultRules(lobby.getId(), seeds, map);
    GameHandler gameHandler = new GameHandler(gs, lobby);

    lobby.attachGame(gameHandler);
    waitingLobbies.remove(lobby);
    playingLobbies.add(lobby);

    gameHandler.startMatch(System.currentTimeMillis());
    lobby.broadcastGameStart();
    gameHandler.startGameLoop();
  }

  /**
   * Creates a new lobby with the given name and host.
   *
   * @param name the name of the lobby
   * @param host the host of the lobby
   * @return the created lobby
   */
  public Lobby createLobby(String name, ClientHandler host) {
    name = name.replace(":", "");
    name = name.replace(" ", "");
    while (findLobbyById(name).isPresent()) {
      name += "1";
    }

    Lobby lobby = new Lobby(name, name, host);
    waitingLobbies.add(lobby);
    host.setCurrentLobby(lobby);
    lobby.broadcastLobbyInfo();

    LOGGER.info("Lobby {} created by {}", name, host.getName());
    return lobby;
  }

  /**
   * Joins a player to a lobby.
   *
   * @param id the ID of the lobby to join
   * @param player the player to join the lobby
   */
  public void joinLobby(String id, ClientHandler player) {
    Optional<Lobby> lobbyOpt = findLobbyById(id, waitingLobbies);
    if (lobbyOpt.isEmpty()) {
      player.sendMessage(
          Packet.of(Command.REJECT, "Lobby not found or has already started: " + id));
      return;
    }

    Lobby lobby = lobbyOpt.get();
    if (lobby.addPlayer(player)) {
      player.setCurrentLobby(lobby);
    }
  }

  /**
   * Allows a player to spectates a lobby.
   *
   * @param id the ID of the lobby to spectate
   * @param player the player to spectate the lobby
   */
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

  /**
   * Removes a player or spectator from a lobby. If the lobby becomes empty, it is destroyed.
   *
   * @param id the ID of the lobby
   * @param player the client to remove
   */
  public void leaveLobby(String id, ClientHandler player) {
    Optional<Lobby> lobbyOpt = findLobbyById(id);
    if (lobbyOpt.isEmpty()) return;

    Lobby lobby = lobbyOpt.get();
    if (lobby.removePlayer(player) || lobby.removeSpectator(player)) {
      player.setCurrentLobby(null);

      boolean isEmpty = lobby.getPlayers().get().isEmpty() && lobby.getSpectators().get().isEmpty();

      LOGGER.info(
          "leaveLobby: isEmpty={}, waitingContains={}", isEmpty, waitingLobbies.contains(lobby));

      if (isEmpty && waitingLobbies.contains(lobby)) {
        waitingLobbies.remove(lobby);
        LOGGER.info("Empty lobby {} removed.", id);
      }
    }
  }

  /**
   * Moves a lobby from the playing state to the finished state.
   *
   * @param id the ID of the lobby to finish
   */
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

  /**
   * Moves a lobby from the finish state to the waiting state.
   *
   * @param id the ID of the lobby to finish
   */
  public void resetLobby(String id) {
    Optional<Lobby> lobbyOpt = findLobbyById(id, finishedLobbies);
    if (lobbyOpt.isPresent()) {
      finishedLobbies.remove(lobbyOpt.get());
    } else {
      lobbyOpt = findLobbyById(id, playingLobbies);
      if (lobbyOpt.isPresent()) {
        playingLobbies.remove(lobbyOpt.get());
      }
    }

    if (lobbyOpt.isEmpty()) {
      LOGGER.warn("Could not find lobby to reset: {}", id);
      return;
    }

    Lobby lobby = lobbyOpt.get();
    lobby.resetGame();
    waitingLobbies.add(lobby);
    lobby.broadcastLobbyInfo();
    LOGGER.info("Lobby {} is open again.", id);
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
