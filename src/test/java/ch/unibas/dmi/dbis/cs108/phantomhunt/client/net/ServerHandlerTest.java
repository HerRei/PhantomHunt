package ch.unibas.dmi.dbis.cs108.phantomhunt.client.net;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class ServerHandlerTest {

    @Test
    void run_withUnconnectedSocket_catchesExceptionAndClose() {
        Socket deadSocket = new Socket();

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
        Socket deadSocket = new Socket();
        ServerHandler handler = new ServerHandler(deadSocket);

        // Test whether null check at beginning of sendMessage works, without causing NullPointerException.
        assertDoesNotThrow(() -> handler.sendMessage(null));
    }

    @Test
    void run_processesIncomingPackets_andSendsResponses() throws Exception {
        Socket mockSocket = mock(Socket.class);

        // As if Server sends two messages: An INFO-Packet and a PING.
        String serverMessage = "CLEARED Welcome on the Server\nPING\n";

        // We redirect these strings to fake streams
        InputStream fakeIn = new ByteArrayInputStream(serverMessage.getBytes());
        ByteArrayOutputStream fakeOut = new ByteArrayOutputStream();

        // When ServerHandler asks Socket, we give fake streams
        when(mockSocket.getInputStream()).thenReturn(fakeIn);
        when(mockSocket.getOutputStream()).thenReturn(fakeOut);

        // He starts thread, reads "INFO" and "PING", then stops, since fake stream is finished
        ServerHandler handler = new ServerHandler(mockSocket);

        // Since Handler runs in own thread, we must give the test a break, so that the handler gets time to process the messages
        Thread.sleep(500);

        // We take everything, the handler send as answer
        String sentData = fakeOut.toString();

        // Check 1: Did he log in during initialization?
        assertTrue(sentData.contains("NICK"), "The client should log in with NICK at the beginning.");

        // Check 2: Did he respond to our fake PING with a PONG?
        assertTrue(sentData.contains("PONG"), "The client must respond to a PING with a PONG.");
    }

    @Test
    void sendMessage_writesToOutputStream() throws Exception {
        Socket mockSocket = mock(Socket.class);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        // blocks reading and keeps thread alive
        InputStream keepAliveStream = new InputStream() {
            @Override
            public int read() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // ignore
                }
                return -1;
            }
        };
        when(mockSocket.getInputStream()).thenReturn(keepAliveStream);

        ByteArrayOutputStream fakeOut = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(fakeOut);

        ServerHandler handler = new ServerHandler(mockSocket);
        Thread.sleep(100); // wait until initializing NICK is through

        // for clean test: clear previous output (NICK from initialization)
        fakeOut.reset();

        // send message manually
        handler.sendMessage(ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet.of(
                Command.WHISPER, "User Hello"
        ));

        String sentData = fakeOut.toString();
        assertTrue(sentData.contains("WHISPER User Hello"), "The Message must be written to the output stream.");
    }
}
