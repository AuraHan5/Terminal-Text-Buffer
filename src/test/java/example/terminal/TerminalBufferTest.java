package example.terminal;

import org.junit.jupiter.api.Test;

import src.main.java.example.terminal.CellAttributes;
import src.main.java.example.terminal.TerminalBuffer;
import src.main.java.example.terminal.TerminalColor;

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

    // Verifies insertion shifts content right and propagates overflow into following lines.
    @Test
    void insertTextShiftsAndWrapsAcrossLines() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        buffer.writeText("abcd");
        buffer.setCursorPosition(0, 1);
        buffer.writeText("efgh");

        buffer.setCursorPosition(2, 0);
        buffer.insertText("Z");

        assertEquals("abZc", buffer.getLineAsString(0));
        assertEquals("defg", buffer.getLineAsString(1));
        assertEquals(3, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
    }

    // Verifies inserting at the last column advances cursor to the next line start.
    @Test
    void insertTextAtLineEndWrapsCursorToNextLine() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 10);
        buffer.setCursorPosition(2, 0);

        buffer.insertText("Q");

        assertEquals("  Q", buffer.getLineAsString(0));
        assertEquals(0, buffer.getCursorColumn());
        assertEquals(1, buffer.getCursorRow());
    }

    // Verifies filling a line writes characters with the currently active attributes.
    @Test
    void fillLineUsesCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        CellAttributes attrs = new CellAttributes(TerminalColor.RED, TerminalColor.BLUE, true, false, true);
        buffer.setCurrentAttributes(attrs);

        buffer.fillLine(1, "*");

        assertEquals("****", buffer.getLineAsString(1));
        assertEquals(attrs, buffer.getAttributesAt(0, 1));
        assertEquals(attrs, buffer.getAttributesAt(3, 1));
    }
}