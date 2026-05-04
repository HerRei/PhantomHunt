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
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameRules(0, 50000, 6.0, 100.0, 1, 50, 10, 10, 3, 10)); // 0 rounds = crash
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameRules(4, 0, 6.0, 100.0, 1, 50, 10, 10, 3, 10)); // 0 time = crash
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameRules(4, 50000, 0.0, 100.0, 1, 50, 10, 10, 3, 10)); // 0 radius = crash
  }

  @Test
  void fromPayload_parsesProtocolSettings() {
    GameRules rules = GameRules.fromPayload("6 70000 7.5 120.0 2 60 15 20 4 25");

    assertEquals(6, rules.totalRounds());
    assertEquals(70000, rules.roundDurationMillis());
    assertEquals(7.5, rules.playerRadius());
    assertEquals(120.0, rules.moveSpeedPerSecond());
    assertEquals(4, rules.humanAbilitys());
  }

  @Test
  void toPayload_canBeParsedAgain() {
    GameRules rules = new GameRules(5, 60000, 6.5, 110.0, 1, 40, 12, 14, 2, 18);

    assertEquals(rules, GameRules.fromPayload(rules.toPayload()));
  }
}
