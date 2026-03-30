package ch.unibas.dmi.dbis.cs108.example.server.game;

import ch.unibas.dmi.dbis.cs108.example.server.game.state.GameRules;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.GameState;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.GameState.PlayerSeed;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.InputState;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.PlayerRole;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.PlayerState;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.Position;
import ch.unibas.dmi.dbis.cs108.example.server.game.state.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds a fresh game state and owns the setup/validation logic for match creation.
 */
public final class GameFactory {

  private static final TileType[][] DEFAULT_MAP = new TileType[16][16]; //this is just a placholder please update this ismail or jan

  static {
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        DEFAULT_MAP[i][j] = TileType.FLOOR;
      }
    }
  }

  /**
   * Creates a game with the provided rules and map.
   */
  public GameState create(
      String matchId, List<PlayerSeed> playerSeeds, GameRules rules, TileType[][] map) {
    TileType[][] validatedMap = deepCopyAndValidateMap(map);
    List<PlayerState> players = createPlayers(playerSeeds, validatedMap);
    return new GameState(matchId, rules, validatedMap, players);
  }

  /**
   * Creates a game using {@link GameRules#defaultRules()}.
   */
  public GameState createWithDefaultRules(
      String matchId, List<PlayerSeed> playerSeeds, TileType[][] map) {
    if (map == null) { //fallback for no Map
      return create(matchId, playerSeeds, GameRules.defaultRules(), DEFAULT_MAP);
    }
    return create(matchId, playerSeeds, GameRules.defaultRules(), map);
  }

  private static List<PlayerState> createPlayers(List<PlayerSeed> playerSeeds, TileType[][] map) {
    Objects.requireNonNull(playerSeeds, "playerSeeds must not be null");

    if (playerSeeds.size() != GameState.REQUIRED_PLAYER_COUNT) {
      throw new IllegalArgumentException(
          "A match requires exactly " + GameState.REQUIRED_PLAYER_COUNT + " players.");
    }

    List<PlayerState> result = new ArrayList<>();
    List<Position> defaultSpawns = createDefaultSpawnPositions(map.length, map[0].length);

    for (int i = 0; i < playerSeeds.size(); i++) {
      PlayerSeed seed = Objects.requireNonNull(playerSeeds.get(i), "player seed must not be null");

      result.add(
          new PlayerState(
              requireNonBlank(seed.playerId(), "playerId must not be blank"),
              requireNonBlank(seed.nickname(), "nickname must not be blank"),
              PlayerRole.PHANTOM,
              defaultSpawns.get(i).copy(),
              new InputState(false, false, false, false),
              0,
              true,
              false));
    }

    return result;
  }

  private static TileType[][] deepCopyAndValidateMap(TileType[][] source) {
    Objects.requireNonNull(source, "map must not be null");

    if (source.length != 16) {
      throw new IllegalArgumentException("Map must currently have height 16.");
    }

    TileType[][] copy = new TileType[source.length][];

    for (int y = 0; y < source.length; y++) {
      Objects.requireNonNull(source[y], "map row must not be null");

      if (source[y].length != 16) {
        throw new IllegalArgumentException("Map must currently have width 16 in every row.");
      }

      copy[y] = new TileType[source[y].length];
      for (int x = 0; x < source[y].length; x++) {
        if (source[y][x] == null) {
          throw new IllegalArgumentException("Map tile must not be null.");
        }
        copy[y][x] = source[y][x];
      }
    }

    return copy;
  }

  static List<Position> createDefaultSpawnPositions(int mapHeight, int mapWidth) {
    List<Position> spawns = new ArrayList<>(4);

    spawns.add(new Position(1.5, 1.5));
    spawns.add(new Position(mapWidth - 2.5, 1.5));
    spawns.add(new Position(1.5, mapHeight - 2.5));
    spawns.add(new Position(mapWidth - 2.5, mapHeight - 2.5));

    return spawns;
  }

  private static String requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
