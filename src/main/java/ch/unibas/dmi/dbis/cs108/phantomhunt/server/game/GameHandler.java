package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.*;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.LobbyHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Owns the match flow and mutates the extracted game state classes safely.
 * Acts as the authoritative controller for a running game.
 */
public class GameHandler {

  private final GameState gameState;
  private final LobbyHandler lobbyHandler;
  private final Lobby lobby;
  private ScheduledExecutorService gameLoopExecutor;
  private static final int TICKS_PER_SECOND = 20;
  private static final long TICK_TIME_MS = 1000 / TICKS_PER_SECOND;
  private static final long ROUND_END_WAIT_MS = 3000;

  public GameHandler(GameState gameState, LobbyHandler lobbyHandler, Lobby lobby) {
    this.lobby = lobby;
    this.gameState = Objects.requireNonNull(gameState, "gameState must not be null");
    this.lobbyHandler = Objects.requireNonNull(lobbyHandler, "lobbyHandler must not be null");
  }

  /**
   * Broadcasts current positions and roles to all clients in the lobby.
   */
  public void broadcastGameState() {
    if (gameState.getPhase() != GamePhase.ROUND_RUNNING) {
      return;
    }
    String payload = String.format("%d %d %s",
            gameState.getRoundStateSnapshot().getCurrentRound(),
            gameState.getRoundTimeRemaining(),
            gameState.getSerializedPlayers()
    );
    lobby.broadcast(Packet.of(Command.GSU, payload));
  }

  /**
   * Main game loop iteration (tick).
   */
  public void tick(double deltaTime, long now) {
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

    // 2. Check if phantoms caught the human
    checkCatchCollisions(now);

    // 3. Check for round timeout
    if (now >= gameState.getRoundEndTimeMillis()) {
      endRoundHumanSurvived(now);
    }

    // 4. Inform all clients about the new state
    broadcastGameState();
  }

  /**
   * Calculates new position based on input and map collisions.
   */
  private void updatePlayerPositions(double deltaTime) {
    double speed = gameState.getRules().moveSpeedPerSecond();
    double playerRadius = gameState.getRules().playerRadius();
    double movementRadius = MapCollision.movementRadius(playerRadius);

    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      PlayerState ps = gameState.getMutablePlayerAt(i);
      InputState input = ps.getInputState(); 
      Position pos = ps.getPosition();

      double moveX = 0;
      double moveY = 0;

      if (input.isUp())    moveY -= speed * deltaTime;
      if (input.isDown())  moveY += speed * deltaTime;
      if (input.isLeft())  moveX -= speed * deltaTime;
      if (input.isRight()) moveX += speed * deltaTime;

      // Check X direction
      if (!isCollidingWithWall(pos.getX() + moveX, pos.getY(), movementRadius)) {
        pos.setX(pos.getX() + moveX);
      }
      // Check Y direction
      if (!isCollidingWithWall(pos.getX(), pos.getY() + moveY, movementRadius)) {
        pos.setY(pos.getY() + moveY);
      }

      ps.setPosition(pos);
    }
  }

  private double calculateDistance(Position p1, Position p2) {
    return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
  }


  private boolean isCollidingWithWall(double x, double y, double radius) {
    return MapCollision.collidesWithWall(gameState.getMapSnapshot(), x, y, radius);
  }

  /**
   * Starts the game loop.
   */
  public void startGameLoop() {
    if (gameLoopExecutor != null) return;

    gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();

    // This runs the tick() method repeatedly
    gameLoopExecutor.scheduleAtFixedRate(() -> {
      try {
        // Calculate deltaTime in seconds (1/30 = 0.0333)
        double deltaTime = TICK_TIME_MS / 1000.0;
        tick(deltaTime, System.currentTimeMillis());
      } catch (Exception e) {
        // Important: Catch exceptions so the loop doesn't stop
        e.printStackTrace();
      }
    }, 0, TICK_TIME_MS, TimeUnit.MILLISECONDS);
  }

  /**
   * Stops the game loop when the game ends.
   */
  public void stopGameLoop() {
    if (gameLoopExecutor != null) {
      gameLoopExecutor.shutdown();
      gameLoopExecutor = null;
    }
  }

  private void checkCatchCollisions(long now) {
    PlayerState human = gameState.getMutablePlayerAt(gameState.getHumanIndex());
    double radius = gameState.getRules().playerRadius();

    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      if (i == gameState.getHumanIndex()) continue;

      PlayerState phantom = gameState.getMutablePlayerAt(i);
      double dist = calculateDistance(human.getPosition(), phantom.getPosition());

      // If circles overlap, human is caught
      if (dist < (radius * 2)) {
        endRoundHumanCaught(phantom.getPlayerId(), now);
        break;
      }
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
   * @param nowMillis       The current server time in milliseconds.
   */
  public synchronized void endRoundHumanCaught(String catcherPlayerId, long nowMillis) {
    ensurePhase(GamePhase.ROUND_RUNNING, "Round is not running.");

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

  /**
   * Aborts the entire match prematurely (e.g., due to player disconnect).
   *
   * @param reason The reason for the abort.
   */
  public synchronized void abortMatch(String reason) {
    if (gameState.getPhase() == GamePhase.MATCH_ENDED || gameState.getPhase() == GamePhase.ABORTED) {
      return;
    }
    gameState.setPhase(GamePhase.ABORTED);
    gameState.setLastRoundOutcome(
        new RoundOutcome(
            gameState.getCurrentRound(),
            RoundOutcomeType.ROUND_ABORTED,
            null,
            Optional.empty(),
            System.currentTimeMillis(),
            reason));
    lobbyHandler.finishLobby(gameState.getMatchId());
  }

  /**
   * Progresses the game to the next round, or finishes the match if all rounds are played.
   *
   * @param nowMillis The current server time in milliseconds.
   */
  public synchronized void advanceToNextRound(long nowMillis) {
    ensurePhase(GamePhase.ROUND_ENDED, "Can only advance after a round has ended.");

    if (!hasNextRound()) {
      gameState.setPhase(GamePhase.MATCH_ENDED);
      lobbyHandler.finishLobby(gameState.getMatchId());
      var playersInLobby = lobby.getPlayers().get();
      for (ClientHandler gh : playersInLobby) {
        lobbyHandler.leaveLobby(lobby.getId(), gh);
      }
      lobby.broadcast(Packet.of(Command.GAME_FINISH));
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

  public synchronized boolean gameIsRunning(){
    return gameState.getPhase() == GamePhase.ROUND_RUNNING || gameState.getPhase() == GamePhase.ROUND_ENDED;
  }

  public synchronized void updateInput(
      String playerId, boolean up, boolean down, boolean left, boolean right) {
    PlayerState player = gameState.requireMutablePlayer(playerId);
    player.setInputState(new InputState(up, down, left, right));
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
    if (roundState.getHumanIndex() < 0 || roundState.getHumanIndex() >= gameState.getPlayerCount()) {
      throw new IllegalStateException("Invalid humanIndex: " + roundState.getHumanIndex());
    }

    assignRolesForCurrentRound();
    resetPlayersForNewRound();
    resetAllInputs();

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
      player.setRole(i == humanIndex ? PlayerRole.HUMAN : PlayerRole.PHANTOM);
      player.setCaughtThisRound(false);
    }
  }

  private void resetPlayersForNewRound() {
    List<Position> spawns =
        GameFactory.createDefaultSpawnPositions(gameState.getMapHeight(), gameState.getMapWidth());
    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      PlayerState player = gameState.getMutablePlayerAt(i);
      player.setPosition(spawns.get(i).copy());
    }
  }

  private void resetAllInputs() {
    for (int i = 0; i < gameState.getPlayerCount(); i++) {
      PlayerState player = gameState.getMutablePlayerAt(i);
      player.setInputState(new InputState(false, false, false, false));
    }
  }

  private void applyRoundScoring(RoundOutcome outcome, long nowMillis) {
    long survivedMillis =
        Math.max(
            0L,
            Math.min(nowMillis, gameState.getRoundEndTimeMillis()) - gameState.getRoundStartTimeMillis());
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
                  gameState.requireMutablePlayer(catcherId).addScore(
                      gameState.getRules().phantomCatchBonus()));
    }
  }

  private void ensurePhase(GamePhase expected, String message) {
    if (gameState.getPhase() != expected) {
      throw new IllegalStateException(message + " Current phase: " + gameState.getPhase());
    }
  }
}
