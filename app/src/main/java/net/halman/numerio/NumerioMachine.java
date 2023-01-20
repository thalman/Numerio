package net.halman.numerio;

import java.io.Serializable;
import java.util.ArrayList;

public class NumerioMachine implements Serializable {
    private enum State {
        INPUT,
        OPERATOR,
        RESULT,
        ERROR
    }

    private final ArrayList<Object> stack = new ArrayList<>();
    private State state = State.INPUT;
    private Number memory = new Number(0);
    private Number repeatNumber = null;
    private Operator repeatOperator = null;
    private Number.DRG drg = Number.DRG.DEG;

    public NumerioMachine() {
        stack.clear();
        stack.add(new Number());
    }

    public Number.DRG getDrg()
    {
        return drg;
    }

    public void nextDrg()
    {
        switch (drg) {
            case DEG:
                drg = Number.DRG.RAD;
                break;
            case RAD:
                drg = Number.DRG.GRAD;
                break;
            case GRAD:
                drg = Number.DRG.DEG;
                break;
        }
    }

    public void toNextDrg()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.toRadians(drg);
            nextDrg();
            n.fromRadians(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void toDms()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.toDms(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void fromDms()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.fromDms(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void numberExp() {
        if (state == State.INPUT) {
            lastNumber().switchEditing();
        }
    }

    public void numberInput(char c)
    {
        clearRepetition();
        switch (state) {
            case INPUT:
                if ((c >= '0' && c <= '9') || c == '.') {
                    Number n = lastNumber();
                    if (n != null) {
                        n.append(c);
                    }
                }
                break;
            case OPERATOR:
                stack.add(new Number(c));
                state = State.INPUT;
                break;
            case RESULT:
                Object o = lastItem();
                if (! (o instanceof Number)) {
                    // This should not happen
                    stack.clear();
                    stack.add(new Number(0));
                    o = lastItem();
                }
                Number n = (Number) o;
                n.set(c);
                state = State.INPUT;
                break;
        }
    }

    public void numberRemove()
    {
        clearRepetition();
        switch (state) {
            case INPUT:
                lastNumber().remove();
                break;
            case RESULT:
                lastNumber().set(0);
                state = State.INPUT;
                break;
        }
    }

    public void plusMinus()
    {
        if (state == State.INPUT || state == State.RESULT) {
            lastNumber().plusMinus();
        }
    }

    public Number numberToDisplay()
    {
        return lastNumber();
    }

    public String numberToDisplayAsString()
    {
        return lastNumber().toString();
    }

    public void operator(char operator) {
        if (state == State.ERROR) {
            return;
        }

        Operator o = new Operator(operator);
        if (o.get() != ' ') {
            if (state == State.OPERATOR) {
                stack.remove(stack.size() - 1);
            }
            state = State.OPERATOR;
            stack.add(o);
        }

        partiallyEvaluate();
    }

    public void parenthesisAdd(char par) {
        if (state == State.ERROR) {
            return;
        }

        Object last = lastItem();
        Parenthesis p = new Parenthesis(par);

        if (p.closing() && parenthesisLevel() == 0) {
            return;
        }

        if (last instanceof Operator && p.opening()) {
            stack.add(p);
            stack.add(new Number(0.0));
            state = State.INPUT;
        }

        if (last instanceof Number && p.opening()) {
            stack.add(stack.size() - 1, p);
        }

        if (last instanceof Number && p.closing()) {
            stack.add(p);
        }

        if (last instanceof Operator && p.closing()) {
            stack.remove(last);
            stack.add(p);
        }

        if (last instanceof Parenthesis) {
            if (((Parenthesis) last).type == p.type) {
                stack.add(p);
            } else {
                stack.remove(last);
            }
        }

        partiallyEvaluate();
    }

    public Number lastNumber()
    {
        int size = stack.size();
        if (size > 0 && stack.get(size - 1) instanceof Number) {
            return  ((Number) stack.get(size - 1));
        }
        if (size > 1 && stack.get(size - 2) instanceof Number) {
            return  ((Number) stack.get(size - 2));
        }
        return null;
    }

    public Operator lastOperator()
    {
        int size = stack.size();
        if (size > 0 && stack.get(size - 1) instanceof Operator) {
            return  ((Operator) stack.get(size - 1));
        }
        if (size > 1 && stack.get(size - 2) instanceof Operator) {
            return  ((Operator) stack.get(size - 2));
        }
        return null;
    }

    public Object lastItem()
    {
        int size = stack.size();
        if (size > 0) {
            return stack.get(size - 1);
        }

        return null;
    }

    private void clearRepetition(){
        repeatNumber = null;
        repeatOperator = null;
    }

    private void partiallyEvaluate()
    {
        try {
            Evaluator.evaluatePartially(stack);
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void evaluate()
    {
        try {
            if (lastItem() instanceof Operator) {
                removeLastItem();
            }
            if (stack.size() >= 3) {
                repeatOperator = new Operator(lastOperator());
                repeatNumber = new Number(lastNumber());
            }

            if (stack.size() == 1 && repeatNumber != null && repeatOperator != null) {
                stack.add(repeatOperator);
                stack.add(repeatNumber);
            }

            Evaluator.evaluate(stack);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void ac()
    {
        stack.clear();
        stack.add(new Number(0));
        state = State.INPUT;
        clearRepetition();
    }

    public void ce()
    {
        if (state == State.ERROR) {
            state = State.RESULT;
            return;
        }
        if (lastItem() instanceof Operator) {
            stack.remove(lastItem());
        } else {
            lastNumber().set(0);
        }
        state = State.INPUT;
    }

    public void memoryGet()
    {
        if(lastItem() instanceof Number) {
            stack.remove(stack.size() - 1);
        }
        stack.add(new Number(memory));
        state = State.RESULT;
    }

    public void memorySet()
    {
        memory = new Number(lastNumber());
        state = State.RESULT;
    }

    public void memoryClear()
    {
        memory.set(0);
    }

    public void memoryAdd()
    {
        try {
            memory.add(lastNumber());
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void memorySub()
    {
        try {
            memory.sub(lastNumber());
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    private void removeLastItem() {
        if (stack.size() > 1) {
            stack.remove(stack.size() - 1);
        }
    }

    public void sqrt()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.sqrt();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void cbrt()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.cbrt();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void pow2()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.pow(new Number(2.0));
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void pow3()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.pow(new Number(3.0));
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void percentage()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }

        try {
            int size = stack.size();
            Number result;

            if (size < 3) {
                Number n = lastNumber();
                n.div(new Number(100));
                state = State.RESULT;
                return;
            }

            Number n1 = stack.get(size - 3) instanceof Number ? (Number) stack.get(size - 3) : null;
            Operator op = stack.get(size - 2) instanceof Operator ? (Operator) stack.get(size - 2) : null;
            Number n2 = stack.get(size - 1) instanceof Number ? (Number) stack.get(size - 1) : null;
            if (n1 == null || n2 == null || op == null) {
                state = State.ERROR;
                return;
            }
            if (op.priority() == 1) {
                // operator +-
                if (op.get() == '+') {
                    n2.add(new Number(100));
                    op.set('*');
                } else {
                    n2.mul(new Number(-1));
                    n2.add(new Number(100));
                    op.set('*');
                }
                n2.div(new Number(100));
                result = Number.operation(n1, op, n2);
                n1.set(result);
                removeLastItem();
                removeLastItem();
                state = State.RESULT;
            } else
            if (op.priority() == 2) {
                // operator */
                n2.div(new Number(100));
                result = Number.operation(n1, op, n2);
                n1.set(result);
                removeLastItem();
                removeLastItem();
                state = State.RESULT;
            } else {
                Number n = lastNumber();
                n.div(new Number(100));
                state = State.RESULT;
            }
        } catch (Exception e) {
            state = State.ERROR;
        }

    }

    public void sin()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.sin(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void cos()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.cos(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void tan()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.tan(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void asin()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.asin(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void acos()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.acos(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void atan()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.atan(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void sinh()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.sinh(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void cosh()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.cosh(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void tanh()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.tanh(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void asinh()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.asinh(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void acosh()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.acosh(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void atanh()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.atanh(drg);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void invert()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.invert();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void factorial()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.factorial();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void ln()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.ln();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void e_pow_x()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.e_pow_x();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void log()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.log();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void ten_pow_x()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            removeLastItem();
        }
        try {
            Number n = lastNumber();
            n.ten_pow_x();
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public void pi()
    {
        if (state == State.ERROR) return;
        if (lastItem() instanceof Operator) {
            stack.add(new Number());
        }
        try {
            Number n = lastNumber();
            n.set(Math.PI);
            state = State.RESULT;
        } catch (Exception e) {
            state = State.ERROR;
        }
    }

    public boolean memoryIsSet()
    {
        return (!memory.toString().equals("0"));
    }

    public boolean errorState()
    {
        return (state == State.ERROR);
    }

    public int parenthesisLevel() {
        int level = 0;
        for(int i = 0; i < stack.size(); i++) {
            Object o = stack.get(i);
            if (o instanceof Parenthesis) {
                Parenthesis p = (Parenthesis) o;
                if (p.opening()) {
                    level++;
                } else {
                    level--;
                }
            }
        }
        return level;
    }
}
