package ch.unibas.dmi.dbis.cs108.example.client;

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

/**
 * Handles communication with the server in a separate thread.
 * Listens for incoming packets and provides methods to send packets.
 */
public class ServerHandler implements Runnable {

    private final Socket socket;
    private BufferedWriter out;

    /**
     * Constructs a new ServerHandler and starts its execution in a new thread.
     *
     * @param socket the established connection to the server
     */
    public ServerHandler(Socket socket) {
        this.socket = socket;
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Main loop that reads incoming lines from the server, decodes them into packets,
     * and dispatches them to the handler.
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
                Packet packet = Protocol.decode(line);
                managePacket(packet);
            }
        } catch (IOException e) {
            System.err.println("Connection to server lost.");
        } finally {
            closeSocket();
        }
    }

    /**
     * Processes commands received from the server.
     *
     * @param packet the received packet to process
     */
    private void managePacket(Packet packet) {
        switch (packet.cmd()) {
            case BEACON:
                send(Packet.of(Command.BEACON_ACK));
                break;
            case UNICOM:
                System.out.println("Chat: " + packet.text());
                break;
            default:
                // Unknown Packets
                break;
        }
    }

    /**
     * Sends a packet to the server.
     *
     * @param packet the packet to be encoded and sent
     */
    public void send(Packet packet) {
        try {
            if (out != null) {
                out.write(Protocol.encode(packet));
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            // Failed to send packet, likely due to a closed connection
        }
    }

    /**
     * Ensures the socket is closed when the handler stops.
     */
    private void closeSocket() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Error while closing the socket
        }
    }
}