package ch.unibas.dmi.dbis.cs108.example.server.game.state;

/**
 * Represents the high-level lifecycle phases of a single match.
 */
public enum GamePhase {
  WAITING_TO_START,
  ROUND_RUNNING,
  ROUND_ENDED,
  MATCH_ENDED,
  ABORTED
}
