package ch.unibas.dmi.dbis.cs108.example.client;

import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main class of the client.
 * It connects to the game server, sets the player's name,
 * and reads input from the console to send it to the server.
 */
public class ClientApp {

  private static final String DEFAULT_HOST = "localhost"; // Loopback domain
  private static final int DEFAULT_PORT = 2222; // Port which we also use in ServerApp
  private static final Logger LOGGER = LogManager.getLogger(ClientApp.class);

  public final TcpClient tcpClient;

  public ClientApp() {
    this(DEFAULT_HOST, DEFAULT_PORT);
  }

  public ClientApp(String host, int port) {
    LOGGER.info("Connecting to {}:{}", host, port);
    this.tcpClient = new TcpClient(host, port);
  }

  public void setNickname(String nickname) {
    if (nickname == null || nickname.isBlank()) {
      LOGGER.warn("Nickname was blank --> not sent to server");
      return;
    }

    tcpClient.getServerHandler().sendMessage(Packet.of(Command.NICK, nickname.trim()));
  }

  public void sendGlobalMessage(String message) {
    if (message == null || message.isBlank()) {
      LOGGER.warn("Message is blank --> Not sent");
      return;
    }

    tcpClient.getServerHandler().sendMessage(Packet.of(Command.UNICOM, message.trim()));
  }

  public void sendWhisper(String targetUser, String message) {
    if (targetUser == null || targetUser.isBlank() || message == null || message.isBlank()) {
      LOGGER.warn("Message or target is blank --> Not sent");
      return;
    }

    tcpClient.getServerHandler()
        .sendMessage(Packet.of(Command.WHISPER, targetUser.trim() + " " + message.trim()));
  }

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
