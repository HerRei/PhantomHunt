package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.Position;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapLogicTest {

  @Test
  void movementRadius_scalesCorrectly() {
    // Formula: Math.max(3.0, radius * 0.4)
    double radius = MapCollision.movementRadius(10.0);
    assertEquals(4.0, radius); // 10 * 0.4 = 4.0

    double small = MapCollision.movementRadius(5.0);
    assertEquals(3.0, small); // min is 3.0
  }

  @Test
  void collidesWithWall_detectsWallAndBounds() {
    // true tiles are walkable, false tiles are walls
    Boolean[][] map = {
      {false, false, false},
      {false, true, false},
      {false, false, false}
    };

    // map checks directly at array index level (Math.floor(x) = tile)
    // player at 1.5, 1.5 is exactly in center of walkable tile [1][1]
    assertFalse(MapCollision.collidesWithWall(map, 1.5, 1.5, 0.4), "Middle (1.5) should be free");

    // we move player to the left (1.2). With radius of 0.4, it extends into tile 0
    assertTrue(MapCollision.collidesWithWall(map, 1.2, 1.5, 0.4), "Should touch the left wall");

    // Out of bounds is treated as if it was a wall
    assertTrue(MapCollision.collidesWithWall(map, 5.0, 5.0, 0.4), "Out of bounds is a wall");
    assertTrue(MapCollision.collidesWithWall(map, -1.0, 1.5, 0.4), "Negative values are a wall");
  }

  @Test
  void getRandomSpawns_tinyMap_doesNotCauseStackOverflow() {
    String[][] tinyMap = {
            {" ", " ", " "},
            {" ", " ", " "},
            {" ", " ", " "}
    };
    MapLogic mapLogic = new MapLogic(tinyMap);
    List<Position> spawns = new ArrayList<>();

    assertDoesNotThrow(() -> {
      mapLogic.getRandomSpawns(4, spawns, 150.0);
    }, "Cannot end in StackOverflow");
  }
}
