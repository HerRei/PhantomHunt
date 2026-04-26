package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Ability {

    private final DoubleProperty xPosition = new SimpleDoubleProperty();
    private final DoubleProperty yPosition = new SimpleDoubleProperty();

    public Ability(double x, double y) {
        this.xPosition.set(x);
        this.yPosition.set(y);
    }

    public double getX() {
        return xPosition.get();
    }

    public DoubleProperty xPosition() {
        return xPosition;
    }

    public double getY() {
        return yPosition.get();
    }

    public DoubleProperty yPosition() {
        return yPosition;
    }
}
