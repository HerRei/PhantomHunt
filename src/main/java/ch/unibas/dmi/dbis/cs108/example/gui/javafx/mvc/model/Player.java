package ch.unibas.dmi.dbis.cs108.example.gui.javafx.mvc.model;

import javafx.beans.property.*;

public class Player {
    private final StringProperty name;
    private final IntegerProperty score;
    private final StringProperty skin; // Pfad zum Bild oder Enum-Name
    private final DoubleProperty xPosition; // Ranglisten-Platz oder X Koordinate
    private final DoubleProperty yPosition; // Ranglisten-Platz oder X Koordinate

    public Player(String name, String skin, int score, double x, double y) {
        this.name = new SimpleStringProperty(name);
        this.skin = new SimpleStringProperty(skin);
        this.score = new SimpleIntegerProperty(score);
        this.xPosition = new SimpleDoubleProperty(x);
        this.yPosition = new SimpleDoubleProperty(y);
    }

    // Getters
    public StringProperty nameProperty() { return name; }
    public IntegerProperty scoreProperty() { return score; }
    public StringProperty skinProperty() { return skin; }
    public DoubleProperty xPosition() { return xPosition; }
    public DoubleProperty yPosition() { return yPosition; }


    // Standard-Getter/Setter für die Werte selbst
    public void setScore(int newScore) { this.score.set(newScore); }
    
    public void setPosition(double x, double y) {
        this.xPosition.set(x);
        this.yPosition.set(y);
    }
}
