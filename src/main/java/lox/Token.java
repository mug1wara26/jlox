package lox;

import lox.scanner.Location;

/**
 * Represents tokens in Lox code, tracks the corresponding type, lexeme, value,
 * and location.
 */
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final Location loc;

    public Token(TokenType type, String lexeme, Object literal, Location loc) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.loc = loc;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
