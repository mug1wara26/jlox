package lox.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lox.Token;
import lox.Lox;
import lox.TokenType;
import lox.ast.Expr;
import lox.ast.Expr.Literal;
import lox.ast.Expr.StringTemplate;
import lox.ast.Expr.TemplateLiteral;
import lox.scanner.Location;
import lox.scanner.Scanner;

import static lox.TokenType.*;

public class Parser {
    private final static OperatorRegistry OPERATOR_REGISTRY = new OperatorRegistry();
    private static final Map<Character, Character> ESCAPE_CHARACTERS = new HashMap<>();

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    static {
        OPERATOR_REGISTRY.registerLeftInfixOperator(COMMA);
        OPERATOR_REGISTRY.registerRightInfixOperator(QUESTION_MARK);
        OPERATOR_REGISTRY.registerLeftInfixOperator(BANG_EQUAL, EQUAL_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(PLUS, MINUS);
        OPERATOR_REGISTRY.registerLeftInfixOperator(STAR, SLASH);
        OPERATOR_REGISTRY.registerPrefixOperator(BANG, MINUS);

        ESCAPE_CHARACTERS.put('\"', '\"');
        ESCAPE_CHARACTERS.put('{', '{');
        ESCAPE_CHARACTERS.put('\\', '\\');
        ESCAPE_CHARACTERS.put('n', '\n');
        ESCAPE_CHARACTERS.put('t', '\t');
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        System.out.println(tokens);
        try {
            return expr(0);
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expr(int min_bp) {
        Token next = advance();
        Expr lhs = switch (next.type) {
            case IDENTIFIER, NUMBER ->
                new Expr.Literal(next.literal);
            case STRING ->
                parseString(next, (String) next.literal);
            case TRUE ->
                new Expr.Literal(true);
            case FALSE ->
                new Expr.Literal(false);
            case NIL ->
                new Expr.Literal(null);
            case LEFT_PAREN -> {
                Expr next_lhs = expr(0);
                if (!match(RIGHT_PAREN))
                    throw error(peek(), "Unexpected token, expected RIGHT_PAREN");
                yield next_lhs;
            }
            case MINUS, BANG -> {
                // We know it must be in the registry
                Operator op = OPERATOR_REGISTRY.getPrefixOperator(next.type).get();
                Expr rhs = expr(op.rbp);
                yield new Expr.Unary(next, rhs);
            }
            default -> throw error(next, "Unexpected token");
        };

        loop: while (true) {
            if (isAtEnd())
                break;

            Optional<Operator.InfixOperator> op = OPERATOR_REGISTRY.getInfixOperator(peek().type);
            if (op.isPresent()) {
                Operator.InfixOperator infixOp = op.get();
                if (infixOp.lbp < min_bp) {
                    break loop;
                }

                Token op_token = advance();
                Expr rhs = null;

                switch (infixOp.operator) {
                    case QUESTION_MARK:
                        Expr mhs = expr(0);
                        if (!match(COLON))
                            throw error(peek(), "Unexpected token, expected COLON");
                        Token colon_token = previous();
                        rhs = expr(infixOp.rbp);
                        lhs = new Expr.Ternary(lhs, op_token, mhs, colon_token, rhs);
                        break;
                    default:
                        rhs = expr(infixOp.rbp);
                        lhs = new Expr.Binary(lhs, op_token, rhs);
                        break;
                }
            } else
                break;
        }

        return lhs;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (isAtEnd())
            return peek();
        current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    static Optional<Character> getEscapedCharacter(char c) {
        return Optional.ofNullable(ESCAPE_CHARACTERS.get(c));
    }

    private Expr parseString(Token t, String s) {
        int i = 0;
        Location loc = new Location(t.loc.offset() + 1, t.loc.line(), t.loc.col() + 1);
        boolean is_escaping = false;
        boolean is_interpolating = false;
        List<TemplateLiteral> templates = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        while (i < s.length()) {
            char c = s.charAt(i);
            if (is_escaping) {
                Optional<Character> escaped = getEscapedCharacter(c);
                if (escaped.isPresent())
                    sb.append(escaped);
                else
                    Lox.error(loc, "Invalid escape sequence");
                is_escaping = false;
            } else {
                switch (c) {
                    case '\\':
                        is_escaping = true;
                        break;
                    case '$':
                        is_interpolating = true;
                        break;
                    case '{':
                        if (is_interpolating) {
                            Scanner sub_sc = Scanner.interpolateString(s, i + 1, loc.line(), loc.col() + 1);
                            Parser sub_parser = new Parser(sub_sc.scanTokens());

                            Expr innerExpr = sub_parser.parse();
                            if (innerExpr == null)
                                // Lox.error already called by sub_parser.
                                throw new ParseError();

                            templates.add(new TemplateLiteral(innerExpr, i - 1, sub_sc.getCurrent().offset()));
                            i = sub_sc.getCurrent().offset();
                            loc.bringTo(sub_sc.getCurrent());

                            is_interpolating = false;
                            continue;
                        } else
                            sb.append(c);
                        break;
                    default:
                        if (is_interpolating) {
                            sb.append('$');
                            is_interpolating = false;
                        }
                        sb.append(c);
                        break;
                }
            }

            loc.increment(c);
            i += 1;
        }

        if (is_escaping)
            error(t, "Unterminated string literal");
        if (is_interpolating)
            sb.append('$');

        if (templates.size() == 0)
            return new Literal(sb.toString());
        else
            return new StringTemplate(sb.toString(), templates);
    }
}
