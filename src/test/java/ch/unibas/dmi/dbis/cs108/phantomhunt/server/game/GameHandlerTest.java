package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GamePhase;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameState;
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
        List<GameState.PlayerSeed> seeds = List.of(
                new GameState.PlayerSeed("P1", "P1"),
                new GameState.PlayerSeed("P2", "P2"),
                new GameState.PlayerSeed("P3", "P3"),
                new GameState.PlayerSeed("P4", "P4")
        );

        GameState state = factory.createWithDefaultRules("Match1", seeds, map);
        GameHandler handler = new GameHandler(state, lobby);

        // waiting for start
        assertEquals(GamePhase.WAITING_TO_START, handler.getPhase(), "Game must start in Waiting-Stage");

        // game start
        handler.startMatch(System.currentTimeMillis());
        assertEquals(GamePhase.ROUND_RUNNING, handler.getPhase(), "After startMatch the round must be running");
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

}