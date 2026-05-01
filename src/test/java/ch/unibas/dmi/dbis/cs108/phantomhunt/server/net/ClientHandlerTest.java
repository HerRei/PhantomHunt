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
    realRegistry = Registry.getInstance();
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

  @Test
  void run_handlesGameAndLobbyCommands() {
    // we put all the missing commands in one simulated data-stream
    String commands =
        "PONG\nLIST_LOBBY\nCHECKIN 123\nLOGOUT_LOBBY 123\nSTART\nINPUT 1 0\nABILITY START\nLOGOUT\n";
    FakeSocket socket = new FakeSocket(commands);
    ClientHandler handler = new ClientHandler(socket, realRegistry, realLobbyHandler);

    handler.run();

    String sentData = socket.getSentData();

    // did he react to LIST_LOBBY and sent us a LIST_LOBBY packet back?
    assertTrue(sentData.contains("LIST_LOBBY"));

    // since Lobby "123" doesn't exist, CHECKIN must be handled with REJECT
    assertTrue(sentData.contains("REJECT Lobby not found"));
  }

  @Test
  void run_handlesCorruptInputsGracefully() {
    // intentionally sends broken commands
    String garbageCommands =
            "INPUT a b\n" + // expects number, receives letters
            "WHISPER\n" + // expects target and message, receives nothing
            "CHECKIN\n" + // expects LobbyID, receives nothing
            "LOGOUT\n";

    FakeSocket socket = new FakeSocket(garbageCommands);
    ClientHandler handler = new ClientHandler(socket, realRegistry, realLobbyHandler);

    // thread must not crash
    assertDoesNotThrow(() -> handler.run(), "ClientHandler must catch exceptions and not crash on bad input");

    String sentData = socket.getSentData();
    assertTrue(sentData.contains("REJECT"), "Server should respond to bad commands with REJECT");
  }

  @Test
  void wisdomUnlock_requiresFifteenSecondsAndIsConsumedOnce() {
    ClientHandler handler = new ClientHandler(new FakeSocket(""), realRegistry, realLobbyHandler);

    handler.startWisdomUnlock(1_000L);

    assertFalse(handler.claimWisdomUnlock(15_999L), "Wisdom cannot unlock before 15 seconds.");
    assertFalse(handler.hasWisdomBonusReady());

    assertTrue(handler.claimWisdomUnlock(16_000L), "Wisdom must unlock after 15 seconds.");
    assertTrue(handler.hasWisdomBonusReady());

    assertTrue(handler.consumeWisdomRoundBonus(), "Ready wisdom bonus should be consumed.");
    assertFalse(handler.consumeWisdomRoundBonus(), "Wisdom bonus can only be consumed once.");
  }
}
