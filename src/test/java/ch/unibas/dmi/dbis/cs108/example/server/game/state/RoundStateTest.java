package ch.unibas.dmi.dbis.cs108.example.server.game.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RoundStateTest {

  @Test
  void advanceHumanIndexRotatesThroughPlayers() {
    RoundState state = new RoundState(1, 0, 0L, 0L);

    state.advanceHumanIndex(4);
    assertEquals(1, state.getHumanIndex());

    state.advanceHumanIndex(4);
    assertEquals(2, state.getHumanIndex());

    state.advanceHumanIndex(4);
    assertEquals(3, state.getHumanIndex());

    state.advanceHumanIndex(4);
    assertEquals(0, state.getHumanIndex());
  }

  @Test
  void advanceHumanIndexRejectsInvalidPlayerCount() {
    RoundState state = new RoundState(1, 0, 0L, 0L);

    assertThrows(IllegalArgumentException.class, () -> state.advanceHumanIndex(0));
  }
}
