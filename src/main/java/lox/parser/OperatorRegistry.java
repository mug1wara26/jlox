package lox.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lox.TokenType;

public class OperatorRegistry {
    private int power = 1;
    private Map<TokenType, Operator> operators = new HashMap<>();

    private int increasePower() {
        return power++;
    }

    void registerLeftInfixOperator(TokenType... tokens) {
        int lbp = increasePower();
        int rbp = increasePower();
        for (TokenType token : tokens) {
            operators.put(token, new Operator.InfixOperator(token, lbp, rbp));
        }
    }

    void registerRightInfixOperator(TokenType... tokens) {
        int rbp = increasePower();
        for (TokenType token : tokens) {
            operators.put(token, new Operator.InfixOperator(token, -1, rbp));
        }
    }

    void registerPrefixOperator(TokenType... tokens) {
        int rbp = increasePower();
        for (TokenType token : tokens) {
            operators.put(token, new Operator.PrefixOperator(token, rbp));
        }
        increasePower();
    }

    void registerPostfixOperator(TokenType... tokens) {
        int lbp = increasePower();
        for (TokenType token : tokens) {
            operators.put(token, new Operator.PostfixOperator(token, lbp));
        }
        increasePower();
    }

    Optional<Operator> getOperator(TokenType token) {
        return Optional.ofNullable(operators.get(token));
    }
}
