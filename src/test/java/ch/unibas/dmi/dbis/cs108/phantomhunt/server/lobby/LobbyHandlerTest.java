package ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.GameHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    public String getName() {
      return fakeName;
    }

    @Override
    public void sendMessage(Packet p) {
      receivedPackets.add(p);
    }

    @Override
    public void setCurrentLobby(Lobby lobby) {
      this.currentLobby = lobby;
    }

    @Override
    public Lobby getCurrentLobby() {
      return currentLobby;
    }
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

    @SuppressWarnings("unchecked")
    private Vector<Lobby> getVectorField(String fieldName) throws Exception {
        Field field = LobbyHandler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Vector<Lobby>) field.get(handler);
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
  void spectateLobby_runningGameSendsSpectatorToGame() throws Exception {
    FakeClientHandler host = new FakeClientHandler("Host");
    Lobby lobby = handler.createLobby("RunningLobby", host);

    GameHandler gameHandler = mock(GameHandler.class);
    lobby.attachGame(gameHandler);
    getVectorField("waitingLobbies").remove(lobby);
    getVectorField("playingLobbies").add(lobby);

    FakeClientHandler spec = new FakeClientHandler("Spec");
    handler.spectateLobby("RunningLobby", spec);

    assertEquals(lobby, spec.getCurrentLobby());
    assertTrue(lobby.getSpectators().get().contains(spec));
    assertTrue(spec.receivedPackets.stream().anyMatch(p -> p.cmd() == Command.GAME_START));
    verify(gameHandler).broadcastGameState();
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
    assertEquals(Command.REJECT, p2.receivedPackets.get(p2.receivedPackets.size() - 1).cmd());
  }

  @Test
  void startGame_rejectsIfWrongPlayerCount() throws Exception {
    FakeClientHandler host = new FakeClientHandler("Host");
    handler.createLobby("LobbyE", host);

    // only 1 player in lobby, game needs 4
    handler.startGame("LobbyE", host);

    // Host must receive reject packet
    assertEquals(Command.REJECT, host.receivedPackets.get(host.receivedPackets.size() - 1).cmd());
    assertTrue(
        host.receivedPackets
            .get(host.receivedPackets.size() - 1)
            .text()
            .contains("Not the right amount of players"));
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

  @Test
  void utilityMethods_generateMapAndFormatLobbyString() throws Exception {
    // Tests static map-generator
    String[][] map = MapLogic.generateExampleMap();
    assertEquals(20, map.length, "Map should be 20 rows high");

    // security reset
    clearVectorField("waitingLobbies");

    // tests lobby-string builder
    FakeClientHandler host = new FakeClientHandler("H1");
    handler.createLobby("SuperLobby", host);

    String lobbiesString = handler.getLobbies();

    // if this fails, gitlab logs string
    assertTrue(lobbiesString.contains("SuperLobby (1/4)"), "Lobby count fehlt im String");
    assertTrue(lobbiesString.contains(";"), "Semikolon fehlt");

    FakeClientHandler p2 = new FakeClientHandler("P2");
    handler.joinLobby("SuperLobby", p2);

    lobbiesString = handler.getLobbies();
    assertTrue(lobbiesString.contains("SuperLobby (2/4)"), "Lobby count should update");
  }

  @Test
  void leaveLobby_onFinishedLobbyWithRemainingPlayers_resetsLobbyAndAllowsRejoin() throws Exception {
        FakeClientHandler host = new FakeClientHandler("Host");
        FakeClientHandler p2 = new FakeClientHandler("P2");
        Lobby lobby = handler.createLobby("LobbyReset", host);
        handler.joinLobby("LobbyReset", p2);

        GameHandler gameHandler = mock(GameHandler.class);
        lobby.attachGame(gameHandler);

        Vector<Lobby> waiting = getVectorField("waitingLobbies");
        Vector<Lobby> finished = getVectorField("finishedLobbies");
        waiting.remove(lobby);
        finished.add(lobby);

        handler.leaveLobby("LobbyReset", p2);

        assertNull(p2.getCurrentLobby());
        assertTrue(waiting.contains(lobby), "Lobby should be reopened as waiting");
        assertFalse(finished.contains(lobby), "Lobby should leave the finished list");
        assertFalse(lobby.hasActiveGame(), "Finished lobby should detach its game after reset");
        verify(gameHandler).shutdown();

        FakeClientHandler p3 = new FakeClientHandler("P3");
        handler.joinLobby("LobbyReset", p3);
        assertEquals(lobby, p3.getCurrentLobby(), "Lobby should be joinable again after reset");
    }

    @Test
    void leaveLobby_onFinishedLobbyWithoutRemainingPlayers_removesLobbyCompletely() throws Exception {
        FakeClientHandler host = new FakeClientHandler("Host");
        Lobby lobby = handler.createLobby("LobbyCleanup", host);

        GameHandler gameHandler = mock(GameHandler.class);
        lobby.attachGame(gameHandler);

        Vector<Lobby> waiting = getVectorField("waitingLobbies");
        Vector<Lobby> finished = getVectorField("finishedLobbies");
        waiting.remove(lobby);
        finished.add(lobby);

        handler.leaveLobby("LobbyCleanup", host);

        assertNull(host.getCurrentLobby());
        assertFalse(waiting.contains(lobby), "Empty finished lobby should be removed");
        assertFalse(finished.contains(lobby), "Empty finished lobby should not stay finished");
        assertFalse(lobby.hasActiveGame(), "Removed finished lobby should detach its game");
        verify(gameHandler).shutdown();
    }
}
