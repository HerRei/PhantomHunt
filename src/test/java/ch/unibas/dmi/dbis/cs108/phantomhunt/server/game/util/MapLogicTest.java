package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapLogicTest {

    @Test
    void loadMapFromString_parsesCorrectly(){
        // mini-map for testing
        String[][] rawMap = {
                {"X", "X", "X"},
                {"X", " ", "X"},
                {"X", "L", "X"},
        };

        MapLogic map = new MapLogic(rawMap);
        Boolean[][] boolMap = map.getMap();

        // Check if size is correct
        assertEquals(3, boolMap.length);

        // 'X' must be recognized as wall -> false
        assertFalse(boolMap[0][0], "'X' should be a wall (false)");

        // ' ' is walkable -> true
        assertTrue(boolMap[1][1], "' ' should be walkable (true)");

        // 'L' must be walkable -> true
        assertTrue(boolMap[2][1], "'L' must be walkable (true)");
    }
}