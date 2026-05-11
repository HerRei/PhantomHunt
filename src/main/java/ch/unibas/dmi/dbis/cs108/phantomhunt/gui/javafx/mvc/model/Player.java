package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model;

import javafx.beans.property.*;

/** Represents a single player with position, role, score, sprite, and color data. */
public class Player {

  /** Hex colors assigned by slot index (0 = Blue, 1 = Green, 2 = Red, 3 = Purple). */
  public static final String[] PLAYER_COLORS = {"#5B9BF5", "#4CAF50", "#F44336", "#9C27B0"};

  /** Human-readable color names matching {@link #PLAYER_COLORS}. */
  public static final String[] PLAYER_COLOR_NAMES = {"Blue", "Green", "Red", "Purple"};

  private final StringProperty name;
  private final IntegerProperty score;
  private final StringProperty skin;
  private final StringProperty role;
  private final DoubleProperty xPosition;
  private final DoubleProperty yPosition;
  private final IntegerProperty playerNumber;
  private final StringProperty playerDirection;
  private boolean moved;

  /** Hex color for this player's slot (e.g. {@code "#5B9BF5"}). */
  private final String color;

  /** Human-readable color name for this player's slot (e.g. {@code "Blue"}). */
  private final String colorName;

  /**
   * Creates a new Player instance.
   *
   * @param name            the player's nickname
   * @param skin            the sprite skin key
   * @param role            the server role string (e.g. "HUMAN", "PHANTOM")
   * @param score           the initial score
   * @param x               the initial x coordinate
   * @param y               the initial y coordinate
   * @param playerNumber    the 1-based slot index used for sprite and color selection
   * @param playerDirection the initial facing direction
   */
  public Player(
      String name,
      String skin,
      String role,
      int score,
      double x,
      double y,
      int playerNumber,
      String playerDirection) {
    this.name = new SimpleStringProperty(name);
    this.skin = new SimpleStringProperty(skin);
    this.score = new SimpleIntegerProperty(score);
    this.xPosition = new SimpleDoubleProperty(x);
    this.yPosition = new SimpleDoubleProperty(y);
    this.playerNumber = new SimpleIntegerProperty(playerNumber);
    this.playerDirection = new SimpleStringProperty(playerDirection);
    this.role = new SimpleStringProperty(role);
    this.moved = false;

    // Assign color by slot; clamp to avoid AIOBE with >4 players
    int idx = Math.max(0, Math.min(playerNumber - 1, PLAYER_COLORS.length - 1));
    this.color = PLAYER_COLORS[idx];
    this.colorName = PLAYER_COLOR_NAMES[idx];
  }

  public void setMoved(Boolean didMove) {
    moved = didMove;
  }

  public boolean didMove() {
    return moved;
  }

  public boolean getMoved() {
    return moved;
  }

  public String getRole() {
    return role.get();
  }

  public void setRole(String value) {
    role.set(value);
  }

  public String getName() {
    return name.get();
  }

  public void setName(String value) {
    name.set(value);
  }

  public StringProperty nameProperty() {
    return name;
  }

  public String getSkin() {
    return skin.get();
  }

  public void setSkin(String value) {
    skin.set(value);
  }

  public StringProperty skinProperty() {
    return skin;
  }

  public int getScore() {
    return score.get();
  }

  public void setScore(int value) {
    score.set(value);
  }

  public IntegerProperty scoreProperty() {
    return score;
  }

  public double getXPosition() {
    return xPosition.get();
  }

  public void setXPosition(double value) {
    xPosition.set(value);
  }

  public DoubleProperty xPosition() {
    return xPosition;
  }

  public double getYPosition() {
    return yPosition.get();
  }

  public void setYPosition(double value) {
    yPosition.set(value);
  }

  public DoubleProperty yPosition() {
    return yPosition;
  }

  public void setPosition(double x, double y) {
    xPosition.set(x);
    yPosition.set(y);
  }

  public int getPlayerNumber() {
    return playerNumber.get();
  }

  public void setPlayerNumber(int value) {
    playerNumber.set(value);
  }

  public IntegerProperty playerNumberProperty() {
    return playerNumber;
  }

  public String getPlayerDirection() {
    return playerDirection.get();
  }

  public void setPlayerDirection(String value) {
    playerDirection.set(value);
  }

  public StringProperty playerDirectionProperty() {
    return playerDirection;
  }

  /** @return hex color string for this player's slot */
  public String getColor() {
    return color;
  }

  /** @return human-readable color name for this player's slot */
  public String getColorName() {
    return colorName;
  }
}
