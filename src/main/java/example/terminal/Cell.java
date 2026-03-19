package example.terminal;

import java.util.Objects;

/**
 * One character cell in the terminal grid.
 */
public record Cell(String character, CellAttributes attributes) {
    public static Cell empty() {
        return new Cell(" ", CellAttributes.DEFAULT);
    }

    public Cell {
        Objects.requireNonNull(character, "character must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");
        if (character.isEmpty()) {
            throw new IllegalArgumentException("character must contain exactly one display character or a blank");
        }
        if (character.codePointCount(0, character.length()) != 1) {
            throw new IllegalArgumentException("character must contain exactly one Unicode code point");
        }
    }

    public static Cell fromCodePoint(int codePoint, CellAttributes attributes) {
        return new Cell(new String(Character.toChars(codePoint)), attributes);
    }
}