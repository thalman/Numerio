package net.halman.numerio;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class EvaluatorUnitTest {

    @Test
    public void evaluation() {
        ArrayList<Object> stack = new ArrayList<>();
        Number result;

        stack.clear();
        stack.add(new Number(3, false));
        stack.add(new Operator('+'));
        stack.add(new Number(4, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(7, result.toLong());

        stack.clear();
        stack.add(new Number(1, false));
        stack.add(new Operator('+'));
        stack.add(new Number(2, false));
        stack.add(new Operator('+'));
        stack.add(new Number(3, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(6, result.toLong());

        stack.clear();
        stack.add(new Number(1, false));
        stack.add(new Operator('+'));
        stack.add(new Number(2, false));
        stack.add(new Operator('*'));
        stack.add(new Number(3, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(7, result.toLong());

        stack.clear();
        stack.add(new Number(1, false));
        stack.add(new Operator('-'));
        stack.add(new Number(2, false));
        stack.add(new Operator('*'));
        stack.add(new Number(3, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(-5, result.toLong());

        stack.clear();
        stack.add(new Number(1, false));
        stack.add(new Operator('-'));
        stack.add(new Number(2, false));
        stack.add(new Operator('*'));
        stack.add(new Number(3, false));
        stack.add(new Operator('+'));
        stack.add(new Number(3, false));
        stack.add(new Operator('*'));
        stack.add(new Number(3, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(4, result.toLong());
    }

    @Test
    public void evaluationDouble() {
        ArrayList<Object> stack = new ArrayList<>();
        Number result;

        stack.clear();
        stack.add(new Number(3, false));
        stack.add(new Operator('/'));
        stack.add(new Number(4, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals("0.75", result.toString());
    }

    @Test
    public void evaluationPow() {
        ArrayList<Object> stack = new ArrayList<>();
        Number result;

        stack.clear();
        stack.add(new Number(1, false));
        stack.add(new Operator('+'));
        stack.add(new Number(2, false));
        stack.add(new Operator('^'));
        stack.add(new Number(3, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(9, result.toLong());

        stack.clear();
        stack.add(new Number(1, false));
        stack.add(new Operator('+'));
        stack.add(new Number(2, false));
        stack.add(new Operator('^'));
        stack.add(new Number(3, false));
        stack.add(new Operator('*'));
        stack.add(new Number(2, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(17, result.toLong());

        stack.clear();
        stack.add(new Number(1, false));
        stack.add(new Operator('-'));
        stack.add(new Number(9, false));
        stack.add(new Operator('&'));
        stack.add(new Number(2, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(-2, result.toLong());
    }

    @Test
    public void evaluationParenthesis() {
        ArrayList<Object> stack = new ArrayList<>();
        Number result;

        stack.clear();
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Number(1, false));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(1, result.toLong());

        stack.clear();
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Number(1, false));
        stack.add(new Operator('+'));
        stack.add(new Number(9, false));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        stack.add(new Operator('*'));
        stack.add(new Number(2, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(20, result.toLong());

        stack.clear();
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Number(1, false));
        stack.add(new Operator('+'));
        stack.add(new Number(9, false));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        stack.add(new Operator('*'));
        stack.add(new Number(2, false));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        stack.add(new Operator('^'));
        stack.add(new Number(2, false));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0, false);
        }
        assertEquals(400, result.toLong());
    }
}