package lox.scanner;

public class Location implements Cloneable {
    private int offset;
    private int line;
    private int col;

    public Location(int offset, int line, int col) {
        this.offset = offset;
        this.line = line;
        this.col = col;
    }

    public Location() {
        this.offset = 0;
        this.line = 0;
        this.col = 0;
    }

    public int offset() {
        return offset;
    }

    public int line() {
        return line;
    }

    public int col() {
        return col;
    }

    /**
     * Increments the location to the next character
     *
     * @param s Source code
     * @return Character at offset before incrementing
     */
    public char increment(String s) {
        char curr = s.charAt(offset++);
        col += 1;
        if (curr == '\n') {
            line += 1;
            col = 0;
        }

        return curr;
    }

    /**
     * Increments the location to the next character
     *
     * @param c The current character the location is at
     */
    public void increment(char c) {
        offset += 1;
        col += 1;
        if (c == '\n') {
            line += 1;
            col = 0;
        }
    }

    public char charAt(String s) {
        return s.charAt(offset);
    }

    /**
     * Copies the values of {@code loc} into this object
     */
    public Location bringTo(Location loc) {
        this.offset = loc.offset;
        this.line = loc.line;
        this.col = loc.col;

        return this;
    }
}
