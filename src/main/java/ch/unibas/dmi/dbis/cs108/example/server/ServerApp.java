package ch.unibas.dmi.dbis.cs108.example.server;

import ch.unibas.dmi.dbis.cs108.example.server.net.TcpServer;

/** The type Server app. */
public final class ServerApp {

  private static final int DEFAULT_PORT = 5050;

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    int port = parsePortOrDefault(args, DEFAULT_PORT);
    System.out.println("SERVER starting on port " + port);

    try {
      TcpServer server = new TcpServer(port);
      server.start();
    } catch (Exception e) {
      System.out.println("Server failed to start...");
      e.printStackTrace();
    }
  }

  private static int parsePortOrDefault(String[] args, int defaultPort) {
    if (args == null || args.length == 0) return defaultPort;

    try {
      int p = Integer.parseInt(args[0]);
      if (!(p >= 1 && p <= 65535)) throw new IllegalArgumentException("Not a valid port");
      return p;
    } catch (NumberFormatException e) {
      System.out.println("Port was not a number, using default...");
      return defaultPort;
    }
  }
}
