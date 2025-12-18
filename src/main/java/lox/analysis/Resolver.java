package lox.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lox.Lox;
import lox.Token;
import lox.ast.Expr;
import lox.ast.Expr.*;
import lox.ast.Stmt;
import lox.ast.Stmt.*;
import lox.interpreter.Interpreter;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    public class ResolverVariable {
        public boolean is_resolved;
        public final int index;

        public ResolverVariable(boolean is_resolved, int index) {
            this.is_resolved = is_resolved;
            this.index = index;
        }
    }

    private final Interpreter interpreter;
    private final Stack<Map<String, ResolverVariable>> scopes = new Stack<>();

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolveProgram(List<Stmt> statements) {
        beginScope();
        resolve(statements);
        endScope();
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements)
            resolve(statement);
    }

    void resolve(Stmt statement) {
        statement.accept(this);
    }

    void resolve(Expr expression) {
        expression.accept(this);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i, scopes.get(i).get(name.lexeme).index);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function) {
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
    }

    public void beginScope() {
        scopes.push(new HashMap<>());
    }

    public void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;

        Map<String, ResolverVariable> scope = scopes.peek();
        scope.put(name.lexeme, new ResolverVariable(false, scope.size()));
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        Map<String, ResolverVariable> scope = scopes.peek();
        ResolverVariable v = scope.get(name.lexeme);
        v.is_resolved = true;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        declare(stmt.name);

        if (stmt.initializer != null)
            resolve(stmt.initializer);

        define(stmt.name);
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        if (!scopes.empty()
                && scopes.peek().containsKey(expr.name.lexeme)
                && !scopes.peek().get(expr.name.lexeme).is_resolved)
            Lox.error(expr.name, "Can't read local variable in its own initializer.");

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.identifier);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.consequent);
        if (stmt.alternate != null)
            resolve(stmt.alternate);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if (stmt.value != null)
            resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Continue stmt) {
        return null;
    }

    @Override
    public Void visitTernaryExpr(Ternary expr) {
        resolve(expr.condition);
        resolve(expr.consequent);
        resolve(expr.alternate);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        resolve(expr.callee);
        for (Expr arg : expr.arguments) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccess expr) {
        resolve(expr.array);
        resolve(expr.index);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitTemplateLiteralExpr(TemplateLiteral expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitTemplateStringExpr(TemplateString expr) {
        for (Expr template : expr.templates)
            resolve(template);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }
}
