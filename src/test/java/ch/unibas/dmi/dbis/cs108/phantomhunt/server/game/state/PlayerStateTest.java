package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStateTest {

    @Test
    void playerState_copy_createsIndependentInstance() {
        // mini-dummy-map for position
        Map dummyMap = new Map(new String[][] {{" "}});
        Position pos = new Position(new int[]{0,0}, dummyMap);
        InputState input = new InputState(0, 0);

        PlayerState original = new PlayerState(
                "id123", "Alice", PlayerRole.HUMAN, pos, input, 100, 3, true, false
        );

        PlayerState copy = original.copy();

        // primitive values must be exactly the same
        assertEquals("Alice", copy.getNickname());
        assertEquals(100, copy.getScore());
        assertEquals(PlayerRole.HUMAN, copy.getRole());

        // objects must be independent
        assertNotSame(original.getPosition(), copy.getPosition(), "Position cannot be the same object.");
        assertNotSame(original.getInputState(), copy.getInputState(), "InputState cannot be the same object.");
    }

    @Test
    void addScore_increasesScore() {
        Map dummyMap = new Map(new String[][] {{" "}});
        PlayerState player = new PlayerState("id", "Bob", PlayerRole.PHANTOM,
                new Position(new int[]{0, 0}, dummyMap), new InputState(0, 0), 0, 0, true, false);

        player.addScore(50);
        assertEquals(50, player.getScore());

        player.addScore(10);
        assertEquals(60, player.getScore());
    }
}