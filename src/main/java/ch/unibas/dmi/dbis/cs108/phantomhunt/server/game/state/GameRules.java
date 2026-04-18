package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

/**
 * Defines the immutable configuration and parameters for a match
 */
public record GameRules(
    int totalRounds,
    long roundDurationMillis,
    double playerRadius,
    double moveSpeedPerSecond,
    int humanPointsPerSecond,
    int humanRoundWinBonus,
    int phantomCatchBonus,
    int phantomRoundWinBonus) {

  public GameRules {
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
   * Provides the standard ruleset for a default game.
   *
   * @return A GameRules instance with default parameters.
   */
  public static GameRules defaultRules() {
    return new GameRules(
        4,
        30000,
        0.5,
        5.0,
        1,
        50,
        10,
        10);
  }
}
