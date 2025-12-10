package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lox.ast.AstPrinter;
import lox.ast.Expr;
import lox.interpreter.Interpreter;
import lox.interpreter.InterpreterUtil;
import lox.interpreter.RuntimeError;
import lox.parser.Parser;
import lox.scanner.Location;
import lox.scanner.Scanner;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Logger logger = System.getLogger(Lox.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        logger.log(Logger.Level.INFO, "Running source:\n" + source);

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        logger.log(Logger.Level.INFO, "Tokens:" + tokens);

        Parser parser = new Parser(tokens);
        Expr result = parser.parse();
        logger.log(Logger.Level.INFO, "AST:" + new AstPrinter().print(result));
        if (result != null) {
            System.out.println(InterpreterUtil.stringify(new Interpreter().interpret(result)));
        }
    }

    public static void error(Location loc, String message) {
        report(loc, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.loc, "at end", message);
        } else {
            report(token.loc, "at '" + token.lexeme + "'", message);
        }
    }

    private static void report(Location loc, String where,
            String message) {
        System.err.println(String.format("[line: %d, col: %d] Error %s: %s", loc.line(), loc.col(), where, message));
        hadError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(
                String.format("%s\n[line: %d, col: %d]", error.getMessage(), error.token.loc.line(),
                        error.token.loc.col()));
        hadRuntimeError = true;
    }

}
