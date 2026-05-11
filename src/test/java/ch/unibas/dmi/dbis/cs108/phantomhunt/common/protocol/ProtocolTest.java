package ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {

    // --- DECODE TESTS ---

    @Test
    void decode_nullOrBlankString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->  Protocol.decode(null));
        assertThrows(IllegalArgumentException.class, () ->  Protocol.decode(""));
        assertThrows(IllegalArgumentException.class, () ->  Protocol.decode(" "));
    }

    @Test
    void decode_validCommandWithArgs_returnsPacket() {
        Packet p = Protocol.decode("WHISPER user1 hallo");
        assertEquals(Command.WHISPER, p.cmd());
        assertEquals("user1 hallo", p.args().get(0));
    }

    @Test
    void decode_unknownCommand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->  Protocol.decode("UNKNOWN_CMD arguments"));
    }

    @Test
    void decode_validCommandNoArgs_returnsPacket() {
        Packet p = Protocol.decode("PING");
        assertEquals(Command.PING, p.cmd());
        assertTrue(p.args().isEmpty());
    }

    // --- ENCODE TESTS ---
    @Test
    void encode_nullPacket_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->  Protocol.encode(null));
    }

    @Test
    void encode_nullCommand_throwsIllegalArgumentException() {
        Packet nullCmdPacket = new Packet(null, null);
        assertThrows(IllegalArgumentException.class, () ->  Protocol.encode(nullCmdPacket));
    }

    @Test
    void encode_validPacketWithArgs_returnsString() {
        Packet p = Packet.of(Command.WHISPER, "user1", "hello");
        String encoded = Protocol.encode(p);
        assertEquals("WHISPER user1 hello", encoded);
    }

    @Test
    void encode_validPacketNoArgs_returnsString() {
        Packet p = new Packet(Command.PING, null);
        String encoded = Protocol.encode(p);
        assertEquals("PING", encoded);
    }

    @Test
    void protocol_supportsAllCommands() {
        for (Command command : Command.values()) {
            Packet p = new Packet(command, List.of("testArg"));
            String encoded = Protocol.encode(p);
            Packet decoded = Protocol.decode(encoded);

            assertEquals(command, decoded.cmd(), "Command " + command + " was not processed correctly");
        }
    }
}