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
    void randomName_followsStructuralRules() {
        String name = NameGenerator.randomName();

        // validation
        assertNotNull(name, "Name cannot be null");
        assertFalse(name.contains(" "), "Name cannot include empty-spaces");

        // checking components
        // since lists in NameGenerator are private, we check them logically
        assertTrue(name.length() >= 4, "Name is to short for rules");
        int iterations = 100;

        // consistency
        for (int i = 0; i < 50; i++) {
            String n = NameGenerator.randomName();
            assertTrue(n.matches("^[A-Z][a-z]+$"), "Name '" + n + "' is invalid");
        }
    }
}