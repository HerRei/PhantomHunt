package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.GameFactory;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.Map;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.LobbyHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void serializedPlayers_formatsCorrectly() {
        Map map = new Map(LobbyHandler.generateExampleMap());
        GameFactory factory = new GameFactory();
        List<GameState.PlayerSeed> seeds = List.of(
                new GameState.PlayerSeed("id1", "Alice"),
                new GameState.PlayerSeed("id2", "Bob"),
                new GameState.PlayerSeed("id3", "Charlie"),
                new GameState.PlayerSeed("id4", "Dave")
        );

        // generate game
        GameState state = factory.createWithDefaultRules("Match1", seeds, map);

        // catch serialization-string
        String payload = state.getSerializedPlayers();

        // format must be -> Name:Role:X:Y:Score;
        assertTrue(payload.contains("Alice:PHANTOM:"), "Alice is missing or her role is wrong");
        assertTrue(payload.contains("Bob:PHANTOM:"), "Bob is missing or his role is wrong");

        // 4 players get separated by 3 semicolons
        long semicolonCount = payload.chars().filter(ch -> ch == ';').count();
        assertEquals(3, semicolonCount, "There must be exactly 3 semicolons between 4 players");
    }

    @Test
    void requireMutablePlayer_unknownId_throwsException() {
        Map map = new Map(LobbyHandler.generateExampleMap());
        GameFactory factory = new GameFactory();
        List<GameState.PlayerSeed> seeds = List.of(
                new GameState.PlayerSeed("id1", "Alice"),
                new GameState.PlayerSeed("id2", "Bob"),
                new GameState.PlayerSeed("id3", "Charlie"),
                new GameState.PlayerSeed("id4", "Dave")
        );

        GameState state = factory.createWithDefaultRules("Match1", seeds, map);

        // we search for non-existing player
        assertThrows(IllegalArgumentException.class, () -> state.requireMutablePlayer("id5"));

        // findPlayer gives us empty optional
        assertTrue(state.findPlayer("id5").isEmpty());
    }

    @Test
    void isColliding_detectsMapBoundsAndWalls() {
        Map map = new Map(LobbyHandler.generateExampleMap());
        GameFactory factory = new GameFactory();
        List<GameState.PlayerSeed> seeds = List.of(
                new GameState.PlayerSeed("1", "A"),
                new GameState.PlayerSeed("2", "B"),
                new GameState.PlayerSeed("3", "C"),
                new GameState.PlayerSeed("4", "D")
        );
        GameState state = factory.createWithDefaultRules("Match1", seeds, map);

        // check out of bounds
        assertTrue(state.isColliding(-1.0, 0.0, 0.5), "Left out of the map must collide");
        assertTrue(state.isColliding(999.0, 0.0, 0.5), "Right out of the map must collide");

        // check collision with wall
        assertTrue(state.isColliding(0.5, 0.5, 0.1), "Wall-tile must collide");

        // check empty field
        assertFalse(state.isColliding(1.5, 1.5, 0.1), "Free field cannot collide");
    }

    @Test
    void getWinner_calculatesCorrectly() {
        Map map = new Map(LobbyHandler.generateExampleMap());
        GameFactory factory = new GameFactory();
        List<GameState.PlayerSeed> seeds = List.of(
                new GameState.PlayerSeed("1", "A"),
                new GameState.PlayerSeed("2", "B"),
                new GameState.PlayerSeed("3", "C"),
                new GameState.PlayerSeed("4", "D")
        );
        GameState state = factory.createWithDefaultRules("Match1", seeds, map);

        // as long as game is not MATCH_ENDED, there is no winner
        assertTrue(state.getWinner().isEmpty());

        // we give points and end game
        state.getMutablePlayerAt(0).addScore(50);
        state.getMutablePlayerAt(1).addScore(200);
        state.getMutablePlayerAt(2).addScore(150);

        state.setPhase(GamePhase.MATCH_ENDED);

        // now b must be the winner
        assertTrue(state.getWinner().isPresent());
        assertEquals("B", state.getWinner().get().getNickname());
    }
}