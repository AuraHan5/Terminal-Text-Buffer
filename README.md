# Terminal Text Buffer

Concise Java implementation of a terminal emulator text buffer with screen + scrollback, cursor-based editing, cell attributes, and JUnit 5 tests.

## Assignment Coverage

Implemented core requirements:
- Configurable width, height, and scrollback limit.
- Character-cell grid with per-cell attributes.
- Foreground/background colors: default + 16 standard terminal colors.
- Style flags: bold, italic, underline.
- Cursor get/set and bounded movement (up/down/left/right).
- Editing operations:
	- Write text (overwrite from cursor, cursor advances)
	- Insert text (shift right with wrapping/overflow propagation)
	- Fill line with one character (or blank)
- Utility operations:
	- Insert empty line at bottom (top line goes to scrollback)
	- Clear screen
	- Clear screen and scrollback
- Content access:
	- Character by position
	- Attributes by position
	- Line by global row (scrollback + screen indexing)
	- Entire screen as string
	- Entire buffer (scrollback + screen) as string

Bonus features not implemented:
- Wide characters (double-cell graphemes)
- Dynamic resize

## Project Structure

- Main implementation: [lib/src/main/java/example/terminal](lib/src/main/java/example/terminal)
- Tests: [lib/src/test/java/example/terminal/TerminalBufferTest.java](lib/src/test/java/example/terminal/TerminalBufferTest.java)
- Detailed design notes: [SOLUTION.md](SOLUTION.md)

## Build and Test

Requirements:
- Java 21+
- Gradle Wrapper

Commands:

```bash
./gradlew test
./gradlew build
```

## Design Decisions and Trade-offs

- Immutable value objects for cells and attributes make behavior predictable and easy to test.
- Insertion uses right-shift plus carry into following lines; simple and correct, but can be O(width) per inserted character.
- Bounded scrollback enforces memory limits by dropping oldest history when full.
- Cursor positions are clamped to visible screen bounds to avoid invalid states.

## Future Improvements

- Add wide-character handling with display-width awareness.
- Add resize support with a defined content reflow strategy.
- Extend terminal semantics (region scrolling, delete/insert char operations, ANSI parsing layer).
