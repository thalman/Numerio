package net.halman.numerio;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberFormattingUnitTest {
    @Test
    public void testFormat()
    {
        Number x = new Number(false);

        x.set(123.01234567890123d);
        assertEquals("123.0123456789", x.toString());

        x.set(-123.01234567890123d);
        assertEquals("-123.0123456789", x.toString());

        x.set(123.01234567890123e12d);
        assertEquals("1.230123456789e+14", x.toString());

        x.set(-123.01234567890123e12d);
        assertEquals("-1.230123456789e+14", x.toString());
    }

    @Test
    public void testEngFormat() {
        Number x = new Number(false);

        x.set(1.234567890123456789e+10d);
        assertEquals("12.34567890123e+09", x.toEngString());

        x.set(1.234567890123456789e-07d);
        assertEquals("123.4567890123e-09", x.toEngString());

        x.set(-1.234567890123456789e-02d);
        assertEquals("-0.012345678901", x.toEngString());

        x.set(0.0000123d);
        assertEquals("12.3e-06", x.toEngString());
    }

    @Test
    public void testSciFormat() {
        Number x = new Number(false);

        x.set(1.234567890123456789e+10d);
        assertEquals("1.234567890123e+10", x.toSciString());

        x.set(1.234567890123456789e-02d);
        assertEquals("1.234567890123e-02", x.toSciString());

        x.set(-123.4567890123456789e-02d);
        assertEquals("-1.234567890123", x.toSciString());

        x.set(456d);
        assertEquals("4.56e+02", x.toSciString());

        x.set(-0.00456d);
        assertEquals("-4.56e-03", x.toSciString());

        x.set(-0.004d);
        assertEquals("-4e-03", x.toSciString());

        x.set(0.00456d);
        assertEquals("4.56e-03", x.toEngString());

        x.set(0.004d);
        assertEquals("4e-03", x.toEngString());
    }
}
