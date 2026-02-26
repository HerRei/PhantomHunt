package ch.unibas.dmi.dbis.cs108.example.common.protocol;

/**
 * These are the first few basic tokens, there will definietly be more to add, esp with the chat
 * which needs to be up and running as soon as Milestone two.
 */
public enum Command {
  /** Beacon command. */
  // health / connection
  BEACON, // ping
  /** Beacon ack command. */
  BEACON_ACK, // pong

  /** Checkin command. */
  // identity
  CHECKIN, // CHECKIN <name>
  /** Cleared command. */
  CLEARED, // login ok
  /** Reject command. */
  REJECT, // REJECT <text...>

  /** Unicom command. */
  // chat
  UNICOM // UNICOM <text...>
}
