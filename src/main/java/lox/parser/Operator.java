package lox.parser;

import lox.TokenType;

abstract class Operator {
    final TokenType operator;
    // Left binding power
    final int lbp;
    // Right binding power
    final int rbp;

    Operator(TokenType operator, int lbp, int rbp) {
        this.operator = operator;
        this.lbp = lbp;
        this.rbp = rbp;
    }

    static class InfixOperator extends Operator {
        InfixOperator(TokenType operator, int lbp, int rbp) {
            super(operator, lbp, rbp);
        }
    }

    static class PrefixOperator extends Operator {
        PrefixOperator(TokenType operator, int rbp) {
            super(operator, -1, rbp);
        }
    }

    static class PostfixOperator extends Operator {
        PostfixOperator(TokenType operator, int lbp) {
            super(operator, lbp, -1);
        }
    }
}
