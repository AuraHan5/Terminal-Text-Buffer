package example.terminal;

import org.junit.jupiter.api.Test;

import src.main.java.example.terminal.TerminalBuffer;

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

    // Verifies cursor positioning and relative moves are clamped within buffer bounds.
    @Test
    void cursorSetAndMovesAreClampedToBounds() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.setCursorPosition(100, 100);
        assertEquals(4, buffer.getCursorColumn());
        assertEquals(2, buffer.getCursorRow());

        buffer.moveCursorLeft(2);
        buffer.moveCursorUp(1);
        assertEquals(2, buffer.getCursorColumn());
        assertEquals(1, buffer.getCursorRow());

        buffer.moveCursorLeft(100);
        buffer.moveCursorUp(100);
        assertEquals(0, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
    }

    // Verifies plain writes replace existing cells in place without shifting characters.
    @Test
    void writeTextOverwritesCurrentLineWithoutShifting() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.writeText("abcde");
        buffer.setCursorPosition(1, 0);
        buffer.writeText("XY");

        assertEquals("aXYde", buffer.getLineAsString(0));
        assertEquals(3, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
    }
}