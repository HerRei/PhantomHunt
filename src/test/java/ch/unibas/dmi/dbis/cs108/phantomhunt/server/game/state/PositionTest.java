package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

  @Test
  void checkValidInput_allowsMovementToFreeTile() {
    // map with 2 free tiles next to each other
    MapLogic map = new MapLogic(new String[][] {{" ", " "}});

    // start on tile [0, 0]
    Position pos = new Position(new int[] {0, 0}, map);

    InputState oldInput = new InputState(0, 0);
    InputState newInput = new InputState(0, 1); // wants to go right

    boolean success = pos.checkValidInput(oldInput, newInput, map);
    assertTrue(success, "Movement on free field must be allowed");
  }

  @Test
  void checkValidInput_blocksMovementToWall() {
    // map with 2 tiles, on the right side is a wall
    MapLogic map = new MapLogic(new String[][] {{" ", "X"}});
    Position pos = new Position(new int[] {0, 0}, map);

    InputState oldInput = new InputState(0, 0);
    InputState newInput = new InputState(0, 1);

    boolean success = pos.checkValidInput(oldInput, newInput, map);
    assertFalse(success, "Movement in a wall must be blocked");
  }

  @Test
  void updatePosition_movesCoordinatesCorrectly() {
    MapLogic map = new MapLogic(new String[][] {{" ", " "}});
    Position pos = new Position(new int[] {0, 0}, map);

    // the right tile is the goal
    pos.checkValidInput(new InputState(0, 0), new InputState(0, 1), map);

    // we move with 100 speed for 0.1 seconds -> should move 10 pixel
    pos.updatePosition(new InputState(0, 1), 100.0, 0.1, map);

    // start x was 0, now should be at 10.0
    assertEquals(10.0, pos.getX(), 0.01, "X-coordinate must increase by Speed * Time");
    assertEquals(0.0, pos.getY(), 0.01, "Y-coordinate must not change when moving to the right");
  }

  @Test
  void checkValidInput_buffersTurnUntilCenter() {
    MapLogic map = new MapLogic(new String[][] {
            {" ", " "},
            {"X", " "}
    });

    Position pos = new Position(new int[] {0, 0}, map);

    InputState currentInput = new InputState(0, 1);
    InputState nextInput = new InputState(1, 0);

    pos.checkValidInput(new InputState(0,0), currentInput, map);

    // execute buffering check
    boolean canTurnEarly = pos.checkValidInput(currentInput, nextInput, map);
    assertFalse(canTurnEarly, "Since there is a blocking wall, cannot turn yet");

    pos.updatePosition(currentInput, 100.0, 0.32, map); // speed 100*0.32 = 32 pixels

    // as long as it executes without throwing exceptions, buffering logic is stable
    boolean canTurnNow = pos.checkValidInput(currentInput, nextInput, map);
    assertTrue(canTurnNow, "Should be able to turn now");
  }
}
