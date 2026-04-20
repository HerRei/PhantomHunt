package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {
  private final StringProperty name;
  private final IntegerProperty score;
  private final StringProperty skin;
  private final DoubleProperty xPosition;
  private final DoubleProperty yPosition;
  private final IntegerProperty playerNumber;

  /**
   * Creates a new Player instance.
   *
   * @param name The player's nickname
   * @param skin The path to the image or enum name representing the player's appearance
   * @param score The initial score
   * @param x The X coordinate
   * @param y The Y coordinate
   * @param playerNumber The player's number
   */
  public Player(String name, String skin, int score, double x, double y, int playerNumber) {
    this.name = new SimpleStringProperty(name);
    this.skin = new SimpleStringProperty(skin);
    this.score = new SimpleIntegerProperty(score);
    this.xPosition = new SimpleDoubleProperty(x);
    this.yPosition = new SimpleDoubleProperty(y);
    this.playerNumber = new SimpleIntegerProperty(playerNumber);
    
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
}
