package ch.unibas.dmi.dbis.cs108.example.client;

import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.function.Consumer;

/**
 * Client-side application wrapper used by the GUI.
 * It manages the TCP client connection and exposes helper methods for nickname changes,
 * global chat, whispers and logout.
 */
public class ClientApp {

  private static final String DEFAULT_HOST = "localhost"; // Loopback domain
  private static final int DEFAULT_PORT = 2222; // Port which we also use in ServerApp
  private static final Logger LOGGER = LogManager.getLogger(ClientApp.class);

  public final TcpClient tcpClient;
  //for thread safety
  public static volatile String confirmedNickname;
  private static volatile Consumer<String> globalMessageListener;
  private static volatile Consumer<String> whisperMessageListener;

  /**
   * Creates a client app that connects to the default host and port.
   */
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
   * Sends a nickname change request to the server.
   * If the nickname contains whitespace, it is removed and a warning is logged.
   *
   * @param nickname requested nickname
   * @return {@code true} if the nickname request was sent, otherwise {@code false}
   */
  public boolean setNickname(String nickname) {
    if (nickname == null || nickname.isBlank()) {
      LOGGER.warn("Nickname was blank --> not sent to server");
      return false;
    }

    String finalNickname = nickname;
    if (nickname.contains(" ")) {
      finalNickname = nickname.replaceAll("\\s", "");
      LOGGER.warn("Username contained whitespace and was changed from '{}' to '{}'", nickname, finalNickname);
    }

    tcpClient.getServerHandler().sendMessage(Packet.of(Command.NICK, finalNickname.trim()));
    return true;
  }

  /**
   * Stores the nickname that was confirmed by the server.
   *
   * @param confirmedNickname nickname accepted by the server
   */
  public static void setConfirmedNickname(String confirmedNickname){ //setter
    ClientApp.confirmedNickname = confirmedNickname;
  }

  /**
   * Returns the most recently confirmed nickname.
   *
   * @return confirmed nickname, or {@code null} if no nickname was confirmed yet
   */
  public static String getConfirmedNickname() { //getter
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

    LOGGER.info("ClientApp sends UNICOM: {}", message); //Debugging
    tcpClient.getServerHandler().sendMessage(Packet.of(Command.UNICOM, message.trim()));
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

    tcpClient.getServerHandler()
        .sendMessage(Packet.of(Command.WHISPER, targetUser.trim() + " " + message.trim()));
  }

  /**
   * Sends a logout request to the server.
   */
  public void logout() {
    tcpClient.getServerHandler().sendMessage(Packet.of(Command.LOGOUT));
  }

  /**
   * Old Version of sending username
   * Starts the client.
   * It connects to the server, sends the username, and waits for
   * user input in a loop.
   * @param args
   *
  public static void main(String[] args) {
    String host = DEFAULT_HOST;
    int port = DEFAULT_PORT;

    System.out.println("Connecting to " + host + ":" + port);
    TcpClient client = new TcpClient(host, port);

    try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        String input = scanner.nextLine();
        client.getServerHandler().sendMessage(Protocol.decode(input));
      }
    }
  }
  */
}
