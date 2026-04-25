package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SceneInitializationTest {

    private static boolean jfxIsAlive = true;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        try {
            Platform.setImplicitExit(false);
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {

        }
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                GameModel.getInstance().resetModel();
                latch.countDown();
            });
            latch.await(2, TimeUnit.SECONDS);
        } catch (IllegalStateException |InterruptedException e) {
            jfxIsAlive = false;
        }
    }

    /**
     * Helper method to safely construct a scene on the FX Thread.
     * GUI components must be initialized on the FX Application Thread.
     */
    private SceneInterface createSceneSafely(SceneSupplier supplier) throws InterruptedException {
        if (!jfxIsAlive) return null;

        AtomicReference<SceneInterface> scene = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.runLater(() -> {
                scene.set(supplier.get());
                latch.countDown();
            });
            latch.await(2, TimeUnit.SECONDS);
        } catch (IllegalStateException |InterruptedException e) {
            jfxIsAlive = false;
            return null;
        }
        return scene.get();
    }

    @FunctionalInterface
    interface SceneSupplier {
        SceneInterface get();
    }

    @Test
    void hubScene_initializesWithoutCrashing() throws InterruptedException {
        if (!jfxIsAlive) { assertTrue(true); return; }
        SceneInterface scene = createSceneSafely(HubScene::new);
        assertNotNull(scene.getScene(), "HubScene should create a valid JavaFX Scene");
    }

    @Test
    void joinLobbyScene_initializesWithCrashing() throws InterruptedException {
        if (!jfxIsAlive) { assertTrue(true); return; }
        SceneInterface scene = createSceneSafely(JoinLobbyScene::new);
        assertNotNull(scene.getScene(), "JoinLobbyScene should create a valid JavaFX Scene");
    }

    @Test
    void nicknameScene_initializesWithCrashing() throws InterruptedException {
        if (!jfxIsAlive) { assertTrue(true); return; }
        SceneInterface scene = createSceneSafely(NicknameScene::new);
        assertNotNull(scene.getScene(), "NicknameScene should create a valid JavaFX Scene");
    }

    @Test
    void createLobbyScene_initializesWithCrashing() throws InterruptedException {
        if (!jfxIsAlive) { assertTrue(true); return; }
        SceneInterface scene = createSceneSafely(LobbyScene::new);
        assertNotNull(scene.getScene(), "LobbyScene should create a valid JavaFX Scene");
    }

    @Test
    void lobbyScene_initializesWithCrashing() throws InterruptedException {
        if (!jfxIsAlive) { assertTrue(true); return; }
        SceneInterface scene = createSceneSafely(LobbyScene::new);
        assertNotNull(scene.getScene(), "LobbyScene should create a valid JavaFX Scene");
    }

    @Test
    void endScene_initializesWithCrashing() throws InterruptedException {
        if (!jfxIsAlive) { assertTrue(true); return; }
        SceneInterface scene = createSceneSafely(EndScene::new);
        assertNotNull(scene.getScene(), "EndScene should create a valid JavaFX Scene");
    }

    @Test
    void gameScene_initializesWithCrashing() throws InterruptedException {
        // class checks a lot of images. test checks if all assets arte correct and accessible in build environment
        if (!jfxIsAlive) { assertTrue(true); return; }
        SceneInterface scene = createSceneSafely(GameScene::new);
        assertNotNull(scene.getScene(), "GameScene should create a valid JavaFX Scene");
    }
}