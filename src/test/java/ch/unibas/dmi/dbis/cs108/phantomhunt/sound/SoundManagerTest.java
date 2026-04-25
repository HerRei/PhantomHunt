package ch.unibas.dmi.dbis.cs108.phantomhunt.sound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundManagerTest {

    @Test
    void soundManager_singletonInstance_isNotNull() {
        SoundManager manager1 = SoundManager.getInstance();
        SoundManager manager2 = SoundManager.getInstance();

        assertNotNull(manager1);
        assertSame(manager1, manager2, "SoundManager must be Singleton");
    }

    @Test
    void soundManager_methods_doNotCrashInHeadlessCI() {
        SoundManager manager = SoundManager.getInstance();

        // we test to ensure that no exception is propagated upward
        assertDoesNotThrow(manager::initialize, "Initialize cannot crash");

        // methods call and check ensureInitialized, so play, stop, shutdown cannot crash without soundcard
        assertDoesNotThrow(() -> manager.play(SoundEffect.COIN_UP));
        assertDoesNotThrow(() -> manager.stop(SoundEffect.COIN_UP));
        assertDoesNotThrow(manager::stopAll);
        assertDoesNotThrow(manager::shutdown, "Shutdown cannot crash");
    }
}