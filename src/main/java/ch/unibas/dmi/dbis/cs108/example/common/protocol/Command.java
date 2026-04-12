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
  /** Request to change or set a nickname, for example {@code NICK name}. */
  NICK,

  /** Server confirms a successful action, for example {@code CLEARED action details}. */
  CLEARED,

  /** Server rejects an action, for example {@code REJECT reason}. */
  REJECT,

  /** Server welcomes the client and confirms their assigned name, for example {@code WELCOME name}. */
  WELCOME,

  /** Request to disconnect completely from the server. */
  LOGOUT,

  // --- Global & Direct Chat ---

  /** Global broadcast message, for example {@code UNICOM text}. */
  UNICOM,

  /** Direct private message, for example {@code WHISPER recipient text}. */
  WHISPER,

  /** Server informational broadcast, for example {@code INFO message}. */
  INFO,

  /** Server broadcasts all connected players, for example {@code PLAYERS player1 player2}. */
  PLAYERS,

  // --- Lobby & Game Management ---

  /** Request to create a new lobby, for example {@code MKL lobbyName}. */
  MKL,

  /** Request to join an existing lobby, for example {@code CHECKIN lobbyId}. */
  CHECKIN,

  /** Request to spectate an existing lobby, for example {@code SPEC lobbyId}. */
  SPEC,

  /** Lobby-specific chat message, for example {@code YAP text}. */
  YAP,

  /** Request to leave the current lobby, for example {@code LOGOUT_LOBBY lobbyId}. */
  LOGOUT_LOBBY,

  /** Server broadcasts current lobby state, for example {@code LOBBY_INFO lobbyId player1 player2}. */
  LOBBY_INFO,

  /** Request from the lobby host to start the game. */
  START,

  /** Server notification to clients that the game is starting. */
  GAME_START,

  /** Server notification to clients that the game has finished. */
  GAME_FINISH,

  /** Request or response containing available lobby data. */
  LIST_LOBBY,

  /** Server game-state update broadcast. */
  GSU,

  /** Client movement input update. */
  INPUT
}
