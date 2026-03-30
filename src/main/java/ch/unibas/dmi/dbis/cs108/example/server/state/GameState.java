package ch.unibas.dmi.dbis.cs108.example.server.state;

import javax.management.relation.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
//.Role to be created
//.Rules to be created


/**
 * This is the Single Source of Trooth for the running Phantomhunt match
 * This Class should do the following:
 *  one match objekt
 *  4 fixed player system
 *  4 Rounds
 *  Roles per round
 *  Forced rotation of human rotation
 *  time per round
 *  score handling
 *  Forced end of Round by:
 *    Human scared
 *    Time run out
 *  Match end after round 4
 *  current input state per player
 *  current position per player
 */
//Attention starting pos to be checketd
public class GameState {
  /**
   * 4 players, everyone once a human
   */
  public static final int REQUIRED_PLAYER_COUNT = 4;

  private final String matchId;
  private final Rules rules;
  private final TileType[][] map;
  private final List<PlayerState> players;

  /*
   *Match Cycle phase --> currently:
   * WAITING_TO_START --> ROUND_RUNNING /ROUND_ENDED --> MATCH_ENDED
   * also ABORTED, for force shutdowns
   */
  private GamePhase phase;

  /*
   * Current round number
   * 0 if not started jet
   */
  private int currentRound;

  /*
   * determines who is human this round
   * bc player order will be fixed --> deterministic rotation, evt bit boring but whatever player doesn't have to know
   */
  private int humanIndex;

  /*
   * Timestamps for the active round
   */
  private long roundStartTimeMillis;
  private long roundEndTimeMillis;

  /*
   * Information about the most recently ended round
   * might be redundant later, if proven when all connected
   */
  private RoundOutcome lastRoundOutcome;

  /**
   * Creates a new Gamestate for one single Match
   *
   * <p> </p>match not started automatically. after call {@link *whatever}
   *
   * @param matchId human-readable or generated match id, useful for debugging
   * @param playerSeeds immutable seed data for the 4 participating players
   * @param rules configurable gameplay values such as round length and scoring
   * @param map authoritative server-side map
   */
  public GameState(String matchId, List<PlayerSeed> playerSeeds, Rules rules, TileType[][] map){
    this.matchId = requireNonBlank(matchId, "Must not be blank");
    this.rules = Objects.requireNonNull(rules, "Rules must not be null (stp)");
    this.map = deepCopyAndValidateMap(map); //like name says
    this.players = createPlayers(playerSeeds, this.map); //Attention starting pos to be altered

    /*
     *setting fields to starting values
     */
    this.phase = GamePhase.WAITING_TO_START;
    this.currentRound = 0;
    this.humanIndex = -1;
    this.roundStartTimeMillis = 0L;
    this.roundEndTimeMillis = 0L;
    this.lastRoundOutcome = null;
  }

  // ---------------------------------------------------------------------------------------------
  // Match lifecycle
  // Yess not vibe coded, I just genuenly think this is a good tool for structuring Code
  // ---------------------------------------------------------------------------------------------

  /**
   * Starts whole match immediatly starts Round 1
   *
   * @param nowMillis introduces current server time in milsek
   */

  public synchronized void startMatch(long nowMillis) {
    ensurePhase(GamePhase.WAITING_TO_START, "Match has already been started.");
    this.currentRound = 1;
    this.humanIndex = 0; // round 1, first player is human
    startCurrentRound(nowMillis);
  }

  /**
   * Starts current Round
   */
  private void startCurrentRound(long nowMillis) {
    if (currentRound < 1 || currentRound > rules.totalRounds()) { //should just hardcode to 1 and 4???, idk
      throw new IllegalStateException("Cannot start illegal round nbr (below 1 or over rules.totalRounds(): " + currentRound);
    }
    if (humanIndex < 0 || humanIndex >= players.size()) {
      throw new IllegalStateException("Invalid humanIndex: " + humanIndex);
    }
    assignRolesForCurrentRound();
    resetPlayersForNewRound();
    resetAllInputs();

    this.roundStartTimeMillis = nowMillis;
    this.roundEndTimeMillis = nowMillis + rules.roundDurationMillis(); //might as well just hardcode this
    //this.roundEndTimeMillis = nowMillis + 30000; //here would be test case of 30sek
    //this.roundEndTimeMillis = nowMillis + 90000; //would be 90sek
    this.phase = GamePhase.ROUND_RUNNING; //setting Gamephase
    this.lastRoundOutcome = null;
  }

  /**
   * Ends current round, bc human lost
   * @param nowMillis still current servertime in milsek
   * @param catcherPlayerId playerId of phantom who cought human
   */
  public synchronized void endRoundHumanCaught(String catcherPlayerId, long nowMillis) {
    ensurePhase(GamePhase.ROUND_RUNNING, "Round is not running.");
    PlayerState human = getHumanPlayer();
    PlayerState catcher = findPlayerOrThrow(catcherPlayerId);

    if (catcher.role != Role.PHANTOM) { //just for the love of exceptions, if you can do this you earned a cookie
      throw new IllegalArgumentException("Only a phantom can catch the human.");
    }

    human.caughtThisRound = true;

    RoundOutcome outcome = new RoundOutcome(currentRound,
        RoundOutcomeType.HUMAN_CAUGHT,
        human.playerId,
        Optional.of(catcher.playerId),
        nowMillis);

    applyRoundScoring(outcome, nowMillis);
    finishRound(outcome);
  }

  /**
   * Ends the whole match immediately as in eg force shutdown
   * Useful, when player disconnects or fatal error
   *
   * @param reason textual explanation for debugging / UI messages
   */
  public synchronized void abortMatch(String reason) {
    if (phase == GamePhase.MATCH_ENDED || phase == GamePhase.ABORTED) {
      return;
    }
    this.phase = GamePhase.ABORTED;
    this.lastRoundOutcome =
        new RoundOutcome(
            currentRound,
            RoundOutcomeType.ROUND_ABORTED,
            null,
            Optional.empty(),
            System.currentTimeMillis(),
            reason);
  }

  /**
   * Advances from the currently ended round to the next round.
   *
   * <p>If the current round was the last one, the match becomes MATCH_ENDED instead.
   *
   * @param nowMillis current server time in milliseconds
   */
  public synchronized void advanceToNextRound(long nowMillis) {
    ensurePhase(GamePhase.ROUND_ENDED, "Can only advance after a round has ended.");

    if (!hasNextRound()) {
      this.phase = GamePhase.MATCH_ENDED;
      return;
    }

    this.currentRound++;
    this.humanIndex++;
    startCurrentRound(nowMillis);
  }

  /**
   * Checks whether the current running round has timed out.
   *
   * @param nowMillis current server time in milliseconds
   * @return true if the round time is over
   */
  public synchronized boolean isRoundTimeExpired(long nowMillis) {
    return phase == GamePhase.ROUND_RUNNING && nowMillis >= roundEndTimeMillis;
  }

  /**
   * Returns the remaining time of the current round in milliseconds.
   *
   * <p>Returns 0 if the round is not currently running.
   */
  public synchronized long getRemainingRoundTimeMillis(long nowMillis) {
    if (phase != GamePhase.ROUND_RUNNING) {
      return 0L;
    }
    return Math.max(0L, roundEndTimeMillis - nowMillis);
  }

  /**
   * Returns whether there is another round after the current one.
   */
  public synchronized boolean hasNextRound() {
    return currentRound < rules.totalRounds();
  }

  /**
   * Internal helper for finishing a round.
   */
  private void finishRound(RoundOutcome outcome) {
    this.lastRoundOutcome = outcome;
    this.phase = GamePhase.ROUND_ENDED;
    resetAllInputs();
  }


  // -------------------------------------------------------------------------------------------
  // Player and role handling
  // -------------------------------------------------------------------------------------------

  /**
   * Assigns roles for the current round.
   * Exactly one player becomes HUMAN, the other three become PHANTOM.
   */
  private void assignRolesForCurrentRound() {
    for (int i = 0; i < players.size(); i++) {
      PlayerState player = players.get(i);
      player.role = (i == humanIndex) ? Role.HUMAN : Role.PHANTOM;
      player.caughtThisRound = false;
    }
  }

  /**
   * Resets all player positions to deterministic default spawn positions.
   *
   * <p>For now this uses four corner-near spawn positions. Later this can be replaced by proper
   * map-defined spawn points.
   */
  private void resetPlayersForNewRound() {
    List<Position> spawns = createDefaultSpawnPositions(map.length, map[0].length);

    for (int i = 0; i < players.size(); i++) {
      PlayerState player = players.get(i);
      player.position = spawns.get(i).copy();
    }
  }

  /**
   * Updates inputstarte of player
   *
   * <p>The server remains authoritative. This only stores the latest intended input.
   *
   * @param playerId  player id
   * @param up whether up is pressed
   * @param down whether down is pressed
   * @param left whether left is pressed
   * @param right whether right is pressed
   */
  public synchronized void updateInput(
      String playerId, boolean up, boolean down, boolean left, boolean right) {
    PlayerState player = findPlayerOrThrow(playerId);
    player.inputState = new InputState(up, down, left, right);
  }

  /**
   * Clears all current input states.
   * Useful at round start and round end.
   */
  public synchronized void resetAllInputs() {
    for (PlayerState player : players) {
      player.inputState = new InputState(false, false, false, false);
    }
  }

  /**
   * Finds one player by plyerId
   */
  public synchronized Optional<PlayerState> findPlayer(String playerId) {
    for (PlayerState player : players) {
      if (player.playerId.equals(playerId)) {
        return Optional.of(player.copy());
      }
    }
    return Optional.empty();
  }

  /**
   * returns copy of player who is homan
   */
  public synchronized PlayerState getHumanPlayer() {
    ensureAtLeastOneRoundStarted();
    return players.get(humanIndex).copy();
  }

  /**
   * Returns copies of phantoms
   */
  public synchronized List<PlayerState> getPhantomPlayers() {
    ensureAtLeastOneRoundStarted();
    List<PlayerState> phantoms = new ArrayList<>();
    for (int i = 0; i < players.size(); i++) {
      if (i != humanIndex) {
        phantoms.add(players.get(i).copy());
      }
    }
    return phantoms;
  }

  /**
   * quick lookup no copy, if not found throw
   */
  private PlayerState findPlayerOrThrow(String playerId) {
    for (PlayerState player : players) {
      if (player.playerId.equals(playerId)) {
        return player;
      }
    }
    throw new IllegalArgumentException("Unknown playerId: " + playerId);
  }

  // ---------------------------------------------------------------------------------------------
  // Scoring
  // ---------------------------------------------------------------------------------------------

  /**
   * Applies the round scoring according to the configured rules.
   *
   * <p>currently planned:
   * <ul>
   *   <li>Human: +1 point per second survived</li>
   *   <li>Human: +50 bonus when surviving the whole round</li>
   *   <li>Phantoms: +10 for catching the human</li>
   *   <li>Phantoms: +10 round-win bonus</li>
   * </ul>
   *
   * <p>Those values are not hardcoded in the methods below. They are taken from Rules so you can
   * rebalance later without rewriting game flow logic.
   */
  private void applyRoundScoring(RoundOutcome outcome, long nowMillis) {
    long survivedMillis = Math.max(0L, Math.min(nowMillis, roundEndTimeMillis) - roundStartTimeMillis);
    int survivedWholeSeconds = (int) Math.ceil(survivedMillis / 1000L); //evt not too smart - sorry if you read this

    PlayerState human = players.get(humanIndex);

    // Human always gets survival points for the time survived before the round ended.
    human.score += survivedWholeSeconds * rules.humanPointsPerSecond();

    if (outcome.type == RoundOutcomeType.HUMAN_SURVIVED) {
      human.score += rules.humanRoundWinBonus();
    }

    if (outcome.type == RoundOutcomeType.HUMAN_CAUGHT) {
      for (int i = 0; i < players.size(); i++) {
        if (i != humanIndex) {
          players.get(i).score += rules.phantomRoundWinBonus();
        }
      }

      outcome.catcherPlayerId.ifPresent(catcherId -> {
        PlayerState catcher = findPlayerOrThrow(catcherId);
        catcher.score += rules.phantomCatchBonus();
      });
    }
  }

  /**
   * Returns a defensive copy of the current scoreboard.
   */
  public synchronized List<PlayerState> getPlayersSnapshot() {
    List<PlayerState> copy = new ArrayList<>();
    for (PlayerState player : players) {
      copy.add(player.copy());
    }
    return copy;
  }

  /**
   * Returns the winner if the match has ended normally.
   *
   * <p>If multiple players share the maximum score, the first one found is returned.
   * For now that is enough. Tie-breakers can be added later if needed.
   */
  public synchronized Optional<PlayerState> getWinner() {
    if (phase != GamePhase.MATCH_ENDED) {
      return Optional.empty();
    }

    PlayerState best = null;
    for (PlayerState player : players) {
      if (best == null || player.score > best.score) {
        best = player;
      }
    }
    return Optional.ofNullable(best == null ? null : best.copy());
  }

  // ---------------------------------------------------------------------------------------------
  // Getters - I like getters
  // ---------------------------------------------------------------------------------------------

  public synchronized String getMatchId() {
    return matchId;
  }

  public synchronized Rules getRules() {
    return rules;
  }

  public synchronized TileType[][] getMapSnapshot() {
    return deepCopyAndValidateMap(map);
  }

  public synchronized GamePhase getPhase() {
    return phase;
  }

  public synchronized int getCurrentRound() {
    return currentRound;
  }

  public synchronized int getHumanIndex() {
    return humanIndex;
  }

  public synchronized long getRoundStartTimeMillis() {
    return roundStartTimeMillis;
  }

  public synchronized long getRoundEndTimeMillis() {
    return roundEndTimeMillis;
  }

  public synchronized Optional<RoundOutcome> getLastRoundOutcome() {
    return Optional.ofNullable(lastRoundOutcome);
  }

  public synchronized boolean isMatchFinished() {
    return phase == GamePhase.MATCH_ENDED;
  }

  // ---------------------------------------------------------------------------------------------
  // Validation and creation helpers -- TBH Mainly by LLM, since whole Map logic ect shoudl not be done herer, but was required to proceed
  // Whatever...
  // ---------------------------------------------------------------------------------------------

  private static List<PlayerState> createPlayers(List<PlayerSeed> playerSeeds, TileType[][] map) {
    Objects.requireNonNull(playerSeeds, "playerSeeds must not be null");

    if (playerSeeds.size() != REQUIRED_PLAYER_COUNT) {
      throw new IllegalArgumentException(
          "A match requires exactly " + REQUIRED_PLAYER_COUNT + " players.");
    }

    List<PlayerState> result = new ArrayList<>();
    List<Position> defaultSpawns = createDefaultSpawnPositions(map.length, map[0].length);

    for (int i = 0; i < playerSeeds.size(); i++) {
      PlayerSeed seed = Objects.requireNonNull(playerSeeds.get(i), "player seed must not be null");

      result.add(
          new PlayerState(
              requireNonBlank(seed.playerId(), "playerId must not be blank"),
              requireNonBlank(seed.nickname(), "nickname must not be blank"),
              Role.PHANTOM, // placeholder until the round starts
              defaultSpawns.get(i).copy(),
              new InputState(false, false, false, false),
              0,
              true,
              false));
    }

    return result;
  }

  /**
   * Validates and deep-copies the map.
   *
   * <p>For now this implementation enforces:
   * <ul>
   *   <li>map must not be null</li>
   *   <li>rectangular 2D array</li>
   *   <li>16x16 size, since that is the currently planned map size</li>
   *   <li>no null tile entries</li>
   * </ul>
   */
  private static TileType[][] deepCopyAndValidateMap(TileType[][] source) {
    Objects.requireNonNull(source, "map must not be null");

    if (source.length != 16) {
      throw new IllegalArgumentException("Map must currently have height 16.");
    }

    TileType[][] copy = new TileType[source.length][];

    for (int y = 0; y < source.length; y++) {
      Objects.requireNonNull(source[y], "map row must not be null");

      if (source[y].length != 16) {
        throw new IllegalArgumentException("Map must currently have width 16 in every row.");
      }

      copy[y] = new TileType[source[y].length];
      for (int x = 0; x < source[y].length; x++) {
        if (source[y][x] == null) {
          throw new IllegalArgumentException("Map tile must not be null.");
        }
        copy[y][x] = source[y][x];
      }
    }

    return copy;
  }

  /**
   * Creates four deterministic default spawns near the corners of the map.
   *
   * <p>Later, proper spawn positions should come from the map itself.
   */
  private static List<Position> createDefaultSpawnPositions(int mapHeight, int mapWidth) {
    List<Position> spawns = new ArrayList<>(4);

    // Slightly inside the corners instead of exactly on the boundary.
    spawns.add(new Position(1.5, 1.5));
    spawns.add(new Position(mapWidth - 2.5, 1.5));
    spawns.add(new Position(1.5, mapHeight - 2.5));
    spawns.add(new Position(mapWidth - 2.5, mapHeight - 2.5));

    return spawns;
  }

  private static String requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private void ensurePhase(GamePhase expected, String message) {
    if (phase != expected) {
      throw new IllegalStateException(message + " Current phase: " + phase);
    }
  }

  private void ensureAtLeastOneRoundStarted() {
    if (currentRound <= 0) {
      throw new IllegalStateException("Match has not started yet.");
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Nested helper types -- here parially used LLMs to complete structure
  // Mainly to clean out and as suggestions, after line
  // ---------------------------------------------------------------------------------------------

  /**
   * Initial immutable seed data used to construct the PlayerState objects.
   *
   * <p>For now you can create these from Lobby players. Later, this can be replaced by a more
   * formal player/session identity model. to reduce loading speed
   */
  public record PlayerSeed(String playerId, String nickname) {}

  /**
   * Immutable configuration for one match.
   *
   * <p>Added Rules here,
   */
  public record Rules( //here record, since supposed to hold data
      int totalRounds,
      long roundDurationMillis,
      double playerRadius,
      double moveSpeedPerSecond,
      int humanPointsPerSecond,
      int humanRoundWinBonus,
      int phantomCatchBonus,
      int phantomRoundWinBonus) {

    public Rules {
      if (totalRounds <= 0) {
        throw new IllegalArgumentException("totalRounds must be > 0");
      }
      if (roundDurationMillis <= 0) {
        throw new IllegalArgumentException("roundDurationMillis must be > 0");
      }
      if (playerRadius <= 0.0) {
        throw new IllegalArgumentException("playerRadius must be > 0");
      }
      if (moveSpeedPerSecond <= 0.0) {
        throw new IllegalArgumentException("moveSpeedPerSecond must be > 0");
      }
    }

    /**
     * just some defaults, will be changed later, when balancing
     * Lots of expirimenting needed to be done
     */
    public static Rules defaultRules() {
      return new Rules(
          4,      // total rounds
          30000, // 30 seconds per round as a first reasonable default
          0.25,   // player hitbox radius, certainly need to experiment with this
          3.0,    // tiles / units per second //with this as well
          1,      // human +1 per survived second
          50,     // human full-round bonus
          10,     // phantom catch bonus
          10      // phantom round-win bonus
      );
    }
  }

  /**
   * Player Class //should evt be in an own class?? well problem for later
   * is to represent one player during a running match, might create and not follow the concept of DRY,
   * but should be more efficient this way.
   * --> This way no separation of collections like Nicknames, positions, roles, inputs...
   *
   * <p>This is distinct from ClientHandler. The same connected client may later reconnect or
   * change nickname
   * could provide benefits if class hase a "Stabld" player class
   *
   */
  public static final class PlayerState {
    private final String playerId;
    private String nickname;
    private Role role;
    private Position position;
    private InputState inputState;
    private int score;
    private boolean connected;
    private boolean caughtThisRound;

    private PlayerState(
        String playerId,
        String nickname,
        Role role,
        Position position,
        InputState inputState,
        int score,
        boolean connected,
        boolean caughtThisRound) {
      this.playerId = playerId;
      this.nickname = nickname;
      this.role = role;
      this.position = position;
      this.inputState = inputState;
      this.score = score;
      this.connected = connected;
      this.caughtThisRound = caughtThisRound;
    }

    /**
     * Copy constructor helper for defensive snapshots.
     */
    public PlayerState copy() {
      return new PlayerState(
          playerId,
          nickname,
          role,
          position.copy(),
          inputState.copy(),
          score,
          connected,
          caughtThisRound);
    }

    // ---------------------------------------------------------------------------------------------
    // Second round of Getters - ofc onyl for Player stuff
    // ---------------------------------------------------------------------------------------------

    public String getPlayerId() {
      return playerId;
    }

    public String getNickname() {
      return nickname;
    }

    public Role getRole() {
      return role;
    }

    public Position getPosition() {
      return position.copy();
    }

    public InputState getInputState() {
      return inputState.copy();
    }

    public int getScore() {
      return score;
    }

    public boolean isConnected() {
      return connected;
    }

    public boolean isCaughtThisRound() {
      return caughtThisRound;
    }

    /*
     *
     * Probably bad, since WISKY
     *
    public void setNickname(String nickname) {
      this.nickname = requireNonBlank(nickname, "nickname must not be blank");
    }
    */

    /*
     * WISKY
     * Useful later for reconnect / disconnect logic.
     *
    public void setConnected(boolean connected) {
      this.connected = connected;
    }
    */
  }


  //Quite unsure, if the Positioning should be here, and not seperatly with map ect, but currently here bc implementation.
  //Evt needs remoddeling, if not memory efficient
  /**
   * Continuous 2D world position.
   *
   * <p>The map is tile-based, but player movement is realtime, so continuous coordinates are the
   * cleaner model here.
   */
  public static final class Position {
    private double x;
    private double y;

    public Position(double x, double y) {
      this.x = x;
      this.y = y;
    }

    public Position copy() {
      return new Position(x, y);
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public void setX(double x) {
      this.x = x;
    }

    public void setY(double y) {
      this.y = y;
    }

    @Override
    public String toString() {
      return "Position{x=" + x + ", y=" + y + "}";
    }
  }

  /**
   * Stores the currently active input intention of one player.
   *
   * <p>The server loop can later read this every tick and compute the authoritative movement.
   */
  public static final class InputState {
    private final boolean up;
    private final boolean down;
    private final boolean left;
    private final boolean right;

    public InputState(boolean up, boolean down, boolean left, boolean right) {
      this.up = up;
      this.down = down;
      this.left = left;
      this.right = right;
    }

    public InputState copy() {
      return new InputState(up, down, left, right);
    }

    public boolean isUp() {
      return up;
    }

    public boolean isDown() {
      return down;
    }

    public boolean isLeft() {
      return left;
    }

    public boolean isRight() {
      return right;
    }

    @Override
    public String toString() {
      return "InputState{up=" + up + ", down=" + down + ", left=" + left + ", right=" + right + "}";
    }
  }

  /**
   * Currently in GameState, but should tecnically be in GameSession
   * But currently kind of a mix between the two
   *
   * Information about how and why a round ended.
   */
  public static final class RoundOutcome {
    private final int roundNumber;
    private final RoundOutcomeType type;
    private final String humanPlayerId;
    private final Optional<String> catcherPlayerId;
    private final long endedAtMillis;
    private final String reason;

    public RoundOutcome(
        int roundNumber,
        RoundOutcomeType type,
        String humanPlayerId,
        Optional<String> catcherPlayerId,
        long endedAtMillis) {
      this(roundNumber, type, humanPlayerId, catcherPlayerId, endedAtMillis, null);
    }

    public RoundOutcome(
        int roundNumber,
        RoundOutcomeType type,
        String humanPlayerId,
        Optional<String> catcherPlayerId,
        long endedAtMillis,
        String reason) {
      this.roundNumber = roundNumber;
      this.type = Objects.requireNonNull(type, "type must not be null");
      this.humanPlayerId = humanPlayerId;
      this.catcherPlayerId = Objects.requireNonNull(catcherPlayerId, "catcherPlayerId must not be null");
      this.endedAtMillis = endedAtMillis;
      this.reason = reason;
    }

    // ---------------------------------------------------------------------------------------------
    // I got Getters again
    // ---------------------------------------------------------------------------------------------

    public int getRoundNumber() {
      return roundNumber;
    }

    public RoundOutcomeType getType() {
      return type;
    }

    public String getHumanPlayerId() {
      return humanPlayerId;
    }

    public Optional<String> getCatcherPlayerId() {
      return catcherPlayerId;
    }

    public long getEndedAtMillis() {
      return endedAtMillis;
    }

    public String getReason() {
      return reason;
    }
  }

  /**
   * States the current lifecycle of match
   */
  public enum GamePhase {
    WAITING_TO_START,
    ROUND_RUNNING,
    ROUND_ENDED,
    MATCH_ENDED,
    ABORTED
  }

  /**
   * Roles of human
   */
  public enum Role {
    HUMAN,
    PHANTOM
  }

  /**
   * Why round ended
   */
  public enum RoundOutcomeType {
    HUMAN_SURVIVED,
    HUMAN_CAUGHT,
    ROUND_ABORTED
  }

  /**
   * Very small first tile model for the map
   * Map sould not be done by me stp. OooO
   */
  public enum TileType {
    FLOOR,
    WALL
  }
}

