package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.*;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.phantomhunt.util.FakeClientHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameHandlerTest {

  @Test
  void matchLifecycle_startsAndAdvancesPhases() {
    // We build fake-lobby with 4 players
    FakeClientHandler host = new FakeClientHandler("P1");
    Lobby lobby = new Lobby("L1", "TestLobby", host);
    lobby.addPlayer(new FakeClientHandler("P2"));
    lobby.addPlayer(new FakeClientHandler("P3"));
    lobby.addPlayer(new FakeClientHandler("P4"));

    // we use the factory to build the state
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameFactory factory = new GameFactory();
    List<GameState.PlayerSeed> seeds =
        List.of(
            new GameState.PlayerSeed("P1", "P1"),
            new GameState.PlayerSeed("P2", "P2"),
            new GameState.PlayerSeed("P3", "P3"),
            new GameState.PlayerSeed("P4", "P4"));

    GameState state = factory.createWithDefaultRules("Match1", seeds, map);
    GameHandler handler = new GameHandler(state, lobby);

    // waiting for start
    assertEquals(
        GamePhase.WAITING_TO_START, handler.getPhase(), "Game must start in Waiting-Stage");

    // game start
    handler.startMatch(System.currentTimeMillis());
    assertEquals(
        GamePhase.ROUND_RUNNING, handler.getPhase(), "After startMatch the round must be running");
    assertEquals(1, handler.getCurrentRound(), "Current round should be 1");

    // human gets caught
    handler.endRoundHumanCaught("P2", System.currentTimeMillis());
    assertEquals(GamePhase.ROUND_ENDED, handler.getPhase(), "Round must be finished");

    // next round starts
    handler.advanceToNextRound(System.currentTimeMillis());
    assertEquals(GamePhase.ROUND_RUNNING, handler.getPhase(), "New Round must have started");
    assertEquals(2, handler.getCurrentRound(), "Current round should be 2");

    // someone lost connection
    handler.abortMatch("Test Abort");
    assertEquals(GamePhase.ABORTED, handler.getPhase(), "Round must be aborted");
  }

  @Test
  void tick_updatesPositionsAndHandlesCollisions() {
    // setup: small map, 4 players
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameFactory factory = new GameFactory();
    List<GameState.PlayerSeed> seeds = List.of(
            new GameState.PlayerSeed("P1", "HumanPlayer"),
            new GameState.PlayerSeed("P2", "Phantom1"),
            new GameState.PlayerSeed("P3", "Phantom2"),
            new GameState.PlayerSeed("P4", "Phantom3")
    );
    GameState state = factory.createWithDefaultRules("Match1", seeds, map);

    FakeClientHandler host;
    try {
      host = new FakeClientHandler("P1");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Lobby lobby = new Lobby("L1", "TestLobby", host);

    try {
      lobby.addPlayer(new FakeClientHandler("P2"));
      lobby.addPlayer(new FakeClientHandler("P3"));
      lobby.addPlayer(new FakeClientHandler("P4"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    GameHandler handler = new GameHandler(state, lobby);
    handler.startMatch(System.currentTimeMillis());

    PlayerState human = state.getMutablePlayerAt(state.getHumanIndex());
    PlayerState phantom = state.getMutablePlayerAt(1); // choose phantom

    // right next to each other
    human.setPosition(new Position(new int[]{1, 1}, map));
    phantom.setPosition(new Position(new int[]{1, 2}, map)); // same tile

    // give the phantom input state moving directly into the human
    phantom.setInputState(new InputState(0, -1)); // moving left
    phantom.setRealInput(new InputState(0, -1));

    // simulate game-loop with multiple ticks
    // speed 100 -> 0.32 sec for small tile. 10*0.05=0.5s
    long currentTime = System.currentTimeMillis();
    for (int i = 0; i < 10; i++) {
      handler.tick(0.05, currentTime + (i * 50));

      if (handler.getPhase() == GamePhase.ROUND_ENDED) {
        break;
      }
    }

    // was collision registered?
    assertEquals(GamePhase.ROUND_ENDED, handler.getPhase(), "Round must end when phantom catches human");
    assertTrue(human.isCaughtThisRound(), "Human-Status must be 'caught'");
  }

  @Test
  void tryAbility_humanCatchesPhantom() {
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameFactory factory = new GameFactory();
    List<GameState.PlayerSeed> seeds = List.of(
            new GameState.PlayerSeed("P1", "P1"),
            new GameState.PlayerSeed("P2", "P2"),
            new GameState.PlayerSeed("P3", "P3"),
            new GameState.PlayerSeed("P4", "P4")
    );
    GameState state = factory.createWithDefaultRules("Match2", seeds, map);

    FakeClientHandler host;
    try {
      host = new FakeClientHandler("P1");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Lobby lobby = new Lobby("L2", "TestLobby", host);

    try {
      lobby.addPlayer(new FakeClientHandler("P2"));
      lobby.addPlayer(new FakeClientHandler("P3"));
      lobby.addPlayer(new FakeClientHandler("P4"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    GameHandler handler = new GameHandler(state, lobby);

    handler.startMatch(System.currentTimeMillis());

    PlayerState human = state.getMutablePlayerAt(state.getHumanIndex());
    PlayerState phantom = state.getMutablePlayerAt(1);

    // set human and phantom on same tile
    Position startPos = new Position(new int[] {1, 1}, map);
    human.setPosition(startPos);
    phantom.setPosition(startPos);

    // activate abilities
    int initialHumanScore = human.getScore();
    handler.tryAbility(human.getPlayerId());

    // simulate tick
    handler.tick(0.1, System.currentTimeMillis());

    // game continues but human received points
    assertEquals(GamePhase.ROUND_RUNNING, handler.getPhase(), "Round cannot end");
    assertTrue(human.getScore() > initialHumanScore, "Human should receive points for catching a phantom");

    // phantom must be respawned elsewhere
    int[] currentPhantomTile = map.pixelToTilePosition(phantom.getPosition().getX(), phantom.getPosition().getY());
    boolean isStillOnSameTile = (currentPhantomTile[0] == 1 && currentPhantomTile[1] == 1);
    assertFalse(isStillOnSameTile, "After getting caught, phantom must be respawned");
  }

  @Test
  void tryLobbyChatAbility_onlyHumanLosesPointsAndStartsAbility() {
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameFactory factory = new GameFactory();
    List<GameState.PlayerSeed> seeds =
        List.of(
            new GameState.PlayerSeed("P1", "P1"),
            new GameState.PlayerSeed("P2", "P2"),
            new GameState.PlayerSeed("P3", "P3"),
            new GameState.PlayerSeed("P4", "P4"));
    GameState state = factory.createWithDefaultRules("Match_ChatAbility", seeds, map);

    FakeClientHandler host = new FakeClientHandler("P1");
    Lobby lobby = new Lobby("L_ChatAbility", "TestLobby", host);
    lobby.addPlayer(new FakeClientHandler("P2"));
    lobby.addPlayer(new FakeClientHandler("P3"));
    lobby.addPlayer(new FakeClientHandler("P4"));

    GameHandler handler = new GameHandler(state, lobby);
    handler.startMatch(System.currentTimeMillis());

    PlayerState human = state.getMutablePlayerAt(state.getHumanIndex());
    PlayerState phantom = state.getMutablePlayerAt(1);

    handler.tryLobbyChatAbility(phantom.getPlayerId());
    handler.tryLobbyChatAbility("Spectator");
    assertEquals(0, phantom.getScore(), "Phantom chat code must not change score.");
    assertFalse(
        host.receivedPackets.stream().anyMatch(packet -> packet.cmd() == Command.ABILITY),
        "Non-human senders must not start the ability.");

    handler.tryLobbyChatAbility(human.getPlayerId());

    assertEquals(-50, human.getScore(), "Human must lose 50 points for the chat code.");
    assertEquals(2, human.getRemainingAbility(), "Human ability should be used once.");
    assertTrue(
        host.receivedPackets.stream()
            .anyMatch(packet -> packet.cmd() == Command.ABILITY && "START".equals(packet.text())),
        "Human ability should start.");
  }

  @Test
  void wisdomRoundBonus_addsFiveAfterEachRoundForInspiredPlayer() {
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameFactory factory = new GameFactory();
    List<GameState.PlayerSeed> seeds =
        List.of(
            new GameState.PlayerSeed("P1", "P1"),
            new GameState.PlayerSeed("P2", "P2"),
            new GameState.PlayerSeed("P3", "P3"),
            new GameState.PlayerSeed("P4", "P4"));
    GameState state = factory.createWithDefaultRules("Match_Wisdom", seeds, map);

    FakeClientHandler host = new FakeClientHandler("P1");
    host.setWisdomRoundBonus(true);
    Lobby lobby = new Lobby("L_Wisdom", "TestLobby", host);
    lobby.addPlayer(new FakeClientHandler("P2"));
    lobby.addPlayer(new FakeClientHandler("P3"));
    lobby.addPlayer(new FakeClientHandler("P4"));

    GameHandler handler = new GameHandler(state, lobby);
    handler.startMatch(1_000L);

    handler.endRoundHumanCaught("P2", 1_000L);
    assertEquals(5, state.getMutablePlayerAt(0).getScore(), "Inspired player gets +5 after round 1.");
    assertEquals(20, state.getMutablePlayerAt(1).getScore(), "Normal catcher score is unchanged.");
    assertFalse(host.consumeWisdomRoundBonus(), "Wisdom bonus is consumed when the game starts.");

    handler.advanceToNextRound(2_000L);
    handler.endRoundHumanCaught("P1", 2_000L);

    assertEquals(30, state.getMutablePlayerAt(0).getScore(), "Inspired player gets +5 each round.");
  }

  @Test
  void tick_timeRunsOut_humanSurvives() {
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameFactory factory = new GameFactory();
    List<GameState.PlayerSeed> seeds = List.of(
            new GameState.PlayerSeed("P1", "HumanPlayer"),
            new GameState.PlayerSeed("P2", "Phantom1"),
            new GameState.PlayerSeed("P3", "Phantom2"),
            new GameState.PlayerSeed("P4", "Phantom3")
    );
    GameState state = factory.createWithDefaultRules("Match_Timeout", seeds, map);

    FakeClientHandler host;
    try {
      host = new FakeClientHandler("P1");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Lobby lobby = new Lobby("L_Timeout", "TestLobby", host);
    try {
      lobby.addPlayer(new FakeClientHandler("P2"));
      lobby.addPlayer(new FakeClientHandler("P3"));
      lobby.addPlayer(new FakeClientHandler("P4"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    GameHandler handler = new GameHandler(state, lobby);
    long startTime = System.currentTimeMillis();
    handler.startMatch(startTime);

    PlayerState human = state.getMutablePlayerAt(state.getHumanIndex());
    int initialScore =  human.getScore();

    long timeOutMillis = handler.getRoundEndTimeMillis() + 100;

    handler.tick(0.1, timeOutMillis);

    assertEquals(GamePhase.ROUND_ENDED, handler.getPhase(), "Round must end when time runs out");

    assertTrue(handler.getLastRoundOutcome().isPresent());
    assertEquals(RoundOutcomeType.HUMAN_SURVIVED, handler.getLastRoundOutcome().get().getType(), "Outcome must be HUMAN_SURVIVED");

    assertTrue(human.getScore() > initialScore, "Human should receive points for surviving");
  }
}
