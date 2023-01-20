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
        stack.add(new Number(3));
        stack.add(new Operator('+'));
        stack.add(new Number(4));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(7, result.toLong());

        stack.clear();
        stack.add(new Number(1));
        stack.add(new Operator('+'));
        stack.add(new Number(2));
        stack.add(new Operator('+'));
        stack.add(new Number(3));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(6, result.toLong());

        stack.clear();
        stack.add(new Number(1));
        stack.add(new Operator('+'));
        stack.add(new Number(2));
        stack.add(new Operator('*'));
        stack.add(new Number(3));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(7, result.toLong());

        stack.clear();
        stack.add(new Number(1));
        stack.add(new Operator('-'));
        stack.add(new Number(2));
        stack.add(new Operator('*'));
        stack.add(new Number(3));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(-5, result.toLong());

        stack.clear();
        stack.add(new Number(1));
        stack.add(new Operator('-'));
        stack.add(new Number(2));
        stack.add(new Operator('*'));
        stack.add(new Number(3));
        stack.add(new Operator('+'));
        stack.add(new Number(3));
        stack.add(new Operator('*'));
        stack.add(new Number(3));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(4, result.toLong());
    }

    @Test
    public void evaluationDouble() {
        ArrayList<Object> stack = new ArrayList<>();
        Number result;

        stack.clear();
        stack.add(new Number(3));
        stack.add(new Operator('/'));
        stack.add(new Number(4));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals("0.75", result.toString());
    }

    @Test
    public void evaluationPow() {
        ArrayList<Object> stack = new ArrayList<>();
        Number result;

        stack.clear();
        stack.add(new Number(1));
        stack.add(new Operator('+'));
        stack.add(new Number(2));
        stack.add(new Operator('^'));
        stack.add(new Number(3));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(9, result.toLong());

        stack.clear();
        stack.add(new Number(1));
        stack.add(new Operator('+'));
        stack.add(new Number(2));
        stack.add(new Operator('^'));
        stack.add(new Number(3));
        stack.add(new Operator('*'));
        stack.add(new Number(2));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(17, result.toLong());

        stack.clear();
        stack.add(new Number(1));
        stack.add(new Operator('-'));
        stack.add(new Number(9));
        stack.add(new Operator('&'));
        stack.add(new Number(2));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(-2, result.toLong());
    }

    @Test
    public void evaluationParenthesis() {
        ArrayList<Object> stack = new ArrayList<>();
        Number result;

        stack.clear();
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Number(1));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(1, result.toLong());

        stack.clear();
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Number(1));
        stack.add(new Operator('+'));
        stack.add(new Number(9));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        stack.add(new Operator('*'));
        stack.add(new Number(2));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(20, result.toLong());

        stack.clear();
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Parenthesis(Parenthesis.Type.OPENING));
        stack.add(new Number(1));
        stack.add(new Operator('+'));
        stack.add(new Number(9));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        stack.add(new Operator('*'));
        stack.add(new Number(2));
        stack.add(new Parenthesis(Parenthesis.Type.CLOSING));
        stack.add(new Operator('^'));
        stack.add(new Number(2));
        try {
            result = Evaluator.evaluate(stack);
        } catch (Exception e) {
            result = new Number(0);
        }
        assertEquals(400, result.toLong());
    }
}