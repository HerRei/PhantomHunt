package ch.unibas.dmi.dbis.cs108.phantomhunt.server.net;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NetworkIntegrationTest {
    @Test
    void fullNetworkStack_clientAndServerCanCommunicate() throws Exception {
        // start real server with test-port
        int testPort = 50505;
        TcpServer server = new TcpServer(testPort);

        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true); // ends automatically, when test is finished
        serverThread.start();

        // waining until server started
        Thread.sleep(500);

        // connect real client-socket over localhost
        try (Socket clientSocket = new Socket("localhost", testPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8))) {

            // send initial nick-message to server
            out.write("NICK IntegrationTester\n");
            out.flush();

            // catch and check server-answers
            boolean welcomeReceived = false;
            boolean pingReceived = false;

            // we read at max 10 lines from stream to find answer
            for (int i = 0; i < 10; i++) {
                String line = in.readLine();
                if (line == null) break;

                if (line.contains("WELCOME IntegrationTester")) {
                    welcomeReceived = true;
                }
                if (line.contains("PING")) {
                    pingReceived = true;
                    // immediate PONG answer to PING, to test server-loop
                    out.write("PONG\n");
                    out.flush();
                }

                if (welcomeReceived && pingReceived) {
                    break; // everything is proven
                }
            }

            assertTrue(welcomeReceived, "Server must confirm registration with WELCOME");
            assertTrue(pingReceived, "Server must confirm registration with PING");
        }
    }
}
