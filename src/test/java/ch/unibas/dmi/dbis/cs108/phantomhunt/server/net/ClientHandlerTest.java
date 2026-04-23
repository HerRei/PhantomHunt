package ch.unibas.dmi.dbis.cs108.phantomhunt.server.net;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.LobbyHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.session.Registry;
import ch.unibas.dmi.dbis.cs108.phantomhunt.util.FakeSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    private Registry realRegistry;
    private LobbyHandler realLobbyHandler;

    @BeforeEach
    void setUp() {
        realRegistry = new Registry();
        realLobbyHandler = LobbyHandler.getInstance();
    }

    @Test
    void run_handlesNickChange() {
        FakeSocket socket = new FakeSocket("NICK Alice\nLOGOUT\n");
        ClientHandler handler = new ClientHandler(socket, realRegistry, realLobbyHandler);

        handler.run();

        // has handler internally changed name?
        assertEquals("Alice", handler.getName());
        // has handler got a welcome from server?
        assertTrue(socket.getSentData().contains("WELCOME Alice"));
    }

    @Test
    void run_handlesLobbyCommands_MKL_and_SPEC() {
        FakeSocket socket = new FakeSocket("MKL MyLobby\nSPEC 123\nLOGOUT\n");
        ClientHandler handler = new ClientHandler(socket, realRegistry, realLobbyHandler);

        handler.run();

        String sentData = socket.getSentData();

        assertTrue(sentData.contains("LOBBY_INFO"), "The Lobby should have been created.");

        assertTrue(sentData.contains("REJECT You are already in a lobby"));
    }

    @Test
    void run_handlesYapping_rejectsIfNoLobby() {
        FakeSocket socket = new FakeSocket("YAP Hello Lobby\nLOGOUT\n");
        ClientHandler handler = new ClientHandler(socket, realRegistry, realLobbyHandler);

        handler.run();

        assertTrue(socket.getSentData().contains("REJECT You are not in a lobby"));
    }

    @Test
    void run_handlesInvalidInput_sendsReject() {
        FakeSocket socket = new FakeSocket("NOT_A_COMMAND This is garbage\nLOGOUT\n");
        ClientHandler handler = new ClientHandler(socket, realRegistry, realLobbyHandler);

        handler.run();

        assertTrue(socket.getSentData().contains("REJECT"));
    }
}