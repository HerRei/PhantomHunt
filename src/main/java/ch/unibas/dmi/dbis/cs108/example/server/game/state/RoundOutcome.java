package ch.unibas.dmi.dbis.cs108.example.server.game.state;

import java.util.Objects;
import java.util.Optional;

/**
 * Encapsulates the results and statistics of a completed game round.
 */
public final class RoundOutcome {
  private final int roundNumber;
  private final RoundOutcomeType type;
  private final String humanPlayerId;
  private final Optional<String> catcherPlayerId;
  private final long endedAtMillis;
  private final String reason;

  /**
   * Constructs a new RoundOutcome.
   *
   * @param roundNumber     Number of the round that ended.
   * @param type            Type of outcome (e.g. "HUMAN_SURVIVED")
   * @param humanPlayerId   ID of the player that was the human this round.
   * @param catcherPlayerId ID of the players that were the ghosts
   * @param endedAtMillis   Timestamp when the round ended.
   */
  public RoundOutcome(
          int roundNumber,
          RoundOutcomeType type,
          String humanPlayerId,
          Optional<String> catcherPlayerId,
          long endedAtMillis) {
    this(roundNumber, type, humanPlayerId, catcherPlayerId, endedAtMillis, null);
  }

  /**
   *
   * @param roundNumber
   * @param type
   * @param humanPlayerId
   * @param catcherPlayerId
   * @param endedAtMillis
   * @param reason
   */
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
    this.catcherPlayerId =
            Objects.requireNonNull(catcherPlayerId, "catcherPlayerId must not be null");
    this.endedAtMillis = endedAtMillis;
    this.reason = reason;
  }

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
