package ch.unibas.dmi.dbis.cs108.phantomhunt.client.net;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.Socket;
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
}
