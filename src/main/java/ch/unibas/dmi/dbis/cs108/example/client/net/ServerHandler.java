package ch.unibas.dmi.dbis.cs108.example.client.net;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;

/**
 * Manages the client's active network connection to the server.
 * This class runs in its own thread, constantly listens for incoming
 * messages from the server, and provides a method for sending packets.
 */
public class ServerHandler implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
    private final Socket socket;
    private BufferedWriter out;

    /**
     * Creates a new handler for the server connection and starts immediately
     * the read thread.
     * @param socket The connected Socket, through which communication with the server takes place.
     */
    public ServerHandler(Socket socket) {
        this.socket = socket;
        Thread thread = new Thread(this);
        thread.start();
    }


    /**
     * Receives Packets from server and sends Packets back
     */
    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            this.out = out;
            String line;
            while ((line = in.readLine()) != null) {
                try{
                    Packet packet = Protocol.decode(line);
                    managePacket(packet);
                }
                catch(IllegalArgumentException e){
                    LOGGER.info("Invalid Input");
                }
            }
        } catch (IOException e) {
            LOGGER.error("Connection to server lost.", e);
        } finally {
            closeSocket();
        }
    }

    //Manages the Packets received from server
    private void managePacket(Packet packet) {
        switch (packet.cmd()) {
            case PING:
                handlePing();
                break;
            case UNICOM:
                handleUnicom(packet);
                break;
            case WHISPER:
                handleWhisper(packet);
                break;
            case CLEARED:
                handleCleared(packet);
                break;
            case REJECT:
                handleReject(packet);
                break;
            default:
                handleUnknown(packet);
                break;
        }
    }

    // helper functions
    private void handlePing() {
        sendMessage(Packet.of(Command.PONG));
    }

    private void handleUnicom(Packet packet) {
        LOGGER.info("Chat: {}", packet.text());
    }

    private void handleWhisper(Packet packet) {
        LOGGER.info(packet.text());
    }

    private void handleCleared(Packet packet) {
        LOGGER.info("System: {}", packet.text());
    }

    private void handleReject(Packet packet) {
        LOGGER.error("Error: {}", packet.text());
    }

    private void handleUnknown(Packet packet) {
        LOGGER.info("Received unknown command: {}", packet.cmd());
    }


    /**
     * Sends Packet to server
     * @param p
     */
    public void sendMessage(Packet p) {
        if (p == null){
            LOGGER.error("User tried sending an empty packet");
            return;
        }
        try {
            if (out != null) {
                out.write(Protocol.encode(p));
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to send packet", e);
        }
    }

   //Closes Socket
    private void closeSocket() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error while closing the socket", e);
        }
    }
}