package ch.unibas.dmi.dbis.cs108.phantomhunt.client.net;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.TcpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Test
    void connect_validHost_createsServerHandler() throws Exception {
        // secretly open server-socket on test-port
        try (ServerSocket testServer = new ServerSocket(8888)) {
            // We let dummy-server accept connection
            Thread serverThread = new Thread(() -> {
                try { testServer.accept(); } catch (Exception ignored) {}
            });
            serverThread.start();

            // TCP-Client tries to connect
            TcpClient client = new TcpClient("localhost", 8888);

            // if connection works, handler cannot be null
            assertNotNull(client.getServerHandler(), "Connection was successful, Handler must exist.");
        }
    }

    @Test
    void start_acceptsConnectionAndHandlesClient() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        TcpServer server = new TcpServer(8889, latch);

        // We start server in "daemon-thread" -> gets killed automatically from system when test is done
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        // wait until server tells us he's ready
        boolean ready = latch.await(2, TimeUnit.SECONDS);
        assertTrue(ready, "Server did not start up in time.");

        // connect to dummy-client
        try (Socket dummyClient = new Socket("localhost", 8889)) {
            assertTrue(dummyClient.isConnected(), "Dummy client successfully connected to the real server!");
        }
    }
}
