package ch.unibas.dmi.dbis.cs108.phantomhunt.client;

import ch.unibas.dmi.dbis.cs108.phantomhunt.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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

    // simple dummy-handler. no real connection. saves last packet, that should get sent.
    static class FakeServerHandler extends ServerHandler {
        public Packet lastSentPacket = null;

        public FakeServerHandler() {
            // we give him a dead socket, thread crashes, but object exists.
            super(new java.net.Socket());
        }

        @Override
        public synchronized void sendMessage(Packet p) {
            // instead of sending it to the internet, we catch it here
            this.lastSentPacket = p;
        }
    }

    // smuggles fake-handler with reflection in clientapp
    private FakeServerHandler injectFakeHandler(ClientApp app) throws Exception {
        FakeServerHandler fakeHandler = new FakeServerHandler();
        java.lang.reflect.Field handlerField = TcpClient.class.getDeclaredField("serverHandler");
        handlerField.setAccessible(true);
        handlerField.set(app.getTcpClient(), fakeHandler);
        return fakeHandler;
    }

    @Test
    void sendGlobalMessage_validMessage_sendsUnicomPacket() throws Exception {
        ClientApp app = new ClientApp("localhost", 9999);
        FakeServerHandler fakeHandler = injectFakeHandler(app);

        app.sendGlobalMessage("Hello Server");

        assertNotNull(fakeHandler.lastSentPacket, "No package was sent!");
        assertEquals(Command.UNICOM, fakeHandler.lastSentPacket.cmd());
        assertEquals("Hello Server", fakeHandler.lastSentPacket.text());
    }

    @Test
    void sendLobbyMessage_validMessage_sendsYapPacket() throws Exception {
        ClientApp app = new ClientApp("localhost", 9999);
        FakeServerHandler fakeHandler = injectFakeHandler(app);

        app.sendLobbyMessage("GG WP");

        assertNotNull(fakeHandler.lastSentPacket);
        assertEquals(Command.YAP, fakeHandler.lastSentPacket.cmd());
        assertEquals("GG WP", fakeHandler.lastSentPacket.text());
    }

    @Test
    void spectateLobby_validId_sendsSpecPacket() throws Exception {
        ClientApp app = new ClientApp("localhost", 9999);
        FakeServerHandler fakeHandler = injectFakeHandler(app);

        app.spectateLobby("Lobby-123");

        assertNotNull(fakeHandler.lastSentPacket);
        assertEquals(Command.SPEC, fakeHandler.lastSentPacket.cmd());
        assertEquals("Lobby-123", fakeHandler.lastSentPacket.text());
    }

    @Test
    void logout_sendsLogoutPacket() throws Exception {
        ClientApp app = new ClientApp("localhost", 9999);
        FakeServerHandler fakeHandler = injectFakeHandler(app);

        app.logout();

        assertNotNull(fakeHandler.lastSentPacket);
        assertEquals(Command.LOGOUT, fakeHandler.lastSentPacket.cmd());
    }
}
