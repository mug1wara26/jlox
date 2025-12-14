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
}
