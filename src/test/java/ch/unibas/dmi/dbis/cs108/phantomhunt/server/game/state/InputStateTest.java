package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputStateTest {

  @Test
  void inputState_directionsAndMovement() {
    InputState up = new InputState(-1, 0);
    assertTrue(up.isUp());
    assertFalse(up.isDown());
    assertTrue(up.isMoving());

    InputState downRight = new InputState(1, 1);
    assertTrue(downRight.isDown());
    assertTrue(downRight.isRight());
    assertTrue(downRight.isMoving());

    InputState idle = new InputState(0, 0);
    assertFalse(idle.isMoving());

    InputState copy = up.copy();
    assertEquals(-1, copy.getVertical());
    assertEquals(0, copy.getHorizontal());
    assertTrue(copy.toString().contains("vertical=-1"));
  }
}
