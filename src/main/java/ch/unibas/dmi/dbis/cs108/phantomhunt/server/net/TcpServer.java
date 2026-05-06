package ch.unibas.dmi.dbis.cs108.phantomhunt.server.net;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.LobbyHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.session.MenuMusicBroadcaster;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.session.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/** The main server class that listens for incoming client connections. */
public class TcpServer {

  private static final Logger LOGGER = LogManager.getLogger(TcpServer.class);
  private final int port;
  private CountDownLatch readyLatch;

  /**
   * Creates a new TCP server without a readiness latch.
   *
   * @param port The port the server will listen on.
   */
  public TcpServer(int port) {
    this.port = port;
  } // should be deleted soon left it for serverApp

  /**
   * Creates a new TCP server.
   *
   * @param port The port the server will listen.
   * @param readyLatch Timer to see if server is started
   */
  public TcpServer(int port, CountDownLatch readyLatch) {
    this.port = port;
    this.readyLatch = readyLatch;
  }

  /**
   * Starts the server loop, accepting incoming connections and assigning each to a new dedicated
   * ClientHandler thread.
   */
  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      LOGGER.info("Server listening on port: {}", port);

      if (readyLatch != null) {
        readyLatch.countDown();
      }

      Registry registry = Registry.getInstance();
      LobbyHandler lobbyHandler = new LobbyHandler();
      MenuMusicBroadcaster menuMusicBroadcaster = new MenuMusicBroadcaster(registry);
      menuMusicBroadcaster.start();

      while (true) {
        Socket socket =
            serverSocket
                .accept(); // loops until successful connection, this is the last step of the server
                           // activation
        socket.setTcpNoDelay(true);
        LOGGER.info(
            "Connection to server from client-adress: {}",
            socket.getRemoteSocketAddress()); // debug logs

        ClientHandler clientHandler =
            new ClientHandler(
                socket,
                registry,
                lobbyHandler); // thread-per-client. everyone gets a handler trough this here.
        Thread t = new Thread(clientHandler);
        t.start();
      }
    } catch (IOException e) {
      LOGGER.error("Error starting server", e);
      throw new RuntimeException(e); // fatal error of the server, should not happen
    }
  }
}
