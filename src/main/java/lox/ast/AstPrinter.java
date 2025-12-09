package lox.ast;

import lox.ast.Expr.Binary;
import lox.ast.Expr.Grouping;
import lox.ast.Expr.Literal;
import lox.ast.Expr.Trinary;
import lox.ast.Expr.Unary;

public class AstPrinter implements Expr.Visitor<String> {
    private int depth;

    public String print(Expr expr) {
        depth = 0;
        return expr.accept(this);
    }

    @Override
    public String visitTrinaryExpr(Trinary expr) {
        return tree(expr.operator1.lexeme, expr.left, expr.mid) + '\n' + tree(expr.operator2.lexeme, expr.right);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return tree(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return tree("Group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        return tree(expr == null ? "nil" : expr.value.toString());
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return tree(expr.operator.lexeme, expr.right);
    }

    private void appendDepth(StringBuilder sb) {
        for (int i = 0; i < depth - 1; i++)
            sb.append("  ");
        if (depth > 0)
            sb.append("| ");
    }

    private String tree(String name, Expr... exprs) {
        StringBuilder sb = new StringBuilder();
        appendDepth(sb);
        sb.append(name);

        if (exprs.length > 0)
            depth += 1;

        for (Expr expr : exprs) {
            sb.append('\n');
            sb.append(expr.accept(this));
        }

        if (exprs.length > 0)
            depth -= 1;

        return sb.toString();
    }

}
