# Terminal Text Buffer - Solution Notes

## Overview

This project implements a terminal text buffer in Java as the core data structure behind a terminal emulator display model.

The implementation centers on:
- A fixed-size visible `screen` grid (`width x height`)
- A bounded `scrollback` history for lines that move off-screen
- A cursor position used by editing operations
- Current cell attributes (foreground, background, style flags) applied to new edits

Primary implementation classes:
- `example.terminal.TerminalBuffer`
- `example.terminal.Cell`
- `example.terminal.CellAttributes`
- `example.terminal.TerminalColor`

## Data Model

### Cell
Each cell stores:
- A single display character (or blank space)
- Cell attributes (`CellAttributes`)

`Cell` is implemented as an immutable Java record for safety and predictable behavior.

### Attributes
`CellAttributes` stores:
- Foreground color (`TerminalColor`)
- Background color (`TerminalColor`)
- Styles: `bold`, `italic`, `underline`

A `DEFAULT` attribute instance is provided and used for blank cells.

### Colors
`TerminalColor` provides:
- `DEFAULT`
- The 16 standard terminal colors (normal + bright variants)

### Buffer Layout
`TerminalBuffer` stores:
- `screen`: mutable list of lines, each line a list of `Cell`
- `scrollback`: history lines that have scrolled off the top

Global row addressing is used for read APIs:
- `0 .. scrollbackSize-1` -> scrollback
- `scrollbackSize .. scrollbackSize+height-1` -> visible screen

## Implemented Operations

### Setup
- Configurable width, height, and max scrollback size via constructor
- Constructor validates all dimensions (`width > 0`, `height > 0`, `scrollbackMaxSize >= 0`)

### Attributes
- Set/get current attributes
- Overload supports direct setting from colors + style flags

### Cursor
- Get cursor row/column
- Set cursor position with clamping to screen bounds
- Move cursor up/down/left/right by N cells with clamping

### Editing (cursor + attributes aware)
- `writeText(text)`:
  - Overwrites existing cells from current cursor
  - Advances cursor
  - Supports newline handling
- `insertText(text)`:
  - Inserts at cursor by shifting right
  - Wraps overflow to following lines
  - Scrolls when needed
- `fillLine(row, character)`:
  - Fills a screen line with one character using current attributes

### Editing (independent utilities)
- `insertEmptyLineAtBottom()`:
  - Moves top screen line into scrollback
  - Appends blank line at screen bottom
- `clearScreen()`:
  - Clears visible screen only
- `clearScreenAndScrollback()`:
  - Clears both visible screen and history

### Content Access
- Character and attributes by position (`getCharacterAt`, `getAttributesAt`)
- Line by global row (`getLineAsString`)
- Entire screen as text (`getScreenContentAsString`)
- Entire scrollback + screen as text (`getBufferContentAsString`)

## Scrollback Design

When content moves off the top of the visible screen, the removed line is copied into scrollback.

If scrollback exceeds configured maximum size, the oldest history line is dropped.

This enforces bounded memory usage while preserving recent terminal history.

## Testing

JUnit 5 tests are implemented in:
- `lib/src/test/java/example/terminal/TerminalBufferTest.java`

Covered behavior includes:
- Initialization
- Cursor clamping and movement
- Write/insert semantics
- Wrap behavior
- Fill line with current attributes
- Scrollback movement and max size enforcement
- Clear operations
- Content access and boundary errors

Build and tests run with Gradle using JUnit Platform.

## Trade-offs and Design Decisions

1. Immutable cells and attributes
- Decision: represent `Cell` and `CellAttributes` as records.
- Benefit: simple, safe value semantics.
- Cost: more object allocation during heavy updates.

2. Simple line-shift insertion algorithm
- Decision: insertion shifts cells to the right and carries overflow to next lines.
- Benefit: easy to reason about and test.
- Cost: insertion is O(width) per inserted character in worst case.

3. Fixed-size screen with bounded scrollback
- Decision: preserve terminal-like behavior and keep memory controlled.
- Benefit: predictable memory growth.
- Cost: old history is dropped when cap is reached.

4. Clamped cursor policy
- Decision: cursor never leaves visible bounds.
- Benefit: robust API and fewer invalid states.
- Cost: behavior differs from terminals that allow hidden cursor states in some modes.

## Known Limitations

1. Wide characters are not implemented.
- Characters are treated as one code point per one cell.

2. Dynamic resize is not implemented.
- Width/height are fixed for the lifetime of `TerminalBuffer`.

3. No ANSI parser.
- The buffer provides primitives only; escape-sequence interpretation is intentionally out of scope.

## Possible Improvements

1. Wide-character support (CJK/emoji)
- Track display width per grapheme and reserve continuation cells.
- Adjust cursor movement and insertion/deletion logic accordingly.

2. Resize support
- Add `resize(newWidth, newHeight)` with explicit reflow strategy options.

3. Performance tuning
- Use more compact internal storage or mutable row arrays for heavy insert workloads.

4. Additional terminal semantics
- Add insert/delete character operations, line insertion/deletion in-place, and region-based scrolling.

## Build Configuration Notes

Gradle module `:lib` is configured for:
- Java plugin
- Maven Central repository
- JUnit Jupiter dependency
- `test { useJUnitPlatform() }`