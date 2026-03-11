package ch.unibas.dmi.dbis.cs108.example.server;

import ch.unibas.dmi.dbis.cs108.example.server.net.TcpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * mostly here to start the server - provides the main method and the logic of the ports. i somehow
 * thought
 * we would need to be able to start it from any given port thats why this logic is in here
 * i am not too sure on what happens if the default port is already taken
 */
public final class ServerApp {

  private static final Logger log = LogManager.getLogger(ServerApp.class);
  private static final int DEFAULT_PORT = 2222;


  /**
   * Starts the server application.
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

  // this for starting the server with a custom port
  private static int parsePortOrDefault(String[] args, int defaultPort) {
    if (args == null || args.length == 0) return defaultPort;
    try {
      int p = Integer.parseInt(args[0]);
      if (!(p >= 1 && p <= 65535))
        throw new IllegalArgumentException(
            "Not a valid port"); // all the valid port, 1-1024 should maye be left out?
      return p;
    } catch (NumberFormatException e) {
      log.warn("Provided port ist not a valid number.: {}", defaultPort);
      return defaultPort;
    } catch (IllegalArgumentException e) {
      log.warn("Port was not in range of ports, using default..: {}", defaultPort);
      return defaultPort;
    }
  }
}
