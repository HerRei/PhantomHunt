package ch.unibas.dmi.dbis.cs108.phantomhunt.client;

import ch.unibas.dmi.dbis.cs108.phantomhunt.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * Client-side application wrapper used by the GUI. It manages the TCP client connection and exposes
 * helper methods for nickname changes, global chat, whispers and logout.
 */
public class ClientApp {

  private static final String DEFAULT_HOST = "localhost"; // Loopback domain
  private static final int DEFAULT_PORT = 2222; // Port which we also use in ServerApp
  private static final Logger LOGGER = LogManager.getLogger(ClientApp.class);

  public final TcpClient tcpClient;

  private static volatile String confirmedNickname; // private since data encapsulation
  private static volatile Consumer<String> globalMessageListener;
  private static volatile Consumer<String> whisperMessageListener;
  private static volatile Consumer<String> lobbyMessageListener;

  /** Creates a client app that connects to the default host and port. */
  public ClientApp() {
    this(DEFAULT_HOST, DEFAULT_PORT);
  }

  /**
   * Creates a client app that connects to the given server address.
   *
   * @param host server host name or IP address
   * @param port server port
   */
  public ClientApp(String host, int port) {
    LOGGER.info("Connecting to {}:{}", host, port);
    this.tcpClient = new TcpClient(host, port);
  }

  /**
   * Helper method to safely send a packet to the server if the connection is active. Centralizes
   * the null-check to avoid code duplication.
   *
   * @param packet the packet to be sent
   */
  private void sendPacket(Packet packet) {
    if (tcpClient.getServerHandler() != null) {
      tcpClient.getServerHandler().sendMessage(packet);
    } else {
      LOGGER.error("Not connected to server. Failed to send packet: {}", packet.cmd());
    }
  }

  /**
   * Sends a nickname change request to the server.
   *
   * @param nickname requested nickname
   * @return {@code true} if the nickname request was sent, otherwise {@code false}
   */
  public boolean setNickname(String nickname) {
    if (nickname == null || nickname.isBlank()) {
      LOGGER.warn("Nickname was blank --> not sent to server");
      return false;
    }
    sendPacket(Packet.of(Command.NICK, nickname.trim()));
    return true;
  }

  /**
   * Stores the nickname that was confirmed by the server.
   *
   * @param confirmedNickname nickname accepted by the server
   */
  public static void setConfirmedNickname(String confirmedNickname) { // setter
    ClientApp.confirmedNickname = confirmedNickname;
  }

  /**
   * Returns the most recently confirmed nickname.
   *
   * @return confirmed nickname, or {@code null} if no nickname was confirmed yet
   */
  public static String getConfirmedNickname() { // getter
    return confirmedNickname;
  }

  /**
   * Sends a global chat message to the server.
   *
   * @param message global chat message
   */
  public void sendGlobalMessage(String message) {
    if (message == null || message.isBlank()) {
      LOGGER.warn("Message is blank --> Not sent");
      return;
    }

    LOGGER.info("ClientApp sends UNICOM: {}", message);
    sendPacket(Packet.of(Command.UNICOM, message.trim()));
  }

  /**
   * Registers the listener that receives global chat messages for the GUI.
   *
   * @param listener callback for received global chat messages
   */
  public static void setGlobalMessageListener(Consumer<String> listener) {
    globalMessageListener = listener;
  }

  /**
   * Forwards a received global chat message to the registered listener.
   *
   * @param message global chat message received from the server
   */
  public static void notifyGlobalMessageReceived(String message) {
    if (globalMessageListener != null && message != null && !message.isBlank()) {
      globalMessageListener.accept(message);
    }
  }

  /**
   * Registers the listener that receives whisper messages for the GUI.
   *
   * @param listener callback for received whisper messages
   */
  public static void setWhisperMessageListener(Consumer<String> listener) {
    whisperMessageListener = listener;
  }

  /**
   * Forwards a received whisper to the registered listener.
   *
   * @param message whisper text received from the server
   */
  public static void notifyWhisperReceived(String message) {
    if (whisperMessageListener != null && message != null && !message.isBlank()) {
      whisperMessageListener.accept(message);
    }
  }

  /**
   * Sends a private whisper message to another user.
   *
   * @param targetUser nickname of the whisper recipient
   * @param message whisper text
   */
  public void sendWhisper(String targetUser, String message) {
    if (targetUser == null || targetUser.isBlank() || message == null || message.isBlank()) {
      LOGGER.warn("Message or target is blank --> Not sent");
      return;
    }

    sendPacket(Packet.of(Command.WHISPER, targetUser.trim() + " " + message.trim()));
  }

  /**
   * Sends a lobby chat message to the server.
   *
   * @param message lobby chat message
   */
  public void sendLobbyMessage(String message) {
    if (message == null || message.isBlank()) {
      LOGGER.warn("Message is blank --> Not sent");
      return;
    }

    LOGGER.info("ClientApp sends YAP: {}", message);
    sendPacket(Packet.of(Command.YAP, message.trim()));
  }

  /**
   * Registers the listener callback for received lobby chat messages.
   *
   * @param listener callback for received lobby chat messages.
   */
  public static void setLobbyMessageListener(Consumer<String> listener) {
    lobbyMessageListener = listener;
  }

  /**
   * Forwards a reveived lobby chat message to the registered listener.
   *
   * @param message lobby chat message received from the server.
   */
  public static void notifyLobbyMessageReceived(String message) {
    if (lobbyMessageListener != null && message != null && !message.isBlank()) {
      lobbyMessageListener.accept(message);
    }
  }

  /**
   * Sends a request to spectate a lobby.
   *
   * @param lobbyId The ID of the lobby to spectate.
   */
  public void spectateLobby(String lobbyId) {
    if (lobbyId == null || lobbyId.isBlank()) {
      LOGGER.warn("Lobby ID is blank --> Not sent");
      return;
    }

    sendPacket(Packet.of(Command.SPEC, lobbyId));
  }

  /** Sends a logout request to the server. */
  public void logout() {
    // Null-Check added
    if (tcpClient.getServerHandler() != null) {
      tcpClient.getServerHandler().sendMessage(Packet.of(Command.LOGOUT));
    }
  }

  /**
   * Gets the underlying TCP client.
   *
   * @return the active TCP client
   */
  public TcpClient getTcpClient() {
    return this.tcpClient;
  }
}
