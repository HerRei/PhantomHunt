package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

/** Defines the immutable configuration and parameters for a match */
public record GameRules(
    int totalRounds,
    long roundDurationMillis,
    double playerRadius,
    double moveSpeedPerSecond,
    int humanPointsPerSecond,
    int humanRoundWinBonus,
    int phantomCatchBonus,
    int humanCatchBonus,
    int humanAbilitys,
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
    return new GameRules(4, 50000, 6.0, 100.0, 1, 50, 10, 10, 3, 10);
  }

  /**
   * Parses the simple protocol payload used by GAME_SETTINGS.
   *
   * <p>Format: totalRounds roundDurationMillis playerRadius moveSpeedPerSecond
   * humanPointsPerSecond humanRoundWinBonus phantomCatchBonus humanCatchBonus humanAbilitys
   * phantomRoundWinBonus
   */
  public static GameRules fromPayload(String payload) {
    if (payload == null || payload.isBlank()) {
      throw new IllegalArgumentException("Game settings payload must not be blank.");
    }

    String[] values = payload.trim().split("\\s+");
    if (values.length != 10) {
      throw new IllegalArgumentException("Game settings must contain exactly 10 values.");
    }

    try {
      return new GameRules(
          Integer.parseInt(values[0]),
          Long.parseLong(values[1]),
          Double.parseDouble(values[2]),
          Double.parseDouble(values[3]),
          Integer.parseInt(values[4]),
          Integer.parseInt(values[5]),
          Integer.parseInt(values[6]),
          Integer.parseInt(values[7]),
          Integer.parseInt(values[8]),
          Integer.parseInt(values[9]));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Game settings contain invalid numbers.", e);
    }
  }

  /** Formats these rules for the simple GAME_SETTINGS protocol payload. */
  public String toPayload() {
    return String.join(
        " ",
        Integer.toString(totalRounds),
        Long.toString(roundDurationMillis),
        Double.toString(playerRadius),
        Double.toString(moveSpeedPerSecond),
        Integer.toString(humanPointsPerSecond),
        Integer.toString(humanRoundWinBonus),
        Integer.toString(phantomCatchBonus),
        Integer.toString(humanCatchBonus),
        Integer.toString(humanAbilitys),
        Integer.toString(phantomRoundWinBonus));
  }
}
