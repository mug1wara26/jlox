package lox.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lox.TokenType;

/**
 * Keeps track of operators and automatically assigns binding power to them.
 * <br/>
 * Operators will always be first assigned an odd binding power for the weakest
 * side.
 * <br/>
 * Operators are inserted into the registry in increasing order of precedence.
 */
public class OperatorRegistry {
    private int power = 1;
    private Map<TokenType, Operator.InfixOperator> infixOperators = new HashMap<>();
    private Map<TokenType, Operator.PrefixOperator> prefixOperators = new HashMap<>();
    private Map<TokenType, Operator.PostfixOperator> postfixOperators = new HashMap<>();

    private int increasePower() {
        return power++;
    }

    void registerLeftInfixOperator(TokenType... tokens) {
        int lbp = increasePower();
        int rbp = increasePower();
        for (TokenType token : tokens) {
            infixOperators.put(token, new Operator.InfixOperator(token, lbp, rbp));
        }
    }

    void registerRightInfixOperator(TokenType... tokens) {
        int rbp = increasePower();
        int lbp = increasePower();
        for (TokenType token : tokens) {
            infixOperators.put(token, new Operator.InfixOperator(token, lbp, rbp));
        }
    }

    void registerPrefixOperator(TokenType... tokens) {
        int rbp = increasePower();
        for (TokenType token : tokens) {
            prefixOperators.put(token, new Operator.PrefixOperator(token, rbp));
        }
        increasePower();
    }

    void registerPostfixOperator(TokenType... tokens) {
        int lbp = increasePower();
        for (TokenType token : tokens) {
            postfixOperators.put(token, new Operator.PostfixOperator(token, lbp));
        }
        increasePower();
    }

    int getLeftInfixBindingPower(TokenType token) {
        return Optional.ofNullable(infixOperators.get(token)).map(x -> x.lbp).orElse(0);
    }

    Optional<Operator.InfixOperator> getInfixOperator(TokenType token) {
        return Optional.ofNullable(infixOperators.get(token));
    }

    Optional<Operator.PrefixOperator> getPrefixOperator(TokenType token) {
        return Optional.ofNullable(prefixOperators.get(token));
    }

    Optional<Operator.PostfixOperator> getPostfixOperator(TokenType token) {
        return Optional.ofNullable(postfixOperators.get(token));
    }
}
