package lox.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lox.Token;
import lox.Lox;
import lox.TokenType;
import lox.ast.Expr;
import lox.ast.Stmt;

import static lox.TokenType.*;

public class Parser {
    private final static OperatorRegistry OPERATOR_REGISTRY = new OperatorRegistry();

    public static class ParseError extends RuntimeException {
        ParseError(String message) {
            super(message);
        }
    }

    private final List<Token> tokens;
    private int current = 0;

    static {
        OPERATOR_REGISTRY.registerRightInfixOperator(EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(COMMA);
        OPERATOR_REGISTRY.registerRightInfixOperator(QUESTION_MARK);
        OPERATOR_REGISTRY.registerLeftInfixOperator(BANG_EQUAL, EQUAL_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(PLUS, MINUS);
        OPERATOR_REGISTRY.registerLeftInfixOperator(STAR, SLASH);
        OPERATOR_REGISTRY.registerPrefixOperator(BANG, MINUS);
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() throws ParseError {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Stmt statement() {
        try {
            if (match(PRINT, VAR)) {
                TokenType prev_type = previous().type;
                switch (prev_type) {
                    case VAR:
                        return varDeclaration();
                    case PRINT:
                        return printStatement();
                    default:
                        break;
                }
            }

            return expressionStatement();
        } catch (ParseError e) {
            synchronize();
            throw e;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expr();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt printStatement() {
        Expr value = expr();
        consume(SEMICOLON, "Expect ; after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr value = expr();
        consume(SEMICOLON, "Expect ; after value.");
        return new Stmt.Expression(value);
    }

    /**
     * Parses the next expression using a minimum binding power of 0.
     * 
     * @return The parsed {@link Expr}
     */
    private Expr expr() {
        return expr(0);
    }

    /**
     * Parses the next expression using the minimum binding power supplied.
     * The parsing will stop when an infix or postfix operator is encountered with a
     * lower left binding power than the minimum binding power provided, or when the
     * end is reached, or when an error occurs.
     * 
     * @param min_bp
     * @return
     */
    private Expr expr(int min_bp) {
        Token next = advance();
        Expr lhs = switch (next.type) {
            case NUMBER, STRING ->
                new Expr.Literal(next.literal);
            case IDENTIFIER ->
                new Expr.Variable(next);
            case INTERP_START -> {
                Expr next_lhs = expr();
                expect(INTERP_END);
                yield new Expr.TemplateLiteral(next_lhs);
            }
            case TRUE ->
                new Expr.Literal(true);
            case FALSE ->
                new Expr.Literal(false);
            case NIL ->
                new Expr.Literal(null);
            case LEFT_PAREN -> {
                Expr next_lhs = expr();
                expect(RIGHT_PAREN);
                yield next_lhs;
            }
            case STRING_START -> {
                List<Expr> templates = new ArrayList<>();
                boolean is_template = false;
                while (!match(STRING_END)) {
                    Expr next_expr = expr();
                    if (next_expr instanceof Expr.TemplateLiteral) {
                        is_template = true;
                    }
                    templates.add(next_expr);
                }

                if (is_template) {
                    yield new Expr.StringTemplate(templates);
                }
                assert (templates.size() == 1);
                yield (Expr.Literal) templates.get(0);
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
                        Expr mhs = expr();
                        expect(COLON);
                        Token colon_token = previous();
                        rhs = expr(infixOp.rbp);
                        lhs = new Expr.Ternary(lhs, op_token, mhs, colon_token, rhs);
                        break;
                    case EQUAL:
                        if (!(lhs instanceof Expr.Variable))
                            throw error(op_token,
                                    "Invalid assignment target, expected identifier on left hand side of EQUAL");

                        rhs = expr(infixOp.rbp);
                        lhs = new Expr.Assign((Expr.Variable) lhs, rhs);
                    default:
                        rhs = expr(infixOp.rbp);
                        lhs = new Expr.Binary(lhs, op_token, rhs);
                        break;
                }
            } else
                // Infix op not found
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

    private void expect(TokenType type) {
        if (!match(type))
            throw error(peek(), "Unexpected token, expected " + type.name());
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

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError(message);
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
                    advance();
            }
        }
    }
}
