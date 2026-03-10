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

public class ServerHandler implements Runnable {

    private final Socket socket;
    private BufferedWriter out;

    //Constructor
    public ServerHandler(Socket socket) {
        this.socket = socket;
        Thread thread = new Thread(this);
        thread.start();
    }

    //Receives Packets from server and sends Packets back
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
        send(Packet.of(Command.PONG));
    }

    private void handleUnicom(Packet packet) {
        System.out.println("Chat: " + packet.text());
    }

    private void handleWhisper(Packet packet) {
        System.out.println(packet.text());
    }

    private void handleCleared(Packet packet) {
        System.out.println("System: " + packet.text());
    }

    private void handleReject(Packet packet) {
        System.err.println("Error: " + packet.text());
    }

    private void handleUnknown(Packet packet) {
        System.out.println("Received unknown command: " + packet.cmd());
    }

    //Sends Packet to server
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

   //Closes Socket
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