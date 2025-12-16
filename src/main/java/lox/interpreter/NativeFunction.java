package lox.interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static lox.interpreter.LoxType.*;

/**
 * Registry for all native functions available in the Lox interpreter.
 */
public class NativeFunction {

    /**
     * Registers all native functions into the provided environment.
     * 
     * @param environment The global environment to register functions into
     */
    public static void registerAll(Environment environment) {
        environment.define("clock", new ClockFunction());
        environment.define("arrayLength", new ArrayLengthFunction());
        environment.define("floor", new FloorFunction());
        environment.define("stringSplit", new StringSplitFunction());
        environment.define("stringToNumber", new StringToNumberFunction());
        environment.define("read", new ReadFunction());
    }

    private static class ClockFunction extends LoxCallable {
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double) System.currentTimeMillis() / 1000.0;
        }

        @Override
        public String toString() {
            return "<native fn clock>";
        }
    }

    private static class ArrayLengthFunction extends LoxCallable {
        ArrayLengthFunction() {
            super(1, new LoxType[] { ARRAY });
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double) ((Object[]) arguments.get(0)).length;
        }

        @Override
        public String toString() {
            return "<native fn arrayLength>";
        }
    }

    private static class FloorFunction extends LoxCallable {
        FloorFunction() {
            super(1, new LoxType[] { NUMBER });
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.floor((Double) arguments.get(0));
        }

        @Override
        public String toString() {
            return "<native fn floor>";
        }
    }

    private static class StringSplitFunction extends LoxCallable {
        StringSplitFunction() {
            super(2, new LoxType[] { STRING, STRING });
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return ((String) arguments.get(0)).split((String) arguments.get(1));
        }

        @Override
        public String toString() {
            return "<native fn stringSplit>";
        }
    }

    private static class StringToNumberFunction extends LoxCallable {
        StringToNumberFunction() {
            super(1, new LoxType[] { STRING });
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            try {
                return Double.valueOf((String) arguments.get(0));
            } catch (NumberFormatException e) {
                throw new RuntimeError("Cannot convert '" + arguments.get(0) + "' to number.");
            }
        }

        @Override
        public String toString() {
            return "<native fn stringToNumber>";
        }
    }

    private static class ReadFunction extends LoxCallable {
        ReadFunction() {
            super(1, new LoxType[] { STRING });
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Path filePath = Paths.get((String) arguments.get(0));
            try {
                return Files.readString(filePath);
            } catch (IOException e) {
                throw new RuntimeError("Could not open file " + arguments.get(0));
            }
        }

        @Override
        public String toString() {
            return "<native fn read>";
        }
    }
}
