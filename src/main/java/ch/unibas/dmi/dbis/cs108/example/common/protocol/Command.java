package ch.unibas.dmi.dbis.cs108.example.common.protocol;

/**
 * These are the first few basic tokens, there will definitely be more to add.
 */
public enum Command {
  // health / connection
  PING, // ping
  PONG, // pong

  // identity
  MKL, // MKL <name> (Make Lobby)
  CHECKIN, // CHECKIN <name>
  CLEARED, // login ok
  REJECT, // REJECT <text...>
  WELCOME, //Welcome <name>

  // chat
  UNICOM, // UNICOM <text...>
  WHISPER, // WHISPER <recipient> <text...>
  YAP, // YAP <text>
  LOGOUT, // LOGOUT
  PLAYERS, //sends all the players to the client
  NICK, // NICK <name>
  INFO, //INFO <msg>

  // lobby
  SPEC, // SPEC <lobbyId>
  START, // START
  LOGOUT_LOBBY, //Logout Lobby
  LOBBY_INFO, // LOBBY_INFO <lobbyId> <player1> <player2> ...
  GAME_START, // GAME_START
  GAME_FINISH, //Game is finished
  LIST_LOBBY,//
  GSU,
  INPUT
}
