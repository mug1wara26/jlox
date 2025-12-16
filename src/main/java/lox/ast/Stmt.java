package lox.ast;

import java.util.List;

import lox.Token;

public abstract class Stmt {
  private static final AstPrinter PRINTER = new AstPrinter();
  public interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitIfStmt(If stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitReturnStmt(Return stmt);
    R visitPrintStmt(Print stmt);
    R visitWhileStmt(While stmt);
    R visitBreakStmt(Break stmt);
    R visitContinueStmt(Continue stmt);
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
  public static class Function extends Stmt {
    public Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;
  }
  public static class Return extends Stmt {
    public Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    public final Token keyword;
    public final Expr value;
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
  public static class While extends Stmt {
    public While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    public final Expr condition;
    public final Stmt body;
  }
  public static class Break extends Stmt {
    public Break(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBreakStmt(this);
    }

    public final Token keyword;
  }
  public static class Continue extends Stmt {
    public Continue(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitContinueStmt(this);
    }

    public final Token keyword;
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
