package ch.unibas.dmi.dbis.cs108.phantomhunt.server.session;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages all connected clients, their nicknames, and persistent highscores.
 * Provides methods for broadcasting, whispering, and leaderboard management.
 */
public final class Registry {

  private static final Logger log = LogManager.getLogger(Registry.class);
  private static final String HIGHSCORE_FILE = "HighScores.txt";
  private static Registry instance;

  private final Vector<ClientHandler> sessions = new Vector<>();
  private final ConcurrentHashMap<String, ClientHandler> byName = new ConcurrentHashMap<>();
  private final Vector<Highscore> highscores = new Vector<>();

  /**
   * Returns the singleton instance of the Registry.
   */
  public static synchronized Registry getInstance() {
    if (instance == null) {
      instance = new Registry();
    }
    return instance;
  }

  private Registry() {
    loadHighscores();
  }

  /**
   * Resets all sessions and highscores. Primarily used for testing purposes.
   */
  void resetForTests() {
    sessions.clear();
    byName.clear();
    highscores.clear();
  }

  /**
   * Inner class to represent a single highscore entry.
   */
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

  /**
   * Adds a new highscore and saves it persistently to the text file.
   * Maintains descending order by score.
   */
  public void addHighscore(String playerName, int score) {
    Highscore newHighscore = new Highscore(playerName, score);

    // Insert into the list while maintaining descending order
    int i = 0;
    while (i < highscores.size() && highscores.get(i).getScore() >= score) {
      i++;
    }
    highscores.add(i, newHighscore);

    saveHighscores();
    log.info("New highscore added and saved for {}: {}", playerName, score);
  }

  /**
   * Returns a string representation of the top 10 highscores.
   * Entries are separated by the '|' character.
   */
  public String getHighscoreBoard() {
    StringBuilder highscoreBoard = new StringBuilder();
    for (int i = 0; i < highscores.size() && i < 10; i++) {
      highscoreBoard.append(i + 1).append(". ").append(highscores.get(i)).append("|");
    }
    return highscoreBoard.toString();
  }

  /**
   * Loads highscores from the HighScores.txt file into memory.
   */
  private void loadHighscores() {
    File file = new File(HIGHSCORE_FILE);
    if (!file.exists()) {
      log.info("No highscore file found. Creating new list.");
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] data = line.split(",");
        if (data.length == 2) {
          try {
            highscores.add(new Highscore(data[0], Integer.parseInt(data[1].trim())));
          } catch (NumberFormatException e) {
            log.warn("Skipping invalid score format in line: {}", line);
          }
        }
      }
      // Ensure data is sorted correctly after loading
      highscores.sort(Comparator.comparingInt(Highscore::getScore).reversed());
    } catch (IOException e) {
      log.error("Could not load HighScores.txt", e);
    }
  }

  /**
   * Writes all current highscores to the HighScores.txt file.
   */
  private void saveHighscores() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
      for (Highscore highscore : highscores) {
        writer.write(highscore.getPlayerName() + "," + highscore.getScore());
        writer.newLine();
      }
    } catch (IOException e) {
      log.error("Could not save HighScores.txt", e);
    }
  }

  /**
   * Assigns a unique nickname. Appends a counter if the name is already taken.
   */
  public String claimName(String requestedName, ClientHandler h) {
    String attempt = requestedName;
    int counter = 1;

    while (byName.putIfAbsent(attempt, h) != null) {
      if (byName.get(attempt) == h) {
        return attempt;
      }
      attempt = requestedName + counter;
      counter++;
    }
    log.info("Client claimed name: {}", attempt);
    return attempt;
  }

  /**
   * Returns a space-separated list of all currently active nicknames.
   */
  public String names() {
    return String.join(" ", byName.keySet());
  }

  /**
   * Releases a nickname so it can be claimed by others.
   */
  public void releaseName(String name, ClientHandler h) {
    if (name != null && !name.isBlank()) {
      byName.remove(name, h);
      log.debug("Nickname: {} released.", name);
    }
  }

  /**
   * Registers a client session.
   */
  public void register(ClientHandler h) {
    sessions.add(h);
    log.info("New client registered. Current count: {}", sessions.size());
  }

  /**
   * Unregisters a client session and releases their name.
   */
  public void unregister(ClientHandler h) {
    sessions.remove(h);
    if (h.getName() != null) {
      byName.remove(h.getName());
    }
    log.info("Client unregistered. Current count: {}", sessions.size());
  }

  /**
   * Broadcasts a packet to all connected clients.
   */
  public void broadcast(ClientHandler sender, Packet p) {
    log.debug("Registry broadcast from {}: {}", sender.getName(), p.cmd());
    for (ClientHandler h : sessions) {
      h.sendMessage(p);
    }
  }

  /**
   * Sends a packet to all clients within the same lobby as the sender.
   */
  public void yapping(ClientHandler sender, Packet p) {
    log.debug("Registry lobby-broadcast from {}: {}", sender.getName(), p.cmd());
    Lobby senderLobby = sender.getCurrentLobby();

    if (senderLobby == null) {
      sender.sendMessage(
              Packet.of(Command.REJECT, "You are not in a lobby."));
      return;
    }

    for (ClientHandler h : sessions) {
      if (h.isInLobby(senderLobby)) {
        h.sendMessage(p);
      }
    }
  }

  /**
   * Sends a private whisper message.
   *
   * @return true if target was found and message sent, false otherwise.
   */
  public boolean whisper(ClientHandler sender, String targetName, String message) {
    ClientHandler recipient = byName.get(targetName);
    if (recipient == null) {
      return false;
    }

    log.info("Whisper: {} -> {}", sender.getName(), targetName);

    String attributed = "[Whisper from " + sender.getName() + "]: " + message;
    recipient.sendMessage(Packet.of(Command.WHISPER, attributed));

    sender.sendMessage(Packet.of(Command.WHISPER, "[You → " + targetName + "]: " + message));
    return true;
  }
}