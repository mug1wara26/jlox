package lox.interpreter;

import java.util.List;

import lox.ast.Stmt;

public class LoxFunction extends LoxCallable {
    private final Stmt.Function declaration;
    private final Environment environment;

    LoxFunction(Stmt.Function declaration, Environment declarationEnvironment) {
        super(declaration.params.size());
        this.declaration = declaration;
        this.environment = declarationEnvironment;
    }

    @Override
    Object call(Interpreter interpreter, List<Object> arguments) {
        Environment extended_environment = new Environment(environment);
        for (int i = 0; i < argumentTypes.length; i++) {
            extended_environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, extended_environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("<fn %s>", declaration.name.lexeme);
    }
}
