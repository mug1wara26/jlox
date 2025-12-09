package lox.scanner;

import java.util.ArrayList;
import java.util.List;

import lox.TokenType;
import lox.Token;
import lox.Lox;

import static lox.TokenType.*;
import static lox.scanner.ScannerUtil.*;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int col = 0;
    private boolean is_interpolated = false;
    private boolean is_execution_stopped = false;

    public Scanner(String source) {
        this.source = source;
    }

    public Scanner(String source, int current) {
        this.source = source;
        this.current = current;
    }

    public static Scanner interpolateString(String source, int current) {
        Scanner sc = new Scanner(source, current);
        sc.is_interpolated = true;

        return sc;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        if (!is_interpolated)
            tokens.add(new Token(EOF, "", null, line, col));
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
                if (is_interpolated)
                    is_execution_stopped = true;
                else
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
        TokenType type = getIdentifierOrKeyword(text);
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
        StringBuilder sb = new StringBuilder();
        boolean is_escaping = false;
        while ((is_escaping || peek() != '"') && !isAtEnd()) {
            char c = advance();
            if (is_escaping)
                getEscapedCharacter(c).ifPresentOrElse(x -> sb.append(c),
                        () -> Lox.error(line, col, "Invalid escape sequence"));
            else if (c == '\\')
                is_escaping = true;
            else if (c == '{') {
                Scanner sub_sc = interpolateString(source, current);
                sub_sc.scanTokens();

                addToken(STRING, '"' + sb.toString() + '"', sb.toString());
                addToken(PLUS, "+", null);
                addToken(LEFT_PAREN, "(", null);
                addTokens(sub_sc.tokens);
                addToken(RIGHT_PAREN, ")", null);
                addToken(PLUS, "+", null);

                // Clear the string builder update start
                sb.setLength(0);
                current = sub_sc.current;
                start = current;
            } else
                sb.append(c);
        }

        if (isAtEnd()) {
            Lox.error(line, col, "Expected closing string but reached end of file.");
            return;
        }

        // The closing ".
        advance();

        addToken(STRING, '"' + sb.toString() + '"', sb.toString());
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addTokens(List<Token> tokens) {
        this.tokens.addAll(tokens);
    }

    private void addToken(TokenType type, String lexeme, Object literal) {
        tokens.add(new Token(type, lexeme, literal, line, col));
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, col));
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
        return is_execution_stopped || current >= source.length();
    }
}
