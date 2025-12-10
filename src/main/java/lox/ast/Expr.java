package lox.ast;

import java.util.List;

import lox.Token;

public abstract class Expr {
  public interface Visitor<R> {
    R visitTernaryExpr(Ternary expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitTemplateLiteralExpr(TemplateLiteral expr);
    R visitStringTemplateExpr(StringTemplate expr);
    R visitUnaryExpr(Unary expr);
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

  public abstract <R> R accept(Visitor<R> visitor);
}
