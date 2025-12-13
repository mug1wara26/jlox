package lox.interpreter;

import lox.ast.Stmt;
import lox.ast.Stmt.Block;
import lox.ast.Stmt.Expression;
import lox.ast.Stmt.If;
import lox.ast.Stmt.Print;
import lox.ast.Stmt.Var;
import lox.ast.Expr;
import lox.ast.Expr.Assign;
import lox.ast.Expr.Binary;
import lox.ast.Expr.Grouping;
import lox.ast.Expr.Literal;
import lox.ast.Expr.Logical;
import lox.ast.Expr.StringTemplate;
import lox.ast.Expr.TemplateLiteral;
import lox.ast.Expr.Ternary;
import lox.ast.Expr.Unary;
import lox.ast.Expr.Variable;

import static lox.interpreter.InterpreterUtil.*;

import java.util.List;

import lox.Lox;
import lox.TokenType;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment(Environment.GLOBAL_ENVIRONMENT);

    /**
     * Loops over statements and interprets them using a tree walking interpreter.
     * 
     * @param statements The {@link Stmt}s to interpret
     */
    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private void executeBlock(List<Stmt> statements, Environment new_environment) {
        Environment previous = environment;

        try {
            environment = new_environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            environment = previous;
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitTernaryExpr(Ternary expr) {
        if (isTruthy(evaluate(expr.left)))
            return evaluate(expr.mid);
        return evaluate(expr.right);
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case COMMA:
                return right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left + (Double) right;
                }

                if (left instanceof Double && right instanceof String) {
                    return stringify((Double) left) + (String) right;
                }

                if (left instanceof String && right instanceof Double) {
                    return (String) left + stringify((Double) right);
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;

            default:
                return null;
        }
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                return null;
        }
    }

    @Override
    public Object visitTemplateLiteralExpr(TemplateLiteral expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitStringTemplateExpr(StringTemplate expr) {
        StringBuilder sb = new StringBuilder();

        for (Expr e : expr.templates)
            sb.append(stringify(evaluate(e)));

        return sb.toString();
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        // Similar to lisp style, return false if the logical expr evaluates to false
        Object lhs = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(lhs))
                return lhs;
        } else if (!isTruthy(lhs))
            return false;

        Object rhs = evaluate(expr.right);

        return isTruthy(rhs) ? rhs : false;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        System.out.println(stringify(evaluate(stmt.expression)));
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.identifier, value);

        return value;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition)))
            execute(stmt.consequent);
        else if (stmt.alternate != null)
            execute(stmt.alternate);
        return null;
    }
}
