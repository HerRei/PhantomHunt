package ch.unibas.dmi.dbis.cs108.example.common.protocol;

/**
 * These are the first few basic tokens, there will definietly be more to add, esp with the chat
 * which needs to be up and running as soon as Milestone two.
 */
public enum Command {
  // health / connection
  PING, // ping
  PONG, // pong

  // identity
  CHECKIN, // CHECKIN <name>
  CLEARED, // login ok
  REJECT, // REJECT <text...>

  // chat
  UNICOM, // UNICOM <text...>
  WHISPER, // WHISPER <recipient> <text...>
  YAP, // YAP <Lobby_id> <text>
  LOGOUT,
  NICK,
}
