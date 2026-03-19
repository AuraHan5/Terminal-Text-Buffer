package example.terminal;

import java.util.Objects;

/**
 * Visual attributes applied to cells.
 */
public record CellAttributes(
        TerminalColor foreground,
        TerminalColor background,
        boolean bold,
        boolean italic,
        boolean underline
) {
    public static final CellAttributes DEFAULT =
            new CellAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, false, false, false);

    public CellAttributes {
        Objects.requireNonNull(foreground, "foreground must not be null");
        Objects.requireNonNull(background, "background must not be null");
    }
}