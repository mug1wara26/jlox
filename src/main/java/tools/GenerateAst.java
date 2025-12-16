package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

// Totally copied from craftinginterpreters, not gonna touch this
public class GenerateAst {
    public static final String OUTPUT_DIR = "lox/ast";

    public static void main(String[] args) throws IOException {
        defineAst(OUTPUT_DIR, "Expr", Arrays.asList(
                "Assign         : Token identifier, Expr value",
                "Ternary        : Expr left, Token operator1, Expr mid, Token operator2, Expr right",
                "Binary         : Expr left, Token operator, Expr right",
                "Call           : Expr callee, Token paren, List<Expr> arguments",
                "ArrayAccess    : Expr array, Token square, Expr index",
                "Logical        : Expr left, Token operator, Expr right",
                "Grouping       : Expr expression",
                "Literal        : Object value",
                "TemplateLiteral: Expr expression",
                "StringTemplate : List<Expr> templates",
                "Unary          : Token operator, Expr right",
                "Variable       : Token name"));

        defineAst(OUTPUT_DIR, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "If         : Expr condition, Stmt consequent, Stmt alternate",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "Print      : Expr expression",
                "While      : Expr condition, Stmt body",
                "Break      : Token keyword",
                "Continue   : Token keyword",
                "Var        : Token name, Expr initializer"));
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package lox.ast;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("import lox.Token;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");
        writer.println("  private static final AstPrinter PRINTER = new AstPrinter();");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println();
        writer.println("  @Override");
        writer.println("  public String toString() {");
        writer.println("    return PRINTER.print(this);");
        writer.println("  }");

        writer.println("}");
        writer.close();
    }

    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
        writer.println("  public static class " + className + " extends " +
                baseName + " {");

        // Constructor.
        writer.println("    public " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            if (field.length() != 0) {
                String name = field.split(" ")[1];
                writer.println("      this." + name + " = " + name + ";");
            }
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            if (field.length() != 0)
                writer.println("    public final " + field + ";");
        }

        writer.println("  }");
    }

    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
}
