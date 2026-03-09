package ch.unibas.dmi.dbis.cs108.example.server;

import ch.unibas.dmi.dbis.cs108.example.server.net.TcpServer;

//mostly here to start the server - provides the main method and the logic of the ports. i somehow thought
//we would need to be able to start it from any given port thats why this logic is in here
// i am not too sure on what happens if the default port is already taken
public final class ServerApp {

  private static final int DEFAULT_PORT = 2222;


  //main method
  public static void main(String[] args) {
    int port = parsePortOrDefault(args, DEFAULT_PORT);
    System.out.println("SERVER starting on port " + port);

    try {
      TcpServer server = new TcpServer(port); //start the server
      server.start();
    } catch (Exception e) {
      System.out.println("Server failed to start..."); //if port taken or server logic is off - we have this error
      e.printStackTrace();
    }
  }

  //this for starting the server with a custom port

  private static int parsePortOrDefault(String[] args, int defaultPort) {
    if (args == null || args.length == 0) return defaultPort;
    try {
      int p = Integer.parseInt(args[0]);
      if (!(p >= 1 && p <= 65535)) throw new IllegalArgumentException("Not a valid port"); //all the valid port, 1-1024 shpuld maye be left out?
      return p;
    } catch (NumberFormatException e) {
      System.out.println("Port was not a number, using default...");
      return defaultPort;
    }
  }
}
