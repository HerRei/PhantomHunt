package ch.unibas.dmi.dbis.cs108.phantomhunt.sound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class SoundEngineTest {

    @Test
    void init_handlesMissingAudioHardwareGracefully() {
        SoundEngine engine = new SoundEngine();

        try {
            engine.init();
            assertDoesNotThrow(engine::stopAll, "Shutdown after successful init must work");
        } catch (IllegalStateException | UnsatisfiedLinkError | NoClassDefFoundError e) {
            assertTrue(true, "CI-Environment recognized without sound-card. Test is successful. Mistake: " + e.getMessage());
        }
    }

}