package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import javafx.scene.Scene;

public interface SceneInterface {
    /** Get the stored JavaFX scene */
    Scene getScene();
    default <T extends SceneInterface> T as(Class<T> clazz) {
        //gets the object of the scene
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        return null;
    }
}