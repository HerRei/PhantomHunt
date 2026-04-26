package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoundOutcomeTest {

  @Test
  void constructor_nullType_throwsException() {
    // type (SURVIVED/CAUGHT) can never be null
    assertThrows(
        NullPointerException.class, () -> new RoundOutcome(1, null, "P1", Optional.empty(), 1000L));

    // catcher can be optional.empty, but not null
    assertThrows(
        NullPointerException.class,
        () -> new RoundOutcome(1, RoundOutcomeType.HUMAN_CAUGHT, "P1", null, 1000L));
  }

  @Test
  void constructor_validParams_storesValues() {
    RoundOutcome outcome =
        new RoundOutcome(
            1, RoundOutcomeType.HUMAN_CAUGHT, "P1", Optional.of("P2"), 1000L, "Reason");

    assertEquals(1, outcome.getRoundNumber());
    assertEquals(RoundOutcomeType.HUMAN_CAUGHT, outcome.getType());
    assertEquals("P1", outcome.getHumanPlayerId());
    assertEquals("P2", outcome.getCatcherPlayerId().get());
    assertEquals(1000L, outcome.getEndedAtMillis());
    assertEquals("Reason", outcome.getReason());
  }
}
