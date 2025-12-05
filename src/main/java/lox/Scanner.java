package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {
    private static final Map<String, TokenType> KEYWORDS;
    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("and", AND);
        KEYWORDS.put("class", CLASS);
        KEYWORDS.put("else", ELSE);
        KEYWORDS.put("false", FALSE);
        KEYWORDS.put("for", FOR);
        KEYWORDS.put("fun", FUN);
        KEYWORDS.put("if", IF);
        KEYWORDS.put("nil", NIL);
        KEYWORDS.put("or", OR);
        KEYWORDS.put("print", PRINT);
        KEYWORDS.put("return", RETURN);
        KEYWORDS.put("super", SUPER);
        KEYWORDS.put("this", THIS);
        KEYWORDS.put("true", TRUE);
        KEYWORDS.put("var", VAR);
        KEYWORDS.put("while", WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int col = 0;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else if (match('*')) {
                    // A block comment goes until it finds its corresponding ending block comment
                    // marker. We allow nested comments for the challenge.
                    int nest_level = 1;

                    while (nest_level != 0 && !isAtEnd()) {
                        char current_char = advance();
                        if (current_char == '/' && match('*'))
                            nest_level += 1;
                        else if (current_char == '*' && match('/')) {
                            nest_level -= 1;
                        }
                    }

                    if (isAtEnd())
                        Lox.error(line, col, "Expected end comment marker but reached end of file.");
                } else {
                    addToken(SLASH);
                }
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '"':
                string();
                break;
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                break;

            default:
                if (isDigit(c))
                    number();
                else if (isAlpha(c))
                    identifier();
                else
                    Lox.error(line, col, "Unexpected character.");
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd())
            advance();

        if (isAtEnd()) {
            Lox.error(line, col, "Expected closing string but reached end of file.");
            return;
        }

        // The closing ".
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // Advance should be the only function that increments current. This is to keep
    // track of line and col
    private char advance() {
        char current_char = source.charAt(current++);
        col += 1;
        if (current_char == '\n') {
            line += 1;
            col = 0;
        }
        return current_char;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';

        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        advance();
        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
