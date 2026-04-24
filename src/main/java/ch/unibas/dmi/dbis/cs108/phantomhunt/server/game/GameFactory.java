package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameRules;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameState;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.GameState.PlayerSeed;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.InputState;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.PlayerRole;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.PlayerState;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.Position;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.util.MapLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds a fresh game state and owns the setup/validation logic for match creation.
 */
public final class GameFactory {
  private static GameState gs;
  /**
   * Creates a game with the provided rules and map.
   *
   * @param matchId The unique identifier for th match.
   * @param playerSeeds Initial configuration data for players.
   * @param rules The specific game rules to apply.
   * @param map The collision map for the game.
   */
  public GameState create(
          String matchId, List<PlayerSeed> playerSeeds, GameRules rules, MapLogic map) {
    Boolean[][] validatedMap = deepCopyAndValidateMap(map.getMap());
    List<PlayerState> players = createPlayers(playerSeeds, validatedMap);
    gs = new GameState(matchId, rules, validatedMap, players);
    return gs;
  }

  /**
   * Creates a game using {@link GameRules#defaultRules()}.
   *
   * @param matchId The unique identifier for the match.
   * @param playerSeeds Initial configuration data for players.
   * @param map The collision map for the game.
   * @return A newly initialized GameState.
   */
  public GameState createWithDefaultRules(
          String matchId, List<PlayerSeed> playerSeeds, MapLogic map) {
    if (map == null) { //fallback for no Map
      return create(matchId, playerSeeds, GameRules.defaultRules(), map);
    }
    return create(matchId, playerSeeds, GameRules.defaultRules(), map);
  }

  private static List<PlayerState> createPlayers(List<PlayerSeed> playerSeeds, Boolean[][] map) {
    Objects.requireNonNull(playerSeeds, "playerSeeds must not be null");

    if (playerSeeds.size() != GameState.REQUIRED_PLAYER_COUNT) {
      throw new IllegalArgumentException(
              "A match requires exactly " + GameState.REQUIRED_PLAYER_COUNT + " players.");
    }

    List<PlayerState> result = new ArrayList<>();
    List<Position> defaultSpawns = createDefaultSpawnPositions();

    for (int i = 0; i < playerSeeds.size(); i++) {
      PlayerSeed seed = Objects.requireNonNull(playerSeeds.get(i), "player seed must not be null");

      result.add(
              new PlayerState(
                      requireNonBlank(seed.playerId(), "playerId must not be blank"),
                      requireNonBlank(seed.nickname(), "nickname must not be blank"),
                      PlayerRole.PHANTOM,
                      defaultSpawns.get(i).copy(),
                      new InputState(0, 0),
                      0,
                      0,
                      true,
                      false));
    }

    return result;
  }

  private static Boolean[][] deepCopyAndValidateMap(Boolean[][] source) {
    Objects.requireNonNull(source, "map must not be null");

    if (source.length == 0 || source[0].length == 0) {
      throw new IllegalArgumentException("Map must not be empty.");
    }
    int width = source[0].length;

    Boolean[][] copy = new Boolean[source.length][];

    for (int y = 0; y < source.length; y++) {
      Objects.requireNonNull(source[y], "map row must not be null");

      if (source[y].length != width) {
        throw new IllegalArgumentException("All rows must have the same width.");
      }

      copy[y] = new Boolean[source[y].length];
      for (int x = 0; x < source[y].length; x++) {
        if (source[y][x] == null) {
          throw new IllegalArgumentException("Map tile must not be null.");
        }
        copy[y][x] = source[y][x];
      }
    }

    return copy;
  }

  static List<Position> createDefaultSpawnPositions() {
    List<Position> spawns = new ArrayList<Position>();
    spawns = MapLogic.getInstance().getRandomSpawns(GameState.REQUIRED_PLAYER_COUNT, spawns, GameState.SPAWN_DISTANCE);
    return spawns;
  }

  private static String requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
