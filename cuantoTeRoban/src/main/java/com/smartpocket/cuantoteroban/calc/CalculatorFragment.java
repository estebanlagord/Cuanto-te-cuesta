package com.smartpocket.cuantoteroban.calc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpocket.cuantoteroban.MainActivity;
import com.smartpocket.cuantoteroban.R;
import com.smartpocket.cuantoteroban.SingleActivityVM;
import com.smartpocket.cuantoteroban.Utilities;
import com.smartpocket.cuantoteroban.databinding.CalculatorBinding;
import com.smartpocket.cuantoteroban.editortype.EditorType;

import java.text.DecimalFormat;

@SuppressWarnings("deprecation")
public class CalculatorFragment extends Fragment {

    public static final String RESULT = "result";
    public static final String RESULT_TYPE = "result_type";
    private static final int FRACTION_DIGITS = 4;
    private static final String INVALID_EXPRESSION = "La expresión no es válida";
    public static final String INVALID_PERCENTAGE = "El porcentaje debe ser un número entre 0 y 100";
    public static final String INVALID_DISCOUNT100 = "El descuento no puede ser del 100% porque el valor quedaría en $0";
    private static final String INFINITE_OR_NAN = "El resultado de la operación no es un número válido.\n¿Dividiste por 0?";
    private static final int DELETE_FREQUENCY = 200;
    private CalculatorBinding binding;
    private String resultTextNameStr;
    private final DecimalFormat localNumberFormat = (DecimalFormat) DecimalFormat.getInstance();
    private char decimalSeparator;
    private EditorType editorType;

    private TextInputLayout til;
    private EditText etCalculator;
    private TextView previous, enterTotal, seven, eight, nine, division, four, five, six, multiply, one, two, three, subtract, decimal, zero, equals, addition, left, right;
    private Button clear, allClear;
    private final Handler mHandler = new Handler();
    private ActionMode mActionMode;
    private SingleActivityVM singleActivityVM;

    private final Runnable mUpdateTask = new Runnable() {
        public void run() {
            deleteOneChar();

            mHandler.postAtTime(this, SystemClock.uptimeMillis() + DELETE_FREQUENCY);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CalculatorBinding.inflate(inflater);
        Toolbar toolbar = binding.getRoot().findViewById(R.id.my_awesome_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        CalculatorFragmentArgs args = CalculatorFragmentArgs.fromBundle(getArguments());
        this.resultTextNameStr = args.getEditTextName();
        this.editorType = args.getType();

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setTitle("Ingresa el valor");
        //actionBar.setLogo(R.drawable.logo);
        actionBar.setDisplayHomeAsUpEnabled(true);

        decimalSeparator = localNumberFormat.getDecimalFormatSymbols().getDecimalSeparator();

        localNumberFormat.setMaximumFractionDigits(FRACTION_DIGITS);
        //localNumberFormat.setRoundingMode(RoundingMode.HALF_UP);
        localNumberFormat.setGroupingUsed(false);

        til = binding.calcDialogDisplay;
        til.setHint(resultTextNameStr);

        previous = binding.previous;
        etCalculator = binding.etCalculator;
        enterTotal = binding.enterTotal;
        clear = binding.clear;
        seven = binding.seven;
        eight = binding.eight;
        nine = binding.nine;
        division = binding.division;
        four = binding.four;
        five = binding.five;
        six = binding.six;
        multiply = binding.multiply;
        one = binding.one;
        two = binding.two;
        three = binding.three;
        subtract = binding.substract;
        decimal = binding.decimal;
        zero = binding.zero;
        equals = binding.equals;
        addition = binding.addition;
        left = binding.left;
        right = binding.right;

        //etCalculator.setKeyListener(DigitsKeyListener.getInstance(true,true));

        previous.setText(args.getEditTextValue());
        decimal.setText(Character.toString(decimalSeparator));

        registerListeners();

        Typeface typeFace = MainActivity.TYPEFACE_ROBOTO_BLACK;
        TextView[] views = new TextView[]{enterTotal, seven, eight, nine, division, four, five, six, multiply, one, two, three, subtract, decimal, zero, equals, addition, left, right};
        //previous.setTypeface(MainActivity.TYPEFACE_CANTARELL);
        for (TextView v : views) {
            v.setTypeface(typeFace);
            //view.setTextSize(20);
        }

        singleActivityVM = ViewModelProviders.of(requireActivity()).get(SingleActivityVM.class);
    }


/*    // Called when the device is rotated //TODO
    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        // Called then
        super.onSaveInstanceState(outState);
        outState.putString("previous", previous.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String previousValue = savedInstanceState.getString("previous");
        if (previousValue != null)
            previous.setText(previousValue);
    }*/

    @SuppressLint("ClickableViewAccessibility")
    public void registerListeners() {
        one.setOnClickListener(new AddValueListener("1"));
        two.setOnClickListener(new AddValueListener("2"));
        three.setOnClickListener(new AddValueListener("3"));
        four.setOnClickListener(new AddValueListener("4"));
        five.setOnClickListener(new AddValueListener("5"));
        six.setOnClickListener(new AddValueListener("6"));
        seven.setOnClickListener(new AddValueListener("7"));
        eight.setOnClickListener(new AddValueListener("8"));
        nine.setOnClickListener(new AddValueListener("9"));
        zero.setOnClickListener(new AddValueListener("0"));
        left.setOnClickListener(new AddValueListener("("));
        right.setOnClickListener(new AddValueListener(")"));
        addition.setOnClickListener(new AddValueListener("+"));
        subtract.setOnClickListener(new AddValueListener("-"));
        multiply.setOnClickListener(new AddValueListener("*"));
        division.setOnClickListener(new AddValueListener("/"));
        decimal.setOnClickListener(new AddValueListener(Character.toString(decimalSeparator)));

        clear.setOnTouchListener((v, motionevent) -> {
            finishCopyPasteMode();

            int action = motionevent.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                mHandler.removeCallbacks(mUpdateTask);
                til.setError(null);
                deleteOneChar();
                mHandler.postAtTime(mUpdateTask, SystemClock.uptimeMillis() + DELETE_FREQUENCY * 2);
            } else if (action == MotionEvent.ACTION_UP) {
                mHandler.removeCallbacks(mUpdateTask);
            }
            return false;
        });

        equals.setOnClickListener(v -> {
            try {
                finishCopyPasteMode();
                String prevExpr = etCalculator.getText().toString();
                if (!prevExpr.equals(INVALID_EXPRESSION))
                    previous.setText(prevExpr);

                Parser parser = new Parser(localToEnglishNumber(etCalculator.getText().toString()));
                double resultDouble = parser.evaluate();
                if (Double.isInfinite(resultDouble) || Double.isNaN(resultDouble)) {
                    showErrorMessage(v, INFINITE_OR_NAN);
                    return;
                }

                resultDouble = Utilities.round(resultDouble, FRACTION_DIGITS);
                String result = localNumberFormat.format(resultDouble);
                etCalculator.setText(result);
                etCalculator.setSelection(result.length());
            } catch (SyntaxError e) {
                til.setError(INVALID_EXPRESSION);
            }
        });

        enterTotal.setOnClickListener(v -> {
            finishCopyPasteMode();
            String value = etCalculator.getText().toString();
            value = localToEnglishNumber(value);
            if (value.length() == 0) {
                NavHostFragment.findNavController(this).navigateUp();
            } else {
                try {
                    Parser parser = new Parser(value);
                    double result = parser.evaluate();

                    if (Double.isInfinite(result) || Double.isNaN(result)) {
                        showErrorMessage(v, INFINITE_OR_NAN);
                        return;
                    }

                    if (editorType == EditorType.DISCOUNT && result == 100) {
                        showErrorMessage(v, INVALID_DISCOUNT100);
                        return;
                    }

                    if (editorType == EditorType.DISCOUNT || editorType == EditorType.TAXES) {
                        if (result > 100 || result < 0) {
                            showErrorMessage(v, INVALID_PERCENTAGE);
                            return;
                        }
                    }

                    SingleActivityVM.CalculatorResult resultValue = new SingleActivityVM.CalculatorResult(result, editorType);
                    singleActivityVM.getCalculatorResultLD().setValue(resultValue);
                    NavHostFragment.findNavController(this).navigateUp();
                } catch (RuntimeException e) {
                    til.setError(INVALID_EXPRESSION);
                    showErrorMessage(v, INVALID_EXPRESSION);
                }
            }
        });

        //etCalculator.setKeyListener(null);

        // for phones with physical keyboards, we want the enter key to mean "OK"
        etCalculator.setOnEditorActionListener((v, actionId, event) -> {
            enterTotal.performClick();
            return true;
        });

        etCalculator.setOnLongClickListener(v -> {
            return mActionMode == null;

//            mActionMode = startSupportActionMode(mActionModeCallback);
        });
    }

    private String localToEnglishNumber(String str) {
        return str.replace(decimalSeparator, '.');
    }


    private void deleteOneChar() {
        String text = etCalculator.getText().toString();
        int start = etCalculator.getSelectionStart();
        if (start > 0) {
            String newText = text.substring(0, start - 1);
            newText += text.substring(start);

            etCalculator.setText(newText);
            etCalculator.setSelection(start - 1);
        }
    }


    private void showErrorMessage(View view, String message) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(view.getContext());
        builder.setMessage(message).setTitle("Error");
        builder.setPositiveButton("Ignorar", (dialog, which) -> NavHostFragment.findNavController(this).navigateUp());
        builder.setNegativeButton("Corregir", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.copy_paste_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);

            switch (item.getItemId()) {
                case R.id.menu_copy:
                    if (etCalculator != null && etCalculator.getText() != null) {
                        String copiedText = etCalculator.getText().toString().trim();
                        clipboard.setText(copiedText);
                        singleActivityVM.getSnackbarLD().setValue("Valor copiado: " + copiedText);
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.menu_paste:
                    CharSequence clipboardText = clipboard.getText();
                    if (clipboardText == null) {
                        singleActivityVM.getSnackbarLD().setValue("El contenido del portapapeles no se puede pegar");
                    } else {
                        String content = clipboardText.toString().trim();
                        etCalculator.setText(content);
                        etCalculator.setSelection(content.length());
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    private void finishCopyPasteMode() {
        if (mActionMode != null)
            mActionMode.finish();
    }

    class AddValueListener implements View.OnClickListener {
        String str;

        public AddValueListener(String str) {
            this.str = str;
        }

        @Override
        public void onClick(View v) {
            finishCopyPasteMode();

            til.setError(null);
            String text = etCalculator.getText().toString();
            int start = etCalculator.getSelectionStart();
            String result = text.substring(0, start) + str;
            if (text.length() > start)
                result += text.substring(start);
            etCalculator.setText(result);
            etCalculator.setSelection(start + 1);
        }
    }
}
