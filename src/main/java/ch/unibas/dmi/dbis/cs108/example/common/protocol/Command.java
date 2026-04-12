package ch.unibas.dmi.dbis.cs108.example.common.protocol;

/**
 * Defines the available network protocol commands used for communication
 * between the client and the server.
 */
public enum Command {
  // --- Connection & Health ---
  /** Server requests to check connection health. */
  PING,

  /** Client response to a health check.*/
  PONG, // pong

  // --- Identity & Session ---
  /** Request to change or set a nickname: NICK <name> */
  NICK,

  /** Server confirms a successful action (e.g., nickname change): CLEARED <action> <details> */
  CLEARED,

  /** Server rejects an action: REJECT <reason> */
  REJECT,

  /** Server welcomes the client and confirms their assigned name: WELCOME <name> */
  WELCOME,

  /** Request to disconnect completely from the server. */
  LOGOUT,

  // --- Global & Direct Chat ---

  /** Global broadcast message: UNICOM <text...> */
  UNICOM,

  /** Direct private message: WHISPER <recipient> <text...> */
  WHISPER,

  /** Server informational broadcast: INFO <msg> */
  INFO,

  // --- Lobby & Game Management ---

  /** Request to create a new lobby: MKL <lobbyName> */
  MKL,

  /** Request to join an existing  lobby: CHECKIN <lobbyId> */
  CHECKIN,

  /** REquest to spectate an existing lobby: SPEC <lobbyId> */
  SPEC,

  /** Lobby-specific chat message: YAP <text...> */
  YAP,

  /** Request to leave the current lobby: LOGOUT_LOBBY <lobbyId> */
  LOGOUT_LOBBY,

  /** Server broadcasts current lobby state: LOBBY_INFO <lobbyId> <player1> <player2> */
  LOBBY_INFO,

  /** Request from the lobby host to start the game. */
  START,

  /** Server notification to clients that the game is starting. */
  GAME_START
}
