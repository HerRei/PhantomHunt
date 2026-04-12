package ch.unibas.dmi.dbis.cs108.example.server.session;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import ch.unibas.dmi.dbis.cs108.example.server.lobby.Lobby;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all connected clients and their nicknames.
 * It provides methods to broadcast messages to everyone or send private whispers.
 */
public final class Registry {

  // hash map that is thread safe as we dont want a O(n) lookup with e.g. vector that is also
  // threadsafe.

  // better idea - please do indulge me.

  private static final Logger log =
          LogManager.getLogger(
                  Registry.class); // somehow here i called it log, i dont wanna change it...
  private final Vector<ClientHandler> sessions = new Vector<>(); //zwar O(n) for get but easier to work with than the hashMap with a dummy.
  private final ConcurrentHashMap<String, ClientHandler> byName =
          new ConcurrentHashMap<>(); // Nickname handling

  /**
   * Assign a requested nickname to a client.
   * If the name is taken, it adds a number to the end
   *
   * @param requestedName Name the player wants
   * @param h             Client handler requesting the name
   * @return final unique nickname
   */
  public String claimName(String requestedName, ClientHandler h) {
    String attempt = requestedName;
    int counter = 1;

    // putIfAbsent gibt null zurück, wenn der Key noch nicht existierte und erfolgreich eingefügt
    // wurde
    while (byName.putIfAbsent(attempt, h) != null) {
      // Wenn dieser ClientHandler den Namen bereits besitzt, gib ihn direkt zurück
      if (byName.get(attempt) == h) {
        return attempt;
      }

      attempt = requestedName + counter;
      counter++;
    }
    log.info("Client claimed anem: {}", attempt);
    return attempt;
  }

  public String names() {
    return byName.keySet().toString(); // this should return all the people that are in the registry
  }

  /**
   * Makes nickname available.
   *
   * @param name Nickname to release.
   * @param h    Client handler with this nickname.
   */
  public void releaseName(String name, ClientHandler h) {
    byName.remove(name, h);
    log.debug("Nickname: {} released.", name); // remove name
  }

  /**
   * keeping track of all the people that got a client handler.
   *
   * @param h Client handler to add.
   */
  public void register(ClientHandler h) {
    sessions.add(h);
    log.info("New client registered. size is: {}", sessions.size());
  }

  /**
   * Removes client from the registry when disconnected.
   *
   * @param h Client handler to remove.
   */
  public void unregister(ClientHandler h) {
    sessions.remove(h);
    log.info("Client unregistered. size: {}", sessions.size());
  }


  /**
   * Send messages to all clients except for the sender.
   *
   * @param sender Client who send the broadcast.
   * @param p      message to send.
   */
  public void broadcast(ClientHandler sender, Packet p) {
    log.debug("registry brioadcat packet from {}: {}", sender.getName(), p.cmd());
    String str = Protocol.encode(p);
    for (ClientHandler h : sessions) {
      //if (h == sender) continue; //this will lead to errors later as the sender doesnt get its own packages again...!
      //section should be unnecessary, the sender will always receve his own messages as well
      h.sendMessage(p);
    }
  }

  public void yapping(ClientHandler sender, Packet p) {
    log.debug("registry yapping packet from {}: {}", sender.getName(), p.cmd());
    Lobby senderLobby = sender.getCurrentLobby();
    if (senderLobby == null) {
      sender.sendMessage(Packet.of(Command.REJECT, "You are not in a lobby, what are you \" yapping \""));
      return;
    }

    for (ClientHandler h : sessions) {
      if (h.isInLobby(senderLobby)) {
        h.sendMessage(p);
      }
    }
  }

  /**
   * Private message from client to client.
   *
   * @param sender
   * @param targetName Nickname of the receiver.
   * @param message    The text.
   * @return True or false
   */
  public boolean whisper(ClientHandler sender, String targetName, String message) {
    ClientHandler recipient = byName.get(targetName);
    if (recipient == null) return false;

    // all of this needs to be tested at some point!!!! #todo test this mess
    log.info("Whisper: {} -> {}: [message hidden]", sender.getName(), targetName);
    String attributed = "[Whisper from " + sender.getName() + "]: " + message;
    recipient.sendMessage(Packet.of(Command.WHISPER, attributed));
    sender.sendMessage(Packet.of(Command.WHISPER, "[You → " + targetName + "]: " + message));
    return true;
  }
}
