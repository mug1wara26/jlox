package lox.interpreter;

import java.util.List;

abstract class LoxCallable {
    final int arity;
    final LoxType[] argumentTypes;

    LoxCallable() {
        this(0, new LoxType[0]);
    }

    LoxCallable(int arity, LoxType[] argumentTypes) {
        this.arity = arity;
        this.argumentTypes = argumentTypes;
    }

    abstract Object call(Interpreter interpreter, List<Object> arguments);
}
