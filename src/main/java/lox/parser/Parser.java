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
        OPERATOR_REGISTRY.registerLeftInfixOperator(PLUS, MINUS);
        OPERATOR_REGISTRY.registerLeftInfixOperator(STAR, SLASH);
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
            default -> throw error(next, "Unexpected token");
        };

        loop: while (true) {
            if (isAtEnd())
                break;

            Optional<Operator> op = OPERATOR_REGISTRY.getOperator(peek().type);
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
