package ch.unibas.dmi.dbis.cs108.example.server.game.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Thread-safe central state repository for a single match.
 * Holds all data regarding players, the map, rounds, and rules
 */
public class GameState {
  public static final int REQUIRED_PLAYER_COUNT = 4;

  private final String matchId;
  private final GameRules rules;
  private final TileType[][] map;
  private final List<PlayerState> players;
  private GamePhase phase;
  private final RoundState roundState;
  private RoundOutcome lastRoundOutcome;

  /**
   * Constructs a new GameState for a match.
   *
   * @param matchId A unique identifier for this specific match.
   * @param rules   defining how this match should be played.
   * @param map     2D array representing the map.
   * @param players A list representing the players.
   */
  public GameState(String matchId, GameRules rules, TileType[][] map, List<PlayerState> players) {
    this.matchId = requireNonBlank(matchId, "Must not be blank");
    this.rules = Objects.requireNonNull(rules, "Rules must not be null (stp)");
    this.map = copyMap(Objects.requireNonNull(map, "map must not be null"));
    this.players = new ArrayList<>(Objects.requireNonNull(players, "players must not be null"));
    if (this.players.size() != REQUIRED_PLAYER_COUNT) {
      throw new IllegalArgumentException(
              "A match requires exactly " + REQUIRED_PLAYER_COUNT + " players.");
    }
    this.phase = GamePhase.WAITING_TO_START;
    this.roundState = new RoundState(0, -1, 0L, 0L);
    this.lastRoundOutcome = null;
  }

  /**
   * Retrieves a safe snapshot of a player's current state.
   */
  public synchronized Optional<PlayerState> findPlayer(String playerId) {
    for (PlayerState player : players) {
      if (player.getPlayerId().equals(playerId)) {
        return Optional.of(player.copy());
      }
    }
    return Optional.empty();
  }

  /**
   * Retrieves a safe snapshot of the current human player.
   */
  public synchronized PlayerState getHumanPlayer() {
    ensureAtLeastOneRoundStarted();
    return players.get(roundState.getHumanIndex()).copy();
  }

  /**
   * Retrieves safe snapshots of all current phantom players.
   */
  public synchronized List<PlayerState> getPhantomPlayers() {
    ensureAtLeastOneRoundStarted();
    List<PlayerState> phantoms = new ArrayList<>();
    for (int i = 0; i < players.size(); i++) {
      if (i != roundState.getHumanIndex()) {
        phantoms.add(players.get(i).copy());
      }
    }
    return phantoms;
  }

  /**
   * Retrieves the mutable player state for internal server modifications.
   */
  public synchronized PlayerState requireMutablePlayer(String playerId) {
    for (PlayerState player : players) {
      if (player.getPlayerId().equals(playerId)) {
        return player;
      }
    }
    throw new IllegalArgumentException("Unknown playerId: " + playerId);
  }

  public synchronized PlayerState getMutablePlayerAt(int index) {
    return players.get(index);
  }

  public synchronized int getPlayerCount() {
    return players.size();
  }

  public synchronized RoundState getMutableRoundState() {
    return roundState;
  }

  public synchronized void setPhase(GamePhase phase) {
    this.phase = Objects.requireNonNull(phase, "phase must not be null");
  }

  public synchronized void setLastRoundOutcome(RoundOutcome lastRoundOutcome) {
    this.lastRoundOutcome = lastRoundOutcome;
  }

  public synchronized void clearLastRoundOutcome() {
    this.lastRoundOutcome = null;
  }

  /**
   * Returns a complete, detached copy of all players.
   */
  public synchronized List<PlayerState> getPlayersSnapshot() {
    List<PlayerState> copy = new ArrayList<>();
    for (PlayerState player : players) {
      copy.add(player.copy());
    }
    return copy;
  }

  /**
   * Determines the winner if the match has ended.
   *
   * @return a copy of the winning players state.
   */
  public synchronized Optional<PlayerState> getWinner() {
    if (phase != GamePhase.MATCH_ENDED) {
      return Optional.empty();
    }

    PlayerState best = null;
    for (PlayerState player : players) {
      if (best == null || player.getScore() > best.getScore()) {
        best = player;
      }
    }
    return Optional.ofNullable(best == null ? null : best.copy());
  }

  public synchronized String getMatchId() {
    return matchId;
  }

  public synchronized GameRules getRules() {
    return rules;
  }

  public synchronized TileType[][] getMapSnapshot() {
    return copyMap(map);
  }

  public synchronized GamePhase getPhase() {
    return phase;
  }

  public synchronized int getCurrentRound() {
    return roundState.getCurrentRound();
  }

  public synchronized int getHumanIndex() {
    return roundState.getHumanIndex();
  }

  public synchronized long getRoundStartTimeMillis() {
    return roundState.getRoundStartTimeMillis();
  }

  public synchronized long getRoundEndTimeMillis() {
    return roundState.getRoundEndTimeMillis();
  }

  public synchronized RoundState getRoundStateSnapshot() {
    return roundState.copy();
  }

  public synchronized Optional<RoundOutcome> getLastRoundOutcome() {
    return Optional.ofNullable(lastRoundOutcome);
  }

  public synchronized boolean isMatchFinished() {
    return phase == GamePhase.MATCH_ENDED;
  }

  public synchronized int getMapHeight() {
    return map.length;
  }

  public synchronized int getMapWidth() {
    return map[0].length;
  }

  private static String requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private void ensureAtLeastOneRoundStarted() {
    if (roundState.getCurrentRound() <= 0) {
      throw new IllegalStateException("Match has not started yet.");
    }
  }

  private static TileType[][] copyMap(TileType[][] source) {
    TileType[][] copy = new TileType[source.length][];
    for (int y = 0; y < source.length; y++) {
      copy[y] = new TileType[source[y].length];
      System.arraycopy(source[y], 0, copy[y], 0, source[y].length);
    }
    return copy;
  }

  /**
   * Initial data required to seed a player into the game.
   */
  public record PlayerSeed(String playerId, String nickname) {
  }
}
