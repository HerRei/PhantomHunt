package ch.unibas.dmi.dbis.cs108.phantomhunt.sound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundEffectTest {

    @Test
    void soundEffect_propertiesAreSetCorrectly() {
        // testing of COIN_UP as Example
        SoundEffect coin = SoundEffect.COIN_UP;

        assertNotNull(coin.resourcePath(), "Ressource-Path cannot be null");
        assertTrue(coin.resourcePath().contains("coin-up.wav"), "Path must include file-name");
        assertFalse(coin.loops(), "Coin-Sound cannot loop");
        assertEquals(0.80f, coin.gain(), 0.01f, "Gain must be 0.80");
    }

    @Test
    void enums_haveCorrectValues() {
        // check if enums exist
        assertNotNull(SoundEffect.valueOf("MAN_SCREAM"));
        assertNotNull(SoundEffect.valueOf("UNIVERSFIELD_SLIME_IMPACT"));
        assertTrue(SoundEffect.values().length > 0);
    }

    @Test
    void soundEffect_resourcesExistOnClasspath() {
        for (SoundEffect soundEffect : SoundEffect.values()) {
            assertNotNull(
                    SoundEffect.class.getResource(soundEffect.resourcePath()),
                    "Missing resource for " + soundEffect.name() + ": " + soundEffect.resourcePath());
        }
    }
}
