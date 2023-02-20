package net.halman.numerio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

public class NumerioDisplay extends View {
    public enum NumberMode {
        FLO,
        ENG,
        SCI
    }

    private boolean secondFn = false;
    private boolean simleMode = true;
    private boolean hyp = false;
    private boolean ahyp = false;
    private NumberMode numberMode = NumberMode.FLO;

    private final SparseArray<Drawable> letters = new SparseArray<Drawable>() {
        {
            put('0', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_0, null));
            put('1', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_1, null));
            put('2', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_2, null));
            put('3', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_3, null));
            put('4', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_4, null));
            put('5', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_5, null));
            put('6', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_6, null));
            put('7', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_7, null));
            put('8', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_8, null));
            put('9', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_9, null));

            put('A', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_a, null));
            put('B', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_b, null));
            put('C', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_c, null));
            put('D', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_d, null));
            put('E', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_e, null));
            put('F', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_f, null));
            put('O', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_o, null));
            put('R', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_r, null));

            put('\'', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_number_separator, null));
            put('-', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_minus, null));
            put('.', ResourcesCompat.getDrawable(getResources(), R.drawable.ic_decimal_point, null));
        }
    };

    private final int DISPLAY_BOXES = 14;
    private final Rect numbersArea = new Rect();
    private final Rect expArea = new Rect();
    private int display_numbers = DISPLAY_BOXES;


    private NumerioMachine machine = null;

    public NumerioDisplay(Context context) {
        super(context);
    }

    public NumerioDisplay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NumerioDisplay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NumerioDisplay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean getSecondFn(){
        return secondFn;
    }
    public void setSecondFn(boolean value){
        secondFn = value;
    }

    public boolean getHyp(){
        return hyp;
    }

    public void setHyp(boolean value){
        hyp = value;
        if (hyp) ahyp = false;
    }

    public boolean getArcHyp(){
        return ahyp;
    }

    public void setArcHyp(boolean value){
        ahyp = value;
        if (ahyp) hyp = false;
    }

    public NumberMode getNumberMode() {
        return numberMode;
    }

    public void setNumberMode(NumberMode mode) {
        numberMode = mode;
    }

    public void numberModeNext() {
        switch (numberMode) {
            case FLO:
                numberMode = NumberMode.ENG;
                break;
            case ENG:
                numberMode = NumberMode.SCI;
                break;
            case SCI:
                numberMode = NumberMode.FLO;
                break;
        }
    }

    private int boxedLength(String number)
    {
        int result = number.length();
        if (number.indexOf('.') >= 0) {
            result--;
        }
        return result;
    }

    private void calculateGeometry()
    {
        int width = getWidth();
        int height = getHeight();
        int box_width = width / (DISPLAY_BOXES + 1);

        if (simleMode) {
            /* simple mode */
            numbersArea.set(
                    box_width / 2,
                    height * 2 / 10,
                    width - box_width / 2,
                    height * 8 / 10
            );
            expArea.set(0,0,0,0);
            display_numbers = DISPLAY_BOXES;
        } else {
            /* scientific mode */
            numbersArea.set(
                    box_width / 2,
                    height * 3 / 10,
                    width - box_width / 2 - 2 * box_width,
                    height * 8 / 10
            );
            expArea.set(
                    width - box_width / 2 - 2 * box_width,
                    height * 2 / 10,
                    width - box_width / 2,
                    height * 5 / 10
            );
            display_numbers = DISPLAY_BOXES;
        }
    }

    private void drawOneLetter(Canvas canvas, char character, int position)
    {
        int box_width = numbersArea.width() / (display_numbers);
        int x = numbersArea.left + position * box_width;

        Drawable d = letters.get(character, null);
        if (d != null) {
            d.setBounds(x, numbersArea.top, x + box_width, numbersArea.bottom);
            d.draw(canvas);
        }
    }

    private String sanitizeExp(String exp)
    {

        String result;
        exp = exp.trim();
        switch(exp.charAt(0)) {
            case '-':
                result = "-";
                exp = exp.substring(1);
                break;
            case '+':
                result = " ";
                exp = exp.substring(1);
                break;
            default:
                result = " ";
                break;
        }
        while (exp.length() < 2) {
            exp = "0" + exp;
        }
        result += exp.substring(0, 2);
        return result;
    }

    private void drawExp(Canvas canvas, String exp)
    {
        if (exp.isEmpty()) return;
        exp = sanitizeExp(exp);

        int box_width = expArea.width() / 3;
        drawText(canvas,
                "×10",
                expArea.left, numbersArea.bottom, expArea.height() * 8 / 10);

        Drawable d;
        for (int i = 0; i < exp.length(); i ++) {
            d = letters.get(exp.charAt(i), null);
            if (d != null) {
                d.setBounds(
                        expArea.left + i * box_width,
                        expArea.top,
                        expArea.left + (i + 1) * box_width,
                        expArea.bottom);
                d.draw(canvas);
            }
        }
    }

    public String formatNumber() {
        if (machine == null || machine.errorState()) {
            return "";
        }

        switch (numberMode) {
            case ENG:
                return machine.lastNumber().toEngString();
            case SCI:
                return machine.lastNumber().toSciString();
            default:
                return machine.lastNumber().toString();
        }
    }

    private void drawNumber(Canvas canvas)
    {
        if (machine == null || machine.errorState()) return;

        String number;
        String base;
        String exp;

        number = formatNumber();
        int e = number.indexOf('e');
        if (e < 0) {
            base = number;
            exp = "";
        } else {
            base = number.substring(0, e);
            exp = number.substring(e + 1);
        }

        if ((! exp.isEmpty()) && simleMode) {
            drawError(canvas);
            return;
        }
        int position = display_numbers - boxedLength(base) - 1;
        int dotPosition = base.indexOf('.');
        if (dotPosition < 0) {
            dotPosition = base.length();
        }

        for (int i = 0; i < base.length(); ++i) {
            char c = base.charAt(i);
            if (c != '.') {
                position++;
            }
            int distanceFromDot = dotPosition - i - 1;
            drawOneLetter(canvas, c, position);

            if (distanceFromDot % 3 == 0 && distanceFromDot > 0 && c != '-') {
                drawOneLetter(canvas, '\'', position);
            }
        }

        drawExp(canvas, exp);
    }

    private void drawText(Canvas canvas, String text, int x, int y, int size)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextSize(size);
        canvas.drawText(text, x, y, paint);
    }

    protected void drawOperator(Canvas canvas) {
        if (machine == null) {
            return;
        }
        if (machine.lastItem() instanceof Operator) {
            int y = getHeight() * 3 / 4;
            int h = getHeight() / 4;
            drawText(canvas, machine.lastOperator().toDisplayString(), 40, y, h);
        }
    }

    private void drawMemory(Canvas canvas) {
        if (machine == null) {
            return;
        }
        if (machine.memoryIsSet()) {
            int y = getHeight() / 4 + 5;
            int h = getHeight() / 4;
            drawText(canvas, "M", 40, y, h);
        }
    }

    private void drawSecondFn(Canvas canvas)
    {
        if (simleMode) return;
        if (secondFn) {
            int y = getHeight() / 4 + 5;
            int h = getHeight() / 6;
            drawText(canvas, "2nd-f", getWidth() / 4, y, h);
        }
    }

    private void drawHyp(Canvas canvas)
    {
        if (simleMode) return;
        if (hyp || ahyp) {
            int y = getHeight() / 4 + 5;
            int h = getHeight() / 6;
            drawText(canvas, hyp ? "HYP" : "AHYP", getWidth() * 3 / 8, y, h);
        }
    }

    private void drawDRG(Canvas canvas)
    {
        if (machine == null || simleMode) {
            return;
        }
        int y = getHeight() / 4 + 5;
        int h = getHeight() / 6;
        String text = "";
        switch (machine.getDrg()) {
            case DEG:
                text = "DEG";
                break;
            case RAD:
                text = "RAD";
                break;
            case GRAD:
                text = "GRAD";
                break;
        }
        drawText(canvas, text, getWidth() / 2, y, h);
    }

    private void drawNumberMode(Canvas canvas)
    {
        if (machine == null || simleMode) {
            return;
        }
        int y = getHeight() / 4 + 5;
        int h = getHeight() / 6;
        String text = "";
        switch (numberMode) {
            case ENG:
                text = "ENG";
                break;
            case SCI:
                text = "SCI";
                break;
            default:
                text = "";
                break;
        }
        drawText(canvas, text, getWidth() * 5 / 8, y, h);
    }

    private void drawParenthesis(Canvas canvas)
    {
        if (machine == null) {
            return;
        }
        int y = getHeight() / 4 - 2;
        int h = getHeight() / 8;
        String text = " ";
        int level = machine.parenthesisLevel();
        for (int i = 0; i < level; i++) {
            text = "(" + text + ")";
        }
        drawText(canvas, text, getWidth() * 6 / 8, y, h);
    }

    private void drawError(Canvas canvas)
    {
        if (machine == null) {
            return;
        }
        if (machine.errorState()) {
            drawOneLetter(canvas, 'E', 1);
            drawOneLetter(canvas, 'R', 2);
            drawOneLetter(canvas, 'R', 3);
            drawOneLetter(canvas, 'O', 4);
            drawOneLetter(canvas, 'R', 5);
        }
    }

    protected void onDraw(Canvas canvas)
    {
        calculateGeometry();
        drawNumber(canvas);
        drawOperator(canvas);
        drawMemory(canvas);
        drawSecondFn(canvas);
        drawError(canvas);
        drawDRG(canvas);
        drawHyp(canvas);
        drawNumberMode(canvas);
        drawParenthesis(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = 300;
        int desiredHeight = 100;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.max(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    public void setMachine(NumerioMachine machine)
    {
        this.machine = machine;
        invalidate();
    }

    public void setMode(String mode)
    {
        simleMode = mode.equals("simple");
        invalidate();
    }
}
