package lox.scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lox.TokenType;

import static lox.TokenType.*;

class ScannerUtil {
    private static final Map<String, TokenType> KEYWORDS;
    private static final Map<Character, Character> ESCAPE_CHARACTERS;
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

        ESCAPE_CHARACTERS = new HashMap<>();
        ESCAPE_CHARACTERS.put('\"', '\"');
        ESCAPE_CHARACTERS.put('{', '{');
        ESCAPE_CHARACTERS.put('\\', '\\');
        ESCAPE_CHARACTERS.put('n', '\n');
        ESCAPE_CHARACTERS.put('t', '\t');
    }

    static TokenType getIdentifierOrKeyword(String text) {
        return KEYWORDS.getOrDefault(text, IDENTIFIER);
    }

    static Optional<Character> getEscapedCharacter(char c) {
        return Optional.ofNullable(ESCAPE_CHARACTERS.getOrDefault(c, null));
    }

    static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
