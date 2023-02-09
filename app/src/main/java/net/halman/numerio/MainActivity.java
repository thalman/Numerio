package net.halman.numerio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainActivity extends AppCompatActivity {
    private NumerioMachine machine = null;
    private NumerioDisplay numerioDisplay = null;
    private final static String state_file = "state.bin";
    private View mLayoutSimple = null;
    private View mLayoutScientific = null;
    private String mMode = "scientific";
    private boolean mVibration = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayoutSimple = findViewById(R.id.calculatorSimple);
        mLayoutScientific = findViewById(R.id.calculatorSci);
        mLayoutSimple.setVisibility(View.GONE);

        View.OnClickListener listener = this::onButtonClick;

        int[] buttons = new int[]{
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9,
                R.id.buttonDot, R.id.buttonBack, R.id.buttonPlusMinus,
                R.id.buttonPlus, R.id.buttonMinus, R.id.buttonMultiply, R.id.buttonDivide,
                R.id.buttonResult, R.id.buttonAC, R.id.buttonCE, R.id.button2fn,
                R.id.buttonMemClear, R.id.buttonMemGet, R.id.buttonMemPlus, R.id.buttonMemMinus,
                R.id.buttonPercent, R.id.buttonSqrt, R.id.buttonPow2, R.id.buttonPow,
                R.id.buttonLn, R.id.buttonLog,
                R.id.buttonDGR, R.id.buttonSin, R.id.buttonCos, R.id.buttonTan,
                R.id.buttonExp, R.id.buttonEng, R.id.buttonHyp,
                R.id.buttonLP, R.id.buttonRP, R.id.buttonDeg
        };

        for (int id : buttons) {
            View v;
            v = mLayoutSimple.findViewById(id);

            if (v != null) {
                v.setOnClickListener(listener);
            }

            v = mLayoutScientific.findViewById(id);
            if (v != null) {
                v.setOnClickListener(listener);
            }
        }

        mLayoutSimple.findViewById(R.id.numerioDisplay).setOnClickListener(this::onDisplayClick);
        mLayoutScientific.findViewById(R.id.numerioDisplay).setOnClickListener(this::onDisplayClick);

        setMode(mMode);
    }

    @Override
    public void onStart () {
        super.onStart();
        loadState();
        numerioDisplay.setMachine(machine);
    }

    @Override
    public void onStop () {
        super.onStop();
        saveState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_numerio, menu);

        MenuItem item = menu.findItem(R.id.actionScientific);
        item.setChecked(mMode.equals("scientific"));

        item = menu.findItem(R.id.actionVibration);
        item.setChecked(mVibration);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int id = item.getItemId();
        if (id == R.id.actionScientific) {
            item.setChecked(! item.isChecked());

            if (item.isChecked()) {
                setMode("scientific");
            } else {
                setMode("simple");
            }
            saveState();
            return true;
        }

        if (id == R.id.actionVibration) {
            item.setChecked(! item.isChecked());

            mVibration = item.isChecked();
            saveState();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMode(String mode) {
        mMode = mode;
        if (mMode.equals("simple")) {
            mLayoutSimple.setVisibility(View.VISIBLE);
            mLayoutScientific.setVisibility(View.GONE);
            numerioDisplay = mLayoutSimple.findViewById(R.id.numerioDisplay);
        } else {
            mMode = "scientific";
            mLayoutScientific.setVisibility(View.VISIBLE);
            mLayoutSimple.setVisibility(View.GONE);
            numerioDisplay = mLayoutScientific.findViewById(R.id.numerioDisplay);
        }
        numerioDisplay.setMachine(machine);
        numerioDisplay.setMode(mMode);
    }

    private void onButtonClick(View view) {
        if (! (view instanceof NumerioButton)) {
            return;
        }

        if (mVibration) {
            view.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            );
        }

        NumerioButton button = (NumerioButton) view;
        int func;
        if (numerioDisplay.getSecondFn()) {
            func = button.function(2);
            if (func < 0) {
                func = button.function(1);
            }
        } else {
            func = button.function(1);
        }
        if (func < 0) {
            return;
        }
        if (func != FunctionNames.f_second_fn) {
            numerioDisplay.setSecondFn(false);
        }

        if (numerioDisplay.getHyp() && func != FunctionNames.f_hyp) {
            switch (func) {
                case FunctionNames.f_sin:
                case FunctionNames.f_asin:
                    func = FunctionNames.f_sinh;
                    break;
                case FunctionNames.f_cos:
                case FunctionNames.f_acos:
                    func = FunctionNames.f_cosh;
                    break;
                case FunctionNames.f_tan:
                case FunctionNames.f_atan:
                    func = FunctionNames.f_tanh;
                    break;
            }
            numerioDisplay.setHyp(false);
        }

        if (numerioDisplay.getArcHyp() && func != FunctionNames.f_arc_hyp) {
            switch (func) {
                case FunctionNames.f_sin:
                case FunctionNames.f_asin:
                    func = FunctionNames.f_asinh;
                    break;
                case FunctionNames.f_cos:
                case FunctionNames.f_acos:
                    func = FunctionNames.f_acosh;
                    break;
                case FunctionNames.f_tan:
                case FunctionNames.f_atan:
                    func = FunctionNames.f_atanh;
                    break;
            }
            numerioDisplay.setArcHyp(false);
        }

        switch(func) {
            case FunctionNames.f_ac:
                machine.ac();
                break;
            case FunctionNames.f_ce:
                machine.ce();
                break;
            case FunctionNames.f_exp:
                machine.numberExp();
                break;
            case FunctionNames.f_eng:
                numerioDisplay.numberModeNext();
                break;
            case FunctionNames.f_second_fn:
                numerioDisplay.setSecondFn(! numerioDisplay.getSecondFn());
                break;
            case FunctionNames.f_hyp:
                numerioDisplay.setHyp(! numerioDisplay.getHyp());
                break;
            case FunctionNames.f_arc_hyp:
                numerioDisplay.setArcHyp(! numerioDisplay.getArcHyp());
                break;
            case FunctionNames.f_parenthesis_opening:
                machine.parenthesisAdd('(');
                break;
            case FunctionNames.f_parenthesis_closing:
                machine.parenthesisAdd(')');
                break;
            case FunctionNames.f_number_0:
                machine.numberInput('0');
                break;
            case FunctionNames.f_number_1:
                machine.numberInput('1');
                break;
            case FunctionNames.f_number_2:
                machine.numberInput('2');
                break;
            case FunctionNames.f_number_3:
                machine.numberInput('3');
                break;
            case FunctionNames.f_number_4:
                machine.numberInput('4');
                break;
            case FunctionNames.f_number_5:
                machine.numberInput('5');
                break;
            case FunctionNames.f_number_6:
                machine.numberInput('6');
                break;
            case FunctionNames.f_number_7:
                machine.numberInput('7');
                break;
            case FunctionNames.f_number_8:
                machine.numberInput('8');
                break;
            case FunctionNames.f_number_9:
                machine.numberInput('9');
                break;
            case FunctionNames.f_number_dot:
                machine.numberInput('.');
                break;
            case FunctionNames.f_remove:
                machine.numberRemove();
                break;
            case FunctionNames.f_plus_minus:
                machine.plusMinus();
                break;
            case FunctionNames.f_plus:
                machine.operator('+');
                break;
            case FunctionNames.f_minus:
                machine.operator('-');
                break;
            case FunctionNames.f_divide:
                machine.operator('/');
                break;
            case FunctionNames.f_multiply:
                machine.operator('*');
                break;
            case FunctionNames.f_pow_y:
                machine.operator('^');
                break;
            case FunctionNames.f_root_y:
                machine.operator('√');
                break;
            case FunctionNames.f_evaluate:
                machine.evaluate();
                break;
            case FunctionNames.f_mem_clear:
                machine.memoryClear();
                break;
            case FunctionNames.f_mem_recall:
                machine.memoryGet();
                break;
            case FunctionNames.f_mem_plus:
                machine.memoryAdd();
                break;
            case FunctionNames.f_mem_minus:
                machine.memorySub();
                break;
            case FunctionNames.f_sqrt:
                machine.sqrt();
                break;
            case FunctionNames.f_cbrt:
                machine.cbrt();
                break;
            case FunctionNames.f_percentage:
                machine.percentage();
                break;
            case FunctionNames.f_pow2:
                machine.pow2();
                break;
            case FunctionNames.f_pow3:
                machine.pow3();
                break;
            case FunctionNames.f_drg:
                machine.nextDrg();
                break;
            case FunctionNames.f_to_drg:
                machine.toNextDrg();
                break;
            case FunctionNames.f_sin:
                machine.sin();
                break;
            case FunctionNames.f_cos:
                machine.cos();
                break;
            case FunctionNames.f_tan:
                machine.tan();
                break;
            case FunctionNames.f_asin:
                machine.asin();
                break;
            case FunctionNames.f_acos:
                machine.acos();
                break;
            case FunctionNames.f_atan:
                machine.atan();
                break;
            case FunctionNames.f_sinh:
                machine.sinh();
                break;
            case FunctionNames.f_cosh:
                machine.cosh();
                break;
            case FunctionNames.f_tanh:
                machine.tanh();
                break;
            case FunctionNames.f_asinh:
                machine.asinh();
                break;
            case FunctionNames.f_acosh:
                machine.acosh();
                break;
            case FunctionNames.f_atanh:
                machine.atanh();
                break;
            case FunctionNames.f_to_dms:
                machine.toDms();
                break;
            case FunctionNames.f_from_dms:
                machine.fromDms();
                break;
            case FunctionNames.f_pi:
                machine.pi();
                break;
            case FunctionNames.f_invert:
                machine.invert();
                break;
            case FunctionNames.f_factorial:
                machine.factorial();
                break;
            case FunctionNames.f_ln:
                machine.ln();
                break;
            case FunctionNames.f_e_pow_x:
                machine.e_pow_x();
                break;
            case FunctionNames.f_log:
                machine.log();
                break;
            case FunctionNames.f_10_pow_x:
                machine.ten_pow_x();
                break;
        }

        numerioDisplay.invalidate();
    }

    private void onDisplayClick(View view) {
        if (numerioDisplay == null) {
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("numerio", numerioDisplay.formatNumber());
        clipboard.setPrimaryClip(clip);
    }

    private  void saveState() {
        try {
            FileOutputStream file = openFileOutput(state_file, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(file);
            oos.writeObject(machine);
            oos.flush();
            oos.close();
            file.close();
        } catch (Exception ignored) {
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("mode", mMode);
        editor.putBoolean("vibration", mVibration);
        if (mLayoutScientific != null) {
            NumerioDisplay nd = mLayoutScientific.findViewById(R.id.numerioDisplay);
            editor.putString("numberMode", nd.getNumberMode().name());
        }
        editor.apply();
    }

    private  void loadState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mVibration = sharedPref.getBoolean("vibration", true);
        mMode = sharedPref.getString("mode", "simple");
        if (mLayoutScientific != null) {
            NumerioDisplay nd = mLayoutScientific.findViewById(R.id.numerioDisplay);
            String tmp = sharedPref.getString("numberMode", "FLO");
            nd.setNumberMode(NumerioDisplay.NumberMode.valueOf(tmp));
        }

        try {
            FileInputStream file = openFileInput(state_file);
            ObjectInputStream ois = new ObjectInputStream(file);
            machine = (NumerioMachine) ois.readObject();
            ois.close();
            file.close();
        } catch (Exception e) {
            machine = new NumerioMachine();
        }

        setMode(mMode);
    }
}