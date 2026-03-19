package example.terminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Core terminal text buffer that stores screen lines and immutable scrollback history.
 */
public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int scrollbackMaxSize;

    private final List<List<Cell>> screen;
    private final List<List<Cell>> scrollback;

    private int cursorColumn;
    private int cursorRow;
    private CellAttributes currentAttributes;

    public TerminalBuffer(int width, int height, int scrollbackMaxSize) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }
        if (scrollbackMaxSize < 0) {
            throw new IllegalArgumentException("scrollbackMaxSize must be >= 0");
        }

        this.width = width;
        this.height = height;
        this.scrollbackMaxSize = scrollbackMaxSize;
        this.cursorColumn = 0;
        this.cursorRow = 0;
        this.currentAttributes = CellAttributes.DEFAULT;

        this.screen = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            this.screen.add(createBlankLine());
        }
        this.scrollback = new ArrayList<>();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScrollbackMaxSize() {
        return scrollbackMaxSize;
    }

    public int getScrollbackSize() {
        return scrollback.size();
    }

    public int getCursorColumn() {
        return cursorColumn;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public void setCursorPosition(int column, int row) {
        this.cursorColumn = clamp(column, 0, width - 1);
        this.cursorRow = clamp(row, 0, height - 1);
    }

    public void moveCursorUp(int cells) {
        moveCursorVertical(-cells);
    }

    public void moveCursorDown(int cells) {
        moveCursorVertical(cells);
    }

    public void moveCursorLeft(int cells) {
        moveCursorHorizontal(-cells);
    }

    public void moveCursorRight(int cells) {
        moveCursorHorizontal(cells);
    }

    public CellAttributes getCurrentAttributes() {
        return currentAttributes;
    }

    public void setCurrentAttributes(CellAttributes attributes) {
        this.currentAttributes = Objects.requireNonNull(attributes, "attributes must not be null");
    }

    public void setCurrentAttributes(
            TerminalColor foreground,
            TerminalColor background,
            boolean bold,
            boolean italic,
            boolean underline
    ) {
        this.currentAttributes = new CellAttributes(foreground, background, bold, italic, underline);
    }

    /**
     * Overwrites text from the cursor on the current line without shifting existing cells.
     */
    public void writeText(String text) {
        Objects.requireNonNull(text, "text must not be null");
        if (text.isEmpty()) {
            return;
        }

        int column = cursorColumn;
        int lastWrittenColumn = cursorColumn;

        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);

            if (codePoint == '\n') {
                cursorColumn = 0;
                cursorRow = Math.min(cursorRow + 1, height - 1);
                column = cursorColumn;
                lastWrittenColumn = column;
                continue;
            }

            if (column >= width) {
                break;
            }

            setCell(cursorRow, column, Cell.fromCodePoint(codePoint, currentAttributes));
            lastWrittenColumn = column;
            column++;
        }

        cursorColumn = clamp(lastWrittenColumn + 1, 0, width - 1);
    }

    /**
     * Inserts text at the cursor, shifting content right and wrapping overflow to following lines.
     */
    public void insertText(String text) {
        Objects.requireNonNull(text, "text must not be null");
        if (text.isEmpty()) {
            return;
        }

        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);

            if (codePoint == '\n') {
                cursorColumn = 0;
                advanceToNextLine();
                continue;
            }

            insertCodePointAtCursor(codePoint);
        }
    }

    public void fillLine(int screenRow, String character) {
        if (screenRow < 0 || screenRow >= height) {
            throw new IndexOutOfBoundsException("screenRow out of bounds");
        }

        String fill = normalizeFillCharacter(character);
        List<Cell> line = screen.get(screenRow);
        for (int c = 0; c < width; c++) {
            line.set(c, new Cell(fill, currentAttributes));
        }
    }

    /**
     * Accesses all buffer content using global row indexing:
     * [0..scrollbackSize-1] => scrollback, [scrollbackSize..] => screen.
     */
    public String getCharacterAt(int column, int globalRow) {
        Cell cell = getCellAt(column, globalRow);
        return cell.character();
    }

    public CellAttributes getAttributesAt(int column, int globalRow) {
        Cell cell = getCellAt(column, globalRow);
        return cell.attributes();
    }

    public String getLineAsString(int globalRow) {
        List<Cell> line = getLineByGlobalRow(globalRow);
        return lineToString(line);
    }

    public String getScreenContentAsString() {
        return joinLines(screen);
    }

    public String getBufferContentAsString() {
        List<List<Cell>> combined = new ArrayList<>(scrollback.size() + screen.size());
        combined.addAll(scrollback);
        combined.addAll(screen);
        return joinLines(combined);
    }

    public List<String> getScreenLines() {
        List<String> lines = new ArrayList<>(height);
        for (List<Cell> line : screen) {
            lines.add(lineToString(line));
        }
        return Collections.unmodifiableList(lines);
    }

    public List<String> getScrollbackLines() {
        List<String> lines = new ArrayList<>(scrollback.size());
        for (List<Cell> line : scrollback) {
            lines.add(lineToString(line));
        }
        return Collections.unmodifiableList(lines);
    }

    private Cell getCellAt(int column, int globalRow) {
        if (column < 0 || column >= width) {
            throw new IndexOutOfBoundsException("column out of bounds");
        }

        List<Cell> line = getLineByGlobalRow(globalRow);
        return line.get(column);
    }

    private List<Cell> getLineByGlobalRow(int globalRow) {
        int totalLines = scrollback.size() + screen.size();
        if (globalRow < 0 || globalRow >= totalLines) {
            throw new IndexOutOfBoundsException("globalRow out of bounds");
        }

        if (globalRow < scrollback.size()) {
            return scrollback.get(globalRow);
        }

        return screen.get(globalRow - scrollback.size());
    }

    private void insertCodePointAtCursor(int codePoint) {
        Cell carry = Cell.fromCodePoint(codePoint, currentAttributes);

        int row = cursorRow;
        int col = cursorColumn;

        while (carry != null) {
            if (row >= height) {
                scrollUpOneLine();
                row = height - 1;
            }

            List<Cell> line = screen.get(row);
            Cell nextCarry = line.get(width - 1);
            shiftRight(line, col);
            line.set(col, carry);

            carry = isEmptyDefaultCell(nextCarry) ? null : nextCarry;
            col = 0;
            row++;
        }

        cursorColumn++;
        if (cursorColumn >= width) {
            cursorColumn = 0;
            advanceToNextLine();
        }
    }

    private void shiftRight(List<Cell> line, int fromColumn) {
        for (int c = width - 1; c > fromColumn; c--) {
            line.set(c, line.get(c - 1));
        }
    }

    private void advanceToNextLine() {
        if (cursorRow < height - 1) {
            cursorRow++;
        } else {
            scrollUpOneLine();
            cursorRow = height - 1;
        }
    }

    private void addToScrollback(List<Cell> line) {
        if (scrollbackMaxSize == 0) {
            return;
        }

        scrollback.add(copyLine(line));
        if (scrollback.size() > scrollbackMaxSize) {
            scrollback.remove(0);
        }
    }

    private void scrollUpOneLine() {
        addToScrollback(screen.remove(0));
        screen.add(createBlankLine());
    }

    private void setCell(int row, int column, Cell cell) {
        screen.get(row).set(column, cell);
    }

    private List<Cell> createBlankLine() {
        List<Cell> line = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            line.add(Cell.empty());
        }
        return line;
    }

    private List<Cell> copyLine(List<Cell> line) {
        return new ArrayList<>(line);
    }

    private void moveCursorVertical(int delta) {
        int next = cursorRow + Math.max(0, Math.abs(delta)) * Integer.signum(delta);
        cursorRow = clamp(next, 0, height - 1);
    }

    private void moveCursorHorizontal(int delta) {
        int next = cursorColumn + Math.max(0, Math.abs(delta)) * Integer.signum(delta);
        cursorColumn = clamp(next, 0, width - 1);
    }

    private String joinLines(List<List<Cell>> lines) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            builder.append(lineToString(lines.get(i)));
            if (i < lines.size() - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private String lineToString(List<Cell> line) {
        StringBuilder builder = new StringBuilder(width);
        for (Cell cell : line) {
            builder.append(cell.character());
        }
        return builder.toString();
    }

    private String normalizeFillCharacter(String character) {
        if (character == null || character.isEmpty()) {
            return " ";
        }
        if (character.codePointCount(0, character.length()) != 1) {
            throw new IllegalArgumentException("fill character must contain at most one Unicode code point");
        }
        return character;
    }

    private boolean isEmptyDefaultCell(Cell cell) {
        return " ".equals(cell.character()) && CellAttributes.DEFAULT.equals(cell.attributes());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}