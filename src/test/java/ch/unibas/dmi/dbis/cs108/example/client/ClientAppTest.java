package ch.unibas.dmi.dbis.cs108.example.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientAppTest {

    @Test
    void testSetAndGetConfirmedNickname() {
        // Arrange & Act:
        ClientApp.setConfirmedNickname("Player123");

        // Assert (expected, actual, "Error")
        assertEquals("Player123", ClientApp.getConfirmedNickname(), "The nickname should be saved and retrieved correctly.");
    }

    @Test
    void testNotifyGlobalMessageReceived_ignoresBlank(){
        // Arrange: We register a listener that stores received messages
        final String[] receivedMessage = {null};
        ClientApp.setGlobalMessageListener(msg -> {
            receivedMessage[0] = msg;
        });

        // Act
        ClientApp.notifyGlobalMessageReceived(" ");

        // Assert
        assertNull(receivedMessage[0], "Empty Messages should be ignored, so the memory must remain null.");
    }

    @Test
    void testNotifyGlobalMessageReceived_validMessage() {
        // Arrange: Register a listener that stores the messages in our array
        final String[] receivedMessage = {null};
        ClientApp.setGlobalMessageListener(msg -> {
            receivedMessage[0] = msg;
        });

        // Act: valid message
        ClientApp.notifyGlobalMessageReceived("Hello Team!");

        // Assert:
        assertEquals("Hello Team!", receivedMessage[0], " The Message should arrive correctly in the listener.");
    }

    @Test
    void testSetNickname_invalidInputReturnsFalse() {
        // Arrange: We create a ClientApp.
        // If Server is running, connection cannot be built.
        ClientApp app = new ClientApp("localhost", 8080);

        // Act
        boolean result = app.setNickname("");

        // Assert
        assertFalse(result, "An empty nickname should be rejected and return false.");
    }

    @Test
    void testSendWhisper_invalidInputAborts(){
        // Arrange: Client without real server
        ClientApp app = new ClientApp("localhost", 8080);

        // Act & Assert: Check if an attempt to send a wrong whisper message is aborted without crashind the program
        assertDoesNotThrow(()->{
            app.sendWhisper("", "Secret message"); // Target is empty
            app.sendWhisper("Player2", ""); // Message is empty
            app.sendWhisper(null, null); // both is null
        }, "Sending invalid whispers should not cause the program to crash.");
    }

    @Test
    void testSpectateLobby_invalidInputAborts(){
        // Arrange
        ClientApp app = new ClientApp("localhost", 8080);

        // Act & Assert:
        assertDoesNotThrow(()->{
            app.spectateLobby("");
            app.spectateLobby(null);
        }, "Calling spectateLobby with empty or null values must not cause a crash.");
    }

    @Test
    void testSendGlobalMessage_invalidInputAborts(){
        // Arrange
        ClientApp app = new ClientApp("localhost", 8080);

        // Act & Assert: Check if sendGlobalMessage with inputs empty/null get caught safely
        assertDoesNotThrow(()->{
            app.sendGlobalMessage("");
            app.sendGlobalMessage(null);
        }, "Calling sendGlobalMessage with empty or null values must not cause a crash.");
    }

    @Test
    void testNotifyWhisperReceived_ignoresBlankMessage() {
        // Arrange: register whisper-listener
        final String[] receivedMessage = {null};
        ClientApp.setWhisperMessageListener(msg -> {
            receivedMessage[0] = msg;
        });

        // Act
        ClientApp.notifyWhisperReceived(" ");

        // Assert
        assertNull(receivedMessage[0], "Empty Messages should be ignored, so the memory must remain null");
    }

    @Test
    void testLogout_offlineDoesNotCrash() {
        // Arrange
        ClientApp app = new ClientApp("localhost", 8080);

        // Act & Assert
        assertDoesNotThrow(()->{
            app.logout();
        }, "Logout if not connected to the Server should not cause a crash.");
    }
}
