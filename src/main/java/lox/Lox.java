package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lox.ast.AstPrinter;
import lox.ast.Stmt;
import lox.interpreter.Interpreter;
import lox.interpreter.RuntimeError;
import lox.parser.Parser;
import lox.parser.Parser.ParseError;
import lox.scanner.Location;
import lox.scanner.Scanner;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Logger logger = System.getLogger(Lox.class.getName());
    private static Interpreter interpreter;

    public static void main(String[] args) throws IOException {
        interpreter = new Interpreter();
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Reads from a file and runs the interpreter against the text in the file.
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    /**
     * Starts the REPL using stdin
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        interpreter.is_repl = true;

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            if (!line.endsWith(";"))
                line += ';';
            run(line);

            hadError = false;
            hadRuntimeError = false;
        }
    }

    /**
     * Runs the interpreter against the source code provided.
     * 
     * @param source Lox code to run
     */
    private static void run(String source) {
        logger.log(Logger.Level.INFO, "Running source:\n" + source);

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        logger.log(Logger.Level.DEBUG, "Tokens:" + tokens);

        try {

            Parser parser = new Parser(tokens);
            List<Stmt> result = parser.parse();
            logger.log(Logger.Level.DEBUG, "AST:\n" + new AstPrinter().print(result));
            interpreter.interpret(result);
        } catch (ParseError e) {
            logger.log(Logger.Level.INFO, "Parser encountered an error:\n" + e.getMessage());
        }
    }

    /**
     * Reports an error at the location provided using the message provided.
     * 
     * Use {@code error(Token token, String message)} to report an error along with
     * the offending token.
     * 
     * @param loc     The location of the error
     * @param message The error message to report
     */
    public static void error(Location loc, String message) {
        report(loc, "", message);
    }

    /**
     * Reports an error at the location provided, along with the offending token and
     * the message provided.
     * 
     * @param token   The token that caused the error
     * @param message The error message to report
     */
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
        if (error.token == null)
            printError(error.getMessage());
        else
            printError(
                    String.format("%s\n[line: %d, col: %d]", error.getMessage(), error.token.loc.line(),
                            error.token.loc.col()));
        hadRuntimeError = true;
    }

    public static void printError(String message) {
        PrintStream output = interpreter.is_repl ? System.out : System.err;
        output.println(message);
    }
}
