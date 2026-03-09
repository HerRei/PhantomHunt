package ch.unibas.dmi.dbis.cs108.example.client;

import java.io.IOException;
import java.net.Socket;

/**
 * Represents a TCP client that connects to a server and manages the connection
 * via a ServerHandler.
 */
public class TcpClient {

    private final int port;
    private final String host;
    private Socket socket;
    private ServerHandler serverHandler;

    /**
     * Creates a new client instance and attempts to connect to the target server.
     *
     * @param host the server's hostname or IP address
     * @param port the port number to connect to
     */
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

    /**
     * Establishes the socket connection and initializes the ServerHandler.
     *
     * @param host the server's hostname
     * @param port the server's port
     * @return a new ServerHandler instance managing the established connection
     * @throws IOException if the connection could not be established
     */
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