package ch.unibas.dmi.dbis.cs108.phantomhunt.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class ClientAppTest {

    @Test
    void setNickname_nullOrBlank_returnsFalse() {
        ClientApp app = new ClientApp("localhost", 9999);

        assertFalse(app.setNickname(null));
        assertFalse(app.setNickname(""));
        assertFalse(app.setNickname(" "));
    }

    @Test
    void sendWhisper_invalidInputs_abortsWithoutError() {
        ClientApp app = new ClientApp("localhost", 9999);

        assertDoesNotThrow(() -> app.sendWhisper(null, "Hello"));
        assertDoesNotThrow(() -> app.sendWhisper("User", " "));
    }

    @Test
    void confirmedNickname_getterAndSetter_worksCorrectly() {
        ClientApp.setConfirmedNickname("Phantom123");
        assertEquals("Phantom123", ClientApp.getConfirmedNickname());
    }

    @Test
    void notifyGlobalMessageReceived_triggersListener() {
        AtomicBoolean listenerTriggered = new AtomicBoolean(false);

        ClientApp.setGlobalMessageListener(msg -> {
            assertEquals("Hello World!", msg);
            listenerTriggered.set(true);
        });

        ClientApp.notifyGlobalMessageReceived("Hello World!");
        assertTrue(listenerTriggered.get(), "The listener should have been triggered");
    }

    @Test
    void notifyWhisperReceived_triggersListener() {
        AtomicBoolean listenerTriggered = new AtomicBoolean(false);

        ClientApp.setWhisperMessageListener(msg -> {
            assertEquals("Secret Message!", msg);
            listenerTriggered.set(true);
        });

        ClientApp.notifyWhisperReceived("Secret Message!");
        assertTrue(listenerTriggered.get(), "The listener should have been triggered");
    }
}
