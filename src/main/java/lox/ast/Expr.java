package lox.ast;

import java.util.List;

import lox.Token;

public abstract class Expr {
  private static final AstPrinter PRINTER = new AstPrinter();
  public interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitTernaryExpr(Ternary expr);
    R visitBinaryExpr(Binary expr);
    R visitLogicalExpr(Logical expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitTemplateLiteralExpr(TemplateLiteral expr);
    R visitStringTemplateExpr(StringTemplate expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
  }
  public static class Assign extends Expr {
    public Assign(Token identifier, Expr value) {
      this.identifier = identifier;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    public final Token identifier;
    public final Expr value;
  }
  public static class Ternary extends Expr {
    public Ternary(Expr left, Token operator1, Expr mid, Token operator2, Expr right) {
      this.left = left;
      this.operator1 = operator1;
      this.mid = mid;
      this.operator2 = operator2;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }

    public final Expr left;
    public final Token operator1;
    public final Expr mid;
    public final Token operator2;
    public final Expr right;
  }
  public static class Binary extends Expr {
    public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    public final Expr left;
    public final Token operator;
    public final Expr right;
  }
  public static class Logical extends Expr {
    public Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    public final Expr left;
    public final Token operator;
    public final Expr right;
  }
  public static class Grouping extends Expr {
    public Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    public final Expr expression;
  }
  public static class Literal extends Expr {
    public Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    public final Object value;
  }
  public static class TemplateLiteral extends Expr {
    public TemplateLiteral(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitTemplateLiteralExpr(this);
    }

    public final Expr expression;
  }
  public static class StringTemplate extends Expr {
    public StringTemplate(List<Expr> templates) {
      this.templates = templates;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitStringTemplateExpr(this);
    }

    public final List<Expr> templates;
  }
  public static class Unary extends Expr {
    public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    public final Token operator;
    public final Expr right;
  }
  public static class Variable extends Expr {
    public Variable(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    public final Token name;
  }

  public abstract <R> R accept(Visitor<R> visitor);

  @Override
  public String toString() {
    return PRINTER.print(this);
  }
}
