package ch.unibas.dmi.dbis.cs108.phantomhunt.client.net;

import ch.unibas.dmi.dbis.cs108.phantomhunt.Main;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.NameGenerator;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.EndScene;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.LobbyScene;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.SceneProtocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.SoundEffect;
import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.SoundManager;
import javafx.application.Platform;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the client's active network connection to the server. This class runs in its own thread,
 * constantly listens for incoming messages from the server, and provides a method for sending
 * packets.
 */
public class ServerHandler implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
  private final Socket socket;
  private BufferedWriter out;
  private String name;

  /**
   * Creates a new handler for the server connection and starts the read thread immediately
   *
   * @param socket The connected socket through which communication with the server takes place.
   */
  public ServerHandler(Socket socket) {
    this.socket = socket;
    Thread thread = new Thread(this);
    thread.start();
  }

  /**
   * Starts the read loop for the server connection. Incoming lines are decoded into packets and
   * forwarded to the packet dispatcher.
   */
  @Override
  public void run() {
    try (BufferedReader in =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
      this.out = out;
      String line;
      initializeUser();
      while ((line = in.readLine()) != null) {
        try {
          Packet packet = Protocol.decode(line);
          managePacket(packet);
        } catch (IllegalArgumentException e) {
          LOGGER.info("Invalid Input");
        }
      }
    } catch (IOException e) {
      LOGGER.error("Connection to server lost.", e);
    } finally {
      closeSocket();
    }
  }

  /**
   * Dispatches an incoming packet to the matching handler method.
   *
   * @param packet packet received from the server
   */
  private void managePacket(Packet packet) {
    switch (packet.cmd()) {
      case WELCOME:
        handleWelcome(packet);
        break;
      case PING:
        handlePing();
        break;
      case UNICOM:
        handleUnicom(packet);
        break;
      case WHISPER:
        handleWhisper(packet);
        break;
      case CLEARED:
        handleCleared(packet);
        break;
      case REJECT:
        handleReject(packet);
        break;
      case CHECKIN:
        handleCheckin(packet);
        break;
      case INFO:
        handleInfo(packet);
        break;
      case PLAYERS:
        handlePlayers(packet);
        break;
      case LOBBY_INFO:
        handleLobbyInfo(packet);
        break;
      case LIST_LOBBY:
        handleLobbyList(packet);
        break;
      case GAME_START:
        handleGameStart();
        break;
      case GAME_FINISH:
        handleGameFinish();
        break;
      case YAP:
        handleYap(packet);
        break;
      case GSU:
        handleGameStateUpdate(packet);
        break;
      case ABILITY:
        handleAbility(packet);
        break;
      case WISDOM:
        handleWisdom(packet);
        break;
      case SHOW_HIGHSCORE:
        handleShowHighscore(packet);
        break;
      default:
        handleUnknown(packet);
        break;
    }
  }

  private void handleAbility(Packet packet) {

    switch (packet.args().get(0)) {
      case "START":
        SoundManager.getInstance().play(SoundEffect.COIN_UP);
        GameModel.getInstance().setAbility(true);
        break;
      case "END":
        GameModel.getInstance().setAbility(false);
        break;
      default:
        LOGGER.error("Wrong format for Abilitycommand");
        break;
    }
  }

  private void handleWisdom(Packet packet) {
    if (packet.argc() < 1) {
      return;
    }

    String text = packet.text();
    Platform.runLater(
        () -> {
          GameModel model = GameModel.getInstance();
          if ("CLAIMED".equals(text) || "ACTIVE".equals(text)) {
            model.setWisdomBonusReady(true);
            model.setWisdomStatus("Thankyou and enjoy >.<");
          } else if ("STARTED".equals(text)) {
            model.setWisdomStatus("Reflecting...");
          } else if (text.startsWith("TOO_EARLY")) {
            model.setWisdomStatus("Stay with the wisdom a little longer.");
          } else if ("CANCELED".equals(text)) {
            model.setWisdomBonusReady(false);
            model.setWisdomStatus("");
          }
        });
  }

  public void handleShowHighscore(Packet p) {
    if (p.argc() < 1) {
      GameModel.getInstance().setHighscores(new LinkedHashMap<>());
      return;
    }

    String text = p.text();
    LOGGER.info(text);
    Map<String, Integer> highscores = new LinkedHashMap<>();
    String[] lines = text.split("\\|");

    for (String line : lines) {
      if (line.isEmpty()) continue;
      String[] rankAndEntry = line.split("\\.\\s*", 2);
      String rank = rankAndEntry[0].trim();
      String entry = rankAndEntry[1].trim();
      String[] parts = entry.split(":\\s*", 2);
      String name = rank + ". " + parts[0].trim();
      highscores.put(name, Integer.parseInt(parts[1].trim()));
    }
    GameModel.getInstance().setHighscores(highscores);
  }

  public void handleGameFinish() {
    SoundManager.getInstance().play(SoundEffect.DESCENT_WHOOSH);
    Platform.runLater(
        () -> {
          EndScene endScene = (EndScene) SceneManager.getInstance().getScene(SceneProtocol.END);
          if (endScene != null) {
            endScene.updateWinner();
          }
          SceneManager.getInstance().showScene(SceneProtocol.END);
        });
    SoundManager.getInstance().stopAll();
  }

  private void handlePlayers(Packet packet) {
    if (packet.argc() < 1) {
      return;
    }
    Platform.runLater(
        () -> {
          String[] names = packet.args().get(0).split(" ");
          GameModel.getInstance().players.setAll(names);
        });
  }

  /**
   * Handles the game start packet by switching the UI to the game scene This action is executed on
   * the JavaFX Application Thread.
   */
  private void handleGameStart() {
    SoundManager.getInstance().play(SoundEffect.DESCENT_WHOOSH);
    SoundManager.getInstance().play(SoundEffect.WIND_OUTSIDE_ROOM_TONE);
    Platform.runLater(
        () -> {
          GameModel.getInstance().setWisdomBonusReady(false);
          GameModel.getInstance().setWisdomStatus("");
          SceneManager.getInstance().showScene(SceneProtocol.GAME);
          GameModel.getInstance().clearChat();
        });
  }

  /**
   * Parses lobby information received from the server and updates the lobby scene. If the lobby
   * scene is active, it updates the lobby ID and the list of connected players.
   *
   * @param packet the packet containing the lobby ID and player names
   */
  private void handleLobbyInfo(Packet packet) {
    LOGGER.debug("LOBBY_INFO raw text: '{}'", packet.text());
    String[] parts = packet.text().split(" ");
    if (parts.length < 1) {
      LOGGER.warn("Received invalid LOBBY_INFO packet");
      return;
    }

    String lobbyId = parts[0];
    String[] players = Arrays.copyOfRange(parts, 1, parts.length);

    Platform.runLater(
        () -> {
          SceneManager sceneManager = SceneManager.getInstance();
          SceneProtocol current = sceneManager.getCurrentScene();

          if (current != SceneProtocol.GAME && current != SceneProtocol.END) {
            sceneManager.showScene(SceneProtocol.LOBBY);
          }

          LobbyScene lobbyScene = (LobbyScene) sceneManager.getScene(SceneProtocol.LOBBY);
          if (lobbyScene != null) {
            lobbyScene.updateLobbyInfo(lobbyId, players);
          } else {
            LOGGER.error("LobbyScene is not registered in SceneManager.");
          }

          EndScene endScene = (EndScene) sceneManager.getScene(SceneProtocol.END);
          if (endScene != null) {
            endScene.updateLobbyInfo(lobbyId, players);
          }
        });
  }

  /**
   * Updates the internal name and synchronizes it with the GameModel on the UI thread.
   *
   * @param packet the welcome packet containing the assigned name.
   */
  private void handleWelcome(Packet packet) {
    this.name = packet.text();
    try {
      Platform.runLater(
          () -> {
            GameModel.getInstance().setName(this.name);
          });
    } catch (Exception e) {
      LOGGER.info("JavaFX is not available");
    }
  }

  private void handleGameStateUpdate(Packet p) {
    if (p.argc() < 1) {
      LOGGER.warn("Received empty game state update packet");
      return;
    }
    String payload = p.args().get(0);

    Platform.runLater(
        () -> {
          try {
            GameModel.getInstance().updatePlayersFromServer(payload);
          } catch (Exception e) {
            LOGGER.error("Failed to process Game State Update: {}", payload, e);
          }
        });
  }

  /** Responds to a server ping with a pong packet. */
  private void handlePing() {
    sendMessage(Packet.of(Command.PONG));
    LOGGER.trace("Received Ping {}", System.currentTimeMillis());
  }

  /**
   * Stores the nickname confirmed by the server.
   *
   * @param packet packet containing the confirmed nickname
   */
  private void handleCheckin(Packet packet) {
    LOGGER.info("Welcome on the Server: {}", packet.text());
  }

  /**
   * Adds Infos such as Nickname Change or left player to chat.
   *
   * @param packet the info packet containing the server message
   */
  private void handleInfo(Packet packet) {
    String text = packet.text();

    if ("__HUMAN_CAUGHT__".equals(text)) {
      SoundManager.getInstance().stop(SoundEffect.RUNNING_ON_FLOOR);
      SoundManager.getInstance().stop(SoundEffect.DRAGGING_CHAIN);
      SoundManager.getInstance().play(SoundEffect.MAN_SCREAM);
      return;
    }

    if ("__GHOST_EATEN__".equals(text)) {
      SoundManager.getInstance().play(SoundEffect.UNIVERSFIELD_SLIME_IMPACT);
      return;
    }

    LOGGER.info("Info: {}", text);
    GameModel.getInstance().addChatMessage(text); // adds message to chat
  }

  /**
   * Forwards a global chat message from the server to the client UI layer.
   *
   * @param packet packet containing the global chat text
   */
  private void handleUnicom(Packet packet) {
    LOGGER.info("Chat: {}", packet.text());
    GameModel.getInstance().addChatMessage(packet.text()); // adds message to text
  }

  /** Handles LOBBY_LIST command. Protocol: LOBBY_LIST waiting1:waiting2;running1:running2 */
  private void handleLobbyList(Packet p) {
    if (p.args().isEmpty()) return;

    String rawData = p.args().get(0);
    List<String> waiting = new ArrayList<>();
    List<String> running = new ArrayList<>();

    // Split into waiting and running groups
    String[] groups = rawData.split(";", -1);

    // waiting lobbies
    if (groups.length > 0 && !groups[0].isBlank()) {
      waiting = Arrays.asList(groups[0].split(":"));
    }

    // running lobbies
    if (groups.length > 1 && !groups[1].isBlank()) {
      running = Arrays.asList(groups[1].split(":"));
    }

    // Update model on FX thread
    final List<String> finalWaiting = waiting;
    final List<String> finalRunning = running;
    Platform.runLater(() -> GameModel.getInstance().updateLobbyList(finalRunning, finalWaiting));
  }

  /**
   * Forwards a whisper message from the server to the client UI layer.
   *
   * @param packet packet containing the whisper text
   */
  private void handleWhisper(Packet packet) {
    String text = packet.text();
    if (text.startsWith(
        "[Whisper from ")) { // if this hardedcoded logic isnt gonna break soemthing down the
                             // line...
      SoundManager.getInstance().play(SoundEffect.UNIVERSFIELD_MESSAGE);
    }
    LOGGER.info("Whisper: {}", packet.text());
    GameModel.getInstance().addChatMessage(packet.text()); // adds message to text
  }

  private void handleYap(Packet packet) {
    LOGGER.info("YAP: {}", packet.text());
    GameModel.getInstance().addLobbyChatMessage(packet.text()); // adds message to text
  }

  /**
   * Logs a successful server-side action such as a nickname change confirmation.
   *
   * @param packet packet describing the completed action
   */
  private void handleCleared(Packet packet) {
    LOGGER.info("System: {}", packet.text());
  }

  /**
   * Processes a rejection sent by the server and logs the error.
   *
   * @param packet packet containing the rejection text
   */
  private void handleReject(Packet packet) {
    LOGGER.error("Error: {}", packet.text());
  }

  /**
   * Logs packets that are currently not handled explicitly by the client.
   *
   * @param packet packet with an unsupported or unexpected command
   */
  private void handleUnknown(Packet packet) {
    LOGGER.info("Received unknown command: {}", packet.cmd());
  }

  /** Initializes the client nickname from the local system user name or a generated fallback. */
  private void initializeUser() {
    String systemName = System.getProperty("user.name");
    if (Main.nickname != null) {
      systemName = Main.nickname;
    }
    if (systemName == null || systemName.isBlank()) {
      systemName = NameGenerator.randomName();
      LOGGER.debug("No system username found, generated random name: {}", systemName);
    }
    sendMessage(Packet.of(Command.NICK, systemName));
  }

  /**
   * Sends a packet to the server over the active socket connection.
   *
   * @param p the packet to be sent to the server
   */
  public synchronized void sendMessage(Packet p) {
    if (p == null) {
      LOGGER.error("User tried sending an invalid packet");
      return;
    }
    // Debugging
    LOGGER.info("Client side sends packet : {}", p);

    try {
      if (out != null) {
        out.write(Protocol.encode(p));
        out.newLine();
        out.flush();
      }
    } catch (IOException e) {
      LOGGER.error("Failed to send packet", e);
    }
  }

  /** Closes the client socket if it is still open. */
  private void closeSocket() {
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.error("Error while closing the socket", e);
    }
  }

  public String getName() {
    return this.name;
  }
}
