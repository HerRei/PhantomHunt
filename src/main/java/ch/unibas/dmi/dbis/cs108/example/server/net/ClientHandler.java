package ch.unibas.dmi.dbis.cs108.example.server.net;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                out.write("ECHO " + line);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            // do nothing
        } finally {
            try {
                socket.close();
            } catch (IOException e) { // do nothing
                 }
        }
    }
}