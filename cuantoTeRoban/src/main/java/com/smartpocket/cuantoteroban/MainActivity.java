package com.smartpocket.cuantoteroban;

// URL para bajar la cotizacion: http://download.finance.yahoo.com/d/quotes.csv?s=BRLARS=x&f=l1
// sino parsear la de AMEX: http://www.amexpromociones.com.ar/cotizacion/

// Nov 10 2012
// Yahoo dice: 4.775
// AMEX dice:  4.800

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartpocket.cuantoteroban.calc.CalculatorFragment;
import com.smartpocket.cuantoteroban.editortype.EditorType;
import com.smartpocket.cuantoteroban.editortype.EditorTypeHelper;
import com.smartpocket.cuantoteroban.preferences.PreferencesManager;
import com.smartpocket.cuantoteroban.search.AddCurrencyFragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

@SuppressLint("RtlHardcoded")
@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements DeleteCurrencyDialogListener {

	public static final int FRACTION_DIGITS = 2;
	public static Typeface TYPEFACE_ROBOTO_MEDIUM;
	public static Typeface TYPEFACE_ROBOTO_BLACK;
	public static Typeface TYPEFACE_ROBOTO_CONDENSED_ITALIC;
	
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	
	
	private EditText amountText, discountText, taxesText, totalText, pesosText, creditCardText, savingsText, blueText, agencyText, payPalText;
	private AmountTextWatcher amountTextWatcher, discountTextWatcher, taxesTextWatcher, totalTextWatcher, pesosTextWatcher,
	                           creditCardTextWatcher, savingsTextWatcher, blueTextWatcher, agencyTextWatcher, payPalTextWatcher;
	private EditText currentEditTextBeingEdited;
	
	private EditorType currentEditorType; //used for copy-paste
	private final DecimalFormat shortNumberFormat = (DecimalFormat)DecimalFormat.getInstance();
	public enum RequestCode {SETTINGS, CALCULATOR, CHOOSE_CURRENCY, ADD_CURRENCY}
	private boolean areTextWatchersEnabled = false;
	enum DiscountOrTax {DISCOUNT, TAXES}
	private static MainActivity theInstance = null;
	private MenuItem refreshItem;
	private ImageView rotatingRefreshButtonView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private Animation refreshButtonRotation;
	private ActionMode mActionMode;
	private ShareActionProvider mShareActionProvider;
	private AlertDialog currentDialog;

//	private MainActivityVM viewModel;

		
	public MainActivity() {
		theInstance = this;
	}

	@Deprecated
	public static MainActivity getInstance() {
		if (theInstance == null)
			throw new IllegalStateException("The MainActivity instance is null!");
		else
			return theInstance;
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.container_main);
    	
    	Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

		mSwipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.my_app_green);

        /*ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle(null);
        actionBar.setLogo(R.drawable.logo);*/

		TYPEFACE_ROBOTO_MEDIUM = Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf");
		TYPEFACE_ROBOTO_BLACK = Typeface.createFromAsset(getAssets(), "fonts/roboto_black.ttf");
		TYPEFACE_ROBOTO_CONDENSED_ITALIC = Typeface.createFromAsset(getAssets(), "fonts/roboto_condensed_italic.ttf");
		shortNumberFormat.setMinimumFractionDigits(FRACTION_DIGITS);
		shortNumberFormat.setMaximumFractionDigits(FRACTION_DIGITS);
        
		setupNavDrawer();
        setFonts();
        
        //PreferencesManager.getInstance().setMainActivity(this);
        
        //showOrHideConversions();
        updateVisibilityForLastUpdateMsg(false);
        updateFlag(false);
        
        // update currency
       	updateExchangeRate(false);

       	setupListeners();

//		setupViewModel();
	}

/*	private void setupViewModel() {
		viewModel = ViewModelProviders.of(this).get(MainActivityVM.class);
		viewModel.getOfficialLiveData().observe(this, officialValue -> {
			pesosText.setText(officialValue.toString());
		});
		viewModel.getCardLiveData().observe(this, cardValue -> {
			creditCardText.setText(cardValue.toString());
		});
	}*/

	@Override
	protected void onResume() {
		if (theInstance == null)
			theInstance = this;
		
        // When rotating the screen, the activity may be created with the Nav Drawer already open
        // in that case I need to set the title in the action bar
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawerList)) {
        	final ActionBar actionBar = getSupportActionBar();
        	actionBar.setTitle(R.string.title_activity_choose_currency);
            //actionBar.setDisplayShowTitleEnabled(true);
        }
		
		enableEditTextListeners();
       	recalculateConversionRates();
       	
		//restoreLastConversion();

       	// hide Total if discount and taxes are 0 on startup 
     	showOrHideConversions();
		
		super.onResume();
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		
		// If we are displaying an alert dialog, we need to dismiss it to prevent an exception
		if(currentDialog != null && currentDialog.isShowing()) {
			currentDialog.dismiss();
		}
	}
	
	private void setupNavDrawer() {
		final ActionBar actionBar = getSupportActionBar();
		
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.menu_change,  /* "open drawer" description */
                R.string.menu_change  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                actionBar.setTitle(R.string.app_name);
                //actionBar.setDisplayShowTitleEnabled(false);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(R.string.title_activity_choose_currency);
                //actionBar.setDisplayShowTitleEnabled(true);
                updateRefreshProgress();
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                
                if (mActionMode != null)
                	mActionMode.finish();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        
        mDrawerList = findViewById(R.id.left_drawer);
        // Set the adapter for the list view
//        mDrawerList.setAdapter(new ChosenCurrenciesAdapter(this));
        
        // Open nav drawer unless it has been opened manually
//        if (PreferencesManager.getInstance().isNavDrawerANewFeature())
        	mDrawerLayout.openDrawer(Gravity.LEFT);
	}
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

	
	private void setupListeners() {
		
		// Edit Text listeners
       	amountTextWatcher = new AmountTextWatcher(this, EditorType.AMOUNT);
       	discountTextWatcher   = new AmountTextWatcher(this, EditorType.DISCOUNT);
       	taxesTextWatcher      = new AmountTextWatcher(this, EditorType.TAXES);
       	totalTextWatcher      = new AmountTextWatcher(this, EditorType.TOTAL);
       	pesosTextWatcher      = new AmountTextWatcher(this, EditorType.PESOS);
       	creditCardTextWatcher = new AmountTextWatcher(this, EditorType.CREDIT_CARD);
        savingsTextWatcher    = new AmountTextWatcher(this, EditorType.SAVINGS);
        blueTextWatcher = new AmountTextWatcher(this, EditorType.BLUE);
       	agencyTextWatcher     = new AmountTextWatcher(this, EditorType.EXCHANGE_AGENCY);
       	payPalTextWatcher     = new AmountTextWatcher(this, EditorType.PAYPAL);
       	
		amountText = findViewById(R.id.amountEditText);
        amountText.setOnClickListener(new OnClickListenerShowCalc(amountText, getResources().getString(R.string.Ammount), EditorType.AMOUNT));
        amountText.setOnLongClickListener(onLongClickShowCopyPaste(amountText, EditorType.AMOUNT));
        
        discountText = findViewById(R.id.discountEditText);
        discountText.setOnClickListener(new OnClickListenerShowCalc(discountText, getResources().getString(R.string.Discount), EditorType.DISCOUNT));
        discountText.setOnLongClickListener(onLongClickShowCopyPaste(discountText, EditorType.DISCOUNT));
        
        taxesText = findViewById(R.id.taxesEditText);
        taxesText.setOnClickListener(new OnClickListenerShowCalc(taxesText, getResources().getString(R.string.Taxes), EditorType.TAXES));
        taxesText.setOnLongClickListener(onLongClickShowCopyPaste(taxesText, EditorType.TAXES));
        
        totalText = findViewById(R.id.totalEditText);
        totalText.setOnClickListener(new OnClickListenerShowCalc(totalText, getResources().getString(R.string.Total), EditorType.TOTAL));
        totalText.setOnLongClickListener(onLongClickShowCopyPaste(totalText, EditorType.TOTAL));
        
        pesosText = findViewById(R.id.inPesosValue);
        pesosText.setOnClickListener(new OnClickListenerShowCalc(pesosText, getResources().getString(R.string.InPesos), EditorType.PESOS));
        pesosText.setOnLongClickListener(onLongClickShowCopyPaste(pesosText, EditorType.PESOS));
        
        creditCardText = findViewById(R.id.withCreditCardValue);
        creditCardText.setOnClickListener(new OnClickListenerShowCalc(creditCardText, getResources().getString(R.string.WithCreditCard), EditorType.CREDIT_CARD));
        creditCardText.setOnLongClickListener(onLongClickShowCopyPaste(creditCardText, EditorType.CREDIT_CARD));

//        savingsText = findViewById(R.id.withSavingsValue);
        savingsText.setOnClickListener(new OnClickListenerShowCalc(savingsText, getResources().getString(R.string.WithSavings), EditorType.SAVINGS));
        savingsText.setOnLongClickListener(onLongClickShowCopyPaste(savingsText, EditorType.SAVINGS));

        blueText = findViewById(R.id.withBlueValue);
        blueText.setOnClickListener(new OnClickListenerShowCalc(blueText, getResources().getString(R.string.WithBlue), EditorType.BLUE));
        blueText.setOnLongClickListener(onLongClickShowCopyPaste(blueText, EditorType.BLUE));
        
        agencyText = findViewById(R.id.exchangeAgencyValue);
        agencyText.setOnClickListener(new OnClickListenerShowCalc(agencyText, getResources().getString(R.string.ExchangeAgency), EditorType.EXCHANGE_AGENCY));
        agencyText.setOnLongClickListener(onLongClickShowCopyPaste(agencyText, EditorType.EXCHANGE_AGENCY));
        
//        payPalText = findViewById(R.id.payPalValue);
        payPalText.setOnClickListener(new OnClickListenerShowCalc(payPalText, getResources().getString(R.string.Paypal), EditorType.PAYPAL));
        payPalText.setOnLongClickListener(onLongClickShowCopyPaste(payPalText, EditorType.PAYPAL));
        
//        mDrawerList.setOnItemLongClickListener(new ChosenCurrencyLongClickListener(this));
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItemFromNavDrawer(position);
			}
		});
                
        //enableEditTextListeners();
        
        // Change currency listener
        ImageView foreignCurrencyFlag = findViewById(R.id.countryFlag);
        //foreignCurrencyFlag.setOnTouchListener(new OnTouchListenerShowNavDrawer());
        foreignCurrencyFlag.setOnClickListener(new OnClickListenerShowNavDrawer());
        
//        View deleteDiscountView = findViewById(R.id.deleteDiscount);
//        deleteDiscountView.setOnClickListener(new OnClickListenerDeleteDiscountOrTax(DiscountOrTax.DISCOUNT));
//        View deleteTaxesView = findViewById(R.id.deleteTaxes);
//        deleteTaxesView.setOnClickListener(new OnClickListenerDeleteDiscountOrTax(DiscountOrTax.TAXES));

		// swipe to refresh
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				updateExchangeRate(true);
			}
		});
	}

	private OnLongClickListener onLongClickShowCopyPaste(final EditText editText, final EditorType type) {
		return new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
		        if (mActionMode != null) {
		            return false;
		        }

		        currentEditTextBeingEdited = editText;
		        //currentEditTextBeingEdited_Name = editTextName;
		        currentEditorType = type;
		        updateEditTextBackgrounds(currentEditTextBeingEdited);
		        mActionMode = startSupportActionMode(mActionModeCallback);
		        return true;
			}
		};
	}
	
	public synchronized void disableEditTextListeners(){
		if (areTextWatchersEnabled) {
			//Log.d("Main Activity", "\tDisabled Edit Text Listeners");
			amountText.removeTextChangedListener(amountTextWatcher);
			discountText.removeTextChangedListener(discountTextWatcher);
			taxesText.removeTextChangedListener(taxesTextWatcher);
			totalText.removeTextChangedListener(totalTextWatcher);
			pesosText.removeTextChangedListener(pesosTextWatcher);
			creditCardText.removeTextChangedListener(creditCardTextWatcher);
            savingsText.removeTextChangedListener(savingsTextWatcher);
            blueText.removeTextChangedListener(blueTextWatcher);
			agencyText.removeTextChangedListener(agencyTextWatcher);
			payPalText.removeTextChangedListener(payPalTextWatcher);
			areTextWatchersEnabled = false;
		}
	}
	
	public synchronized void enableEditTextListeners(){
		
		if (!areTextWatchersEnabled) {
			//Log.d("Main Activity", "Enabled Edit Text Listeners");
			areTextWatchersEnabled = true;
			amountText.addTextChangedListener(amountTextWatcher);
			discountText.addTextChangedListener(discountTextWatcher);
			taxesText.addTextChangedListener(taxesTextWatcher);
			totalText.addTextChangedListener(totalTextWatcher);
			pesosText.addTextChangedListener(pesosTextWatcher);
			creditCardText.addTextChangedListener(creditCardTextWatcher);
            savingsText.addTextChangedListener(savingsTextWatcher);
            blueText.addTextChangedListener(blueTextWatcher);
			agencyText.addTextChangedListener(agencyTextWatcher);
			payPalText.addTextChangedListener(payPalTextWatcher);
		}
	}
	

	public void updateExchangeRatesAfterAddingNewCurrency() {
//		if (PreferencesManager.getInstance().isAutomaticUpdateEnabled())
			updateExchangeRate(true);
	}

	private void updateExchangeRate(boolean force) {
		DownloadExchangeRate downloadExchangeRate = new DownloadExchangeRate(this, force);
		downloadExchangeRate.loadLastUpdateStr();

//		if (force || ( PreferencesManager.getInstance().isAutomaticUpdateEnabled()
//					   && downloadExchangeRate.needsUpdate()
//					 )){
//			if (!DownloadExchangeRate.updateInProgress){
//				updateVisibilityForLastUpdateMsg(true);
//				downloadExchangeRate.execute();
//			}
//		}
	}

	public synchronized void updateRefreshProgress() {
		if (rotatingRefreshButtonView != null && refreshItem != null && refreshButtonRotation != null) {
			if (DownloadExchangeRate.updateInProgress 
					&& (mDrawerLayout == null || !mDrawerLayout.isDrawerOpen(mDrawerList)))
			{
				rotatingRefreshButtonView.startAnimation(refreshButtonRotation);
				MenuItemCompat.setActionView(refreshItem, rotatingRefreshButtonView);
				mSwipeRefreshLayout.setRefreshing(true);
			} else{
				if (MenuItemCompat.getActionView(refreshItem) != null){
					MenuItemCompat.getActionView(refreshItem).clearAnimation();
					MenuItemCompat.setActionView(refreshItem, null);
				}
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}
		
		/*ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		if (progressBar != null) {
			if (DownloadExchangeRate.updateInProgress)
				progressBar.setVisibility(View.VISIBLE);
			else
				progressBar.setVisibility(View.INVISIBLE);
		}*/
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.clear();
    	getMenuInflater().inflate(R.menu.activity_main, menu);

    	refreshItem = menu.findItem(R.id.menu_update);

    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rotatingRefreshButtonView = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

        refreshButtonRotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
        
       	// this is necessary because the update begins before onCreateOptionsMenu is called
        updateRefreshProgress();

        // testing line
        
        // Set up ShareActionProvider's default share intent
        MenuItem shareItem = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getUpdatedShareIntent());
        mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
			
			@Override
			public boolean onShareTargetSelected(ShareActionProvider arg0, Intent shareIntent) {
				try {
					// copiar el texto al portapapeles, para poder pegarlo por ejemplo en Facebook
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					//Intent shareIntent = getUpdatedShareIntent();
					String shareContent = shareIntent.getExtras().get(Intent.EXTRA_TEXT).toString();
					
					clipboard.setText(shareContent);
					Utilities.showToast("Conversión copiada al portapapeles.\nLa podes pegar en cualquier aplicación.");
				} catch (Exception e){}
				return false;
			}
		});

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// show/hide buttons depending on whether the nav drawer is open
    	int[] disabledWhenNavDrawerIsOpen = new int[]{R.id.menu_share, R.id.menu_about, R.id.menu_help, R.id.menu_settings, R.id.menu_share, R.id.menu_update};
    	
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        if (drawerOpen) {
        	menu.findItem(R.id.menu_add_currency).setVisible(true);
        	for (int i : disabledWhenNavDrawerIsOpen)
        		menu.findItem(i).setVisible(false);
        } else {
        	menu.findItem(R.id.menu_add_currency).setVisible(false);
        	for (int i : disabledWhenNavDrawerIsOpen)
        		menu.findItem(i).setVisible(true);
        }
    	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	// If the user pressed the app icon, and if the drawer is closed, change the preference
    	// to avoid opening automatically the nav drawer on next launch
    	if ((item.getItemId() == android.R.id.home) && (!mDrawerLayout.isDrawerOpen(mDrawerList)))
//    		PreferencesManager.getInstance().setIsNavDrawerNew(false);
    	
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
    	
    	switch(item.getItemId()){
    		case R.id.menu_settings:
    			//Intent settingsIntent = new Intent(this, PreferencesScreen.class);
//    			Intent settingsIntent = new Intent(this, PreferencesActivity.class);
//    			startActivityForResult(settingsIntent, RequestCode.SETTINGS.ordinal());
    			break;
    		case R.id.menu_update:
    			updateExchangeRate(true);
    			recalculateConversionRates();
    			break;
//    		case R.id.menu_change:
//    			Intent chooseCurrency = new Intent(this, ChooseCurrency.class);
//				startActivityForResult(chooseCurrency, RequestCode.CHOOSE_CURRENCY.ordinal());
//				break;
    		case R.id.menu_add_currency:
    			Intent intent = new Intent(this, AddCurrencyFragment.class);
    	    	startActivityForResult(intent, RequestCode.ADD_CURRENCY.ordinal());
				break;
    		case R.id.menu_help:
    			Intent helpIntent = new Intent(this, HelpActivity.class);
    			startActivity(helpIntent);
    			break;
    		case R.id.menu_about:
    			Intent aboutIntent = new Intent(this, About.class);
    			startActivity(aboutIntent);
    			break;
            default:
                return super.onOptionsItemSelected(item);
    	}
    	return true;
    }
    

    @Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	    // called when the PreferenceScreen is closed
		if (requestCode == RequestCode.SETTINGS.ordinal()) {
			// show/hide last update time
			updateVisibilityForLastUpdateMsg(false);
			// change flag if necessary
			updateFlag(true);
			// update selected item in nav drawer
			ChosenCurrenciesAdapter navDrawerAdapter = (ChosenCurrenciesAdapter) mDrawerList.getAdapter();
			navDrawerAdapter.updateSelectedItem();
			
			// update currency if the user wants it now
			updateExchangeRate(false);
			
	       	// if "Discount %" or "Taxes %" is disabled, set its preference value to 0
//	       	if (!PreferencesManager.getInstance().isShowDiscount() && PreferencesManager.getInstance().getDiscount() == 0)
//	       		PreferencesManager.getInstance().setDiscount(0);
//	       	if (!PreferencesManager.getInstance().isShowTaxes() && PreferencesManager.getInstance().getTaxes() == 0)
//	       		PreferencesManager.getInstance().setTaxes(0);
	       	
			// get preference values again
			recalculateConversionRates();
			// recalcular los valores cuando se vuelva de la pantalla de preferencias
			TextView lastOneChanged = AmountTextWatcher.lastOneChanged;
			if (lastOneChanged == amountText) {
				amountText.setText(amountText.getText());
			} else if (lastOneChanged == pesosText) {
				pesosText.setText(pesosText.getText());
			} else if (lastOneChanged == creditCardText) {
				creditCardText.setText(creditCardText.getText());
            } else if (lastOneChanged == savingsText) {
                savingsText.setText(savingsText.getText());
            } else if (lastOneChanged == blueText) {
                blueText.setText(blueText.getText());
			} else if (lastOneChanged == agencyText) {
				agencyText.setText(agencyText.getText());
			} else if (lastOneChanged == payPalText) {
				payPalText.setText(payPalText.getText());
			}  else if (lastOneChanged == discountText) {
				discountText.setText(discountText.getText());
			} else if (lastOneChanged == taxesText) {
				taxesText.setText(taxesText.getText());
			} else if (lastOneChanged == totalText) {
				totalText.setText(totalText.getText());
			}
		} else if (requestCode == RequestCode.CALCULATOR.ordinal()){
			if (resultCode == RESULT_OK){
				String editorTypeName = data.getExtras().getString(CalculatorFragment.RESULT_TYPE);
				EditorType editorType = EditorTypeHelper.getEditorType(editorTypeName);
				EditText targetEditText = EditorTypeHelper.getEditTextForEditorType(this, editorType);
				
				double newValue = data.getExtras().getDouble(CalculatorFragment.RESULT);
				newValue = Utilities.round(newValue, FRACTION_DIGITS);
				String valueStr = shortNumberFormat.format(newValue);
				targetEditText.setText(valueStr);
				updateEditTextBackgrounds(targetEditText);
				
				// save which EditText was edited
				if (targetEditText != discountText 
						&& targetEditText != taxesText)
				{
//					PreferencesManager.getInstance().setLastConversionType(editorType);
//					PreferencesManager.getInstance().setLastConversionValue(newValue);
				}
				
				updateDeleteTaxOrDiscountVisibility();
			}
		} else if (requestCode == RequestCode.CHOOSE_CURRENCY.ordinal()) {
			if (resultCode == RESULT_OK) {
				onActivityResult(RequestCode.SETTINGS.ordinal(), 0, null);
			}
		} else if (requestCode == RequestCode.ADD_CURRENCY.ordinal()) {
			((ChosenCurrenciesAdapter)mDrawerList.getAdapter()).updateCurrenciesList();
		}
		
		// show/hide wanted/unwanted conversions
		showOrHideConversions();
		
		if (mShareActionProvider != null)
			mShareActionProvider.setShareIntent(getUpdatedShareIntent());
	}
    
    private void updateDeleteTaxOrDiscountVisibility() {
//    	View deleteTaxButton = findViewById(R.id.deleteTaxes);
//    	View deleteDiscountButton = findViewById(R.id.deleteDiscount);
    	String discountStr = ((TextView) findViewById(R.id.discountEditText)).getText().toString();
    	String taxStr = ((TextView) findViewById(R.id.taxesEditText)).getText().toString();
    	
    	try {
	    	NumberFormat nf = NumberFormat.getInstance();
	    	double discount = nf.parse(discountStr).doubleValue();
	    	double tax      = nf.parse(taxStr).doubleValue();
//	    	deleteDiscountButton.setVisibility(discount==0 ? View.GONE : View.VISIBLE);
//	    	deleteTaxButton.setVisibility(tax==0 ? View.GONE : View.VISIBLE);
    	} catch (ParseException e) {}
    }

    // gets the content to share with other apps
    private Intent getUpdatedShareIntent() {
    	boolean showDiscount = false; 
    	boolean	showTaxes = false;
    	boolean	showTotal;
//    	boolean	showPesos = PreferencesManager.getInstance().isShowPesos();
//    	boolean	showCreditCard = PreferencesManager.getInstance().isShowCreditCard();
//        boolean showSavings = PreferencesManager.getInstance().isShowSavings();
//        boolean showBlue = PreferencesManager.getInstance().isShowBlue();
    	boolean	showAgency = false;
//    	boolean	showPayPal = PreferencesManager.getInstance().isShowPaypal();
    	
//    	if (PreferencesManager.getInstance().isShowDiscount()) {
    		try {
    			double discountValue = Double.parseDouble(discountText.getText().toString());
    			if (discountValue != 0)	
    				showDiscount = true;
    		} catch (Exception e) { }
//    	}
    	
//    	if (PreferencesManager.getInstance().isShowTaxes()) {
    		try {
    			double taxesValue = Double.parseDouble(taxesText.getText().toString());
    			if (taxesValue != 0) 
    				showTaxes = true;
    		} catch (Exception e) { }
//    	}
    	
    	showTotal = showDiscount || showTaxes;
    	
//    	if (PreferencesManager.getInstance().isShowExchangeAgency()) {
    		try {
    			double agencyValue = Double.parseDouble(agencyText.getText().toString());
    			if (agencyValue != 0) 
    				showAgency = true;
    		} catch (Exception e) { }
//    	}
    	
    	/*StringBuilder sharedText = new StringBuilder("Lo que te cobran en " + PreferencesManager.getInstance().getCurrentCurrency().getName() + ":");
    	sharedText.append("\nMonto: $" + amountText.getText().toString());
    	
    	if (showDiscount)
    		sharedText.append("\nDescuento: " + discountText.getText().toString() + "%");
    	
    	if (showTaxes)
    		sharedText.append("\nRecargo: " + taxesText.getText().toString() + "%");
    	
    	if (showTotal)
    		sharedText.append("\nTotal: $" + totalText.getText().toString());
    	
    	sharedText.append("\n\nLo que te cuesta en Pesos argentinos:");
    	
    	if (showPesos)
    		sharedText.append("\nOficial: $" + pesosText.getText().toString());
        if (showSavings)
            sharedText.append("\nAhorro: $" + savingsText.getText().toString());
    	if (showCreditCard)
    		sharedText.append("\nTarjeta: $" + creditCardText.getText().toString());
        if (showBlue)
            sharedText.append("\nBlue: $" + blueText.getText().toString());
    	if (showAgency)
    		sharedText.append("\nCasa de Cambio: $" + agencyText.getText().toString());
    	if (showPayPal)
    		sharedText.append("\nPayPal: $" + payPalText.getText().toString());
    	
    	sharedText.append("\n\nCalculado por la aplicación ¿Cuanto Te Cuesta? para Android." +
    			"\nBajala gratis desde: http://play.google.com/store/apps/details?id=com.smartpocket.cuantoteroban"); */
    	
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "¿Cuanto Te Cuesta?");
//        intent.putExtra(Intent.EXTRA_TEXT, sharedText.toString());
		return intent;
	}

	private void updateEditTextBackgrounds(EditText lastOneChanged){
    	Typeface defaultFont = android.graphics.Typeface.DEFAULT;
    	Typeface bold = TYPEFACE_ROBOTO_BLACK;
    	
    	if (lastOneChanged == amountText)
    		amountText.setTypeface(bold);
    	else
    		amountText.setTypeface(defaultFont);
    	
    	if (lastOneChanged == discountText)
    		discountText.setTypeface(bold);
    	else
    		discountText.setTypeface(defaultFont);
    	
    	if (lastOneChanged == taxesText)
    		taxesText.setTypeface(bold);
    	else
    		taxesText.setTypeface(defaultFont);
    	
    	if (lastOneChanged == totalText)
    		totalText.setTypeface(bold);
    	else
    		totalText.setTypeface(defaultFont);
    	
    	if (lastOneChanged == pesosText)
    		pesosText.setTypeface(bold);
    	else
    		pesosText.setTypeface(defaultFont);

        if (lastOneChanged == savingsText)
            savingsText.setTypeface(bold);
        else
            savingsText.setTypeface(defaultFont);

        if (lastOneChanged == blueText)
            blueText.setTypeface(bold);
        else
            blueText.setTypeface(defaultFont);

    	if (lastOneChanged == creditCardText)
    		creditCardText.setTypeface(bold);
    	else
    		creditCardText.setTypeface(defaultFont);
    	
    	if (lastOneChanged == agencyText)
    		agencyText.setTypeface(bold);
    	else
    		agencyText.setTypeface(defaultFont);
    	
    	if (lastOneChanged == payPalText)
    		payPalText.setTypeface(bold);
    	else
    		payPalText.setTypeface(defaultFont);
    }
    
	public void recalculateConversionRates() {
		AmountTextWatcher.preferencesChanged();
		restoreLastConversion(); // recalculates the conversions
	}
	
	private void restoreLastConversion() {
		/*try {
			boolean userWantsToRemember = PreferencesManager.getInstance().isRememberLastConversion();
			
			if (userWantsToRemember){
				//String lastConversionEditText = PreferencesManager.getInstance().getLastConversionText();
				EditorType editorType = PreferencesManager.getInstance().getLastConversionType();
				double lastConversionValue = PreferencesManager.getInstance().getLastConversionValue();
				
				if (editorType == null)
					editorType = EditorType.AMOUNT;
				
				switch (editorType) {
					case AMOUNT:
						currentEditTextBeingEdited = amountText;
						break;
					case DISCOUNT:
						currentEditTextBeingEdited = discountText;
						break;
					case TAXES:
						currentEditTextBeingEdited = taxesText;
						break;
					case TOTAL:
						currentEditTextBeingEdited = totalText;
						break;
					case PESOS:
						currentEditTextBeingEdited = pesosText;
						break;
					case CREDIT_CARD:
						currentEditTextBeingEdited = creditCardText;
						break;
                    case SAVINGS:
                        currentEditTextBeingEdited = savingsText;
                        break;
                    case BLUE:
                        currentEditTextBeingEdited = blueText;
                        break;
					case EXCHANGE_AGENCY:
						currentEditTextBeingEdited = agencyText;
						break;
					case PAYPAL:
						currentEditTextBeingEdited = payPalText;
						break;

				}
			
				Intent resultIntent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putDouble(CalculatorFragment.RESULT, lastConversionValue);
				bundle.putString(CalculatorFragment.RESULT_TYPE, editorType.name());
				resultIntent.putExtras(bundle);
			
				onActivityResult(RequestCode.CALCULATOR.ordinal(), RESULT_OK, resultIntent);
			}
		} catch (Exception e) {
			System.out.println("Error restoring last conversion");
			e.printStackTrace();
		}*/
		
	}


	private void updateFlag(boolean fadeFlag) {
//		final Currency chosenCurrency = PreferencesManager.getInstance().getCurrentCurrency();
		final ImageView countryFlagView = findViewById(R.id.countryFlag);
//		final int newFlagIdentifier = chosenCurrency.getFlagIdentifier();
		
		if(fadeFlag) {
			final Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.flag_transition_in);
			final Animation fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.flag_transition_out);
			
			fadeOutAnim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
//					countryFlagView.setImageResource(newFlagIdentifier);
					countryFlagView.startAnimation(fadeInAnim);
				}
			});
			countryFlagView.startAnimation(fadeOutAnim);
		} else {
			// change the flag without showing the fade animation
//			countryFlagView.setImageResource(newFlagIdentifier);
		}
				
		TextView chosenCurrencyName = findViewById(R.id.currencyName);
//		chosenCurrencyName.setText("en " + chosenCurrency.getName());
	}

	private void updateVisibilityForLastUpdateMsg(boolean forceShow) {
//		boolean areUpdatesEnabled = PreferencesManager.getInstance().isAutomaticUpdateEnabled();
		View lastUpdateStr = findViewById(R.id.textLastUpdate);
		View lastUpdateVal = findViewById(R.id.textLastUpdateValue);
		
//		if (areUpdatesEnabled || forceShow) {
			lastUpdateStr.setVisibility(View.VISIBLE);
			lastUpdateVal.setVisibility(View.VISIBLE);
//		} else if (!DownloadExchangeRate.updateInProgress){
			lastUpdateStr.setVisibility(View.GONE);
			lastUpdateVal.setVisibility(View.GONE);
//		}
	}
	
	private void showOrHideConversions() {
		View discountView = null;//findViewById(R.id.tableRowDiscount);
		View totalView    = null;//findViewById(R.id.tableRowTotal);
		View pesosView    =  null;//findViewById(R.id.tableRowPesos);
		View cardView     =  null;//findViewById(R.id.tableRowWithCard);
        View savingsView  =  null;//findViewById(R.id.tableRowSavings);
        View blueView     =  null;//findViewById(R.id.tableRowBlue);
		View agencyView   =  null;//findViewById(R.id.tableRowExchangeAgency);
		View paypalView   =  null;//findViewById(R.id.tableRowPayPal);
		
       	// if "Discount %" or "Taxes %" is disabled, set its preference value to 0
//       	if (PreferencesManager.getInstance().isShowDiscount())
       		discountView.setVisibility(View.VISIBLE);
//       	else {
       		discountView.setVisibility(View.GONE);
//       		if (PreferencesManager.getInstance().getDiscount() != 0)
//       			PreferencesManager.getInstance().setDiscount(0);
//       	}
       	
/*       	if (PreferencesManager.getInstance().isShowTaxes())
       		taxesView.setVisibility(View.VISIBLE);
       	else {
       		taxesView.setVisibility(View.GONE);
       		if (PreferencesManager.getInstance().getTaxes() != 0)
       			PreferencesManager.getInstance().setTaxes(0);
       	}*/

       	// if "Discount %" and "Taxes%" are both 0, hide "Total"
		/*if (PreferencesManager.getInstance().getDiscount() == 0 && PreferencesManager.getInstance().getTaxes() == 0)
			totalView.setVisibility(View.GONE);
		else
			totalView.setVisibility(View.VISIBLE);
		
		if (PreferencesManager.getInstance().isShowPesos())
			pesosView.setVisibility(View.VISIBLE);
		else
			pesosView.setVisibility(View.GONE);

        if (PreferencesManager.getInstance().isShowSavings())
            savingsView.setVisibility(View.VISIBLE);
        else
            savingsView.setVisibility(View.GONE);

		if (PreferencesManager.getInstance().isShowCreditCard())
			cardView.setVisibility(View.VISIBLE);
		else
			cardView.setVisibility(View.GONE);

        if (PreferencesManager.getInstance().isShowBlue())
            blueView.setVisibility(View.VISIBLE);
        else
            blueView.setVisibility(View.GONE);

		if (PreferencesManager.getInstance().isShowExchangeAgency())
			agencyView.setVisibility(View.VISIBLE);
		else
			agencyView.setVisibility(View.GONE);
		
		if (PreferencesManager.getInstance().isShowPaypal())
			paypalView.setVisibility(View.VISIBLE);
		else
			paypalView.setVisibility(View.GONE);*/
	}
	
	private void setFonts() {
		((TextView)findViewById(R.id.whatTheyChargeYou)).setTypeface(TYPEFACE_ROBOTO_BLACK);
		((TextView)findViewById(R.id.amountTextView)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
		//((TextView)findViewById(R.id.discountTextView)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
		//((TextView)findViewById(R.id.taxesTextView)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
		((TextView)findViewById(R.id.totalTextView)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
		
		((TextView)findViewById(R.id.whatItCostsYou)).setTypeface(TYPEFACE_ROBOTO_BLACK);
		((TextView)findViewById(R.id.textViewInPesos)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
		((TextView)findViewById(R.id.textViewWithCard)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
//        ((TextView)findViewById(R.id.textViewSavings)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
        ((TextView)findViewById(R.id.textViewBlue)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
		((TextView)findViewById(R.id.textViewAgency)).setTypeface(TYPEFACE_ROBOTO_MEDIUM);
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
			switch (item.getItemId()) {
			case R.id.menu_copy:
				if (currentEditTextBeingEdited != null && currentEditTextBeingEdited.getText() != null) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					String copiedText = currentEditTextBeingEdited.getText().toString().trim();
					clipboard.setText(copiedText);
					Utilities.showToast("Valor copiado: " + copiedText);
				} else {
					Utilities.showToast("No se pudo copiar el valor");
				}
				
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.menu_paste:
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				CharSequence clipboardText = clipboard.getText();
				if (clipboardText == null) {
					Utilities.showToast("El contenido del portapapeles no se puede pegar");
				} else {
					String content = clipboardText.toString().trim();
					try {
						Double newNumber = Double.parseDouble(content);
						
						// validar si es un porcentaje
						if (currentEditTextBeingEdited == discountText || currentEditTextBeingEdited == taxesText){
							if ((newNumber > 100 || newNumber < 0) || (newNumber == 100 && currentEditTextBeingEdited == discountText)) {
								AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());

								if (newNumber == 100 && currentEditTextBeingEdited == discountText){
									builder.setMessage(CalculatorFragment.INVALID_DISCOUNT100);
								} else { 
									builder.setMessage(CalculatorFragment.INVALID_PERCENTAGE + " pero el portapapeles tiene el valor: " + newNumber);
								}
								builder.setTitle("Error");
								builder.setNeutralButton("OK", null);
								currentDialog = builder.create();
								currentDialog.show();
								return true;
							}
						}
						
						Intent resultIntent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putDouble(CalculatorFragment.RESULT, newNumber);
						bundle.putString(CalculatorFragment.RESULT_TYPE, currentEditorType.name());
						resultIntent.putExtras(bundle);
						setResult(RESULT_OK, resultIntent);
						onActivityResult(RequestCode.CALCULATOR.ordinal(), RESULT_OK, resultIntent);
						
					} catch (NumberFormatException e) {
						String shortContent = content;
						if (shortContent == null)
							shortContent = "(vacío)";
						else if (shortContent.length() > 20)
							shortContent = shortContent.substring(0, 20) + "...";
						
						Utilities.showToast("El contenido del portapapeles no es un número válido: " + shortContent);
					} finally {
						mode.finish(); // Action picked, so close the CAB
					}
					return true;
				}
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
	
	
		
	public class OnClickListenerShowCalc implements OnClickListener {

		private final EditText editText;
		private final String editTextName;
		private final EditorType editorType;

		public OnClickListenerShowCalc(EditText editText, String editTextName, EditorType editorType) {
			this.editText = editText;
			this.editorType = editorType;
			this.editTextName = editTextName.replace(":", "");
		}

		@Override
		public void onClick(View v) {
			currentEditTextBeingEdited = editText;
			//currentEditTextBeingEdited_Name = editTextName;
			
			if (mActionMode != null)
				mActionMode.finish();
				
			Intent calc = new Intent(MainActivity.this, CalculatorFragment.class);
			calc.putExtra("editTextValue", editText.getText().toString());
			calc.putExtra("editTextName", editTextName);
			calc.putExtra("type", editorType.name());
			startActivityForResult(calc, RequestCode.CALCULATOR.ordinal());
		}
	}
	
	public class OnClickListenerShowNavDrawer implements OnClickListener {
		@Override
		public void onClick(View v) {
			mDrawerLayout.openDrawer(Gravity.LEFT);
//			PreferencesManager.getInstance().setIsNavDrawerNew(false);
		}
	}
	
	public class OnClickListenerDeleteDiscountOrTax implements OnClickListener {
		DiscountOrTax type;
		public OnClickListenerDeleteDiscountOrTax(DiscountOrTax type) {
			this.type = type;
		}

		@Override
		public void onClick(View v) {
			String title;
				
			if (type == DiscountOrTax.DISCOUNT)
				title = "¿Borrar \"Descuento %\" ?";
			else
				title = "¿Borrar \"Recargo %\" ?";
				
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle(title);
			builder.setMessage("Presione OK para poner este valor en 0");
			builder.setNegativeButton("Cancelar", null);
			
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditorType editorType;
					
					if (type == DiscountOrTax.DISCOUNT) {
						currentEditTextBeingEdited = discountText;
						editorType = EditorType.DISCOUNT;
					} else {
						currentEditTextBeingEdited = taxesText;
						editorType = EditorType.TAXES;
					}
						// set the value to 0
					Intent resultIntent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putDouble(CalculatorFragment.RESULT, 0);
					bundle.putString(CalculatorFragment.RESULT_TYPE, editorType.name());
					resultIntent.putExtras(bundle);
					onActivityResult(RequestCode.CALCULATOR.ordinal(), RESULT_OK, resultIntent);
				}
			});
				
			currentDialog = builder.create();
			currentDialog.show();
		}
	}
	
	private void selectItemFromNavDrawer(int position) {

	    // Highlight the selected item
	    mDrawerList.setItemChecked(position, true);
	    ChosenCurrenciesAdapter adapter = (ChosenCurrenciesAdapter) mDrawerList.getAdapter();
	    Currency newCurr = (Currency) mDrawerList.getAdapter().getItem(position);
	    
	    if (newCurr != null) {
	    	adapter.setSelectedItem(newCurr);
//	    	PreferencesManager.getInstance().setCurrentCurrency(newCurr);
			onActivityResult(RequestCode.CHOOSE_CURRENCY.ordinal(), RESULT_OK, null);
		}
	    mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	@Override
	public void onBackPressed() {
	    if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
	    	mDrawerLayout.closeDrawer(Gravity.LEFT);
	    }else{
	        super.onBackPressed();
	    }
	}
	
	/**
	 * Used to refresh NavDrawer list after a currency is deleted
	 */
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		ChosenCurrenciesAdapter adapter = (ChosenCurrenciesAdapter) mDrawerList.getAdapter();
		adapter.updateCurrenciesList();
	}

}
