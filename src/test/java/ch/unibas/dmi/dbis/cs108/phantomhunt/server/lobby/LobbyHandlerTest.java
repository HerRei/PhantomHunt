package ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class LobbyHandlerTest {
    static class FakeClientHandler extends ClientHandler {
        public List<Packet> receivedPackets = new ArrayList<>();
        private String fakeName;
        private Lobby currentLobby;

        public FakeClientHandler(String name) throws Exception {
            super(new Socket(), null, null);
            this.fakeName = name;
        }

        @Override
        public String getName() { return fakeName; }

        @Override
        public void sendMessage(Packet p) { receivedPackets.add(p); }

        @Override
        public void setCurrentLobby(Lobby lobby) { this.currentLobby = lobby; }

        @Override
        public Lobby getCurrentLobby() { return currentLobby; }
    }

    private LobbyHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = LobbyHandler.getInstance();

        // since LobbyHandler is Singleton, listen before every test
        clearVectorField("waitingLobbies");
        clearVectorField("playingLobbies");
        clearVectorField("finishedLobbies");
    }

    private void clearVectorField(String fieldName) throws Exception {
        Field field = LobbyHandler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ((Vector<?>) field.get(handler)).clear();
    }

    @Test
    void spectateLobby_addsSpectator() throws Exception {
        FakeClientHandler host = new FakeClientHandler("Host");
        handler.createLobby("LobbyC", host);

        FakeClientHandler spec = new FakeClientHandler("Spec");
        handler.spectateLobby("LobbyC", spec);

        Lobby lobby = handler.getWaitingLobbies().get().get(0);
        assertTrue(lobby.getSpectators().get().contains(spec));
        assertEquals(lobby, spec.getCurrentLobby());
    }

    @Test
    void startGame_rejectsIfNotHost() throws Exception {
        FakeClientHandler host = new FakeClientHandler("Host");
        FakeClientHandler p2 = new FakeClientHandler("P2");
        handler.createLobby("LobbyD", host);
        handler.createLobby("LobbyD", p2);

        // p2 tries to start the game
        handler.startGame("LobbyD", p2);

        // p2 must receive reject packet
        assertEquals(Command.REJECT, p2.receivedPackets.get(p2.receivedPackets.size()-1).cmd());
    }

    @Test
    void startGame_rejectsIfWrongPlayerCount() throws Exception {
        FakeClientHandler host = new FakeClientHandler("Host");
        handler.createLobby("LobbyE", host);

        // only 1 player in lobby, game needs 4
        handler.startGame("LobbyE", host);

        // Host must receive reject packet
        assertEquals(Command.REJECT, host.receivedPackets.get(host.receivedPackets.size()-1).cmd());
        assertTrue(host.receivedPackets.get(host.receivedPackets.size() -1).text().contains("Not the right amount of players"));
    }

    @Test
    void finishedLobby_movesLobbyToFinishedList() throws Exception {
        FakeClientHandler host = new FakeClientHandler("Host");
        Lobby lobby = handler.createLobby("LobbyF", host);

        // put lobby in playing mode
        handler.getWaitingLobbies().get().remove(lobby);

        Field playingField = LobbyHandler.class.getDeclaredField("playingLobbies");
        playingField.setAccessible(true);
        ((Vector<Lobby>) playingField.get(handler)).add(lobby);

        handler.finishLobby("LobbyF");

        assertFalse(((Vector<Lobby>) playingField.get(handler)).contains(lobby));

        Field finishedField = LobbyHandler.class.getDeclaredField("finishedLobbies");
        finishedField.setAccessible(true);
        assertTrue(((Vector<Lobby>) finishedField.get(handler)).contains(lobby));
    }

}