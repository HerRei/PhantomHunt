package ch.unibas.dmi.dbis.cs108.phantomhunt.server.game;

import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.TileType;

public final class MapCollision {
    private static final double MOVEMENT_COLLISION_RADIUS_SCALE = 0.4;
    private static final double MIN_MOVEMENT_COLLISION_RADIUS = 3.0;

    private MapCollision() {}

    static double movementRadius(double playerRadius) {
        return Math.max(MIN_MOVEMENT_COLLISION_RADIUS, playerRadius * MOVEMENT_COLLISION_RADIUS_SCALE);
    }

    public static boolean collidesWithWall(TileType[][] map, double x, double y, double radius) {
        int height = map.length;
        int width = map[0].length;

        // Points on the circle, for checking collision
        double[] angles = {0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, Math.PI, 5 * Math.PI / 4, 3 * Math.PI / 2, 7 * Math.PI / 4};

        for (double angle : angles) {
            double checkX = x + radius * Math.cos(angle);
            double checkY = y + radius * Math.sin(angle);
            if (isWallAt(map, width, height, checkX, checkY)) {
                return true;
            }
        }
        return isWallAt(map, width, height, x, y); // Also check center
    }

    private static boolean isWallAt(TileType[][] map, int width, int height, double x, double y) {
        int tileX = (int) Math.floor(x);
        int tileY = (int) Math.floor(y);
        return tileX < 0
                || tileX >= width
                || tileY < 0
                || tileY >= height
                || map[tileY][tileX] == TileType.WALL;
    }
}
