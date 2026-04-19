package ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacketTest {

    @Test
    void constructor_withNullArgs_createsEmptyList() {
        Packet p = new Packet(Command.PING, null);
        assertEquals(0, p.argc());
        assertTrue(p.args().isEmpty());
    }

    @Test
    void text_withArgs_returnsFirstArgument() {
        Packet p = Packet.of(Command.UNICOM, "Hello World");
        assertEquals("Hello World", p.text());
    }

    @Test
    void text_emptyArgs_throwsIllegalStateException() {
        Packet p = new Packet(Command.PING, null);
        assertThrows(IllegalStateException.class, p::text);
    }

    @Test
    void toString_formatsCorrectly() {
        Packet p = Packet.of(Command.PING, "test");
        String result = p.toString();
        assertTrue(result.contains("test"));
        assertTrue(result.contains("PING"));
    }
}