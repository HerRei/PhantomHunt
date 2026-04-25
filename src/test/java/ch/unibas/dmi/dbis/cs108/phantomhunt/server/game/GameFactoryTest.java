package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameState;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameFactoryTest {

  @Test
  void createWithDefaultRules_generatesValidGameState() {
    // we must use the real map here, that spawn_distance gets fulfilled without endless loop
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());

    GameFactory factory = new GameFactory();

    // We implement 4 test-players
    List<GameState.PlayerSeed> seeds =
        List.of(
            new GameState.PlayerSeed("id1", "Alice"),
            new GameState.PlayerSeed("id2", "Bob"),
            new GameState.PlayerSeed("id3", "Charlie"),
            new GameState.PlayerSeed("id4", "Dave"));

    // factory constructs the whole game
    GameState state = factory.createWithDefaultRules("Match1", seeds, map);

    assertNotNull(state, "GameState cannot be null");
    assertEquals("Match1", state.getMatchId());
    assertEquals(4, state.getPlayerCount(), "There must be exactly 4 players");

    // player got name and valid pos
    assertEquals("Alice", state.getMutablePlayerAt(0).getNickname());
    assertNotNull(state.getMutablePlayerAt(0).getPosition(), "Player needs starting position");
  }

  @Test
  void create_rejectsWrongPlayerCount() {
    MapLogic map = new MapLogic(MapLogic.generateExampleMap());
    GameFactory factory = new GameFactory();

    // only 2 players -> factory must throw IllegalArgumentException
    List<GameState.PlayerSeed> seeds =
        List.of(new GameState.PlayerSeed("id1", "Alice"), new GameState.PlayerSeed("id2", "Bob"));

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          factory.createWithDefaultRules("Match1", seeds, map);
        },
        "Factory must decline game start with less than 4 players.");
  }
}
