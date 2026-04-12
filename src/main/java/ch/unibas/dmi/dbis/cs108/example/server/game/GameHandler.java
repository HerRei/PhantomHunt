package ch.unibas.dmi.dbis.cs108.example.server.game;

import ch.unibas.dmi.dbis.cs108.example.server.game.state.GamePhase;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.GameState;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.InputState;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.PlayerRole;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.PlayerState;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.Position;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.RoundOutcome;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.RoundOutcomeType;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.RoundState;
import ch.unibas.dmi.dbis.cs108.example.server.lobby.LobbyHandler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Owns the match flow and mutates the extracted game state classes safely.
 * Acts as the authoritative controller for a running game.
 */
public class GameHandler {

  private final GameState gameState;
  private final LobbyHandler lobbyHandler;

  public GameHandler(GameState gameState, LobbyHandler lobbyHandler) {
    this.gameState = Objects.requireNonNull(gameState, "gameState must not be null");
    this.lobbyHandler = Objects.requireNonNull(lobbyHandler, "lobbyHandler must not be null");
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
      return;
    }

    RoundState roundState = gameState.getMutableRoundState();
    roundState.incrementCurrentRound();
    roundState.incrementHumanIndex();
    startCurrentRound(nowMillis);
  }

  /**
   * Checks if the round time has expired.
   *
   * @param nowMillis The current server time in milliseconds.
   * @return True if the round time has expired, false otherwise.
   */
  public synchronized boolean isRoundTimeExpired(long nowMillis) {
    return gameState.getPhase() == GamePhase.ROUND_RUNNING
            && nowMillis >= gameState.getRoundEndTimeMillis();
  }

  /**
   * Gets the remaining time in the current round.
   *
   * @param nowMillis The current server time in milliseconds.
   * @return The remaining time in the current round in milliseconds.
   */
  public synchronized long getRemainingRoundTimeMillis(long nowMillis) {
    if (gameState.getPhase() != GamePhase.ROUND_RUNNING) {
      return 0L;
    }
    return Math.max(0L, gameState.getRoundEndTimeMillis() - nowMillis);
  }

  /**
   * Checks if there is a next round
   *
   * @return True if there is a next round, false otherwise.
   */
  public synchronized boolean hasNextRound() {
    return gameState.getCurrentRound() < gameState.getRules().totalRounds();
  }

  /**
   * Checks if the game is running.
   *
   * @return True if the game is running, false otherwise.
   */
  public synchronized boolean gameIsRunning() {
    return gameState.getPhase() == GamePhase.ROUND_RUNNING || gameState.getPhase() == GamePhase.ROUND_ENDED;
  }

  /**
   * Updates the input state of a player.
   *
   * @param playerId The ID of the player.
   * @param up       True if the up key is pressed, false otherwise.
   * @param down     True if the down key is pressed, false otherwise.
   * @param left     True if the left key is pressed, false otherwise.
   * @param right    True if the right key is pressed, false otherwise.
   */
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
