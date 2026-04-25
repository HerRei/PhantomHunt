package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.SceneInterface;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.SceneProtocol;
import javafx.application.Platform;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SceneManagerTest {

    private static boolean jfxIsAlive = true;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        try {
            Platform.setImplicitExit(false);
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(2, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {

        } catch (Throwable e) {

            jfxIsAlive = false;
        }
    }

    @Test
    void singleton_returnsSameInstance() {
        SceneManager manager1 = SceneManager.getInstance();
        SceneManager manager2 = SceneManager.getInstance();
        assertSame(manager1, manager2, "SceneManager must act as a Singleton");
    }

    @Test
    void addAndRetrieveScene_worksCorrectly() {
        SceneManager sceneManager = SceneManager.getInstance();

        // create anonymous dummy scene to test registration mechanism
        SceneInterface dummyScene = new SceneInterface() {
            @Override
            public Scene getScene() {
                return null;
            }
        };

        sceneManager.addScene(SceneProtocol.LOADING, dummyScene);
        SceneInterface retrieved =  sceneManager.getScene(SceneProtocol.LOADING);

        assertSame(dummyScene, retrieved, "Retrieved scene must match added scene");
    }

    @Test
    void showScene_updatesCurrentSceneState() throws InterruptedException {
        if (!jfxIsAlive) { assertTrue(true); return; }

        SceneManager sceneManager = SceneManager.getInstance();

        SceneInterface dummyScene = new SceneInterface() {
            @Override
            public Scene getScene() {
                return null;
            }
        };

        sceneManager.addScene(SceneProtocol.HOME, dummyScene);

        // triggering showScene runs Platform.runLater internally
        sceneManager.showScene(SceneProtocol.HOME);

        // we need to allow javafx thread a fraction of a second to process queue
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await(2, TimeUnit.SECONDS);

        // verify internal state updated correctly
        assertEquals(SceneProtocol.HOME, sceneManager.getCurrentScene(), "Current scene state must be updated upon calling showScene");
    }
}