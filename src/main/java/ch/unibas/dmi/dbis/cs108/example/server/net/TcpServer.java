package ch.unibas.dmi.dbis.cs108.example.server.net;

import ch.unibas.dmi.dbis.cs108.example.server.state.Registry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

  private final int port;


  public TcpServer(int port) {
    this.port = port;
  }

  //this starts the server
  public void start() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server listening on port" + port);

      Registry registry = new Registry();

      while (true) {
        Socket socket = serverSocket.accept(); // lopps until succesful connection, this is the last step of the server activation!
        System.out.println("Connection to server from client" + socket.getRemoteSocketAddress()); //debug logs

        ClientHandler clientHandler = new ClientHandler(socket, registry); //thread-per-client. everyone gets a handler trough this here.
        Thread t = new Thread(clientHandler);
        t.start();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

