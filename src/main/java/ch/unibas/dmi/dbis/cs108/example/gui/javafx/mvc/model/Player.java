package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model;

import javafx.beans.property.*;

public class Player {
    private final StringProperty name;
    private final IntegerProperty score;
    private final StringProperty skin; // Pfad zum Bild oder Enum-Name
    private final IntegerProperty xPosition; // Ranglisten-Platz oder X Koordinate
    private final IntegerProperty yPosition; // Ranglisten-Platz oder X Koordinate

    /**
     * Crates a new Player instance.
     *
     * @param name The player's nickname
     * @param skin The patch to the image or enum name representing the player's appearance
     * @param score The initial score
     * @param x The X coordinate (or leaderboard rank)
     * @param y The Y coordinate
     */
    public Player(String name, String skin, int score, int x, int y) {
        this.name = new SimpleStringProperty(name);
        this.skin = new SimpleStringProperty(skin);
        this.score = new SimpleIntegerProperty(score);
        this.xPosition = new SimpleIntegerProperty(x);
        this.yPosition = new SimpleIntegerProperty(y);
    }

    // --- Name ---
    public String getName() {return name.get();}
    public void setName(String value) { this.name.set(value); }
    public StringProperty nameProperty() { return name; }

    // --- Skin ---
    public String getSkin() { return skin.get(); }
    public void setSkin(String value) { this.skin.set(value); }
    public StringProperty skinProperty() { return skin; }

    // --- Score ---
    public int getScore() { return score.get(); }
    public void setScore(int value) {score.set(value);}
    public IntegerProperty scoreProperty() { return score; }

    // --- x-Position ---
    public int getXPosition() { return xPosition.get(); }
    public void setXPosition(int value) {xPosition.set(value);}
    public IntegerProperty xPosition() { return xPosition; }

    // --- y-Position ---
    public int getYPosition() { return yPosition.get(); }
    public void setYPosition(int value) {yPosition.set(value);}
    public IntegerProperty yPosition() { return yPosition; }
}