package ch.unibas.dmi.dbis.cs108.example.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    private final int port;

    public TcpServer(int port){
        this.port = port;
    }

    public void start () throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serer listening on port" + port);

            while(true){
                Socket socket = serverSocket.accept(); //lopps until succesful connection
                System.out.println("Connection to server from client" + socket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(socket);
                Thread t = new Thread(clientHandler);
                t.start();
            }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    }
}