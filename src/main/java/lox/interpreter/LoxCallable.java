package lox.interpreter;

import java.util.Collections;
import java.util.List;

abstract class LoxCallable {
    final int arity;
    final LoxType[] argumentTypes;

    LoxCallable() {
        this(0, new LoxType[0]);
    }

    LoxCallable(int arity) {
        this.arity = arity;
        argumentTypes = new LoxType[arity];
        Collections.nCopies(arity, LoxType.ANY).toArray(argumentTypes);
    }

    LoxCallable(int arity, LoxType[] argumentTypes) {
        this.arity = arity;
        this.argumentTypes = argumentTypes;
    }

    abstract Object call(Interpreter interpreter, List<Object> arguments);
}
