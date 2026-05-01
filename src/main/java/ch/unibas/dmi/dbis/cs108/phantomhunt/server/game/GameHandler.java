package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.*;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.LobbyHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.session.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Owns the match flow and mutates the extracted game state classes safely. Acts as the
 * authoritative controller for a running game.
 */
public class GameHandler {

  private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
  private static GameHandler instance;
  private final GameState gameState;
  private final LobbyHandler lobbyHandler;
  private final Lobby lobby;
  private ScheduledExecutorService gameLoopExecutor;
  private boolean humanCatchesGhosts;
  private static final int TICKS_PER_SECOND = 20;
  private static final long TICK_TIME_MS = 1000 / TICKS_PER_SECOND;
  private static final long ROUND_END_WAIT_MS = 3000;
  private static final int WISDOM_ROUND_SCORE_BONUS = 5;
  private final Set<String> wisdomBonusPlayerIds;

  public GameHandler(GameState gs, Lobby lobby) {
    this.lobby = lobby;
    this.gameState = Objects.requireNonNull(gs, "gameState must not be null");
    this.lobbyHandler =
        Objects.requireNonNull(LobbyHandler.getInstance(), "lobbyHandler must not be null");
    this.humanCatchesGhosts = false;
    this.wisdomBonusPlayerIds = consumeWisdomBonuses(lobby);
    instance = this;
  }

  public static synchronized GameHandler getInstance() {
    return instance;
  }

  /** Broadcasts current positions and roles to all clients in the lobby. */
  public void broadcastGameState() {
    if (gameState.getPhase() != GamePhase.ROUND_RUNNING && gameState.getPhase() != GamePhase.ROUND_ENDED) {
      return;
    }
    String payload =
        String.format(
            "%d %d %s %f %f %b",
            gameState.getRoundStateSnapshot().getCurrentRound(),
            gameState.getRoundTimeRemaining(),
            gameState.getSerializedPlayers(),
            gameState.getAbilityPosition().getX(),
            gameState.getAbilityPosition().getY(),
            gameState.isAbilityAvailable());
    lobby.broadcast(Packet.of(Command.GSU, payload));
  }

  /** Main game loop iteration (tick). */
  public synchronized void tick(double deltaTime, long now) {
    if (gameState.getPhase() == GamePhase.ROUND_ENDED) {
      long timeSinceRoundEnd = now - gameState.getLastRoundOutcome().get().getEndedAtMillis();
      if (timeSinceRoundEnd >= ROUND_END_WAIT_MS) {
        advanceToNextRound(now);
      }
      return;
    }

    if (gameState.getPhase() != GamePhase.ROUND_RUNNING) {
      return;
    }

    // 1. Update movement for all players
    updatePlayerPositions(deltaTime);

    // 2. Check if phantoms caught the human or reversed
    checkCatchCollisions(now, humanCatchesGhosts);

    // 3. Check for ability collision
    checkAbilityCollision();

    // 4. Check for round timeout
    if (now >= gameState.getRoundEndTimeMillis()) {
      endRoundHumanSurvived(now);
    }

    // 5. Inform all clients about the new state
    broadcastGameState();

    // 6. Check if there are still 4 players inside
    checkPlayerSize();
  }

  private void updatePlayerPositions(double deltaTime) {
    double speed = gameState.getRules().moveSpeedPerSecond();
    MapLogic map = MapLogic.getInstance();

    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      PlayerState ps = gameState.getMutablePlayerAt(i);
      Position pos = ps.getPosition();
      InputState currentMovement = ps.getRealInput();

      pos.updatePosition(currentMovement, speed, deltaTime, map);
      InputState nextRequest = ps.getInputState();
      ;
      if (pos.checkValidInput(currentMovement, nextRequest, map) && nextRequest.isMoving()) {
        ps.setRealInput(nextRequest);
      } else {
        pos.checkValidInput(currentMovement, currentMovement, map);
      }
    }
  }

  private double calculateDistance(Position p1, Position p2) {
    return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
  }

  private boolean isCollidingWithWall(double x, double y, double radius) {
    return MapCollision.collidesWithWall(gameState.getMapSnapshot(), x, y, radius);
  }

  /** Starts the game loop. */
  public void startGameLoop() {
    if (gameLoopExecutor != null) return;

    gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();

    gameLoopExecutor.scheduleAtFixedRate(
        () -> {
          try {
            double deltaTime = TICK_TIME_MS / 1000.0;
            tick(deltaTime, System.currentTimeMillis());
          } catch (Exception e) {
            // Important
          }
        },
        0,
        TICK_TIME_MS,
        TimeUnit.MILLISECONDS);
  }

  /** Stops the game loop when the game ends. */
  public void stopGameLoop() {
    if (gameLoopExecutor != null) {
      gameLoopExecutor.shutdown();
      gameLoopExecutor = null;
    }
  }

  public void shutdown() {
    stopGameLoop();
    this.humanCatchesGhosts = false;
    LOGGER.info("GameHandler for lobby {} shut down.", lobby.getId());
  }

  private void checkCatchCollisions(long now, boolean state) {
    PlayerState human = gameState.getMutablePlayerAt(gameState.getHumanIndex());
    double radius = gameState.getRules().playerRadius();

    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      if (i == gameState.getHumanIndex()) continue;

      PlayerState phantom = gameState.getMutablePlayerAt(i);
      double dist = calculateDistance(human.getPosition(), phantom.getPosition());

      // If circles overlap, human is caught or player respawns
      if (dist < (radius * 2)) {
        if (state) {
          human.addScore(gameState.getHumanCatchBonus());
          phantom.setPosition(
              new Position(
                  MapLogic.getInstance()
                      .setRandomPosition(
                          phantom.getPosition().getLastSpawn(),
                          gameState.getPlayers(),
                          GameState.SPAWN_DISTANCE),
                  MapLogic.getInstance()));
        } else {
          endRoundHumanCaught(phantom.getPlayerId(), now);
          break;
        }
      }
    }
  }

  private void checkAbilityCollision() {
    if (!gameState.isAbilityAvailable()) {
      return;
    }
    PlayerState human = gameState.getHumanPlayer();
    double dist = calculateDistance(human.getPosition(), gameState.getAbilityPosition());
    if (dist < gameState.getRules().playerRadius() * 2) {
      gameState.setAbilityAvailable(false);
      tryAbility(human.getPlayerId());
    }
  }

  /**
   * Starts the match and initializes the first round.
   *
   * @param nowMillis The current server time in milliseconds.
   */
  public synchronized void startMatch(long nowMillis) {
    ensurePhase(GamePhase.WAITING_TO_START, "Match has already been started.");
    RoundState roundState = gameState.getMutableRoundState();
    roundState.setCurrentRound(1);
    roundState.setHumanIndex(0);
    startCurrentRound(nowMillis);
  }

  /**
   * Concludes the round immediately with the human being caught.
   *
   * @param catcherPlayerId The ID of the phantom who caught the human.
   * @param nowMillis The current server time in milliseconds.
   */
  public synchronized void endRoundHumanCaught(String catcherPlayerId, long nowMillis) {
    ensurePhase(GamePhase.ROUND_RUNNING, "Round is not running.");
    lobby.broadcast(Packet.of(Command.INFO, "__HUMAN_CAUGHT__"));

    RoundState roundState = gameState.getMutableRoundState();
    PlayerState human = gameState.getMutablePlayerAt(roundState.getHumanIndex());
    PlayerState catcher = gameState.requireMutablePlayer(catcherPlayerId);

    if (catcher.getRole() != PlayerRole.PHANTOM) {
      throw new IllegalArgumentException("Only a phantom can catch the human.");
    }

    human.setCaughtThisRound(true);

    RoundOutcome outcome =
        new RoundOutcome(
            roundState.getCurrentRound(),
            RoundOutcomeType.HUMAN_CAUGHT,
            human.getPlayerId(),
            Optional.of(catcher.getPlayerId()),
            nowMillis);

    applyRoundScoring(outcome, nowMillis);
    finishRound(outcome);
  }

  /**
   * Concludes the round immediately with the human surviving the time limit.
   *
   * @param nowMillis The current server time in milliseconds.
   */
  public synchronized void endRoundHumanSurvived(long nowMillis) {
    ensurePhase(GamePhase.ROUND_RUNNING, "Round is not running.");

    RoundState roundState = gameState.getMutableRoundState();
    PlayerState human = gameState.getMutablePlayerAt(roundState.getHumanIndex());

    RoundOutcome outcome =
        new RoundOutcome(
            roundState.getCurrentRound(),
            RoundOutcomeType.HUMAN_SURVIVED,
            human.getPlayerId(),
            Optional.empty(),
            nowMillis);

    applyRoundScoring(outcome, nowMillis);
    finishRound(outcome);
  }

  private void checkPlayerSize() {
    if (lobby.getPlayers().get().size() != GameState.REQUIRED_PLAYER_COUNT) {
      if (gameState.getPhase() == GamePhase.MATCH_ENDED
          || gameState.getPhase() == GamePhase.ABORTED) {
        return;
      }
      LOGGER.warn("Not enough players in lobby {}, aborting match.", lobby.getId());
      abortMatch("Match got aborted. Player Left.");
    }
  }

  /**
   * Aborts the entire match prematurely (e.g., due to player disconnect).
   *
   * @param reason The reason for the abort.
   */
  public synchronized void abortMatch(String reason) {
    if (gameState.getPhase() == GamePhase.MATCH_ENDED
        || gameState.getPhase() == GamePhase.ABORTED) {
      return;
    }
    gameState.setPhase(GamePhase.ABORTED);
    stopGameLoop();
    lobby.broadcast(Packet.of(Command.INFO, reason));
    lobbyHandler.resetLobby(gameState.getMatchId());
    lobby.broadcastLobbyInfo();
  }

  /**
   * Progresses the game to the next round, or finishes the match if all rounds are played.
   *
   * @param nowMillis The current server time in milliseconds.
   */
  public synchronized void advanceToNextRound(long nowMillis) {
    ensurePhase(GamePhase.ROUND_ENDED, "Can only advance after a round has ended.");
    lobby.broadcast(Packet.of(Command.ABILITY, "END"));
    if (!hasNextRound()) {
      List<PlayerState> players = gameState.getPlayers();
      for (PlayerState p : players) {
        Registry.getInstance().addHighscore(p.getNickname(), p.getScore());
      }
      broadcastGameState();
      gameState.setPhase(GamePhase.MATCH_ENDED);
      stopGameLoop();
      lobby.broadcast(Packet.of(Command.GAME_FINISH));
      lobbyHandler.finishLobby(gameState.getMatchId());
      return;
    }
    RoundState roundState = gameState.getMutableRoundState();
    roundState.incrementCurrentRound();
    roundState.advanceHumanIndex(gameState.getPlayerCount());
    startCurrentRound(nowMillis);
  }

  public synchronized boolean isRoundTimeExpired(long nowMillis) {
    return gameState.getPhase() == GamePhase.ROUND_RUNNING
        && nowMillis >= gameState.getRoundEndTimeMillis();
  }

  public synchronized long getRemainingRoundTimeMillis(long nowMillis) {
    if (gameState.getPhase() != GamePhase.ROUND_RUNNING) {
      return 0L;
    }
    return Math.max(0L, gameState.getRoundEndTimeMillis() - nowMillis);
  }

  public synchronized boolean hasNextRound() {
    return gameState.getCurrentRound() < gameState.getRules().totalRounds();
  }

  public synchronized boolean gameIsRunning() {
    return gameState.getPhase() == GamePhase.ROUND_RUNNING
        || gameState.getPhase() == GamePhase.ROUND_ENDED;
  }

  public synchronized void updateInput(String playerId, int vertical, int horizontal) {
    PlayerState player = gameState.requireMutablePlayer(playerId);
    player.setInputState(new InputState(vertical, horizontal));
  }

  public synchronized Optional<PlayerState> findPlayer(String playerId) {
    return gameState.findPlayer(playerId);
  }

  public synchronized PlayerState getHumanPlayer() {
    return gameState.getHumanPlayer();
  }

  public synchronized List<PlayerState> getPhantomPlayers() {
    return gameState.getPhantomPlayers();
  }

  public synchronized List<PlayerState> getPlayersSnapshot() {
    return gameState.getPlayersSnapshot();
  }

  public synchronized Optional<PlayerState> getWinner() {
    return gameState.getWinner();
  }

  public synchronized Optional<RoundOutcome> getLastRoundOutcome() {
    return gameState.getLastRoundOutcome();
  }

  public synchronized GamePhase getPhase() {
    return gameState.getPhase();
  }

  public synchronized int getCurrentRound() {
    return gameState.getCurrentRound();
  }

  public synchronized int getHumanIndex() {
    return gameState.getHumanIndex();
  }

  public synchronized long getRoundStartTimeMillis() {
    return gameState.getRoundStartTimeMillis();
  }

  public synchronized long getRoundEndTimeMillis() {
    return gameState.getRoundEndTimeMillis();
  }

  public synchronized RoundState getRoundStateSnapshot() {
    return gameState.getRoundStateSnapshot();
  }

  public synchronized boolean isMatchFinished() {
    return gameState.isMatchFinished();
  }

  public synchronized GameState getGameState() {
    return gameState;
  }

  private void startCurrentRound(long nowMillis) {
    RoundState roundState = gameState.getMutableRoundState();

    if (roundState.getCurrentRound() < 1
        || roundState.getCurrentRound() > gameState.getRules().totalRounds()) {
      throw new IllegalStateException(
          "Cannot start illegal round number: " + roundState.getCurrentRound());
    }
    if (roundState.getHumanIndex() < 0
        || roundState.getHumanIndex() >= gameState.getPlayerCount()) {
      throw new IllegalStateException("Invalid humanIndex: " + roundState.getHumanIndex());
    }

    assignRolesForCurrentRound();
    resetPlayersForNewRound();
    resetAllInputs();

    gameState.setAbilityPosition(new Position(MapLogic.getInstance().useRandomSpawnPoint(), MapLogic.getInstance()));
    gameState.setAbilityAvailable(true);

    roundState.setRoundStartTimeMillis(nowMillis);
    roundState.setRoundEndTimeMillis(nowMillis + gameState.getRules().roundDurationMillis());
    gameState.setPhase(GamePhase.ROUND_RUNNING);
    gameState.clearLastRoundOutcome();
  }

  private void finishRound(RoundOutcome outcome) {
    gameState.setLastRoundOutcome(outcome);
    gameState.setPhase(GamePhase.ROUND_ENDED);
    resetAllInputs();
  }

  private void assignRolesForCurrentRound() {
    int humanIndex = gameState.getHumanIndex();
    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      PlayerState player = gameState.getMutablePlayerAt(i);
      if (i == humanIndex) {
        player.setRole(PlayerRole.HUMAN);
        player.setRemainingAbility(gameState.getRules().humanAbilitys());
      } else {
        player.setRole(PlayerRole.PHANTOM);
      }
      player.setCaughtThisRound(false);
    }
  }

  private void resetPlayersForNewRound() {
    MapLogic.getInstance().resetSpawnPoints();
    List<Position> spawns = GameFactory.createDefaultSpawnPositions();
    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      PlayerState player = gameState.getMutablePlayerAt(i);
      player.setPosition(spawns.get(i).copy());
    }
  }

  private void resetAllInputs() {
    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      PlayerState player = gameState.getMutablePlayerAt(i);
      player.setRealInput(new InputState(0, 0));
      player.setInputState(new InputState(0, 0));
    }
  }

  private void applyRoundScoring(RoundOutcome outcome, long nowMillis) {
    long survivedMillis =
        Math.max(
            0L,
            Math.min(nowMillis, gameState.getRoundEndTimeMillis())
                - gameState.getRoundStartTimeMillis());
    int survivedWholeSeconds = (int) Math.ceil(survivedMillis / 1000L);

    int humanIndex = gameState.getHumanIndex();
    PlayerState human = gameState.getMutablePlayerAt(humanIndex);
    human.addScore(survivedWholeSeconds * gameState.getRules().humanPointsPerSecond());

    if (outcome.getType() == RoundOutcomeType.HUMAN_SURVIVED) {
      human.addScore(gameState.getRules().humanRoundWinBonus());
    }

    if (outcome.getType() == RoundOutcomeType.HUMAN_CAUGHT) {
      for (int i = 0; i < gameState.getPlayerCount(); i++) {
        if (i != humanIndex) {
          gameState.getMutablePlayerAt(i).addScore(gameState.getRules().phantomRoundWinBonus());
        }
      }

      outcome
          .getCatcherPlayerId()
          .ifPresent(
              catcherId ->
                  gameState
                      .requireMutablePlayer(catcherId)
                      .addScore(gameState.getRules().phantomCatchBonus()));
    }

    applyWisdomRoundBonuses();
  }

  private Set<String> consumeWisdomBonuses(Lobby lobby) {
    Set<String> playerIds = new HashSet<>();
    lobby.getPlayers()
        .ifPresent(
            players -> {
              for (var player : players) {
                if (player.consumeWisdomRoundBonus()) {
                  playerIds.add(player.getName());
                }
              }
            });
    return playerIds;
  }

  private void applyWisdomRoundBonuses() {
    if (wisdomBonusPlayerIds.isEmpty()) {
      return;
    }
    for (PlayerState player : gameState.getPlayers()) {
      if (wisdomBonusPlayerIds.contains(player.getPlayerId())) {
        player.addScore(WISDOM_ROUND_SCORE_BONUS);
      }
    }
  }

  private void ensurePhase(GamePhase expected, String message) {
    if (gameState.getPhase() != expected) {
      throw new IllegalStateException(message + " Current phase: " + gameState.getPhase());
    }
  }

  public void tryAbility(String playerId) {
    PlayerState player = gameState.requireMutablePlayer(playerId);
    if (player.getRole() == PlayerRole.HUMAN
        && player.getRemainingAbility() > 0
        && !humanCatchesGhosts) {
      humanCatchesGhosts = true;
      lobby.broadcast(Packet.of(Command.ABILITY, "START"));
      player.setRemainingAbility(player.getRemainingAbility() - 1);
      ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
      scheduler.schedule(
          () -> {
            humanCatchesGhosts = false;
            lobby.broadcast(Packet.of(Command.ABILITY, "END"));
            scheduler.shutdown();
          },
          10,
          TimeUnit.SECONDS);
    }
  }

  public synchronized void tryLobbyChatAbility(String playerId) {
    Optional<PlayerState> playerSnapshot = gameState.findPlayer(playerId);
    if (playerSnapshot.isEmpty() || playerSnapshot.get().getRole() != PlayerRole.HUMAN) {
      return;
    }

    PlayerState player = gameState.requireMutablePlayer(playerId);
    player.addScore(-50);
    tryAbility(playerId);
    broadcastGameState();
  }
}
