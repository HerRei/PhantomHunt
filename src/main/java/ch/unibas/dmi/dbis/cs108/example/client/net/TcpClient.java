package ch.unibas.dmi.dbis.cs108.example.client.net;

import java.io.IOException;
import java.net.Socket;

import ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.controller.EventHandlers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Establishes the TCP connection to the game server.
 * Enclose the socket and after establishing the connection, creates
 * the ServerHandler for communication.
 */
public class TcpClient {
    private static final Logger LOGGER = LogManager.getLogger(TcpClient.class);
    private final int port;
    private final String host;
    private Socket socket;
    private ServerHandler serverHandler;

    /**
     * Attempting to connect to the server.
     * @param host The IP address or hostname of the server
     * @param port The port on which the server listens
     */
    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            this.serverHandler = connect(host, port);
            EventHandlers.getInstance().setSH(this.serverHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to connect to server", e);
        }
    }

    /**
     * Connects The client to the server
     * @param host The IP address or hostname of the server
     * @param port The port on which the server listens
     * @return
     * @throws IOException If the connection cannot be established
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