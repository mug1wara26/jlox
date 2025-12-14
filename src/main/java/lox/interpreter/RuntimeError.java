package lox.interpreter;

import lox.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public RuntimeError(String message) {
        super(message);
        this.token = null;
    }
}
