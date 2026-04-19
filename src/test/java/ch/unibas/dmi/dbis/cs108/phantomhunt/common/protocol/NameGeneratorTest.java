package ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameGeneratorTest {

    @Test
    void randomName_returnsValidString() {
        String name = NameGenerator.randomName();

        assertNotNull(name, "The name cannot be null");
        assertFalse(name.trim().isEmpty(), "The name cannot be empty");
        assertTrue(name.length() >= 3, "The name must be at least 3 characters");
    }

    @Test
    void randomName_loopForCoverage_coversAllRandomBranches() {
        // 100 Rounds for the two 50%-Chances
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            String name = NameGenerator.randomName();

            // We test if name stays valid over all rounds
            assertNotNull(name);
            assertFalse(name.trim().isEmpty());
        }
    }
}