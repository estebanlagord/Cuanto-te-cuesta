package com.smartpocket.cuantoteroban.calc;

import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.smartpocket.cuantoteroban.MainActivity;
import com.smartpocket.cuantoteroban.R;
import com.smartpocket.cuantoteroban.Utilities;
import com.smartpocket.cuantoteroban.editortype.EditorType;
import com.smartpocket.cuantoteroban.editortype.EditorTypeHelper;

@SuppressWarnings("deprecation")
public class Calculator extends AppCompatActivity {

	public static final String RESULT = "result";
	public static final String RESULT_TYPE = "result_type";
	private static final int FRACTION_DIGITS = 4;
	private static final String INVALID_EXPRESSION = "La expresi�n no es v�lida";
	public static final String INVALID_PERCENTAGE = "El porcentaje debe ser un n�mero entre 0 y 100";
	public static final String INVALID_DISCOUNT100 = "El descuento no puede ser del 100% porque el valor quedar�a en $0";
	private static final String INFINITE_OR_NAN= "El resultado de la operaci�n no es un n�mero v�lido.\n�Dividiste por 0?";
	private static final int DELETE_FREQUENCY = 200;
	private String resultTextNameStr;
	private DecimalFormat localNumberFormat = (DecimalFormat)DecimalFormat.getInstance();
	private char decimalSeparator;
	private EditorType editorType;

	private EditText calcDialogDisplay;
	private TextView previous, enterTotal, seven, eight, nine, division, four, five, six, multiply, one, two, three, subtract, decimal, zero, equals, addition, left, right;
	private ImageView clear, allClear;
	private Handler mHandler = new Handler();
	private ActionMode mActionMode; 
	
    private Runnable mUpdateTask = new Runnable()
    {
        public void run()
        {
			deleteOneChar();
			
			mHandler.postAtTime(this, SystemClock.uptimeMillis() + DELETE_FREQUENCY);
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		this.resultTextNameStr = b.getString("editTextName");
		this.editorType = EditorTypeHelper.getEditorType(b.getString("type"));
        
        setContentView(R.layout.calculator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        
		ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Ingresa un valor para");
        actionBar.setSubtitle(resultTextNameStr);
        //actionBar.setLogo(R.drawable.logo);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
		decimalSeparator = localNumberFormat.getDecimalFormatSymbols().getDecimalSeparator();
		
		localNumberFormat.setMaximumFractionDigits(FRACTION_DIGITS);
		//localNumberFormat.setRoundingMode(RoundingMode.HALF_UP);
		localNumberFormat.setGroupingUsed(false);
        
		previous = (TextView) findViewById(R.id.previous);
        calcDialogDisplay = (EditText) findViewById(R.id.calc_dialog_display);
        enterTotal = (TextView) findViewById(R.id.enter_total);
        allClear = (ImageView) findViewById(R.id.all_clear);
        clear = (ImageView) findViewById(R.id.clear);
        seven = (TextView) findViewById(R.id.seven);
        eight = (TextView) findViewById(R.id.eight);
        nine = (TextView) findViewById(R.id.nine);
        division =(TextView) findViewById(R.id.division);
        four = (TextView) findViewById(R.id.four);
        five = (TextView) findViewById(R.id.five);
        six =(TextView) findViewById(R.id.six);
        multiply = (TextView) findViewById(R.id.multiply);
        one = (TextView) findViewById(R.id.one);
        two = (TextView) findViewById(R.id.two);
        three = (TextView) findViewById(R.id.three);
        subtract = (TextView) findViewById(R.id.substract);
        decimal = (TextView) findViewById(R.id.decimal);
        zero = (TextView) findViewById(R.id.zero);
        equals = (TextView) findViewById(R.id.equals);
        addition = (TextView) findViewById(R.id.addition);
        left = (TextView) findViewById(R.id.left);
        right = (TextView) findViewById(R.id.right);

        //calcDialogDisplay.setKeyListener(DigitsKeyListener.getInstance(true,true));

        previous.setText(b.getString("editTextValue"));
        decimal.setText(Character.toString(decimalSeparator));
        
        registerListeners();
        
        Typeface typeFace = MainActivity.TYPEFACE_ROBOTO_BLACK;
        TextView[] views = new TextView[]{enterTotal, seven, eight, nine, division, four, five, six, multiply, one, two, three, subtract, decimal, zero, equals, addition, left, right};
        //previous.setTypeface(MainActivity.TYPEFACE_CANTARELL);
        for (TextView view : views){
        	view.setTypeface(typeFace);
        	//view.setTextSize(20);
        }
    }

    // Called when the device is rotated
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// Called then
    	super.onSaveInstanceState(outState);
    	outState.putString("previous", previous.getText().toString());
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	if (savedInstanceState != null){
    		String previousValue = savedInstanceState.getString("previous");
    		if (previousValue != null)
    			previous.setText(previousValue);
    	}
    }
    
    public void registerListeners () {
    	one     .setOnClickListener(new AddValueListener("1"));
    	two     .setOnClickListener(new AddValueListener("2"));
    	three   .setOnClickListener(new AddValueListener("3"));
    	four    .setOnClickListener(new AddValueListener("4"));
    	five    .setOnClickListener(new AddValueListener("5"));
    	six     .setOnClickListener(new AddValueListener("6"));
    	seven   .setOnClickListener(new AddValueListener("7"));
        eight   .setOnClickListener(new AddValueListener("8"));
        nine    .setOnClickListener(new AddValueListener("9"));
        zero    .setOnClickListener(new AddValueListener("0"));
        left    .setOnClickListener(new AddValueListener("("));
        right   .setOnClickListener(new AddValueListener(")"));
        addition.setOnClickListener(new AddValueListener("+"));
        subtract.setOnClickListener(new AddValueListener("-"));
        multiply.setOnClickListener(new AddValueListener("*"));
        division.setOnClickListener(new AddValueListener("/"));
        decimal .setOnClickListener(new AddValueListener(Character.toString(decimalSeparator)));
        
        allClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishCopyPasteMode();
				calcDialogDisplay.setText("");
				previous.setText("");
				
			}
		});
        
        
        clear.setOnTouchListener(new View.OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent motionevent) {
				finishCopyPasteMode();
				
				int action = motionevent.getAction();
                if (action == MotionEvent.ACTION_DOWN)
                {
                    mHandler.removeCallbacks(mUpdateTask);
                    deleteOneChar();
                    mHandler.postAtTime(mUpdateTask, SystemClock.uptimeMillis() + DELETE_FREQUENCY*2);
                }
                else if (action == MotionEvent.ACTION_UP)
                {
                    mHandler.removeCallbacks(mUpdateTask);
                }
                return false;
			}
		});
        
        equals.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					finishCopyPasteMode();
					String prevExpr = calcDialogDisplay.getText().toString();
					if (!prevExpr.equals(INVALID_EXPRESSION))
						previous.setText(prevExpr);
					
					Parser parser = new Parser(localToEnglishNumber(calcDialogDisplay.getText().toString()));
					double resultDouble = parser.evaluate();
					if (Double.isInfinite(resultDouble) || Double.isNaN(resultDouble)) {
						showErrorMessage(v, INFINITE_OR_NAN);
						return;
					}
					
					resultDouble = Utilities.round(resultDouble, FRACTION_DIGITS);
					String result = localNumberFormat.format(resultDouble);
					calcDialogDisplay.setText(result);
					calcDialogDisplay.setSelection(result.length());
				} catch (SyntaxError e) {
					calcDialogDisplay.setText(INVALID_EXPRESSION);
				}
			}
		});
        
        enterTotal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishCopyPasteMode();
				String value = calcDialogDisplay.getText().toString();
				value = localToEnglishNumber(value);
				if (value.length() == 0){
					finish();
				} else {
					try {
						Parser parser = new Parser(value);
						double result = parser.evaluate();
						
						if (Double.isInfinite(result) || Double.isNaN(result)) {
							showErrorMessage(v, INFINITE_OR_NAN);
							return;
						}
						
						if (editorType == EditorType.DISCOUNT && result == 100){
							showErrorMessage(v, INVALID_DISCOUNT100);
							return; 
						}
						
						if (editorType == EditorType.DISCOUNT || editorType == EditorType.TAXES) {
								if (result > 100 || result < 0) {
									showErrorMessage(v, INVALID_PERCENTAGE);
									return;
								}
						}
						
						Intent resultIntent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putDouble(RESULT, result);
						bundle.putString(RESULT_TYPE, editorType.name());
						resultIntent.putExtras(bundle);
						setResult(RESULT_OK, resultIntent);
						finish();
					} catch (RuntimeException e) {
						showErrorMessage(v, INVALID_EXPRESSION);
					}
				}
			}
		});
        
        //calcDialogDisplay.setKeyListener(null);
        
        // for phones with physical keyboards, we want the enter key to mean "OK"
        calcDialogDisplay.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				enterTotal.performClick();
				return true;
			}
		});
        
        calcDialogDisplay.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
		        if (mActionMode != null) {
		            return false;
		        }

		        mActionMode = startSupportActionMode(mActionModeCallback);
		        return true;
			}
		});
    }

    private String localToEnglishNumber(String str){
    	String result = str.replace(decimalSeparator, '.');
    	return result;
    }
    

    private void deleteOneChar() {
		String text = calcDialogDisplay.getText().toString();
		int start = calcDialogDisplay.getSelectionStart();
		if (start > 0) {
			String newText = "";
			if (start > 0) {
				newText = text.substring(0, start-1);
				newText += text.substring(start, text.length());
			}
			
			calcDialogDisplay.setText(newText);

			if (text.length() > 0)
				calcDialogDisplay.setSelection(start-1);
		}
	}


	private void showErrorMessage(View view, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
		builder.setMessage(message).setTitle("Error");
		builder.setPositiveButton("Ignorar", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.setNegativeButton("Corregir", null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

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
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			
			switch (item.getItemId()) {
			case R.id.menu_copy:
				if (calcDialogDisplay != null && calcDialogDisplay.getText() != null) {
					String copiedText = calcDialogDisplay.getText().toString().trim();
					clipboard.setText(copiedText);
					Utilities.showToast("Valor copiado: " + copiedText);
				}
				mode.finish(); // Action picked, so close the CAB
				return true;

			case R.id.menu_paste:
				CharSequence clipboardText = clipboard.getText();
				if (clipboardText == null) {
					Utilities.showToast("El contenido del portapapeles no se puede pegar");
				} else {
					String content = clipboardText.toString().trim();
					calcDialogDisplay.setText(content);
					calcDialogDisplay.setSelection(content.length());
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
	
	class AddValueListener implements View.OnClickListener{
		String str;
		
		public AddValueListener(String str) {
			this.str = str;
		}
		
		@Override
		public void onClick(View v) {
			finishCopyPasteMode();
			
			String text = calcDialogDisplay.getText().toString();
			int start = calcDialogDisplay.getSelectionStart();
			String result = text.substring(0, start) + str;
			if (text.length() > start)
				result += text.substring(start, text.length());
			calcDialogDisplay.setText(result);
			calcDialogDisplay.setSelection(start+1);
		}
	}
}
