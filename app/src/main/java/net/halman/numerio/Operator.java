package net.halman.numerio;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Operator implements Serializable {
    private char operator;
    static final String operators = "+-÷×^√";

    public Operator(char operator) {
        set(operator);
    }

    public Operator(Operator operator) {
        set(operator.operator);
    }

    public char get() {
        return operator;
    }

    @NotNull
    @Override
    public String toString() {
        return Character.toString(operator);
    }

    @NotNull
    public String toDisplayString() {
        switch (operator) {
            case '^':
                return "xʸ";
            case '√':
                return "ʸ√¯";
            default:
                return toString();
        }
    }

    public void set(char operator) {
        if (operator == '*') {
            operator = '×';
        }

        if (operator == '/') {
            operator = '÷';
        }

        if (operator == '&') {
            operator = '√';
        }

        if (operators.indexOf(operator) >= 0) {
            this.operator = operator;
        } else {
            this.operator = ' ';
        }
    }

    public int priority()
    {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '×':
            case '÷':
                return 2;
            case '^':
            case '√':
                return 3;
            default:
                return 0;
        }
    }
}
