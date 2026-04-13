package ch.unibas.dmi.dbis.cs108.phantomhunt.client.net;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.Socket;
import static org.junit.jupiter.api.Assertions.*;

public class ServerHandlerTest {

    @Test
    void testServerHandler_constructorCreatesObject(){
        // Arrange
        Socket dummySocket = new Socket();

        // Act
        ServerHandler serverHandler = new ServerHandler(dummySocket);

        // Assert
        assertNotNull(serverHandler, "ServerHandler should not be null.");
    }

    @Test
    void testRun_nullSocketThrowsNullPointerException(){
        // Arrange
        ServerHandler serverHandler = new ServerHandler(null);
        // Act & Assert
        assertThrows(NullPointerException.class, () -> serverHandler.run(),
                "A null socket in the run() method will immediately cause a NullPointerException.");
    }

    @Test
    void testRun_closedSocketHandlesIOExceptionGracefully() throws IOException {
        // Arrange
        Socket closedSocket = new Socket();
        closedSocket.close();
        ServerHandler serverHandler = new ServerHandler(closedSocket);

        // Act & Assert
        assertDoesNotThrow(() -> {
            serverHandler.run();
        }, "A closed socket should throw an IOException, which is safely caught by the catch block.");
    }

    @Test
    void testSendMessage_nullPacketAbortsSafely(){
        // Arrange: create handler (with dummy-null-socket for test)
        ServerHandler handler = new ServerHandler(null);

        // Act & Assert:
        assertDoesNotThrow(() ->{
            handler.sendMessage(null);
        }, "Sending a null packet must not throw an exception. It must terminate silently.");
    }

    @Test
    void testGetName_initialStateIsNull(){
        // Arrange
        ServerHandler handler = new ServerHandler(null);

        // Act
        String result = handler.getName();

        // Assert
        assertNull(result,"Immediately after creation, without a server response, the name must be null.");
    }
}
