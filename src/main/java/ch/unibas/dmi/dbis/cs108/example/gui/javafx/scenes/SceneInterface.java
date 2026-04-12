package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scenes;

import javafx.scene.Scene;

/**
 * Base interface for all scenes managed by the SceneManager.
 */
public interface SceneInterface {
    /**
     * Get the stored JavaFX scene.
     * @return The active JavaFX Scene object.
     */
    Scene getScene();

    /**
     * Casts the scene to a specific implementation class if applicable.
     *
     * @param clazz The target class type.
     * @param <T> The type parameter.
     * @return The casted scene object, or null if incompatible.
     */
    default <T extends SceneInterface> T as(Class<T> clazz) {
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        return null;
    }
}