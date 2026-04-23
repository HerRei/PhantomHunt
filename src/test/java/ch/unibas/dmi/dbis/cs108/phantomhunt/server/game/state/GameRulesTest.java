package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {

    @Test
    void defaultRules_createsValidRules() {
        GameRules rules = GameRules.defaultRules();
        assertNotNull(rules);
        assertEquals(4, rules.totalRounds());
    }

    @Test
    void constructor_rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameRules(0, 50000, 6.0, 100.0, 1, 50, 10, 10, 3, 10)); // 0 rounds = crash
        assertThrows(IllegalArgumentException.class, () ->
                new GameRules(4, 0, 6.0, 100.0, 1, 50, 10, 10, 3, 10)); // 0 time = crash
        assertThrows(IllegalArgumentException.class, () ->
                new GameRules(4, 50000, 0.0, 100.0, 1, 50, 10, 10, 3, 10)); // 0 radius = crash
    }
}