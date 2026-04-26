package ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol;

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
  /** Request to change or set a nickname, for example  name. */
  NICK,

  /** Server confirms a successful action, for example  action details. */
  CLEARED,

  /** Server rejects an action, for example  reason. */
  REJECT,

  /** Server welcomes the client and confirms their assigned name, for example  name. */
  WELCOME,

  /** Request to disconnect completely from the server. */
  LOGOUT,

  // --- Global & Direct Chat ---

  /** Global broadcast message, for example  text. */
  UNICOM,

  /** Direct private message, for example  recipient text. */
  WHISPER,

  /** Server informational broadcast, for example  message. */
  INFO,

  /** Server broadcasts all connected players, for example  player1 player2. */
  PLAYERS,

  // --- Lobby & Game Management ---

  /** Request to create a new lobby, for example  lobbyName. */
  MKL,

  /** Request to join an existing lobby, for example  lobbyId. */
  CHECKIN,

  /** Request to spectate an existing lobby, for example  lobbyId. */
  SPEC,

  /** Lobby-specific chat message, for example  text. */
  YAP,

  /** Request to leave the current lobby, for example  lobbyId. */
  LOGOUT_LOBBY,

  /** Server broadcasts current lobby state, for example  lobbyId player1 player2. */
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
  INPUT,

  /** Client want to use Ability. */
  ABILITY,

  /** Request to show the highscore. */
  SHOW_HIGHSCORE
}