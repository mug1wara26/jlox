package lox.parser;

import java.util.ArrayList;
import java.util.Arrays;
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
        OPERATOR_REGISTRY.registerLeftInfixOperator(COMMA);
        OPERATOR_REGISTRY.registerRightInfixOperator(EQUAL);
        OPERATOR_REGISTRY.registerRightInfixOperator(QUESTION_MARK);
        OPERATOR_REGISTRY.registerLeftInfixOperator(OR);
        OPERATOR_REGISTRY.registerLeftInfixOperator(AND);
        OPERATOR_REGISTRY.registerLeftInfixOperator(BANG_EQUAL, EQUAL_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
        OPERATOR_REGISTRY.registerLeftInfixOperator(PLUS, MINUS);
        OPERATOR_REGISTRY.registerLeftInfixOperator(STAR, SLASH);
        OPERATOR_REGISTRY.registerPrefixOperator(BANG, MINUS);
        OPERATOR_REGISTRY.registerPostfixOperator(LEFT_SQUARE);
        OPERATOR_REGISTRY.registerPostfixOperator(LEFT_PAREN);
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() throws ParseError {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) {
                TokenType prev_type = previous().type;
                switch (prev_type) {
                    case VAR:
                        return varDeclaration();
                    default:
                        break;
                }
            }

            return statement();
        } catch (ParseError e) {
            synchronize();
            throw e;
        }
    }

    private Stmt statement() {
        if (match(PRINT, LEFT_BRACE, IF, WHILE, FOR, BREAK, CONTINUE)) {
            TokenType prev_type = previous().type;
            switch (prev_type) {
                case PRINT:
                    return printStatement();
                case LEFT_BRACE:
                    return blockStatement();
                case IF:
                    return ifStatement();
                case WHILE:
                    return whileStatement();
                case FOR:
                    return forStatement();
                case BREAK:
                    return breakStatement();
                case CONTINUE:
                    return continueStatement();
                default:
                    break;
            }
        }

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after for.");
        Stmt init = null;
        if (!match(SEMICOLON))
            init = match(VAR) ? varDeclaration() : expressionStatement();

        Expr condition = new Expr.Literal(true);
        if (!match(SEMICOLON))
            condition = expr();
        consume(SEMICOLON, "Expect ';' after condition in for loop.");

        Expr update = null;
        if (!match(SEMICOLON))
            update = expr();
        consume(RIGHT_PAREN, "Expect ')' after update in for loop.");

        Stmt body = statement();

        if (update != null)
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(update)));

        body = new Stmt.While(condition, body);

        if (init != null)
            body = new Stmt.Block(Arrays.asList(init, body));

        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after while.");
        Expr condition = expr();
        consume(RIGHT_PAREN, "Expect closing ')'.");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after if.");
        Expr condition = expr();
        consume(RIGHT_PAREN, "Expect closing ')'.");

        Stmt consequent = statement();
        Stmt alternate = match(ELSE) ? statement() : null;

        return new Stmt.If(condition, consequent, alternate);
    }

    private Stmt breakStatement() {
        Token keyword = previous();
        consume(SEMICOLON, "Expect ; after break.");
        return new Stmt.Break(keyword);
    }

    private Stmt continueStatement() {
        Token keyword = previous();
        consume(SEMICOLON, "Expect ; after continue.");
        return new Stmt.Continue(keyword);
    }

    private Stmt.Block blockStatement() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' at end of block.");
        return new Stmt.Block(statements);
    }

    private Stmt.Var varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expr();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt.Print printStatement() {
        Expr value = expr();
        consume(SEMICOLON, "Expect ; after value.");
        return new Stmt.Print(value);
    }

    private Stmt.Expression expressionStatement() {
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
            default -> throw error(next, "Unexpected token, got " + next.lexeme);
        };

        while (true) {
            if (isAtEnd())
                break;

            Optional<Operator.PostfixOperator> postOp = OPERATOR_REGISTRY.getPostfixOperator(peek().type);
            if ((postOp).isPresent()) {
                Operator.PostfixOperator op = postOp.get();
                if (op.lbp < min_bp)
                    break;

                Token op_token = advance();

                switch (op_token.type) {
                    case LEFT_PAREN:
                        List<Expr> arguments = new ArrayList<>();
                        if (!check(RIGHT_PAREN)) {
                            do {
                                if (arguments.size() >= 255)
                                    error(peek(), "Can't have more than 255 arguments.");
                                arguments.add(expr(OPERATOR_REGISTRY.getLeftInfixBindingPower(COMMA) + 1));
                            } while (match(COMMA));
                        }

                        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
                        lhs = new Expr.Call(lhs, paren, arguments);
                        continue;
                    case LEFT_SQUARE:
                        Expr index = expr();
                        Token square = consume(RIGHT_SQUARE, "Expect ']' after array index.");
                        lhs = new Expr.ArrayAccess(lhs, square, index);
                        continue;
                    default:
                        throw error(op_token, "Unexpected postfix operator, got " + next.lexeme);
                }
            }

            Optional<Operator.InfixOperator> infixOp = OPERATOR_REGISTRY.getInfixOperator(peek().type);
            if (infixOp.isPresent()) {
                Operator.InfixOperator op = infixOp.get();
                if (op.lbp < min_bp) {
                    break;
                }

                Token op_token = advance();
                Expr rhs = null;

                switch (op.operator) {
                    case QUESTION_MARK:
                        Expr mhs = expr();
                        expect(COLON);
                        Token colon_token = previous();
                        rhs = expr(op.rbp);
                        lhs = new Expr.Ternary(lhs, op_token, mhs, colon_token, rhs);
                        continue;
                    case EQUAL:
                        if (!(lhs instanceof Expr.Variable))
                            throw error(op_token,
                                    "Invalid assignment target, expected identifier on left hand side of EQUAL");

                        rhs = expr(op.rbp);
                        lhs = new Expr.Assign(((Expr.Variable) lhs).name, rhs);
                        continue;
                    default:
                        rhs = expr(op.rbp);
                        lhs = new Expr.Binary(lhs, op_token, rhs);
                        continue;
                }
            } else
                // op not found
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
