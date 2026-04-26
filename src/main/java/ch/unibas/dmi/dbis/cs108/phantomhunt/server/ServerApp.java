package ch.unibas.dmi.dbis.cs108.phantomhunt.server;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.TcpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main entry point for the server application. Initializes the TCP server on a specified custom
 * port or falls back to the default.
 */
public final class ServerApp {

  private static final Logger log = LogManager.getLogger(ServerApp.class);
  private static final int DEFAULT_PORT = 2222;

  // Private constructor to prevent instantiation of this utility class
  private ServerApp() {}

  /**
   * Starts the server application.
   *
   * @param args First argument can be custom port number
   */
  public static void main(String[] args) {
    int port = parsePortOrDefault(args, DEFAULT_PORT);

    log.info("SERVER starting on port {}", port);

    try {
      TcpServer server = new TcpServer(port); // start the server
      server.start();
    } catch (Exception e) {
      log.error("Server failed to start on port {}. Is the port already in use?", port, e);
    }
  }

  /**
   * Parses the port from command line arguments or returns the default port. Ensures the port is
   * within the valid range (1-65535)
   *
   * @param args Command line arguments
   * @param defaultPort Fallback port
   * @return A valid port number
   */
  private static int parsePortOrDefault(String[] args, int defaultPort) {
    if (args == null || args.length == 0) return defaultPort;

    try {
      int p = Integer.parseInt(args[0]);
      if (p < 1 || p > 65535) {
        throw new IllegalArgumentException(
            "Port out of bounds: " + p); // all the valid port, 1-1024 should maye be left out?
      }
      return p;
    } catch (NumberFormatException e) {
      log.warn("Invalid port format '{}'. Using default port {}.", args[0], defaultPort);
      return defaultPort;
    } catch (IllegalArgumentException e) {
      log.warn("Port {} is out of range. Using default port {}.", args[0], defaultPort);
      return defaultPort;
    }
  }
}
