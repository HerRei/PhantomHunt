package ch.unibas.dmi.dbis.cs108.phantomhunt.client.net;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class TcpClientTest {

    @Test
    void constructor_noServerRunning_catchesExceptionAndSetsHandlerNull() {
        // Try to connect with a dead Port
        // This internally throws a IOException, and gets caught from catch-Block.
        TcpClient client = new TcpClient("localhost", 65432);

        // Getter should non the less give the right values
        assertEquals("localhost", client.getHost());
        assertEquals(65432, client.getPort());

        // Since failed connection, no serverhandler should be made
        assertNull(client.getServerHandler(), "The Handler must be null, since there was no connection");
    }

    @Test
    void connect_invalidHost_throwsIOException() {
        TcpClient client = new TcpClient("localhost", 65432);

        // If we call connect method directly, throws IOException, so we catch it using assertThrows
        assertThrows(IOException.class, () -> {
            client.connect("a.completely.incorrect.host.that.does.not.exist.", 9999);
        });
    }
}
