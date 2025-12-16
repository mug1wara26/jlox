package lox.scanner;

import lox.Token;
import lox.TokenType;

import org.checkerframework.checker.units.qual.N;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;
import static lox.TokenType.*;

/**
 * Written with Claude
 */

public class ScannerTest {

    // ========== SINGLE CHARACTER TOKENS ==========

    private void assertTokenTypesEqual(List<Token> tokens, TokenType... types) {
        assertEquals(tokens.size(), types.length + 1);
        for (int i = 0; i < types.length; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
        assertEquals(tokens.get(types.length).type, EOF);
    }

    @Test
    public void testSingleCharacterTokens() {
        Scanner scanner = new Scanner("(){},.+-;/?:*");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, PLUS, MINUS,
                SEMICOLON, SLASH, QUESTION_MARK, COLON, STAR);
    }

    @Test
    public void testQuestionMarkAndColon() {
        Scanner scanner = new Scanner("? :");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, QUESTION_MARK, COLON);
    }

    // ========== TWO CHARACTER TOKENS ==========

    @Test
    public void testBangToken() {
        Scanner scanner = new Scanner("!");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, BANG);
    }

    @Test
    public void testBangEqualToken() {
        Scanner scanner = new Scanner("!=");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, BANG_EQUAL);
    }

    @Test
    public void testEqualToken() {
        Scanner scanner = new Scanner("=");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, EQUAL);
    }

    @Test
    public void testEqualEqualToken() {
        Scanner scanner = new Scanner("==");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, EQUAL_EQUAL);
    }

    @Test
    public void testGreaterToken() {
        Scanner scanner = new Scanner(">");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, GREATER);
    }

    @Test
    public void testGreaterEqualToken() {
        Scanner scanner = new Scanner(">=");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, GREATER_EQUAL);
    }

    @Test
    public void testLessToken() {
        Scanner scanner = new Scanner("<");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, LESS);
    }

    @Test
    public void testLessEqualToken() {
        Scanner scanner = new Scanner("<=");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, LESS_EQUAL);
    }

    @Test
    public void testMultipleComparisons() {
        Scanner scanner = new Scanner("< <= > >= == !=");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL_EQUAL, BANG_EQUAL);
    }

    // ========== COMMENTS ==========

    @Test
    public void testSingleLineComment() {
        Scanner scanner = new Scanner("// this is a comment\n42");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(42.0, tokens.get(0).literal);
    }

    @Test
    public void testSingleLineCommentAtEnd() {
        Scanner scanner = new Scanner("42 // comment at end");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(42.0, tokens.get(0).literal);
    }

    @Test
    public void testBlockComment() {
        Scanner scanner = new Scanner("/* block comment */ 42");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(42.0, tokens.get(0).literal);
    }

    @Test
    public void testNestedBlockComments() {
        Scanner scanner = new Scanner("/* outer /* inner */ outer */ 42");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(42.0, tokens.get(0).literal);
    }

    @Test
    public void testMultipleNestedBlockComments() {
        Scanner scanner = new Scanner("/* a /* b /* c */ b */ a */ 42");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(42.0, tokens.get(0).literal);
    }

    @Test
    public void testBlockCommentMultiline() {
        Scanner scanner = new Scanner("/* line 1\nline 2\nline 3 */ 42");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(42.0, tokens.get(0).literal);
    }

    // ========== NUMBERS ==========

    @Test
    public void testInteger() {
        Scanner scanner = new Scanner("123");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(123.0, tokens.get(0).literal);
    }

    @Test
    public void testFloat() {
        Scanner scanner = new Scanner("123.456");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(123.456, tokens.get(0).literal);
    }

    @Test
    public void testZero() {
        Scanner scanner = new Scanner("0");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(0.0, tokens.get(0).literal);
    }

    @Test
    public void testFloatStartingWithZero() {
        Scanner scanner = new Scanner("0.123");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER);
        assertEquals(0.123, tokens.get(0).literal);
    }

    @Test
    public void testMultipleNumbers() {
        Scanner scanner = new Scanner("1 2.5 300 0.001");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(5, tokens.size());
        assertEquals(1.0, tokens.get(0).literal);
        assertEquals(2.5, tokens.get(1).literal);
        assertEquals(300.0, tokens.get(2).literal);
        assertEquals(0.001, (Double) tokens.get(3).literal, 0.0001);
    }

    @Test
    public void testNumberFollowedByDot() {
        Scanner scanner = new Scanner("123.foo");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NUMBER, DOT, IDENTIFIER);
        assertEquals(123.0, tokens.get(0).literal);
    }

    // ========== IDENTIFIERS ==========

    @Test
    public void testSimpleIdentifier() {
        Scanner scanner = new Scanner("foo");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, IDENTIFIER);
        assertEquals("foo", tokens.get(0).lexeme);
    }

    @Test
    public void testIdentifierWithUnderscore() {
        Scanner scanner = new Scanner("_foo_bar");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, IDENTIFIER);
        assertEquals("_foo_bar", tokens.get(0).lexeme);
    }

    @Test
    public void testIdentifierWithNumbers() {
        Scanner scanner = new Scanner("foo123");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, IDENTIFIER);
        assertEquals("foo123", tokens.get(0).lexeme);
    }

    @Test
    public void testMultipleIdentifiers() {
        Scanner scanner = new Scanner("foo bar baz");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, IDENTIFIER, IDENTIFIER, IDENTIFIER);
        assertEquals("foo", tokens.get(0).lexeme);
        assertEquals("bar", tokens.get(1).lexeme);
        assertEquals("baz", tokens.get(2).lexeme);
    }

    // ========== KEYWORDS ==========

    @Test
    public void testKeywordAnd() {
        Scanner scanner = new Scanner("and");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, AND);
    }

    @Test
    public void testKeywordClass() {
        Scanner scanner = new Scanner("class");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, CLASS);
    }

    @Test
    public void testKeywordElse() {
        Scanner scanner = new Scanner("else");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, ELSE);
    }

    @Test
    public void testKeywordFalse() {
        Scanner scanner = new Scanner("false");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, FALSE);
    }

    @Test
    public void testKeywordFor() {
        Scanner scanner = new Scanner("for");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, FOR);
    }

    @Test
    public void testKeywordFun() {
        Scanner scanner = new Scanner("fun");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, FUN);
    }

    @Test
    public void testKeywordIf() {
        Scanner scanner = new Scanner("if");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, IF);
    }

    @Test
    public void testKeywordNil() {
        Scanner scanner = new Scanner("nil");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, NIL);
    }

    @Test
    public void testKeywordOr() {
        Scanner scanner = new Scanner("or");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, OR);
    }

    @Test
    public void testKeywordPrint() {
        Scanner scanner = new Scanner("print");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, PRINT);
    }

    @Test
    public void testKeywordReturn() {
        Scanner scanner = new Scanner("return");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, RETURN);
    }

    @Test
    public void testKeywordSuper() {
        Scanner scanner = new Scanner("super");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, SUPER);
    }

    @Test
    public void testKeywordThis() {
        Scanner scanner = new Scanner("this");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, THIS);
    }

    @Test
    public void testKeywordTrue() {
        Scanner scanner = new Scanner("true");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(TRUE, tokens.get(0).type);
    }

    @Test
    public void testKeywordVar() {
        Scanner scanner = new Scanner("var");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, VAR);
    }

    @Test
    public void testKeywordWhile() {
        Scanner scanner = new Scanner("while");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, WHILE);
    }

    @Test
    public void testAllKeywords() {
        Scanner scanner = new Scanner("and class else false for fun if nil or print return super this true var while");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, AND, CLASS, ELSE, FALSE, FOR, FUN, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE,
                VAR, WHILE);
    }

    @Test
    public void testIdentifierVsKeyword() {
        Scanner scanner = new Scanner("andor forward klassify");
        List<Token> tokens = scanner.scanTokens();

        assertTokenTypesEqual(tokens, IDENTIFIER, IDENTIFIER, IDENTIFIER);
    }

    // ========== STRINGS ==========

    @Test
    public void testSimpleString() {
        Scanner scanner = new Scanner("\"hello\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING_START, tokens.get(0).type);
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("hello", tokens.get(1).literal);
        assertEquals(STRING_END, tokens.get(2).type);
        assertEquals(EOF, tokens.get(3).type);
    }

    @Test
    public void testEmptyString() {
        Scanner scanner = new Scanner("\"\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING_START, tokens.get(0).type);
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("", tokens.get(1).literal);
        assertEquals(STRING_END, tokens.get(2).type);
    }

    @Test
    public void testStringWithSpaces() {
        Scanner scanner = new Scanner("\"hello world\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("hello world", tokens.get(1).literal);
    }

    @Test
    public void testMultilineString() {
        Scanner scanner = new Scanner("\"line 1\nline 2\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("line 1\nline 2", tokens.get(1).literal);
    }

    @Test
    public void testStringWithEscapedQuote() {
        Scanner scanner = new Scanner("\"say \\\"hello\\\"\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("say \"hello\"", tokens.get(1).literal);
    }

    @Test
    public void testStringWithEscapedBackslash() {
        Scanner scanner = new Scanner("\"path\\\\to\\\\file\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("path\\to\\file", tokens.get(1).literal);
    }

    @Test
    public void testStringWithEscapedNewline() {
        Scanner scanner = new Scanner("\"line 1\\nline 2\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("line 1\nline 2", tokens.get(1).literal);
    }

    @Test
    public void testStringWithEscapedTab() {
        Scanner scanner = new Scanner("\"col1\\tcol2\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("col1\tcol2", tokens.get(1).literal);
    }

    @Test
    public void testStringWithDollarSign() {
        Scanner scanner = new Scanner("\"price is $5\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("price is $5", tokens.get(1).literal);
    }

    // ========== STRING INTERPOLATION ==========

    @Test
    public void testStringInterpolationSimple() {
        Scanner scanner = new Scanner("\"value: ${x}\"");
        List<Token> tokens = scanner.scanTokens();

        // STRING_START, STRING("value: "), INTERP_START, IDENTIFIER(x), INTERP_END,
        // STRING(""), STRING_END, EOF
        assertTrue(tokens.size() >= 7);
        assertEquals(STRING_START, tokens.get(0).type);
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("value: ", tokens.get(1).literal);
        assertEquals(INTERP_START, tokens.get(2).type);
        assertEquals(IDENTIFIER, tokens.get(3).type);
        assertEquals("x", tokens.get(3).lexeme);
        assertEquals(INTERP_END, tokens.get(4).type);
    }

    @Test
    public void testStringInterpolationExpression() {
        Scanner scanner = new Scanner("\"result: ${1 + 2}\"");
        List<Token> tokens = scanner.scanTokens();

        assertTrue(tokens.size() >= 9);
        assertEquals(STRING_START, tokens.get(0).type);
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("result: ", tokens.get(1).literal);
        assertEquals(INTERP_START, tokens.get(2).type);
        assertEquals(NUMBER, tokens.get(3).type);
        assertEquals(PLUS, tokens.get(4).type);
        assertEquals(NUMBER, tokens.get(5).type);
        assertEquals(INTERP_END, tokens.get(6).type);
    }

    @Test
    public void testStringInterpolationMultiple() {
        Scanner scanner = new Scanner("\"${a} and ${b}\"");
        List<Token> tokens = scanner.scanTokens();

        assertTrue(tokens.size() >= 10);
        assertEquals(STRING_START, tokens.get(0).type);
        assertEquals(INTERP_START, tokens.get(2).type);
        assertEquals(IDENTIFIER, tokens.get(3).type);
        assertEquals("a", tokens.get(3).lexeme);
        assertEquals(INTERP_END, tokens.get(4).type);
        assertEquals(STRING, tokens.get(5).type);
        assertEquals(" and ", tokens.get(5).literal);
        assertEquals(INTERP_START, tokens.get(6).type);
        assertEquals(IDENTIFIER, tokens.get(7).type);
        assertEquals("b", tokens.get(7).lexeme);
    }

    @Test
    public void testStringInterpolationEscapedBrace() {
        Scanner scanner = new Scanner("\"not interpolated: $\\{x}\"");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(STRING_START, tokens.get(0).type);
        assertEquals(STRING, tokens.get(1).type);
        assertEquals("not interpolated: ${x}", tokens.get(1).literal);
        assertEquals(STRING_END, tokens.get(2).type);
    }

    // ========== WHITESPACE ==========

    @Test
    public void testWhitespaceIgnored() {
        Scanner scanner = new Scanner("   \t\n  123  \r\n  ");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(NUMBER, tokens.get(0).type);
    }

    @Test
    public void testEmptyInput() {
        Scanner scanner = new Scanner("");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(1, tokens.size());
        assertEquals(EOF, tokens.get(0).type);
    }

    @Test
    public void testOnlyWhitespace() {
        Scanner scanner = new Scanner("   \t\n\r   ");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(1, tokens.size());
        assertEquals(EOF, tokens.get(0).type);
    }

    // ========== COMPLEX EXPRESSIONS ==========

    @Test
    public void testSimpleExpression() {
        Scanner scanner = new Scanner("1 + 2");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(NUMBER, tokens.get(0).type);
        assertEquals(PLUS, tokens.get(1).type);
        assertEquals(NUMBER, tokens.get(2).type);
    }

    @Test
    public void testComplexExpression() {
        Scanner scanner = new Scanner("(1 + 2) * 3 - 4 / 5");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(12, tokens.size());
        assertEquals(LEFT_PAREN, tokens.get(0).type);
        assertEquals(NUMBER, tokens.get(1).type);
        assertEquals(PLUS, tokens.get(2).type);
        assertEquals(NUMBER, tokens.get(3).type);
        assertEquals(RIGHT_PAREN, tokens.get(4).type);
        assertEquals(STAR, tokens.get(5).type);
        assertEquals(NUMBER, tokens.get(6).type);
        assertEquals(MINUS, tokens.get(7).type);
        assertEquals(NUMBER, tokens.get(8).type);
        assertEquals(SLASH, tokens.get(9).type);
        assertEquals(NUMBER, tokens.get(10).type);
    }

    @Test
    public void testVariableDeclaration() {
        Scanner scanner = new Scanner("var x = 42;");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(6, tokens.size());
        assertEquals(VAR, tokens.get(0).type);
        assertEquals(IDENTIFIER, tokens.get(1).type);
        assertEquals(EQUAL, tokens.get(2).type);
        assertEquals(NUMBER, tokens.get(3).type);
        assertEquals(SEMICOLON, tokens.get(4).type);
    }

    @Test
    public void testIfStatement() {
        Scanner scanner = new Scanner("if (x > 5) { print x; }");
        List<Token> tokens = scanner.scanTokens();

        assertTrue(tokens.size() >= 11);
        assertEquals(IF, tokens.get(0).type);
        assertEquals(LEFT_PAREN, tokens.get(1).type);
        assertEquals(IDENTIFIER, tokens.get(2).type);
        assertEquals(GREATER, tokens.get(3).type);
        assertEquals(NUMBER, tokens.get(4).type);
        assertEquals(RIGHT_PAREN, tokens.get(5).type);
        assertEquals(LEFT_BRACE, tokens.get(6).type);
        assertEquals(PRINT, tokens.get(7).type);
        assertEquals(IDENTIFIER, tokens.get(8).type);
        assertEquals(SEMICOLON, tokens.get(9).type);
        assertEquals(RIGHT_BRACE, tokens.get(10).type);
    }

    @Test
    public void testFunctionDeclaration() {
        Scanner scanner = new Scanner("fun add(a, b) { return a + b; }");
        List<Token> tokens = scanner.scanTokens();

        assertTrue(tokens.size() >= 14);
        assertEquals(FUN, tokens.get(0).type);
        assertEquals(IDENTIFIER, tokens.get(1).type);
        assertEquals(LEFT_PAREN, tokens.get(2).type);
        assertEquals(IDENTIFIER, tokens.get(3).type);
        assertEquals(COMMA, tokens.get(4).type);
        assertEquals(IDENTIFIER, tokens.get(5).type);
        assertEquals(RIGHT_PAREN, tokens.get(6).type);
        assertEquals(LEFT_BRACE, tokens.get(7).type);
        assertEquals(RETURN, tokens.get(8).type);
    }

    @Test
    public void testTernaryOperator() {
        Scanner scanner = new Scanner("x > 5 ? true : false");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(8, tokens.size());
        assertEquals(IDENTIFIER, tokens.get(0).type);
        assertEquals(GREATER, tokens.get(1).type);
        assertEquals(NUMBER, tokens.get(2).type);
        assertEquals(QUESTION_MARK, tokens.get(3).type);
        assertEquals(TRUE, tokens.get(4).type);
        assertEquals(COLON, tokens.get(5).type);
        assertEquals(FALSE, tokens.get(6).type);
    }

    // ========== LOCATION TRACKING ==========

    @Test
    public void testLocationTracking() {
        Scanner scanner = new Scanner("123");
        List<Token> tokens = scanner.scanTokens();

        assertNotNull(tokens.get(0).loc);
        assertEquals(0, tokens.get(0).loc.offset());
        assertEquals(1, tokens.get(0).loc.line());
        assertEquals(0, tokens.get(0).loc.col());
    }

    @Test
    public void testLocationTrackingMultiline() {
        Scanner scanner = new Scanner("123\n456");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(1, tokens.get(0).loc.line());
        assertEquals(2, tokens.get(1).loc.line());
    }

    // ========== EDGE CASES ==========

    @Test
    public void testConsecutiveOperators() {
        Scanner scanner = new Scanner("+-*/");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(5, tokens.size());
        assertEquals(PLUS, tokens.get(0).type);
        assertEquals(MINUS, tokens.get(1).type);
        assertEquals(STAR, tokens.get(2).type);
        assertEquals(SLASH, tokens.get(3).type);
    }

    @Test
    public void testMixedTokens() {
        Scanner scanner = new Scanner("var foo = \"hello\" + 123;");
        List<Token> tokens = scanner.scanTokens();

        assertTrue(tokens.size() >= 8);
        assertEquals(VAR, tokens.get(0).type);
        assertEquals(IDENTIFIER, tokens.get(1).type);
        assertEquals(EQUAL, tokens.get(2).type);
        assertEquals(STRING_START, tokens.get(3).type);
        assertEquals(PLUS, tokens.get(6).type);
        assertEquals(NUMBER, tokens.get(7).type);
    }

    @Test
    public void testLongIdentifier() {
        String longId = "a".repeat(100);
        Scanner scanner = new Scanner(longId);
        List<Token> tokens = scanner.scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(IDENTIFIER, tokens.get(0).type);
        assertEquals(longId, tokens.get(0).lexeme);
    }

    @Test
    public void testLargeNumber() {
        Scanner scanner = new Scanner("999999999.999999999");
        List<Token> tokens = scanner.scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(NUMBER, tokens.get(0).type);
        assertEquals(999999999.999999999, (Double) tokens.get(0).literal, 0.000001);
    }
}
