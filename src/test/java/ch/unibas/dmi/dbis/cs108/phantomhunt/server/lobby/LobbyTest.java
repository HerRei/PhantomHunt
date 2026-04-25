package ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

  // Fake-Handler
  static class FakeClientHandler extends ClientHandler {
    public List<Packet> receivedPackets = new ArrayList<>();
    private String fakeName;

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
  }

  private FakeClientHandler host;
  private Lobby lobby;

  @BeforeEach
  void setUp() throws Exception {
    host = new FakeClientHandler("HostUser");
    lobby = new Lobby("123", "TestLobby", host);
  }

  @Test
  void constructor_setsHostAndAddsToPlayers() {
    assertEquals("123", lobby.getId());
    assertEquals("TestLobby", lobby.getName());
    assertEquals(host, lobby.getHost());
    assertTrue(lobby.getPlayers().get().contains(host));
  }

  @Test
  void addPlayer_successAndRejectsDuplicates() throws Exception {
    FakeClientHandler p2 = new FakeClientHandler("Player2");

    // adding works
    assertTrue(lobby.addPlayer(p2));
    assertTrue(lobby.getPlayers().get().contains(p2));

    // double adding doesn't work
    assertFalse(lobby.addPlayer(p2));
  }

  @Test
  void addPlayer_rejectsWhenFull() throws Exception {
    // lobby has host, we add 3 others -> 4 = full
    lobby.addPlayer(new FakeClientHandler("P2"));
    lobby.addPlayer(new FakeClientHandler("P3"));
    lobby.addPlayer(new FakeClientHandler("P4"));

    FakeClientHandler p5 = new FakeClientHandler("P5");
    assertFalse(lobby.addPlayer(p5));
  }

  @Test
  void removePlayer_reassignsHostIfHostLeaves() throws Exception {
    FakeClientHandler p2 = new FakeClientHandler("P2");
    lobby.addPlayer(p2);

    // Host leaves lobby
    assertTrue(lobby.removePlayer(host));

    // p2 must be the new host
    assertEquals(p2, lobby.getHost());
    assertFalse(lobby.getPlayers().get().contains(host));
  }

  @Test
  void spectators_canBeAddedAndRemoved() throws Exception {
    FakeClientHandler spec = new FakeClientHandler("Spec1");

    assertTrue(lobby.addSpectator(spec));
    assertTrue(lobby.getSpectators().get().contains(spec));

    // adding doubled doesn't work
    assertFalse(lobby.addSpectator(spec));

    // removing works
    assertTrue(lobby.removeSpectator(spec));
    assertFalse(lobby.getSpectators().get().contains(spec));
  }

  @Test
  void broadcast_sendsToPlayersAndSpectators() throws Exception {
    FakeClientHandler p2 = new FakeClientHandler("P2");
    FakeClientHandler spec = new FakeClientHandler("Spec1");

    lobby.addPlayer(p2);
    lobby.addSpectator(spec);

    Packet testPacket = Packet.of(Command.INFO, "Hello Lobby");
    lobby.broadcast(testPacket);

    // host, p2, spectator must have received the packet
    assertEquals(Command.INFO, host.receivedPackets.get(host.receivedPackets.size() - 1).cmd());
    assertEquals(Command.INFO, p2.receivedPackets.get(p2.receivedPackets.size() - 1).cmd());
    assertEquals(Command.INFO, spec.receivedPackets.get(spec.receivedPackets.size() - 1).cmd());
  }
}
