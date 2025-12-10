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

    public char increment(String s) {
        char curr = s.charAt(offset++);
        col += 1;
        if (curr == '\n') {
            line += 1;
            col = 0;
        }

        return curr;
    }

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

    public void bringTo(Location loc) {
        this.offset = loc.offset;
        this.line = loc.line;
        this.col = loc.col;
    }
}
