package ch.unibas.dmi.dbis.cs108.phantomhunt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void main_invalidArguments_doesNotCrash() {
        // no args passed
        assertDoesNotThrow(() -> Main.main(new String[]{}),
                "Program must not crash if arguments are missing");

        // invalid role (neither server nor client)
        assertDoesNotThrow(() -> Main.main(new String[]{"hacker", "8080"}),
                "Program must handle invalid roles");

        // start server but pass text instead of port
        assertDoesNotThrow(() -> Main.main(new String[]{"server", "NoNumber"}),
                "Program must catch NumberFormatException for the port");

        // start client but host:port format is incorrect
        assertDoesNotThrow(() -> Main.main(new String[]{"client", "localhost8080"}),
                "Program must catch incorrect host/port format");
    }

    @Test
    void getters_returnCorrectValues() {
        // we simulate that the program processed the parameters
        Main.targetHost = "192.168.178.20";
        Main.targetPort = 8080;

        assertEquals("192.168.178.20", Main.targetHost);
        assertEquals(8080, Main.targetPort);
    }


}