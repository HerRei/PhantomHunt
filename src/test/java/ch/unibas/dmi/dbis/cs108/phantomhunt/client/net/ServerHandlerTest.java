package ch.unibas.dmi.dbis.cs108.phantomhunt.client.net;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.util.FakeSocket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerHandlerTest {

    @Test
    void run_withUnconnectedSocket_catchesExceptionAndClose() {
        FakeSocket deadSocket = new FakeSocket("");

        // We create handler. Thread starts and crashes silently in background (throws IOException and closes socket)
        // We check that constructor call doesn't cause our test program to crash.
        assertDoesNotThrow(() -> {
            ServerHandler handler = new ServerHandler(deadSocket);

            // since socket is dead, name never gets initialized
            assertNull(handler.getName());
        });
    }

    @Test
    void sendMessage_nullPacket_doesNothing() {
        // creat socket, since not connected
        FakeSocket deadSocket = new FakeSocket("");
        ServerHandler handler = new ServerHandler(deadSocket);

        // Test whether null check at beginning of sendMessage works, without causing NullPointerException.
        assertDoesNotThrow(() -> handler.sendMessage(null));
    }

    @Test
    void run_processesIncomingPackets_andSendsResponses() throws Exception {
        String serverMessage = "CLEARED Welcome on the Server\nPING\n";
        FakeSocket fakeSocket = new FakeSocket(serverMessage);

        // He starts thread, reads "INFO" and "PING", then stops, since fake stream is finished
        ServerHandler handler = new ServerHandler(fakeSocket);

        // Since Handler runs in own thread, we must give the test a break, so that the handler gets time to process the messages
        Thread.sleep(200);

        // We take everything, the handler send as answer
        String sentData = fakeSocket.getSentData();

        // Check 1: Did he log in during initialization?
        assertTrue(sentData.contains("NICK"), "The client should log in with NICK at the beginning.");

        // Check 2: Did he respond to our fake PING with a PONG?
        assertTrue(sentData.contains("PONG"), "The client must respond to a PING with a PONG.");
    }

    @Test
    void sendMessage_writesToOutputStream() throws Exception {
        // create Stream, blocking and keeping alive thread
        java.io.InputStream keepAliveStream = new java.io.InputStream() {
            @Override
            public int read() {
                try {
                    Thread.sleep(2000); // keeps thread alive
                } catch (InterruptedException e) {
                    // ignore
                }
                return -1;
            }
        };

        FakeSocket fakeSocket = new FakeSocket(keepAliveStream);
        ServerHandler handler = new ServerHandler(fakeSocket);

        Thread.sleep(100); // wait until initializing NICK is through

        // for clean test: clear previous output (NICK from initialization)
        fakeSocket.clearOutput();

        // send message manually
        handler.sendMessage(ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet.of(
                Command.WHISPER, "User Hello"
        ));

        String sentData = fakeSocket.getSentData();
        assertTrue(sentData.contains("WHISPER User Hello"), "The Message must be written to the output stream.");
    }
}
