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
                send(Packet.of(Command.PONG));
                break;
            case UNICOM:
                System.out.println("Chat: " + packet.text());
                break;
            default:
                // Unknown Packets
                break;
        }
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