package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void copy_createsIndependentInstanceWithSameValues() {
    RoundState state = new RoundState(2, 1, 1000L, 5000L);
    RoundState copy = state.copy();

    // values must be identical
    assertEquals(2, copy.getCurrentRound());
    assertEquals(1, copy.getHumanIndex());
    assertEquals(1000L, copy.getRoundStartTimeMillis());
    assertEquals(5000L, copy.getRoundEndTimeMillis());

    // objects must be different in storage
    assertNotSame(state, copy, "Copy must be a new instance");
  }

  @Test
  void setters_updateValuesCorrectly() {
    RoundState state = new RoundState(1, 0, 0L, 0L);

    state.incrementCurrentRound();
    assertEquals(2, state.getCurrentRound());

    state.setRoundStartTimeMillis(50L);
    state.setRoundEndTimeMillis(150L);
    assertEquals(50L, state.getRoundStartTimeMillis());
    assertEquals(150L, state.getRoundEndTimeMillis());
  }

}
