package ch.unibas.dmi.dbis.cs108.example.server.state;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all connected clients and their nicknames.
 * It provides methods to broadcast messages to everyone or send private whispers.
 */
public final class Registry {

  // hash map that is thread safe as we dont want a O(n) lookup with e.g. vector that is also
  // threadsafe.
  // ArrayList doesnt(?) have a threadsafe version?!, also the thing is that there is no concurrent
  // hashset so we need to use boolean.TRUE
  // as a dummy key value pair, this is definently not the optimal solution so if anyone has a
  // better idea - please do indulge me.

  private static final Logger log =
      LogManager.getLogger(
          Registry.class); // somehow here i called it log, i dont wanna change it...
  private final ConcurrentHashMap<ClientHandler, Boolean> sessions = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ClientHandler> byName =
      new ConcurrentHashMap<>(); // Nickname handling

  /**
   * Assign a requested nickname to a client.
   * If the name is taken, it adds a number to the end
   * @param requestedName Name the player wants
   * @param h Client handler requesting the name
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
   * @param name Nickname to release.
   * @param h Client handler with this nickname.
   */
  public void releaseName(String name, ClientHandler h) {
    byName.remove(name, h);
    log.debug("Nickname: {} released.", name); // remove name
  }

  /**
   * keeping track of all the people that got a client handler.
   * @param h Client handler to add.
   */
  public void register(ClientHandler h) {
    sessions.put(h, true); // autoboxing of Boolean.TRUE - might bring issues idk
    log.info("New client registered. size is: {}", sessions.size());
  }

  /**
   * Removes client from the registry when disconnected.
   * @param h Client handler to remove.
   */
  public void unregister(ClientHandler h) {
    sessions.remove(h);
    log.info("Client unregistered. size: {}", sessions.size());
  }



  /**
   * Send messages to all clients except for the sender.
   * @param sender Client who send the broadcast.
   * @param p message to send.
   */
  public void broadcast(ClientHandler sender, Packet p) {
    log.debug("registry brioadcat packet from {}: {}", sender.getName(), p.cmd());
    String str = Protocol.encode(p);
    for (ClientHandler h : sessions.keySet()) {
      if (h == sender) continue;

      h.sendMessage(p);
    }
  }

  /**
   * Private message from client to client.
   * @param sender
   * @param targetName Nickname of the receiver.
   * @param message The text.
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
