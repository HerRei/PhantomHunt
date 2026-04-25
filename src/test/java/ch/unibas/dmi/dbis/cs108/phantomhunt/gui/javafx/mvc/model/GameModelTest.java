package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class GameModelTest {

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        // required to test javafx properties, observableLists in headless environment
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await(2, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // toolkit already initialized
        }
    }

    @BeforeEach
    void setUp() {
        // reset singleton before each test
        Platform.runLater(() -> GameModel.getInstance().resetModel());
    }

    @Test
    void singleton_returnsSameInstance() {
        GameModel model1 = GameModel.getInstance();
        GameModel model2 = GameModel.getInstance();
        assertSame(model1, model2, "GameModel must be a Singleton");
    }

    @Test
    void updatePlayersFromServer_parsesPayloadCorrectly() throws InterruptedException {
        GameModel model = GameModel.getInstance();

        // payload format from GSU packet
        String payload = "2 45000 Alice:HUMAN:10.5:20.5:150;Bob:PHANTOM:5.0:5.0:300";

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            model.updatePlayersFromServer(payload);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for model update.");

        assertEquals(2, model.getRound().get(), "Round number should be parsed correctly.");
        assertEquals(45, model.getTime().get(), "Remaining time should be converted to seconds.");

        assertEquals(2, model.getPlayers().size(), "There should be 2 players extracted.");

        Player alice = model.getPlayers().stream().filter(p -> p.getName().equals("Alice")).findFirst().orElseThrow();

        assertEquals("HUMAN", alice.getRole(), "Role should be parsed correctly.");
        assertEquals(10.5, alice.getXPosition(), "X position should be parsed correctly.");
        assertEquals(150, alice.getScore(), "Score should be parsed correctly.");
    }

    @Test
    void getWinner_returnsPlayerWithHighestScore() throws InterruptedException {
        GameModel model = GameModel.getInstance();
        // setup 3 players with different scores
        String payload = "1 10000 A:HUMAN:0:0:100;B:PHANTOM:0:0:500;C:PHANTOM:0:0:50";

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            model.updatePlayersFromServer(payload);
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for model update.");

        String winner = model.getWinner();
        assertEquals("B", winner, "Player B should be identified as the winner with 500 points.");
    }

    @Test
    void updateLobbyList_updatesObservableLists() {
        GameModel model = GameModel.getInstance();

        List<String> running = List.of("LobbyA");
        List<String> waiting = List.of("LobbyB", "LobbyC");

        model.updateLobbyList(running, waiting);

        assertEquals(1, model.getRunningLobbies().size(), "Running lobbies list size should match.");
        assertEquals(2, model.getAvailableLobbies().size(), "Waiting lobbies list size should match.");
        assertTrue(model.getAvailableLobbies().contains("LobbyC"), "Specific lobby should be present");
    }
}