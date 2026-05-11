package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class SceneInitializationTest {

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
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            GameModel.getInstance().resetModel();
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
    }

    /**
     * Helper method to safely construct a scene on the FX Thread.
     * GUI components must be initialized on the FX Application Thread.
     */
    private SceneInterface createSceneSafely(SceneSupplier supplier) throws InterruptedException {
        AtomicReference<SceneInterface> scene = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            scene.set(supplier.get());
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
        return scene.get();
    }

    @FunctionalInterface
    interface SceneSupplier {
        SceneInterface get();
    }

    @Test
    void hubScene_initializesWithoutCrashing() throws InterruptedException {
        SceneInterface scene = createSceneSafely(HubScene::new);
        assertNotNull(scene.getScene(), "HubScene should create a valid JavaFX Scene");
    }

    @Test
    void joinLobbyScene_initializesWithoutCrashing() throws InterruptedException {
        SceneInterface scene = createSceneSafely(JoinLobbyScene::new);
        assertNotNull(scene.getScene(), "JoinLobbyScene should create a valid JavaFX Scene");
    }

    @Test
    void nicknameScene_initializesWithoutCrashing() throws InterruptedException {
        SceneInterface scene = createSceneSafely(NicknameScene::new);
        assertNotNull(scene.getScene(), "NicknameScene should create a valid JavaFX Scene");
    }

    @Test
    void createLobbyScene_initializesWithoutCrashing() throws InterruptedException {
        SceneInterface scene = createSceneSafely(LobbyScene::new);
        assertNotNull(scene.getScene(), "LobbyScene should create a valid JavaFX Scene");
    }

    @Test
    void lobbyScene_initializesWithoutCrashing() throws InterruptedException {
        SceneInterface scene = createSceneSafely(LobbyScene::new);
        assertNotNull(scene.getScene(), "LobbyScene should create a valid JavaFX Scene");
    }

    @Test
    void gameSettingsScene_initializesWithoutCrashing() throws InterruptedException {
        SceneInterface scene = createSceneSafely(GameSettingsScene::new);
        assertNotNull(scene.getScene(), "GameSettingsScene should create a valid JavaFX Scene");
    }

    @Test
    void endScene_initializesWithCrashing() throws InterruptedException {
        SceneInterface scene = createSceneSafely(EndScene::new);
        assertNotNull(scene.getScene(), "EndScene should create a valid JavaFX Scene");
    }

    @Test
    void gameScene_initializesWithoutCrashing() throws InterruptedException {
        // class checks a lot of images. test checks if all assets arte correct and accessible in build environment
        SceneInterface scene = createSceneSafely(GameScene::new);
        assertNotNull(scene.getScene(), "GameScene should create a valid JavaFX Scene");
    }
}
