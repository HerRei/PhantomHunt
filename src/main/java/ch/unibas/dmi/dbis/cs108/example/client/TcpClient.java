package ch.unibas.dmi.dbis.cs108.example.client;

import java.io.IOException;
import java.net.Socket;


public class TcpClient {

    private final int port;
    private final String host;
    private Socket socket;
    private ServerHandler serverHandler;

    //Constructor
    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            this.serverHandler = connect(host, port);
        } catch (IOException e) {
            // Error handling: printing the stack trace for debugging purposes
            e.printStackTrace();
        }
    }

    //Connects The client to the server
    public ServerHandler connect(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        // Start the counterpart to the ClientHandler
        return new ServerHandler(this.socket);
    }

    //getters
    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}