package ch.unibas.dmi.dbis.cs108.example.common.protocol;

//#todo  make sure the terms in tcp server are used in the right way!!

/**
 * These are the first few basic tokens, there will definietly be more to add, esp with the chat
 * which needs to be up and running as soon as Milestone two.
 */
public enum Command {
  BEACON, BEACON_ACK,
  CHECKIN, CLEARED, REJECT,
  UNICOM,
  NICK,
  LOGOUT
}
