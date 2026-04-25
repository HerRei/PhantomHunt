package ch.unibas.dmi.dbis.cs108.phantomhunt.server.session;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Comparator;

/**
 * Manages all connected clients and their nicknames. It provides methods to broadcast messages to
 * everyone, send lobby messages, or send private whispers.
 */
public final class Registry {

  // hash map that is thread safe as we dont want a O(n) lookup with e.g. vector that is also
  // threadsafe.

  private static final Logger log = LogManager.getLogger(Registry.class);
  private static Registry instance;
  private final Vector<ClientHandler> sessions =
      new Vector<>(); // zwar O(n) for get but easier to work with than the hashMap with a dummy.
  private final ConcurrentHashMap<String, ClientHandler> byName =
      new ConcurrentHashMap<>(); // Nickname handling
  private final Vector<Highscore> highscores = new Vector<>();

  public static Registry getInstance() {
    if (instance == null) {
      instance = new Registry();
    }
    return instance;
  }

  private Registry() {}

  void resetForTests() {
    sessions.clear();
    byName.clear();
    highscores.clear();
  }

  private static class Highscore {
    private final String playerName;
    private final int score;

    public Highscore(String playerName, int score) {
      this.playerName = playerName;
      this.score = score;
    }

    public String getPlayerName() {
      return playerName;
    }

    public int getScore() {
      return score;
    }

    @Override
    public String toString() {
      return playerName + ": " + score;
    }
  }

  public void addHighscore(String playerName, int score) {
    Highscore newHighscore = new Highscore(playerName, score);

    int i = 0;
    while (i < highscores.size() && highscores.get(i).getScore() >= score) {
      i++;
    }

    highscores.add(i, newHighscore);
    log.info("New highscore added for {}: {}", playerName, score);
  }

  public String getHighscoreBoard() {
    StringBuilder highscoreBoard = new StringBuilder("");
    for (int i = 0; i < highscores.size() && i < 10; i++) {
      highscoreBoard.append(i + 1).append(". ").append(highscores.get(i)).append("|");
    }
    return highscoreBoard.toString();
  }

  /**
   * Assign a requested nickname to a client. If the name is taken, it appends a number to ensure
   * uniqueness.
   *
   * @param requestedName Name the player wants
   * @param h Client handler requesting the name
   * @return final unique nickname
   */
  public String claimName(String requestedName, ClientHandler h) {
    String attempt = requestedName;
    int counter = 1;

    // putIfAbsent returns null, if key didn't exist before and was successfully added.
    while (byName.putIfAbsent(attempt, h) != null) {
      // If this ClientHandler already has a name, return it directly
      if (byName.get(attempt) == h) {
        return attempt;
      }

      attempt = requestedName + counter;
      counter++;
    }
    log.info("Client claimed anem: {}", attempt);
    return attempt;
  }

  /**
   * Returns a string representation of all currently registered nicknames.
   *
   * @return a list of all names as a string
   */
  public String names() {
    return String.join(
        " ", byName.keySet()); // this should return all the people that are in the registry
  }

  /**
   * Makes nickname available.
   *
   * @param name Nickname to release.
   * @param h Client handler with this nickname.
   */
  public void releaseName(String name, ClientHandler h) {
    if (name != null & !name.isBlank()) {
      byName.remove(name, h);
      log.debug("Nickname: {} released.", name); // remove name
    }
  }

  /**
   * Registers a new client handler.
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
    byName.remove(h.getName());
    log.info("Client unregistered. size: {}", sessions.size());
  }

  /**
   * Send messages to all connected clients.
   *
   * @param sender Client who send the broadcast.
   * @param p message to send.
   */
  public void broadcast(ClientHandler sender, Packet p) {
    log.debug("Registry broadcast packet from {}: {}", sender.getName(), p.cmd());
    String str = Protocol.encode(p);
    for (ClientHandler h : sessions) {
      // if (h == sender) continue; //this will lead to errors later as the sender doesn't get its
      // own packages again...!
      // section should be unnecessary, the sender will always receive his own messages as well
      h.sendMessage(p);
    }
  }

  /**
   * Sends a message to all clients within the same lobby as the sender.
   *
   * @param sender Client who sent the yap.
   * @param p message to send.
   */
  public void yapping(ClientHandler sender, Packet p) {
    log.debug("registry yapping packet from {}: {}", sender.getName(), p.cmd());
    Lobby senderLobby = sender.getCurrentLobby();

    if (senderLobby == null) {
      sender.sendMessage(
          Packet.of(Command.REJECT, "You are not in a lobby, what are you \" yapping \""));
      return;
    }

    for (ClientHandler h : sessions) {
      if (h.isInLobby(senderLobby)) {
        h.sendMessage(p);
      }
    }
  }

  /**
   * Sends a private message from one client to another.
   *
   * @param sender Client handler sending the message.
   * @param targetName Nickname of the receiver.
   * @param message The text.
   * @return true if the whisper was successfully sent, false if the target was not found.
   */
  public boolean whisper(ClientHandler sender, String targetName, String message) {
    ClientHandler recipient = byName.get(targetName);
    if (recipient == null) {
      return false;
    }

    // all of this needs to be tested at some point!!!!
    log.info("Whisper: {} -> {}: [message hidden]", sender.getName(), targetName);

    String attributed = "[Whisper from " + sender.getName() + "]: " + message;
    recipient.sendMessage(Packet.of(Command.WHISPER, attributed));

    sender.sendMessage(Packet.of(Command.WHISPER, "[You → " + targetName + "]: " + message));
    return true;
  }
}
