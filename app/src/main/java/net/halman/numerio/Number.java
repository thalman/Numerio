package net.halman.numerio;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Locale;

public class Number implements Serializable {
    private enum State {
        EDITING,
        EDITINGEXP,
        CALCULATING
    }

    public enum DRG {
        DEG,
        RAD,
        GRAD
    }

    private static final int MAX_DIGITS = 13;
    public static final double max_simple = Math.pow(10, MAX_DIGITS) - 1.0;
    public static final double min_simple = Math.pow(10, - MAX_DIGITS - 1);
    public static final double max_exp = 9.9e+99d;
    public static final double min_exp = 1.0e-99d;
    public static final double ZERO_THRESHOLD = 1.0e-200d;
    private double value = 0.0;
    private String valueText = "0";
    private State state = State.CALCULATING;
    private boolean small_numbers = false;

    public Number(boolean small_numbers)
    {
        this.small_numbers = small_numbers;
        set(0);
    }

    public Number(int value, boolean small_numbers)
    {
        this.small_numbers = small_numbers;

        set(value);
    }

    public Number(char value, boolean small_numbers)
    {
        this.small_numbers = small_numbers;
        set(0);
        append(value);
    }

    public Number(double value, boolean small_numbers)
    {
        this.small_numbers = small_numbers;
        set(value);
    }

    public Number(Number value)
    {
        this.small_numbers = value.small_numbers;
        set(value.get());
    }

    public void set(int value)
    {
        this.value = value;
        this.state = State.CALCULATING;
    }

    public void set(char value)
    {
        toEditingState();
        this.valueText = Character.toString(value);
        this.state = State.EDITING;
    }

    public void set(String value) {
        this.value = Double.parseDouble(value);
        this.state = State.CALCULATING;
    }

    public void set(Number value) {
        this.value = value.value;
        this.state = State.CALCULATING;
        this.valueText = "";
    }

    public void set(double value) {
        this.value = value;
        this.state = State.CALCULATING;
        this.valueText = "";
    }

    public double get()
    {
        toCalculatingState();
        return value;
    }

    public boolean getSmallNumbers() {
        return small_numbers;
    }

    public void setSmallNumbers(boolean small_numbers) {
        this.small_numbers = small_numbers;
    }

    private void toEditingState()
    {
        if (state == State.CALCULATING) {
            state = State.EDITING;
            valueText = formatDouble(value);
            sanitize();
        }
    }

    private void toCalculatingState()
    {
        if (state == State.EDITING || state == State.EDITINGEXP) {
            state = State.CALCULATING;
            value = Double.parseDouble(valueText);
            valueText = "0";
        }
    }

    private void toEditingExpState()
    {
        if (state == State.CALCULATING) {
            toEditingState();
        }
        state = State.EDITINGEXP;
        int e = valueText.indexOf('e');
        if (e < 0) {
            valueText += "e+00";
        }
        sanitize();
    }

    public void switchEditing()
    {
        if (state == State.EDITING) {
            toEditingExpState();
        } else if (state == State.EDITINGEXP) {
            state = State.EDITING;
        }
    }

    public void append(char c)
    {
        toEditingState();

        if (state == State.EDITING) {
            String exp = getExponent();
            String base = getBase();
            if (c == '.') {
                if (base.indexOf('.') < 0) {
                    base += '.';
                }
            } else {
                if (c >= '0' && c <= '9') {
                    base += c;
                }
            }
            if (exp.equals("+00")) {
                valueText = base;
            } else  {
                valueText = base + "e" + exp;
            }
        }
        if (state == State.EDITINGEXP) {
            if (c >= '0' && c <= '9') {
                String exp = getExponent();
                String base = getBase();
                valueText = base + "e" + exp.charAt(0) + exp.charAt(2) + c;
            }
        }
        sanitize();
    }

    public void remove()
    {
        toEditingState();
        String base = getBase();
        String exp = getExponent();
        if (state == State.EDITING) {
            if (base.length() < 2) {
                valueText = "0";
            } else {
                base = base.substring(0, base.length() - 1);
                if (valueText.indexOf('e') > 0) {
                    valueText = base + "e" + exp;
                } else {
                    valueText = base;
                }
            }
        }
        if (state == State.EDITINGEXP) {
            String newexp = exp.charAt(0) + "0" + exp.charAt(1);
            if (newexp.equals("-00")) {
                newexp = "+00";
            }
            valueText = base + "e" + newexp;
        }
        sanitize();
    }

    private void sanitize()
    {
        if (valueText.length() == 0) {
            valueText = "0";
            return;
        }

        while (valueText.matches("^0[0-9]")) {
            valueText = valueText.substring(1);
        }

        while (valueText.matches("^-0[0-9]")) {
            valueText = "-" + valueText.substring(2);
        }

        if (valueText.equals("-")) {
            valueText = "0";
            return;
        }

        boolean haveExp = valueText.indexOf('e') >= 0;
        String base = getBase(valueText);
        String exp = getExponent(valueText);

        int digits = base.length();
        if (base.indexOf('.')>=0) {
            digits--;
        }
        if (base.indexOf('-')>=0) {
            digits--;
        }
        if (digits > MAX_DIGITS) {
            base = base.substring(0,base.length() - digits + MAX_DIGITS);
        }
        valueText = base;
        if (haveExp) {
            valueText += "e" + exp;
        }
    }

    public void plusMinus() {
        if (state == State.EDITING) {
            if (valueText.startsWith("-")) {
                valueText = valueText.substring(1);
            } else {
                valueText = "-" + valueText;
            }
            sanitize();
        }
        if (state == State.EDITINGEXP) {
            String exp = getExponent();
            String base = getBase();
            if (exp.charAt(0) == '-') {
                valueText = base + "e+" + exp.substring(1);
            } else {
                valueText = base + "e-" + exp.substring(1);
            }
            sanitize();
        }
        if (state == State.CALCULATING) {
            value *= -1.0;
        }
    }

    public double toDouble() {
        toCalculatingState();
        return value;
    }

    @NotNull
    public String toString() {
        if (state == State.EDITING || state == State.EDITINGEXP) {
            return valueText;
        }
        return formatDouble(value);
    }

    private String getExponent(String number)
    {
        int e = number.indexOf('e');
        if (e < 0) {
            return "+00";
        }
        String result = number.substring(e + 1);
        if (result.length() != 3) {
            return "+00";
        }
        return result;
    }

    private String getExponent()
    {
        if (state == State.CALCULATING) {
            valueText = formatDouble(value);
        }
        return getExponent(valueText);
    }

    private String getBase(String number)
    {
        int e = number.indexOf('e');
        if (e < 0) {
            return number;
        }
        return number.substring(0, e);
    }

    private String getBase()
    {
        if (state == State.CALCULATING) {
            valueText = formatDouble(value);
        }
        return getBase(valueText);
    }

    public String toSciString() {
        if (state == State.EDITING || state == State.EDITINGEXP) {
            return valueText;
        }

        String num = String.format(Locale.ENGLISH, "%1.12e", value);
        String base = getBase(num);
        String exp = getExponent(num);

        while (base.charAt(base.length() - 1) == '0') {
            base = base.substring(0, base.length() - 1);
        }

        if (base.charAt(base.length() - 1) == '.') {
            base = base.substring(0, base.length() - 1);
        }

        if (exp.endsWith("00")) {
            return base;
        }
        return base + "e" + exp;
    }

    public String toEngString() {
        if (state == State.EDITING || state == State.EDITINGEXP) {
            return valueText;
        }

        if (Math.abs(value) < 1000000.0d && Math.abs(value) >= 0.01) {
            return toString();
        }
        String sci = toSciString();
        String base = getBase(sci);
        if (base.indexOf('.') < 0) base += ".";
        base += "000";
        int exp = Integer.parseInt(getExponent(sci));
        while (exp % 3 != 0) {
            exp -= 1;
            int dot = base.indexOf('.');
            base = base.substring(0, dot) + base.substring(dot + 1, dot + 2) + "." + base.substring(dot + 2);
        }

        while (base.charAt(base.length() - 1) == '0') {
            base = base.substring(0, base.length() - 1);
        }

        if (base.charAt(base.length() - 1) == '.') {
            base = base.substring(0, base.length() - 1);
        }

        if (exp != 0) {
            return String.format(Locale.ENGLISH, "%se%s%02d", base, exp >= 0 ? "+" : "-", Math.abs(exp));
        }
        return base;
    }

    public long toLong() {
        toCalculatingState();
        return (long)value;
    }

    private String formatDouble(Double x)
    {
        boolean needExponent = false;
        if (Math.abs(value) > max_simple) {
            needExponent = true;
        }
        if (Math.abs(value) < 0.0000001 && Math.abs(value) > ZERO_THRESHOLD) {
            needExponent = true;
        }

        String result;
        int desiredLength;

        if (needExponent) {
            result = String.format(Locale.ENGLISH, "%1.12e", x);
            desiredLength = MAX_DIGITS + 1 + 4;
            if (x < 0.0) {
                // leading -
                desiredLength += 1;
            }
            while (result.length() > desiredLength) {
                int e = result.indexOf('e');
                result = result.substring(0, e - 1) + result.substring(e);
            }
        } else {
            result = String.format(Locale.ENGLISH, "%.15f", x);
            desiredLength = MAX_DIGITS;
            if (x < 0.0) {
                // leading -
                desiredLength += 1;
            }
            if (result.indexOf('.') >= 0) {
                // dot is not counted
                desiredLength += 1;
            }
            while (result.length() > desiredLength) {
                result = result.substring(0, result.length() - 1);
            }

            // remove trailing 0
            if (result.indexOf('.') >= 0) {
                while (result.charAt(result.length() - 1) == '0') {
                    result = result.substring(0, result.length() - 1);
                }
            }

            // remove trailing dot
            if (result.endsWith(".")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    public void checkValue() throws Exception {
        if (state == State.CALCULATING) {
            checkValue(value);
        }
    }

    private double checkValue(double x) throws Exception {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            throw new Exception("Out of range");
        }
        if (small_numbers) {
            if (Math.abs(x) > max_simple) {
                throw new Exception("Out of range");
            }
            if (Math.abs(x) < min_simple) {
                x = 0;
            }

        } else {
            if (Math.abs(x) > max_exp) {
                throw new Exception("Out of range");
            }
            if (Math.abs(x) < min_exp) {
                x = 0;
            }
        }
        return x;
    }

    public void add(Number n) throws Exception {
        toCalculatingState();
        value += n.get();
        sanitize();
    }

    public void sub(Number n) throws Exception {
        toCalculatingState();
        value = value - n.get();
        sanitize();
    }

    public void mul(Number n) throws Exception {
        toCalculatingState();
        double result = value * n.get();
        result = checkValue(result);
        value = result;
        sanitize();
    }

    public void div(Number n) throws Exception {
        toCalculatingState();
        double result = value / n.get();
        result = checkValue(result);
        value = result;
        sanitize();
    }

    public void sqrt() throws Exception {
        toCalculatingState();
        if (value < 0.0) {
            throw new Exception("Sqrt of negative number");
        }
        double result = Math.sqrt(value);
        result = checkValue(result);
        value = result;
        sanitize();
    }

    public void pow(Number exp) throws Exception {
        toCalculatingState();
        double result = Math.pow(value, exp.toDouble());
        result = checkValue(result);
        value = result;
        sanitize();
    }

    public void root(Number nth) throws Exception {
        toCalculatingState();
        double result = Math.pow(value, 1.0/nth.toDouble());
        result = checkValue(result);
        value = result;
        sanitize();
    }

    public void cbrt() throws Exception {
        toCalculatingState();
        double result = Math.cbrt(value);
        result = checkValue(result);
        value = result;
        sanitize();
    }

    public void sin(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double radians = toRadians(toDouble(), mode);
        double result = Math.sin(radians);
        result = checkValue(result);
        value = result;
    }

    public void cos(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double radians = toRadians(toDouble(), mode);
        double result = Math.cos(radians);
        result = checkValue(result);
        value = result;
    }

    public void tan(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double radians = toRadians(toDouble(), mode);
        double result = Math.tan(radians);
        result = checkValue(result);
        value = result;
    }

    public void asin(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double result = fromRadians(Math.asin(value), mode);
        result = checkValue(result);
        value = result;
    }

    public void acos(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double result = fromRadians(Math.acos(value), mode);
        result = checkValue(result);
        value = result;
    }

    public void atan(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double result = fromRadians(Math.atan(value), mode);
        result = checkValue(result);
        value = result;
    }

    public void sinh(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double radians = toRadians(toDouble(), mode);
        double result = Math.sinh(radians);
        result = checkValue(result);
        value = result;
    }

    public void cosh(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double radians = toRadians(toDouble(), mode);
        double result = Math.cosh(radians);
        result = checkValue(result);
        value = result;
    }

    public void tanh(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double radians = toRadians(toDouble(), mode);
        double result = Math.tanh(radians);
        result = checkValue(result);
        value = result;
    }

    public void asinh(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double angle =  Math.log(value + Math.sqrt(Math.pow(value, 2) + 1));
        double result = fromRadians(angle, mode);
        result = checkValue(result);
        value = result;
    }

    public void acosh(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double angle = Math.log(value + Math.sqrt(value * value - 1.0));
        double result = fromRadians(angle, mode);
        result = checkValue(result);
        value = result;
    }

    public void atanh(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double angle = (StrictMath.log((1.0 + value) * StrictMath.sqrt(1.0 / (1.0 - value * value))));
        double result = fromRadians(angle, mode);
        result = checkValue(result);
        value = result;
    }

    public void toDms(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double angle = toDegrees(value, mode);
        double tmp = Math.abs(angle);
        double degrees, minutes, seconds, result, sign;

        if (value < 0.0d) {
            sign = -1.0d;
        } else {
            sign = 1.0d;
        }

        degrees = Math.floor(tmp);
        tmp = (tmp - degrees) * 60.0d;
        minutes = Math.floor(tmp);
        seconds = (tmp - minutes) * 60.0d;

        result = sign * (degrees + minutes / 100.0d + seconds / 1000.0d);
        result = checkValue(result);
        value = result;
    }

    public void fromDms(Number.DRG mode) throws Exception
    {
        toCalculatingState();
        double angle = value;
        double tmp = Math.abs(angle);
        double degrees, minutes, seconds, result, sign;

        if (value < 0.0d) {
            sign = -1.0d;
        } else {
            sign = 1.0d;
        }

        degrees = Math.floor(tmp);
        tmp = (tmp - degrees) * 100.0d;
        minutes = Math.floor(tmp);
        seconds = (tmp - minutes) * 100.0d;
        result = sign * (degrees + minutes / 60.0d + seconds / 3600.0d);
        result = fromDegrees(result, mode);
        result = checkValue(result);
        value = result;
    }

    public void invert() throws Exception
    {
        toCalculatingState();
        double result = 1.0d / value;
        result = checkValue(result);
        value = result;
    }

    public void ln() throws Exception
    {
        toCalculatingState();
        double result = Math.log(value);
        result = checkValue(result);
        value = result;
    }

    public void e_pow_x() throws Exception
    {
        toCalculatingState();
        double result = Math.exp(value);
        result = checkValue(result);
        value = result;
    }

    public void log() throws Exception
    {
        toCalculatingState();
        double result = Math.log10(value);
        result = checkValue(result);
        value = result;
    }

    public void ten_pow_x() throws Exception
    {
        toCalculatingState();
        double result = Math.pow(10.0, value);
        result = checkValue(result);
        value = result;
    }

    public void factorial() throws Exception
    {
        toCalculatingState();
        double end = Math.round(value);
        if (end < 0.0 || end > 69.0) {
            throw new Exception("out of range");
        }
        long int_end = (long)end;
        double result = 1.0;
        for(long i = 2; i <= int_end; i++){
            result *= i;
        }
        result = checkValue(result);
        value = result;
    }

    public static Number operation(Number n1, Operator op, Number n2) throws Exception
    {
        Number result = new Number(n1);
        switch (op.get()) {
            case '+':
                result.add(n2);
                break;
            case '-':
                result.sub(n2);
                break;
            case '*':
            case '×':
                result.mul(n2);
                break;
            case '/':
            case '÷':
                result.div(n2);
                break;
            case '^':
                result.pow(n2);
                break;
            case '√':
                result.root(n2);
                break;
        }
        return result;
    }

    public static double toRadians(double angle, DRG mode)
    {
        switch (mode) {
            case DEG:
                return Math.toRadians(angle);
            case GRAD:
                return Math.toRadians(angle * 0.9);
            case RAD:
            default:
                return angle;
        }
    }

    public void toRadians(DRG currentMode) throws Exception
    {
        toCalculatingState();
        double result = toRadians(value, currentMode);
        result = checkValue(result);
        value = result;
    }

    public double fromRadians(double angle, DRG mode)
    {
        switch (mode) {
            case DEG:
                return Math.toDegrees(angle);
            case GRAD:
                return Math.toDegrees(angle) * 100.0 / 90.0;
            case RAD:
            default:
                return angle;
        }
    }

    public void fromRadians(DRG currentMode) throws Exception
    {
        toCalculatingState();
        double result = fromRadians(value, currentMode);
        result = checkValue(result);
        value = result;
    }

    public double fromDegrees(double angle, DRG mode)
    {
        switch (mode) {
            case DEG:
                return angle;
            case GRAD:
                return angle * 100.0d / 90.0d;
            case RAD:
            default:
                return Math.toRadians(angle);
        }
    }

    public double toDegrees(double angle, DRG mode)
    {
        switch (mode) {
            case DEG:
                return angle;
            case GRAD:
                return angle * 90.0d / 100.0d;
            case RAD:
            default:
                return Math.toDegrees(angle);
        }
    }
}
