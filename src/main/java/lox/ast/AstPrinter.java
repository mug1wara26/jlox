package lox.ast;

import java.util.ArrayList;
import java.util.List;

import lox.ast.Expr.ArrayAccess;
import lox.ast.Expr.Assign;
import lox.ast.Expr.Binary;
import lox.ast.Expr.Call;
import lox.ast.Expr.Grouping;
import lox.ast.Expr.Literal;
import lox.ast.Expr.Logical;
import lox.ast.Expr.StringTemplate;
import lox.ast.Expr.TemplateLiteral;
import lox.ast.Expr.Ternary;
import lox.ast.Expr.Unary;
import lox.ast.Expr.Variable;
import lox.ast.Stmt.Block;
import lox.ast.Stmt.Break;
import lox.ast.Stmt.Continue;
import lox.ast.Stmt.Expression;
import lox.ast.Stmt.Function;
import lox.ast.Stmt.If;
import lox.ast.Stmt.Print;
import lox.ast.Stmt.Var;
import lox.ast.Stmt.While;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    private int depth;

    public String print(List<Stmt> stmts) {
        StringBuilder sb = new StringBuilder();
        stmts.forEach(x -> {
            sb.append('\n');
            sb.append(x.accept(this));
        });

        sb.delete(0, 1);
        return sb.toString();
    }

    public String print(Stmt stmt) {
        return stmt.accept(this);
    }

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitTernaryExpr(Ternary expr) {
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

    @Override
    public String visitTemplateLiteralExpr(TemplateLiteral expr) {
        return tree("${}", expr.expression);
    }

    @Override
    public String visitStringTemplateExpr(StringTemplate expr) {
        Expr[] exprs = {};
        return tree("Template String", expr.templates.toArray(exprs));
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        return tree(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitExpressionStmt(Expression stmt) {
        return tree("Expression", stmt.expression);
    }

    @Override
    public String visitPrintStmt(Print stmt) {
        return tree("Print", stmt.expression);
    }

    @Override
    public String visitVarStmt(Var stmt) {
        return tree("Var Decl " + stmt.name.lexeme, stmt.initializer);
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return tree("Identifier " + expr.name.lexeme);
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        return tree(expr.identifier.lexeme + " =", expr.value);
    }

    @Override
    public String visitBlockStmt(Block stmt) {
        String s = tree("Block\n");
        depth += 1;
        s += print(stmt.statements);
        depth -= 1;

        return s;
    }

    @Override
    public String visitIfStmt(If stmt) {
        String s = tree("if", stmt.condition) + '\n';
        depth += 1;
        s += print(stmt.consequent);

        if (stmt.alternate != null)
            s += '\n' + print(stmt.alternate);
        depth -= 1;

        return s;
    }

    @Override
    public String visitWhileStmt(While stmt) {
        String s = tree("while", stmt.condition);
        depth += 1;
        s += print(stmt.body);
        depth -= 1;

        return s;
    }

    @Override
    public String visitCallExpr(Call expr) {
        List<Expr> exprs = new ArrayList<>();
        exprs.add(expr.callee);
        exprs.addAll(expr.arguments);

        return tree("call", exprs.toArray(new Expr[] {}));
    }

    @Override
    public String visitBreakStmt(Break stmt) {
        return tree("Break");
    }

    @Override
    public String visitContinueStmt(Continue stmt) {
        return tree("Continue");
    }

    @Override
    public String visitArrayAccessExpr(ArrayAccess expr) {
        return tree("arrayAccess", expr.array, expr.index);
    }

    @Override
    public String visitFunctionStmt(Function stmt) {
        StringBuilder ret = new StringBuilder(tree(String.format("Fun Decl %s (%s)", stmt.name,
                String.join(",", stmt.params.stream().map(x -> x.lexeme).toList()))));
        depth += 1;
        for (Stmt statement : stmt.body) {
            ret.append(print(statement));
        }
        depth -= 1;

        return ret.toString();
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
            sb.append(expr == null ? tree("null") : expr.accept(this));
        }

        if (exprs.length > 0)
            depth -= 1;

        return sb.toString();
    }
}
