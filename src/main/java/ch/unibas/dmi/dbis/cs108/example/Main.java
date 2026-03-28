package ch.unibas.dmi.dbis.cs108.example;

import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.GUI;
import ch.unibas.dmi.dbis.cs108.example.server.net.TcpServer;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

public class Main {

  private static final Logger LOGGER = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    // [0] = server/client [1] = port / host:port
    if (args.length == 2) {
      switch (args[0]) {
        case "client" -> {

          try {
            String[] socketInput = args[1].split(":");
            String host = socketInput[0];
            int port = Integer.parseInt(socketInput[1]);
            connect(host, port, args);

          } catch (Exception e) {
            LOGGER.error("invalid input format has to be <host>:<port>, not: {}", args[1]);
          }
        }

        case "server" -> {
          try {
            int port = Integer.parseInt(args[1]);
            startServer(port);

          } catch (NumberFormatException e) {
            LOGGER.error("port has to be a digit, not : {}", args[1]);
          }
        }

        default -> {
          LOGGER.error("Invalid role: {}", args[0]);
        }
      }
    } else {
      LOGGER.error("Expected format: role (<host>:)<port>");
    }
  }

  /**
   * @param port port for socket
   *             User is host
   */
  public static void startServer(int port) {
    LOGGER.info("Starting Server...");
    CountDownLatch serverReadySignal = new CountDownLatch(1); //used to know when server ready

    // Start Server via thread
    Thread serverThread = new Thread(() -> {
      TcpServer server = new TcpServer(port, serverReadySignal);
      server.start();
    });
    serverThread.start();
  }

  /**
   * @param host ip-adress for socket
   * @param port port for socket
   *             User is client
   */
  public static void connect(String host, int port, String[] args) {
    LOGGER.info("Connecting to {}:{}...", host, port);
    new TcpClient(host, port);
    Application.launch(GUI.class, args);
  }
}