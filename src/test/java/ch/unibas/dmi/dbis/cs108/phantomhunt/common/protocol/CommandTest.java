package ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    @Test
    void valueOf_validString_returnsEnum(){
        assertEquals(Command.PING, Command.valueOf("PING"));
        assertEquals(Command.WHISPER, Command.valueOf("WHISPER"));
        assertEquals(Command.WISDOM, Command.valueOf("WISDOM"));
        assertEquals(Command.GAME_SETTINGS, Command.valueOf("GAME_SETTINGS"));
    }

    @Test
    void values_containsAllCommands(){
        Command[] commands = Command.values();
        assertNotNull(commands);
        assertTrue(commands.length > 0, "The Enum should contain Commands");
    }

    @Test
    void valueOf_invalidString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, ()-> Command.valueOf("WRONG_COMMAND"));
        assertThrows(IllegalArgumentException.class, ()-> Command.valueOf(""));
        assertThrows(IllegalArgumentException.class, ()-> Command.valueOf(" "));
    }

    @Test
    void valueOf_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, ()-> Command.valueOf(null));
    }
}
