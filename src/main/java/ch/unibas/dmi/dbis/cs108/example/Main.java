package ch.unibas.dmi.dbis.cs108.example;

import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.GUI;
import ch.unibas.dmi.dbis.cs108.example.server.net.TcpServer;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * Main entry point for the application.
 * Starts either the server or the JavaFX client based on command-line arguments.
 */
public class Main {

  private static final Logger LOGGER = LogManager.getLogger(Main.class);

  // Saving Host and Port here, so Gui has access
  public static String targetHost;
  public static int targetPort;

  // private constructor to prevent instantiation
  private Main() {
  }

  /**
   * The main method to start the application.
   * It parses command-line arguments to start either a server or a client.
   * For a client, it expects "client" and "host:port".
   * For a server, it expects "server" and "port".
   *
   * @param args the command-line arguments
   */
  public static void main(String[] args) {
    // [0] = server/client [1] = port / host:port
    if (args.length == 2) {
      switch (args[0]) {
        case "client" -> {

          try {
            String[] socketInput = args[1].split(":");
            String host = socketInput[0];
            int port = Integer.parseInt(socketInput[1]);

            // Make parameters ready for GUI
            targetHost = host;
            targetPort = port;

            connect(host, port, args);

          } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
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
   * Starts the server application on a new thread.
   *
   * @param port the port on which the server will listen
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
   * Launches the JavaFX client application
   *
   * @param host the server IP address or hostname
   * @param port port for the server port
   * @param args the command-line arguments to pass the JavaFX application.
   */
  public static void connect(String host, int port, String[] args) {
    LOGGER.info("Connecting to {}:{}...", host, port);
    // TcpClient deleted, since it should only start in GUI.
    Application.launch(GUI.class, args);
  }

  // Getters for GUI
  public static String getTargetHost() {
    return targetHost;
  }

  public static int getTargetPort() {
    return targetPort;
  }
}