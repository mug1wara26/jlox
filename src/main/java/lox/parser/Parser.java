package lox.parser;

import java.util.List;
import java.util.Optional;

import lox.Token;
import lox.Lox;
import lox.TokenType;
import lox.ast.Expr;

import static lox.TokenType.*;

public class Parser {
    private final static OperatorRegistry OPERATOR_REGISTRY = new OperatorRegistry();

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    static {
        OPERATOR_REGISTRY.registerLeftInfixOperator(BANG_EQUAL, EQUAL_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(PLUS, MINUS);
        OPERATOR_REGISTRY.registerLeftInfixOperator(STAR, SLASH);
        OPERATOR_REGISTRY.registerPrefixOperator(BANG, MINUS);
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        try {
            return expr(0);
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expr(int min_bp) throws ParseError {
        Token next = advance();
        Expr lhs = switch (next.type) {
            case IDENTIFIER, NUMBER, STRING ->
                new Expr.Literal(next.literal);
            case TRUE ->
                new Expr.Literal(true);
            case FALSE ->
                new Expr.Literal(false);
            case NIL ->
                new Expr.Literal(null);
            case LEFT_PAREN -> {
                Expr next_lhs = expr(0);
                Token t = advance();
                if (t.type != RIGHT_PAREN)
                    throw error(next, "Unexpected token");
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

            Optional<Operator> op = OPERATOR_REGISTRY.getInfixOperator(peek().type);
            if (op.isPresent()) {
                switch (op.get()) {
                    case Operator.InfixOperator infixOp:
                        if (infixOp.lbp < min_bp) {
                            break loop;
                        }
                        Token op_token = advance();
                        Expr rhs = expr(infixOp.rbp);
                        lhs = new Expr.Binary(lhs, op_token, rhs);
                        break;

                    default:
                        break;
                }
            } else {
                break;
            }
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
        if (!isAtEnd())
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
}
