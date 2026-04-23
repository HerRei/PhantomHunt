package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateEnumsTest {

    @Test
    void enums_haveCorrectValues() {
        // checks GamePhase
        assertNotNull(GamePhase.valueOf("WAITING_TO_START"));
        assertTrue(GamePhase.values().length > 0);

        // checks PlayerRole
        assertNotNull(PlayerRole.valueOf("HUMAN"));
        assertNotNull(PlayerRole.valueOf("PHANTOM"));
        assertTrue(PlayerRole.values().length > 0);

        // checks RoundOutcomeType
        assertNotNull(RoundOutcomeType.valueOf("HUMAN_SURVIVED"));
        assertTrue(RoundOutcomeType.values().length > 0);

        // checks TileType
        assertNotNull(TileType.valueOf("FLOOR"));
        assertNotNull(TileType.valueOf("WALL"));
        assertTrue(TileType.values().length > 0);
    }
}
