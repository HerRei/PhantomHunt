package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

/**
 * Tracks the progression, timing, and role assignments of the active round.
 */
public final class RoundState {
  private int currentRound;
  private int humanIndex;
  private long roundStartTimeMillis;
  private long roundEndTimeMillis;

  /**
   *
   *
   * @param currentRound         The current round number.
   * @param humanIndex           The index of the human player in the player list.
   * @param roundStartTimeMillis The timestamp when the round started.
   * @param roundEndTimeMillis   The timestamp when the round is scheduled to end.
   */
  public RoundState(
          int currentRound, int humanIndex, long roundStartTimeMillis, long roundEndTimeMillis) {
    this.currentRound = currentRound;
    this.humanIndex = humanIndex;
    this.roundStartTimeMillis = roundStartTimeMillis;
    this.roundEndTimeMillis = roundEndTimeMillis;
  }

  public int getCurrentRound() {
    return currentRound;
  }

  public int getHumanIndex() {
    return humanIndex;
  }

  public long getRoundStartTimeMillis() {
    return roundStartTimeMillis;
  }

  public long getRoundEndTimeMillis() {
    return roundEndTimeMillis;
  }

  public void setCurrentRound(int currentRound) {
    this.currentRound = currentRound;
  }

  public void incrementCurrentRound() {
    this.currentRound++;
  }

  public void setHumanIndex(int humanIndex) {
    this.humanIndex = humanIndex;
  }

  public void advanceHumanIndex(int playerCount) {
    if (playerCount <= 0) {
      throw new IllegalArgumentException("playerCount must be positive");
    }
    this.humanIndex = (this.humanIndex + 1) % playerCount;
  }

  public void setRoundStartTimeMillis(long roundStartTimeMillis) {
    this.roundStartTimeMillis = roundStartTimeMillis;
  }

  public void setRoundEndTimeMillis(long roundEndTimeMillis) {
    this.roundEndTimeMillis = roundEndTimeMillis;
  }

  public RoundState copy() {
    return new RoundState(currentRound, humanIndex, roundStartTimeMillis, roundEndTimeMillis);
  }
}
