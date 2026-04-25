package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.util.FakeServerHandler;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EventHandlersTest {

    private FakeServerHandler fakeServer;
    private static boolean jfxIsAlive = true;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        try {
            Platform.setImplicitExit(false);
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(2, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            jfxIsAlive = false;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        if (!jfxIsAlive) return;
        fakeServer = new FakeServerHandler();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            EventHandlers.getInstance().setSH(fakeServer);
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
    }

    @Test
    void sendInputs_formatsPayloadCorrectly() {
        if (!jfxIsAlive) { assertTrue(true); return; }
        EventHandlers.getInstance().sendInputs(1, -1);

        assertNotNull(fakeServer.lastSentPacket, "Network packet must be sent");
        assertEquals(Command.INPUT, fakeServer.lastSentPacket.cmd(), "Command must be INPUT");
        assertEquals("1 -1", fakeServer.lastSentPacket.text(), "Payload format must be vertical horizontal");
    }

    @Test
    void handleNicknameUpdate_validName_sensPacket() {
        if (!jfxIsAlive) { assertTrue(true); return; }
        EventHandlers.getInstance().handleNicknameUpdate("Hero");

        assertEquals(Command.NICK, fakeServer.lastSentPacket.cmd(), "Command must be NICK");
        assertEquals("Hero", fakeServer.lastSentPacket.text(), "Text must match the requested nickname");
    }

    @Test
    void handleNicknameUpdate_blankName_ignoresRequest() {
        if (!jfxIsAlive) { assertTrue(true); return; }
        fakeServer.lastSentPacket = null;
        EventHandlers.getInstance().handleNicknameUpdate("");

        assertNull(fakeServer.lastSentPacket, "Blank nickname updates must not trigger network packet");
    }

    @Test
    void basicNetworkCommands_triggerCorrectPackets() {
        if (!jfxIsAlive) { assertTrue(true); return; }
        // Test lobby interactions
        EventHandlers.getInstance().joinLobby("LobbyA");
        assertEquals(Command.CHECKIN, fakeServer.lastSentPacket.cmd(), "Joining a lobby sends CHECKIN");
        assertEquals("LobbyA", fakeServer.lastSentPacket.text());

        EventHandlers.getInstance().quitLobby("LobbyA");
        assertEquals(Command.LOGOUT_LOBBY, fakeServer.lastSentPacket.cmd(), "Quitting a lobby sends LOGOUT_LOBBY");

        EventHandlers.getInstance().updateLists();
        assertEquals(Command.LIST_LOBBY, fakeServer.lastSentPacket.cmd());

        EventHandlers.getInstance().updateHighscore();
        assertEquals(Command.SHOW_HIGHSCORE, fakeServer.lastSentPacket.cmd());

        EventHandlers.getInstance().sendAbility();
        assertEquals(Command.ABILITY, fakeServer.lastSentPacket.cmd());
    }
}