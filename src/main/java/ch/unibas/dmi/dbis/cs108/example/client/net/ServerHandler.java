package ch.unibas.dmi.dbis.cs108.example.client.net;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.NameGenerator;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.LobbyScene;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes.SceneProtocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javafx.application.Platform;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model.GameModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Manages the client's active network connection to the server.
 * This class runs in its own thread, constantly listens for incoming
 * messages from the server, and provides a method for sending packets.
 */
public class ServerHandler implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
  private final Socket socket;
  private BufferedWriter out;
  private String name;

  /**
   * Creates a new handler for the server connection and starts immediately
   * the read thread.
   *
   * @param socket The connected Socket, through which communication with the server takes place.
   */
  public ServerHandler(Socket socket) {
    this.socket = socket;
    Thread thread = new Thread(this);
    thread.start();
  }


  /**
   * Starts the read loop for the server connection.
   * Incoming lines are decoded into packets and forwarded to the packet dispatcher.
   */
  @Override
  public void run() {
    try (
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
    ) {
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
      case LOBBY_INFO:
        handleLobbyInfo(packet);
        break;
      case GAME_START:
        handleGameStart();
        break;
      case YAP:
        handleYap(packet);
        break;
      default:
        handleUnknown(packet);
        break;
    }
  }

  private void handleGameStart() {
    Platform.runLater(() -> {
      SceneManager.getInstance().showScene(SceneProtocol.GAME);
    });
  }

  private void handleLobbyInfo(Packet packet) {
    String[] parts = packet.text().split(" ");
    if (parts.length < 1) {
      LOGGER.warn("Received invalid LOBBY_INFO packet");
      return;
    }
    String lobbyId = parts[0];
    String[] players = Arrays.copyOfRange(parts, 1, parts.length);

    Platform.runLater(() -> {
      SceneManager sceneManager = SceneManager.getInstance();
      sceneManager.showScene(SceneProtocol.LOBBY);
      LobbyScene lobbyScene = (LobbyScene) sceneManager.scenes.get(SceneProtocol.LOBBY);
      if (lobbyScene != null) {
        lobbyScene.updateLobbyInfo(lobbyId, players);
      } else {
        LOGGER.error("LobbyScene is not registered in SceneManager.");
      }
    });
  }

  /**
   * Updates the name.
   */
  private void handleWelcome(Packet packet) {
    this.name = packet.text();
    try {
      Platform.runLater(() -> {
        GameModel.getInstance().setName(this.name);
      });
    } catch (Exception e) {
      LOGGER.info("JavaFX nicht vorhanden");
    }

  }


  /**
   * Responds to a server ping with a pong packet.
   */
  private void handlePing() {
    sendMessage(Packet.of(Command.PONG));
    LOGGER.info("Received Ping {}", System.currentTimeMillis());
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
   * adds Infos such as Nickname Change or left player to chat
   *
   * @param packet
   */
  private void handleInfo(Packet packet) {
    LOGGER.info("Info: {}", packet.text());
    GameModel.getInstance().addChatMessage(packet.text()); //adds message to text
  }

  /**
   * Forwards a global chat message from the server to the client UI layer.
   *
   * @param packet packet containing the global chat text
   */
  private void handleUnicom(Packet packet) {
    LOGGER.info("Chat: {}", packet.text());
    GameModel.getInstance().addChatMessage(packet.text()); //adds message to text
  }

  /**
   * Forwards a whisper message from the server to the client UI layer.
   *
   * @param packet packet containing the whisper text
   */
  private void handleWhisper(Packet packet) {
    LOGGER.info("Whisper: {}", packet.text());
    GameModel.getInstance().addChatMessage(packet.text()); //adds message to text
  }

  private void handleYap(Packet packet) {
    LOGGER.info("YAP: {}", packet.text());
    GameModel.getInstance().addLobbyChatMessage(packet.text()); //adds message to text
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
   * Processes a rejection sent by the server and updates the confirmed nickname if needed.
   *
   * @param packet packet containing the rejection text
   */
  private void handleReject(Packet packet) {
    String text = packet.text();
    String marker = "You are now: ";
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

  /**
   * Initializes the client nickname from the local system user name or a generated fallback.
   */
  private void initializeUser() {
    String systemName = System.getProperty("user.name");
    if (systemName == null || systemName.isBlank()) {
      systemName = NameGenerator.randomName();
      LOGGER.debug("No system username found, generated random name: {}", systemName);
    }
    sendMessage(Packet.of(Command.NICK, systemName));
  }


  /**
   * Sends a packet to the server over the active socket connection.
   *
   * @param p
   */
  public synchronized void sendMessage(Packet p) {
    if (p == null) {
      LOGGER.error("User tried sending an invalid packet");
      return;
    }
    //Debugging
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

  /**
   * Closes the client socket if it is still open.
   */
  private void closeSocket() {
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.error("Error while closing the socket", e);
    }
  }

  //---getters---
  public String getName() {
    return this.name;
  }
}