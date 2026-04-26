package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void player_constructorAndProperties_workCorrectly() {
        // create test player
        Player p = new Player("Alice", "HUMAN", "HUMAN", 100, 50.0, 60.0, 1, "front");

        assertEquals("Alice", p.getName(), "Name should match the constructor argument.");
        assertEquals("HUMAN", p.getSkin(),"Skin should match the constructor argument.");
        assertEquals(100, p.getScore(),"Score should match the constructor argument.");
        assertEquals(50.0, p.getXPosition(),"X position should match the constructor argument.");
        assertEquals(60.0, p.getYPosition(),"Y position should match the constructor argument.");
        assertEquals(1, p.getPlayerNumber(), "Player number should match the constructor argument.");
        assertEquals("front", p.getPlayerDirection(), "Direction should match the constructor argument.");
        assertFalse(p.getMoved(), "Player should not be moving initially.");

        p.setMoved(true);
        assertTrue(p.didMove(), "didMove should return true after setting it.");

        p.setPosition(10.0, 20.0);
        assertEquals(10.0, p.getXPosition(), "X position should update.");
        assertEquals(20.0, p.getYPosition(), "Y position should update.");
    }
}