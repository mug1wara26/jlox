package lox.interpreter;

import lox.ast.Expr;
import lox.ast.Expr.Binary;
import lox.ast.Expr.Grouping;
import lox.ast.Expr.Literal;
import lox.ast.Expr.StringTemplate;
import lox.ast.Expr.TemplateLiteral;
import lox.ast.Expr.Ternary;
import lox.ast.Expr.Unary;

import static lox.interpreter.InterpreterUtil.*;

import lox.Lox;

public class Interpreter implements Expr.Visitor<Object> {
    public Object interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            return value;
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
            return null;
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
}
