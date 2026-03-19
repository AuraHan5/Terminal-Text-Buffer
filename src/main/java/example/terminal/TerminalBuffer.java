package example.terminal;

import java.util.ArrayList;
import java.util.List;

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

    private List<Cell> createBlankLine() {
        List<Cell> line = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            line.add(Cell.empty());
        }
        return line;
    }
}