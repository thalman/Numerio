package net.halman.numerio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Stack;

public class Evaluator {
    @Nullable
    private static boolean checkParenthesis(@NotNull ArrayList<Object> expr, int begin, int end) throws Exception
    {
        Object o;
        int level = 0;
        for(int i = begin; i <= end; i++) {
            o = expr.get(i);
            if (o instanceof Parenthesis) {
                Parenthesis p = (Parenthesis) o;
                if (p.type == Parenthesis.Type.OPENING) {
                    level++;
                } else {
                    level--;
                    if (level < 0) {
                        throw new Exception("Too many closing parenthesis");
                    }
                }
            }
        }
        // missing ) can be easily fixed - do not throw exception
        return (level == 0);
    }

    private static int firstParenthesis(@NotNull ArrayList<Object> expr)
    {
        for(int i = 0; i < expr.size(); i++) {
            if (expr.get(i) instanceof Parenthesis) {
                return i;
            }
        }

        return -1;
    }

    public static int matchingParenthesis(@NotNull ArrayList<Object> stack, int idx)
    {
        Object o = stack.get(idx);
        Parenthesis p;
        int level = 0;

        if (! (o instanceof Parenthesis)) {
            return -1;
        }

        p = (Parenthesis) o;
        if (p.type == Parenthesis.Type.CLOSING) {
            idx--;
            while (idx >= 0) {
                o = stack.get(idx);
                if (o instanceof Parenthesis) {
                    p = (Parenthesis) o;
                    if (p.type == Parenthesis.Type.CLOSING) {
                        level++;
                    } else {
                        if (level == 0) {
                            return idx;
                        } else {
                            level--;
                        }
                    }
                }
                idx--;
            }
        } else {
            idx++;
            while (idx < stack.size()) {
                o = stack.get(idx);
                if (o instanceof Parenthesis) {
                    p = (Parenthesis) o;
                    if (p.type == Parenthesis.Type.OPENING) {
                        level++;
                    } else {
                        if (level == 0) {
                            return idx;
                        } else {
                            level--;
                        }
                    }
                }
                idx++;
            }
        }

        return -1;
    }


    @Nullable
    public static Number evaluate(@NotNull ArrayList<Object> expr) throws Exception
    {

        if (expr.size() == 1) {
            Number n = (Number) expr.get(0);
            n.toDouble();
            return n;
        }

        int begin = firstParenthesis(expr);
        while (begin >= 0) {
            int end = matchingParenthesis(expr, begin);
            ArrayList<Object> subExpr = copyStack(expr, begin + 1, end - 1);
            Number n = evaluate(subExpr);
            for (int i = end; i > begin; i--) {
                expr.remove(i);
            }
            expr.set(begin, n);
            begin = firstParenthesis(expr);
        }

        for(int priority=3; priority>0; priority--) {
            int i = 0;
            while (i < expr.size()) {
                Object o = expr.get(i);
                if (o instanceof Operator) {
                    Operator op = (Operator) o;
                    if (op.priority() == priority) {
                        Number n1 = (Number) expr.get(i - 1);
                        Number n2 = (Number) expr.get(i + 1);
                        n1.set(Number.operation(n1, op, n2));
                        expr.remove(i + 1);
                        expr.remove(i);
                    } else {
                        i++;
                    }
                } else {
                    i++;
                }
            }
        }

        if (expr.size() == 1) {
            return (Number) expr.get(0);
        }

        // broken expression
        throw new Exception("Broken expression");
    }

    public static void evaluatePartially(@NotNull ArrayList<Object> expr) throws Exception {
        if (expr.size() <= 1) {
            return;
        }

        Object o = expr.get(expr.size() - 1);

        if (o instanceof Number) {
            return;
        }

        if (o instanceof Parenthesis) {
            Parenthesis p = (Parenthesis) o;
            if (p.closing()) {
                int end = expr.size() - 1;
                int begin = matchingParenthesis(expr, end);
                ArrayList<Object> subExpr = copyStack(expr, begin + 1, end - 1);
                Number n = evaluate(subExpr);
                for (int i = end; i > begin; i--) {
                    expr.remove(i);
                }
                expr.set(begin, n);
                return;
            }
        }

        Operator last =  o instanceof Operator ? (Operator) o : null;
        if (last == null) {
            return;
        }

        int start_expr = 0;
        if (firstParenthesis(expr) >= 0) {
            // we have parenthesis, lets find last (
            for (int i = expr.size() - 1; i >= 0; i--) {
                Object obj = expr.get(i);
                if (obj instanceof Parenthesis) {
                    Parenthesis p = (Parenthesis) obj;
                    if (p.closing()) {
                        return;
                    } else {
                        start_expr = i + 1;
                        break;
                    }
                }
            }
        }

        expr.remove(last);
        /* evaluate all operators with the same or higher priority */
        for (int priority = 3; priority >= last.priority(); priority--) {
            int i = start_expr;
            while (i < expr.size()) {
                Operator op = expr.get(i) instanceof Operator ? (Operator) expr.get(i) : null;
                if (op != null && op.priority() == priority) {
                    Number n1 = (Number) expr.get(i - 1);
                    Number n2 = (Number) expr.get(i + 1);
                    n1.set(Number.operation(n1, op, n2));
                    expr.remove(i + 1);
                    expr.remove(i);
                } else {
                    i++;
                }
            }
        }

        expr.add(last);
    }

    public static ArrayList<Object> copyStack(@NotNull ArrayList<Object> stack) {
        return copyStack(stack, 0, stack.size() - 1);
    }

    public static ArrayList<Object> copyStack(@NotNull ArrayList<Object> stack, int from, int to) {
        ArrayList<Object> result = new ArrayList<>();
        if (from < 0) {
            from = 0;
        }
        if (to >= stack.size()) {
            to = stack.size() - 1;
        }

        for (int i = from; i <= to; i++) {
            Object o = stack.get(i);
            if (o instanceof Operator) {
                result.add(new Operator((Operator) o));
            } else if (o instanceof Number) {
                result.add(new Number((Number) o));
            } else if (o instanceof Parenthesis) {
                result.add(new Parenthesis((Parenthesis) o));
            }

        }
        return result;
    }

}
