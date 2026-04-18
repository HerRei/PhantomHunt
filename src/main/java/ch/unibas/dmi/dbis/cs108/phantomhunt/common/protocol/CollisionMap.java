package ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol;

public class CollisionMap {
  // 19 x 20
  public static final String[] ColGrid = {
      "        O        ",
      " OO OOO O OOO OO ",
      "                 ",
      " OO O OOOOO O OO ",
      "    O   O   O    ",
      "OOO OOO O OOO OOO",
      "    O       O    ",
      " OO   OO OO   OO ",
      "  O O OO OO O O  ",
      "O O O       O O O",
      "    O OO OO O    ",
      " OO           OO ",
      "  O OOO O OOO O  ",
      "O O O       O O O",
      "    O OOOOO O    ",
      " OOOO   O   OOOO ",
      " OOOOOO O OOOOOO ",
      "                 "
  };

  // Helper method to check collisions
  public static boolean isWalkable(int x, int y) {
    // Prevent OutOfBounds errors
    if (y < 0 || y >= ColGrid.length || x < 0 || x >= ColGrid[0].length()) {
      return false;
    }
    // A tile is only walkable if it is a space ' '
    return ColGrid[y].charAt(x) == ' ';
  }
}
