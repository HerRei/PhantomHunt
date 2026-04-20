package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import java.util.Objects;

/**
 * Tracks the mutable state of a specific player during a match,
 * including position, score, and connection status.
 */
public final class PlayerState {
  private final String playerId;
  private String nickname;
  private PlayerRole role;
  private Position position;
  private InputState inputState;
  private InputState realInput;
  private int score;
  private int remainingAbility;
  private boolean connected;
  private boolean caughtThisRound;

  /**
   * Creates an independent copy of this player's state.
   */
  public PlayerState(
          String playerId,
          String nickname,
          PlayerRole role,
          Position position,
          InputState inputState,
          int score,
          int remainingAbility,
          boolean connected,
          boolean caughtThisRound) {
    this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
    this.nickname = Objects.requireNonNull(nickname, "nickname must not be null");
    this.role = Objects.requireNonNull(role, "role must not be null");
    this.position = Objects.requireNonNull(position, "position must not be null");
    this.inputState = Objects.requireNonNull(inputState, "inputState must not be null");
    this.realInput = Objects.requireNonNull(inputState);
    this.score = score;
    this.remainingAbility = remainingAbility;
    this.connected = connected;
    this.caughtThisRound = caughtThisRound;
  }
  /**
   * Creates an independent copy of this player's state.
   */
  public PlayerState(
          String playerId,
          String nickname,
          PlayerRole role,
          Position position,
          InputState inputState,
          InputState realInput,
          int remainingAbility,
          int score,
          boolean connected,
          boolean caughtThisRound) {
    this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
    this.nickname = Objects.requireNonNull(nickname, "nickname must not be null");
    this.role = Objects.requireNonNull(role, "role must not be null");
    this.position = Objects.requireNonNull(position, "position must not be null");
    this.inputState = Objects.requireNonNull(inputState, "inputState must not be null");
    this.realInput = Objects.requireNonNull(realInput);
    this.score = score;
    this.connected = connected;
    this.caughtThisRound = caughtThisRound;
  }

  /**
   * Creates a copy of this player's state.
   *
   * @return A new PlayerState object with the same values.
   */
  public PlayerState copy() {
    return new PlayerState(
            playerId,
            nickname,
            role,
            position.copy(),
            inputState.copy(),
            realInput.copy(),
            remainingAbility,
            score,
            connected,
            caughtThisRound);
  }

  public String getPlayerId() {
    return playerId;
  }

  public String getNickname() {
    return nickname;
  }

  public PlayerRole getRole() {
    return role;
  }

  public Position getPosition() {
    return position;
  }

  public InputState getInputState() {
    return inputState;
  }

  public InputState getRealInput() { return realInput; }

  public int getScore() {
    return score;
  }

  public int getRemainingAbility() {return remainingAbility;}

  public boolean isConnected() {
    return connected;
  }

  public boolean isCaughtThisRound() {
    return caughtThisRound;
  }

  public void setRole(PlayerRole role) {
    this.role = Objects.requireNonNull(role, "role must not be null");
  }

  public void setPosition(Position position) {
    this.position = Objects.requireNonNull(position, "position must not be null");
  }

  public void setInputState(InputState inputState) {
    this.inputState = Objects.requireNonNull(inputState, "inputState must not be null");
  }

  public void setRealInput(InputState inputState) {
    this.realInput = Objects.requireNonNull(inputState, "inputState must not be null");
  }

  public void setRemainingAbility(int set){this.remainingAbility = set;}

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public void setCaughtThisRound(boolean caughtThisRound) {
    this.caughtThisRound = caughtThisRound;
  }

  public void addScore(int scoreDelta) {
    this.score += scoreDelta;
  }
}
