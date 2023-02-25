package net.halman.numerio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class NumerioButton extends LinearLayout {
    private TextView mSecondFunctionView = null;
    private TextView mFirstFunctionView = null;
    private CharSequence mFirstFunctionText = "";
    private CharSequence mSecondFunctionText = "";
    private int mFirstFunction = -1;
    private int mSecondFunction = -1;
    private float mFirstFunctionTextSize = -1;
    private float mSecondFunctionTextSize = -1;
    private int mFirstFunctionTextColor = -1;
    private int mSecondFunctionTextColor = -1;
    private CharSequence buttonColor = "";

    public NumerioButton(Context context) {
        super(context);
        init(context, null);
    }

    public NumerioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public NumerioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     * the current context for the view.
     */

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.numerio_button, this);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumerioButton);
            final int N = a.getIndexCount();
            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);

                if (attr == R.styleable.NumerioButton_firstText) {
                    mFirstFunctionText = a.getText(attr);
                } else if (attr == R.styleable.NumerioButton_firstFunction) {
                    mFirstFunction = a.getInt(attr, -1);
                } else if (attr == R.styleable.NumerioButton_firstTextColor) {
                    mFirstFunctionTextColor = a.getColor(attr, -1);
                } else if (attr == R.styleable.NumerioButton_firstTextSize) {
                    mFirstFunctionTextSize = a.getDimension(attr, getResources().getDimension(R.dimen.font_size));
                } else if (attr == R.styleable.NumerioButton_secondText) {
                    mSecondFunctionText = a.getText(attr);
                } else if (attr == R.styleable.NumerioButton_secondFunction) {
                    mSecondFunction = a.getInt(attr, -1);
                } else if (attr == R.styleable.NumerioButton_secondTextColor) {
                    mSecondFunctionTextColor = a.getColor(attr, -1);
                } else if (attr == R.styleable.NumerioButton_secondTextSize) {
                    mSecondFunctionTextSize = a.getDimension(attr, getResources().getDimension(R.dimen.font_size_small));
                } else if (attr == R.styleable.NumerioButton_buttonColor) {
                    buttonColor = a.getText(attr);
                }
            }
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Drawable background;
        mFirstFunctionView = this.findViewById(R.id.first_function);
        mSecondFunctionView = this.findViewById(R.id.second_function);
        if (buttonColor.equals("red")) {
            background = this.getResources().getDrawable(R.drawable.ic_button_red);
        } else if (buttonColor.equals("gray")) {
            background = this.getResources().getDrawable(R.drawable.ic_button_gray);
        } else {
            background = this.getResources().getDrawable(R.drawable.ic_button_black);
        }
        mFirstFunctionView.setBackground(background);

        mFirstFunctionView.setText(mFirstFunctionText);
        if (mFirstFunctionTextColor != -1) mFirstFunctionView.setTextColor(mFirstFunctionTextColor);
        if (mFirstFunctionTextSize > 0.0) mFirstFunctionView.setTextSize(mFirstFunctionTextSize);

        mSecondFunctionView.setText(mSecondFunctionText);
        if (mSecondFunctionTextColor != -1) mSecondFunctionView.setTextColor(mSecondFunctionTextColor);
        if (mSecondFunctionTextSize > 0.0) mSecondFunctionView.setTextSize(mSecondFunctionTextSize);
    }

    int function(int i) {
        if (i == 2) {
            return mSecondFunction;
        }
        return mFirstFunction;
    }
}
