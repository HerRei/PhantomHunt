package ch.unibas.dmi.dbis.cs108.example.client;

import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.NameGenerator;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
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

  /**
   * Starts the client.
   * It connects to the server, sends the username, and waits for
   * user input in a loop.
   * @param args
   */
  public static void main(String[] args) {
    String host = DEFAULT_HOST;
    int port = DEFAULT_PORT;

    System.out.println("Connecting to " + host + ":" + port);
    TcpClient client = new TcpClient(host, port);

    String systemName = System.getProperty("user.name");
    if (systemName == null || systemName.isBlank()) {
      systemName = NameGenerator.randomName();
      LOGGER.debug("No system username found, generated random name: {}", systemName);
    }

    LOGGER.info("Sending NICK command for user: {}", systemName);
    client.getServerHandler().send(Packet.of(Command.NICK, systemName));

    try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        String input = scanner.nextLine();
        client.getServerHandler().send(Protocol.decode(input));
      }
    }
  }
}
