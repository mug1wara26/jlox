package lox.ast;

import java.util.List;

import lox.Token;

public abstract class Stmt {
  private static final AstPrinter PRINTER = new AstPrinter();
  public interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitIfStmt(If stmt);
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
  }
  public static class Block extends Stmt {
    public Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    public final List<Stmt> statements;
  }
  public static class If extends Stmt {
    public If(Expr condition, Stmt consequent, Stmt alternate) {
      this.condition = condition;
      this.consequent = consequent;
      this.alternate = alternate;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    public final Expr condition;
    public final Stmt consequent;
    public final Stmt alternate;
  }
  public static class Expression extends Stmt {
    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    public final Expr expression;
  }
  public static class Print extends Stmt {
    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    public final Expr expression;
  }
  public static class Var extends Stmt {
    public Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    public final Token name;
    public final Expr initializer;
  }

  public abstract <R> R accept(Visitor<R> visitor);

  @Override
  public String toString() {
    return PRINTER.print(this);
  }
}
