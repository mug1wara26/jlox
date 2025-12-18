package lox.interpreter;

import java.util.ArrayList;
import java.util.List;

import lox.Token;

public class Environment {
    private static class Tombstone {
    }

    private final static Tombstone TOMBSTONE = new Tombstone();

    final Environment enclosing;
    private List<Object> values = new ArrayList<>();

    Environment() {
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Environment ancestor(int depth) {
        Environment environment = this;
        for (int i = 0; i < depth; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    void define(Object value) {
        values.add(value);
    }

    Object get(Token name, int index) {
        if (values.size() > index && values.get(index) != TOMBSTONE)
            return values.get(index);

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    Object getAt(int depth, int index, Token name) {
        return ancestor(depth).get(name, index);
    }

    void assign(Token name, int index, Object value) {
        if (values.size() > index) {
            values.set(index, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void assignAt(int depth, int index, Token name, Object value) {
        ancestor(depth).assign(name, index, value);
    }
}
