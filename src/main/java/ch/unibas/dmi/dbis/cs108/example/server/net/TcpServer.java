package ch.unibas.dmi.dbis.cs108.example.server.net;

import ch.unibas.dmi.dbis.cs108.example.server.state.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

  private static final Logger LOGGER = LogManager.getLogger(TcpServer.class);
  private final int port;

  public TcpServer(int port) {
    this.port = port;
  }

  // this starts the server
  public void start() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      LOGGER.info("Server listening on port: {}", port);

      Registry registry = new Registry();

      while (true) {
        Socket socket =
            serverSocket
                .accept(); // lopps until succesful connection, this is the last step of the server
        // activation!
        LOGGER.info(
            "Connection to server from client-adress: {}",
            socket.getRemoteSocketAddress()); // debug logs

        ClientHandler clientHandler =
            new ClientHandler(
                socket, registry); // thread-per-client. everyone gets a handler trough this here.
        Thread t = new Thread(clientHandler);
        t.start();
      }
    } catch (Exception e) {
      LOGGER.error("Error starting server", e);
      throw new RuntimeException(e); // fatal error of the server, should not happen
    }
  }
}
