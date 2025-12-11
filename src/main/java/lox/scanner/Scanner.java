package lox.scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lox.TokenType;
import lox.Token;
import lox.Lox;

import static lox.TokenType.*;
import static lox.scanner.ScannerUtil.*;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private Location start;
    private Location current;
    private boolean is_interpolated = false;
    private boolean is_execution_stopped = false;

    public Scanner(String source) {
        this.source = source;
        this.start = new Location();
        this.current = new Location();
    }

    public Scanner(String source, Location start) {
        this(source);
        this.start.bringTo(start);
        this.current.bringTo(start);
    }

    /**
     * Instantiate a new {@link Scanner} and set it to interpolation mode
     * 
     * @param source The original source code
     * @param start  The starting location of the expression in the interpolation
     *               template
     * @return The new instantiated {@link Scanner}
     */
    public static Scanner interpolateString(String source, Location start) {
        Scanner sc = new Scanner(source, new Location().bringTo(start));
        sc.is_interpolated = true;

        return sc;
    }

    /**
     * Scans tokens in the source code
     * 
     * @return List of tokens, including an EOF token at the end if not in
     *         interpolation mode
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start.bringTo(current);
            scanToken();
        }

        if (!is_interpolated)
            tokens.add(new Token(EOF, "", null, current));
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
                if (is_interpolated) {
                    is_execution_stopped = true;
                    addToken(INTERP_END);
                } else
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
                        Lox.error(current, "Expected end comment marker but reached end of file.");
                } else {
                    addToken(SLASH);
                }
                break;
            case '*':
                addToken(STAR);
                break;
            case '?':
                addToken(QUESTION_MARK);
                break;
            case ':':
                addToken(COLON);
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
                    Lox.error(start, "Unexpected character.");
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start.offset(), current.offset());
        TokenType type = getIdentifierOrKeyword(text);
        addToken(type, text, text);
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start.offset(), current.offset())));
    }

    private void string() {
        addToken(STRING_START);
        start.bringTo(current);

        boolean is_escaping = false;
        StringBuilder sb = new StringBuilder();

        while (peek() != '"' && !isAtEnd()) {
            char c = advance();

            if (is_escaping) {
                Optional<Character> escaped = getEscapedCharacter(c);
                if (escaped.isPresent())
                    sb.append(escaped);
                else
                    Lox.error(current, "Invalid escape sequence");
            } else {
                switch (c) {
                    case '\\':
                        is_escaping = true;
                        break;
                    case '$':
                        if (match('{')) {
                            addToken(STRING, sb.toString(), sb.toString());
                            sb.setLength(0);
                            // This is safe since the previous 2 characters must be on the same line and
                            // must be ${
                            start.bringTo(new Location(current.offset() - 2, current.line(), current.col()));
                            addToken(INTERP_START);
                            start.bringTo(current);

                            Scanner sub_sc = Scanner.interpolateString(source, start);
                            List<Token> new_tokens = sub_sc.scanTokens();
                            addTokens(new_tokens);
                            current.bringTo(sub_sc.current);
                            start.bringTo(current);
                        } else
                            sb.append(c);
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }

        addToken(STRING, sb.toString(), sb.toString());

        // Consume the ending "
        start.bringTo(current);
        advance();
        addToken(STRING_END);
    }

    private void addTokens(Collection<Token> tokens) {
        this.tokens.addAll(tokens);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, String lexeme, Object literal) {
        tokens.add(new Token(type, lexeme, literal, start));
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start.offset(), current.offset());
        addToken(type, text, literal);
    }

    // Advance should be the only function that increments current. This is to keep
    // track of line and col
    private char advance() {
        return current.increment(source);
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return current.charAt(source);
    }

    private char peekNext() {
        if (current.offset() + 1 >= source.length())
            return '\0';

        return source.charAt(current.offset() + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (current.charAt(source) != expected)
            return false;

        advance();
        return true;
    }

    private boolean isAtEnd() {
        return is_execution_stopped || current.offset() >= source.length();
    }

    public Location getCurrent() {
        return current;
    }
}
