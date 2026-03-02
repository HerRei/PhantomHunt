package ch.unibas.dmi.dbis.cs108.example;

import ch.unibas.dmi.dbis.cs108.example.server.*;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



/**
 * The type Server tests.
 */
public class ServerTests {

    /**
     * helper function to make testing easier
     */
    private static String extractArgs(Packet packet) {
        if (packet.args() == null || packet.args().isEmpty()) {
            return "";
        }
        return String.join(" ", packet.args());
    }

    /**
     * Test simple beacon command.
     */
@Test
    void testSimpleBeaconCommand() {

        Packet packet = Protocol.decode("BEACON");

        assertEquals(Command.BEACON, packet.cmd(), "BEACON test");
    }

    /**
     * Test checkin captures username.
     */
@Test
    void testCheckinCapturesUsername() {

        String rawInput = "CHECKIN Jens67";
        Packet packet = Protocol.decode(rawInput);

        assertEquals(Command.CHECKIN, packet.cmd());
        assertTrue(extractArgs(packet).contains("Jens67"), "Username correctness");
    }

    /**
     * Test unicom preserves spaces.
     */
@Test
    void testUnicomPreservesSpaces() {

        String messageWithSpaces = "goodbye, mars! and jens67";

        Packet packet = Protocol.decode("UNICOM " + messageWithSpaces);

        assertEquals(Command.UNICOM, packet.cmd());
        assertEquals(messageWithSpaces, extractArgs(packet), "parsing must not change the message");
    }

    /**
     * Test round trip consistency.
     */
@Test
    void testRoundTripConsistency() {
        String inputLine = "UNICOM hi there";
        Packet parsedPacket = Protocol.decode(inputLine);
        String reEncodedLine = Protocol.encode(parsedPacket);
        Packet reParsedPacket = Protocol.decode(reEncodedLine);

        // Compare
        assertEquals(parsedPacket.cmd(), reParsedPacket.cmd(), "Encode -> Decode -> Encode should be the same message");
        assertEquals(extractArgs(parsedPacket), extractArgs(reParsedPacket), "Encode -> Decode -> Encode should be the same message");
    }

    /**
     * Test invalid command throws exception.
     */
@Test
    void testInvalidCommandThrowsException() {

        String garbageInput = "Jens67";

        assertThrows(RuntimeException.class, () -> {
            Protocol.decode(garbageInput);
        }, "We expect a exception with unknown cmd");
    }
}