package lox.interpreter;

import java.util.Arrays;

import lox.Token;

public class InterpreterUtil {
    static boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    static boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    static void checkNumberOperands(Token operator, Object... operand) {
        for (Object object : operand) {
            if (!(object instanceof Double)) {
                throw new RuntimeError(operator, "Operand must be a number.");
            }
        }
    }

    public static String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        if (object instanceof Object[])
            return Arrays.toString((Object[]) object);

        return object.toString();
    }

    static boolean matchesType(Object val, LoxType type) {
        return switch (type) {
            case NUMBER -> val instanceof Double;
            case STRING -> val instanceof String;
            case ARRAY -> val instanceof Object[];
            case BOOLEAN -> val instanceof Boolean;
            case ANY -> val instanceof Object;
            case NIL -> val == null;
            default -> false;
        };
    }

    static String getTypeName(Object value) {
        if (value == null)
            return "nil";
        if (value instanceof Double)
            return "number";
        if (value instanceof String)
            return "string";
        if (value instanceof Boolean)
            return "boolean";
        if (value instanceof Object[])
            return "array";
        if (value instanceof LoxCallable)
            return "function";
        return "unknown";
    }
}
