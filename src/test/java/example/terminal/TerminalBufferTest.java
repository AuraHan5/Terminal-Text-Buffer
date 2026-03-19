package example.terminal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TerminalBufferTest {

    // Verifies initial dimensions, cursor origin, and blank screen contents.
    @Test
    void initializesWithBlankScreenAndZeroCursor() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        assertEquals(5, buffer.getWidth());
        assertEquals(3, buffer.getHeight());
        assertEquals(0, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
        assertEquals("     \n     \n     ", buffer.getScreenContentAsString());
    }
}