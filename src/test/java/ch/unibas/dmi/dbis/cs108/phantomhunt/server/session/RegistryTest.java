package ch.unibas.dmi.dbis.cs108.phantomhunt.server.session;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.util.FakeClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistryTest {

  private Registry registry;

    // This method is automatically used before each test
    @BeforeEach
    void setUp() throws Exception {
        registry = Registry.getInstance();
        clearRegistryState("sessions");
        clearRegistryState("byName");
        clearRegistryState("highscores");
    }

    private void clearRegistryState(String fieldName) throws Exception {
        Field field = Registry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(registry);
        if (value instanceof java.util.Collection<?> collection) {
            collection.clear();
        } else if (value instanceof java.util.Map<?, ?> map) {
            map.clear();
        }
    }

  // Domain-management

  @Test
  void claimName_uniqueName_returnsRequestedName() {
    FakeClientHandler handler = new FakeClientHandler("Unknown");

    String assignedName = registry.claimName("Alice", handler);

    assertEquals("Alice", assignedName);
    assertTrue(registry.names().contains("Alice"));
  }

  @Test
  void claimName_takenName_appendsNumber() {
    FakeClientHandler handler1 = new FakeClientHandler("Unknown");
    FakeClientHandler handler2 = new FakeClientHandler("Unknown");

    // Player 1 takes "Alice"
    registry.claimName("Alice", handler1);

    // Player 2 takes "Alice" as well
    String assignedName = registry.claimName("Alice", handler2);

    assertEquals("Alice1", assignedName);
    assertTrue(registry.names().contains("Alice"));
    assertTrue(registry.names().contains("Alice1"));
  }

  @Test
  void unregister_removesHandlerCompletely() {
    FakeClientHandler handler = new FakeClientHandler("Phantom");

    registry.register(handler);
    registry.claimName("Phantom", handler);

    registry.unregister(handler);

    assertFalse(registry.names().contains("Phantom"));
  }

  // Broadcast & Whisper

  @Test
  void broadcast_sendsToAllRegisteredClients() {
    FakeClientHandler handler1 = new FakeClientHandler("User1");
    FakeClientHandler handler2 = new FakeClientHandler("User2");
    registry.register(handler1);
    registry.register(handler2);

    Packet p = Packet.of(Command.INFO, "Test");
    registry.broadcast(handler1, p);

    // Check if sendMessage was called once but in both cases
    assertEquals(1, handler1.receivedPackets.size());
    assertEquals(1, handler2.receivedPackets.size());
    assertEquals(Command.INFO, handler1.receivedPackets.get(0).cmd());
  }

  @Test
  void whisper_targetMissing_returnsFalse() {
    FakeClientHandler sender = new FakeClientHandler("Sender");

    // sender tries to whisper but registry is empty
    boolean success = registry.whisper(sender, "Ghost", "Hello?");

    assertFalse(success);
    // Check that nothing was sent
    assertTrue(sender.receivedPackets.isEmpty());
  }

  @Test
  void whisper_targetExists_sendsToBoth() {
    FakeClientHandler sender = new FakeClientHandler("Alice");
    FakeClientHandler receiver = new FakeClientHandler("Bob");

    // we register bob, so that the registry knows him
    registry.claimName("Bob", receiver);

    boolean success = registry.whisper(sender, "Bob", "Psst!");

    assertTrue(success);

    // we catch package from bob to check the text
    assertEquals(1, receiver.receivedPackets.size());
    assertEquals(Command.WHISPER, receiver.receivedPackets.get(0).cmd());
    assertTrue(receiver.receivedPackets.get(0).text().contains("Psst!"));

    // check if sender gets copy
    assertEquals(1, sender.receivedPackets.size());
    assertTrue(sender.receivedPackets.get(0).text().contains("Psst!"));
  }

  // Lobby Chat (Yapping)

  @Test
  void yapping_noLobby_sendsRejectToSender() {
    FakeClientHandler sender = new FakeClientHandler("Sender");

    Packet yapPacket = Packet.of(Command.YAP, "Hello?");
    registry.yapping(sender, yapPacket);

    assertEquals(1, sender.receivedPackets.size());
    assertEquals(Command.REJECT, sender.receivedPackets.get(0).cmd());
  }

  @Test
  void yapping_inLobby_sendsOnlyToLobbyMembers() throws Exception {
    // Sender in lobby
    FakeClientHandler sender = new FakeClientHandler("Sender");

    Lobby dummyLobby = new Lobby("Lobby1", "Lobby123", sender);

    sender.setCurrentLobby(dummyLobby);

    // another player is not in lobby
    FakeClientHandler outsider = new FakeClientHandler("Outsider");

    registry.register(sender);
    registry.register(outsider);

    Packet yapPacket = Packet.of(Command.YAP, "Lobby Chat");
    registry.yapping(sender, yapPacket);

    // Sender must receive messages
    assertEquals(1, sender.receivedPackets.size());
    assertTrue(outsider.receivedPackets.isEmpty(), "Outsider cannot get messages");
  }

  @Test
  void highscores_sortAndFormatCorrectly() {
    // we add random players
    registry.addHighscore("Alice", 50);
    registry.addHighscore("Bob", 200);
    registry.addHighscore("Charlie", 150);

    // We take board
    String board = registry.getHighscoreBoard();

    // board must be sorted
    assertTrue(board.contains("1. Bob: 200"), "Bob has the most points and must be in first place");
    assertTrue(board.contains("2. Charlie: 150"), "Charlie must be in second place");
    assertTrue(board.contains("3. Alice: 50"), "Alice must be in third place");
  }
}
