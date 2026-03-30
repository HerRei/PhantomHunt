package ch.unibas.dmi.dbis.cs108.example.server.game.state;

public final class RoundState {
  private int currentRound;
  private int humanIndex;
  private long roundStartTimeMillis;
  private long roundEndTimeMillis;

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

  public void incrementHumanIndex() {
    this.humanIndex++;
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
