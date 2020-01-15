package com.smartpocket.cuantoteroban

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.smartpocket.cuantoteroban.MainActivity.FRACTION_DIGITS
import com.smartpocket.cuantoteroban.MainActivity.RequestCode
import com.smartpocket.cuantoteroban.calc.Calculator
import com.smartpocket.cuantoteroban.editortype.EditorType
import com.smartpocket.cuantoteroban.editortype.EditorTypeHelper
import com.smartpocket.cuantoteroban.preferences.PreferencesActivity
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.container_main.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity2 : AppCompatActivity(), DeleteCurrencyDialogListener {

    private lateinit var refreshItem: MenuItem
    private lateinit var rotatingRefreshButtonView: ImageView
    private lateinit var refreshButtonRotation: Animation
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mDrawerList: ListView
    private lateinit var viewModel: MainActivityVM
    private lateinit var adViewHelper: AdViewHelper
    private val preferences by lazy { PreferencesManager.getInstance() }

    private var currentCurr: Currency? = null
    private val displayDateFormat = SimpleDateFormat("dd/MMM HH:mm", Locale("es", "AR"))
    private val shortNumberFormat = (DecimalFormat.getInstance() as DecimalFormat).apply {
        minimumFractionDigits = FRACTION_DIGITS
        maximumFractionDigits = FRACTION_DIGITS
        positivePrefix = "$ "
    }
    private val percentageNumberFormat = (DecimalFormat.getInstance() as DecimalFormat).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = FRACTION_DIGITS
        positiveSuffix = " %"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_main)
        setSupportActionBar(my_awesome_toolbar)

        currentCurr = null
        mSwipeRefreshLayout = activity_main_swipe_refresh_layout
        mSwipeRefreshLayout.setColorSchemeResources(R.color.my_app_green)
        mSwipeRefreshLayout.setOnRefreshListener { viewModel.refreshRates(true) }

        setupViewModel()
        setupClickListeners()
        setupNavDrawer()

        adViewHelper = AdViewHelper(adViewContainer, this)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
        tableRowDiscount.visibility = if (preferences.isShowDiscount) View.VISIBLE else View.GONE
        tableRowTaxes.visibility = if (preferences.isShowTaxes) View.VISIBLE else View.GONE
        tableRowPesos.visibility = if (preferences.isShowPesos) View.VISIBLE else View.GONE
        tableRowWithCard.visibility = if (preferences.isShowCreditCard) View.VISIBLE else View.GONE
        tableRowExchangeAgency.visibility = if (preferences.isShowExchangeAgency) View.VISIBLE else View.GONE
        updateBlueVisibility(currentCurr)
    }

    override fun onResume() {
        super.onResume()
        if (::adViewHelper.isInitialized) adViewHelper.resume()
    }

    override fun onPause() {
        if (::adViewHelper.isInitialized) adViewHelper.pause()
        super.onPause()
    }

    override fun onDestroy() {
        if (::adViewHelper.isInitialized) adViewHelper.destroy()
        super.onDestroy()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainActivityVM::class.java)
        with(viewModel) {
            isLoadingLiveData.observe(this@MainActivity2, Observer {
                setLoadingState(it)
            })
            currencyLiveData.observe(this@MainActivity2, Observer {
                if (currentCurr != it) {
                    updateFlag(it, true)
                    updateBlueVisibility(it)
                    currentCurr = it
                }
            })
            amountLiveData.observe(this@MainActivity2, Observer {
                showValue(it, amountEditText)
            })
            discountLiveData.observe(this@MainActivity2, Observer {
                showPercentage(it, discountEditText)
                updateTotalVisibility(it, taxesLiveData.value)
            })
            taxesLiveData.observe(this@MainActivity2, Observer {
                showPercentage(it, taxesEditText)
                updateTotalVisibility(discountLiveData.value, it)
            })
            totalLiveData.observe(this@MainActivity2, Observer {
                showValue(it, totalEditText)
            })
            pesosLiveData.observe(this@MainActivity2, Observer {
                showValue(it, inPesosValue)
            })
            creditCardLiveData.observe(this@MainActivity2, Observer {
                showValue(it, withCreditCardValue)
            })
            blueLiveData.observe(this@MainActivity2, Observer {
                showValue(it, withBlueValue)
            })
            exchangeAgencyLiveData.observe(this@MainActivity2, Observer {
                showValue(it, exchangeAgencyValue)
            })
            currencyEditorTypeLiveData.observe(this@MainActivity2, Observer {
                showCurrentEditor(it)
            })
            lastUpdateLiveData.observe(this@MainActivity2, Observer {
                showLastUpdate(it)
            })
            errorLiveData.observe(this@MainActivity2, Observer {
                showErrorMsg(it)
            })
        }
    }

    private fun updateTotalVisibility(discount: Double?, taxes: Double?) {
        tableRowTotal.visibility = if (discount == 0.0 && taxes == 0.0) View.GONE else View.VISIBLE
    }

    private fun showErrorMsg(errorState: MainActivityVM.ErrorState) {
        val msgRes = when (errorState) {
            MainActivityVM.ErrorState.NO_INTERNET -> R.string.error_no_internet
            MainActivityVM.ErrorState.DOWNLOAD_ERROR -> R.string.error_downloading
        }
        Utilities.showToast(getString(msgRes))
    }

    private fun showLastUpdate(date: Date) {
        textLastUpdateValue.text =
                if (date.time > 0) displayDateFormat.format(date)
                else getString(R.string.LastUpdateNever)
    }

    private fun updateBlueVisibility(curr: Currency?) {
        tableRowBlue.visibility = if (shouldShowBlue(curr)) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun shouldShowBlue(curr: Currency?) = preferences.isShowBlue && (curr?.code == CurrencyManager.USD)

    private fun setLoadingState(isLoading: Boolean) {
        mSwipeRefreshLayout.isRefreshing = isLoading
/*        if (isLoading) {
            rotatingRefreshButtonView.startAnimation(refreshButtonRotation)
            refreshItem.setActionView(rotatingRefreshButtonView)
        } else {
            if (refreshItem.actionView != null) {
                refreshItem.actionView.clearAnimation()
                refreshItem.actionView = null
            }
        }*/
    }

    private fun showCurrentEditor(editorType: EditorType) {
        when (editorType) {
            EditorType.AMOUNT -> highlightOnly(amountEditText)
            EditorType.PESOS -> highlightOnly(inPesosValue)
            EditorType.CREDIT_CARD -> highlightOnly(withCreditCardValue)
            EditorType.EXCHANGE_AGENCY -> highlightOnly(exchangeAgencyValue)
            EditorType.PAYPAL -> highlightOnly(payPalValue)
            EditorType.DISCOUNT -> highlightOnly(discountEditText)
            EditorType.TAXES -> highlightOnly(taxesEditText)
            EditorType.TOTAL -> highlightOnly(totalEditText)
            EditorType.SAVINGS -> highlightOnly(withSavingsValue)
            EditorType.BLUE -> highlightOnly(withBlueValue)
        }
    }

    private fun highlightOnly(editText: TextView) {
        listOf<TextView>(amountEditText, discountEditText, taxesEditText, totalEditText, inPesosValue,
                withCreditCardValue, withSavingsValue, withBlueValue)
                .forEach {
                    it.setTypeface(null, if (it == editText) Typeface.BOLD else Typeface.NORMAL)
                }
    }

    private fun showValue(value: Double, textView: TextView) {
        textView.text = shortNumberFormat.format(value)
    }

    private fun showPercentage(value: Double, textView: TextView) {
        textView.text = percentageNumberFormat.format(value)
    }

    private fun setupClickListeners() {
        amountEditText.setOnClickListener(OnClickListenerShowCalc(amountEditText, resources.getString(R.string.Ammount), EditorType.AMOUNT))
        discountEditText.setOnClickListener(OnClickListenerShowCalc(discountEditText, resources.getString(R.string.Discount), EditorType.DISCOUNT))
        taxesEditText.setOnClickListener(OnClickListenerShowCalc(taxesEditText, resources.getString(R.string.Taxes), EditorType.TAXES))
        totalEditText.setOnClickListener(OnClickListenerShowCalc(totalEditText, resources.getString(R.string.Total), EditorType.TOTAL))
        inPesosValue.setOnClickListener(OnClickListenerShowCalc(inPesosValue, resources.getString(R.string.InPesos), EditorType.PESOS))
        withCreditCardValue.setOnClickListener(OnClickListenerShowCalc(withCreditCardValue, resources.getString(R.string.WithCreditCard), EditorType.CREDIT_CARD))
//        withSavingsValue.setOnClickListener(OnClickListenerShowCalc(withSavingsValue, resources.getString(R.string.WithSavings), EditorType.SAVINGS))
        withBlueValue.setOnClickListener(OnClickListenerShowCalc(withBlueValue, resources.getString(R.string.WithBlue), EditorType.BLUE))
        deleteDiscount.setOnClickListener { viewModel.onDeleteDiscount() }
        deleteTaxes.setOnClickListener { viewModel.onDeleteTaxes() }
        countryFlag.setOnClickListener { mDrawerLayout.openDrawer(GravityCompat.START) }
    }

    inner class OnClickListenerShowCalc(private val editText: EditText, editTextName: String, private val editorType: EditorType) : View.OnClickListener {
        private val editTextName: String = editTextName.replace(":", "")
        override fun onClick(v: View) {
//            currentEditTextBeingEdited = editText
            //currentEditTextBeingEdited_Name = editTextName;
//            if (mActionMode != null) mActionMode.finish()
            val calc = Intent(this@MainActivity2, Calculator::class.java)
            calc.putExtra("editTextValue", editText.text.toString())
            calc.putExtra("editTextName", editTextName)
            calc.putExtra("type", editorType.name)
            startActivityForResult(calc, RequestCode.CALCULATOR.ordinal)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.CALCULATOR.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    val newValue = data!!.extras!!.getDouble(Calculator.RESULT)
                    val editorTypeName = data.extras!!.getString(Calculator.RESULT_TYPE)
                    val editorType = EditorTypeHelper.getEditorType(editorTypeName)
                    viewModel.onCalculatorValueChanged(editorType,
                            Utilities.round(newValue, FRACTION_DIGITS))
                }
            }
            RequestCode.CHOOSE_CURRENCY.ordinal,
            RequestCode.SETTINGS.ordinal -> viewModel.onSettingsChanged()

            RequestCode.ADD_CURRENCY.ordinal -> (mDrawerList.adapter as ChosenCurrenciesAdapter).updateCurrenciesList()
        }
    }

    private fun setupNavDrawer() {
        val actionBar = supportActionBar as ActionBar
        mDrawerLayout = drawer_layout
        mDrawerToggle = object : ActionBarDrawerToggle(
                this,  /* host Activity */
                mDrawerLayout,  /* DrawerLayout object */
                R.string.menu_change,  /* "open drawer" description */
                R.string.menu_change /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                actionBar.setTitle(R.string.app_name)
                //actionBar.setDisplayShowTitleEnabled(false);
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                actionBar.setTitle(R.string.title_activity_choose_currency)
                //actionBar.setDisplayShowTitleEnabled(true);
//                updateRefreshProgress()
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
//                if (mActionMode != null) mActionMode.finish()
            }
        }
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        mDrawerList = left_drawer
        // Set the adapter for the list view
        mDrawerList.adapter = ChosenCurrenciesAdapter(this)
        mDrawerList.onItemLongClickListener = ChosenCurrencyLongClickListener(this)
        mDrawerList.onItemClickListener = OnItemClickListener { _, _, position, _ -> selectItemFromNavDrawer(position) }
    }

    private fun selectItemFromNavDrawer(position: Int) { // Highlight the selected item
        mDrawerList.setItemChecked(position, true)
        val adapter = mDrawerList.adapter as ChosenCurrenciesAdapter
        val newCurr = mDrawerList.adapter.getItem(position) as Currency
        adapter.selectedItem = newCurr
        preferences.currentCurrency = newCurr
        onActivityResult(RequestCode.CHOOSE_CURRENCY.ordinal, Activity.RESULT_OK, null)
        mDrawerLayout.closeDrawer(mDrawerList)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun updateFlag(currency: Currency, fadeFlag: Boolean) {
        val countryFlagView = countryFlag
        val newFlagIdentifier = currency.flagIdentifier
        if (fadeFlag) {
            val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.flag_transition_in)
            val fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.flag_transition_out)
            fadeOutAnim.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    countryFlagView.setImageResource(newFlagIdentifier)
                    countryFlagView.startAnimation(fadeInAnim)
                }
            })
            countryFlagView.startAnimation(fadeOutAnim)
        } else { // change the flag without showing the fade animation
            countryFlagView.setImageResource(newFlagIdentifier)
        }
        currencyName.text = getString(R.string.what_they_charge_you_in, currency.name)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.activity_main, menu)
        refreshItem = menu.findItem(R.id.menu_update)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rotatingRefreshButtonView = inflater.inflate(R.layout.refresh_action_view, null) as ImageView
        refreshButtonRotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh)
        // this is necessary because the update begins before onCreateOptionsMenu is called
//        updateRefreshProgress()
        return true
    }

    // gets the content to share with other apps
    private fun getUpdatedShareIntent(): Intent {
        val currentCurrency = preferences.currentCurrency
        val showDiscount = preferences.isShowDiscount && viewModel.discountLiveData.value != 0.0
        val showTaxes = preferences.isShowTaxes && viewModel.taxesLiveData.value != 0.0
        val showTotal = showDiscount || showTaxes
        val showPesos = preferences.isShowPesos
        val showCreditCard = preferences.isShowCreditCard
        val showBlue = shouldShowBlue(currentCurrency)
        val showAgency = preferences.isShowExchangeAgency && viewModel.exchangeAgencyLiveData.value != 0.0

        val sharedText = StringBuilder("Lo que te cobran en " + currentCurrency.name + ":").apply {
            append("\nMonto: " + amountEditText.text)
            if (showDiscount) append("\nDescuento: " + discountEditText.text)
            if (showTaxes) append("\nRecargo: " + taxesEditText.text)
            if (showTotal) append("\nTotal: " + totalEditText.text)
            append("\n\nLo que te cuesta en Pesos argentinos:")
            if (showPesos) append("\nOficial: " + inPesosValue.text)
            if (showCreditCard) append("\nTarjeta: " + withCreditCardValue.text)
            if (showBlue) append("\nBlue: " + withBlueValue.text)
            if (showAgency) append("\nCasa de Cambio: " + exchangeAgencyValue.text)
            append("\n\nCalculado por la aplicación ¿Cuanto Te Cuesta? para Android." +
                    "\nBajala gratis desde: http://bit.ly/Cuanto-Te-Cuesta")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_label))
            putExtra(Intent.EXTRA_TEXT, sharedText.toString())
        }
        return Intent.createChooser(sendIntent, getString(R.string.share_screen_title))
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean { // show/hide buttons depending on whether the nav drawer is open
        val disabledWhenNavDrawerIsOpen = intArrayOf(R.id.menu_share, R.id.menu_about, R.id.menu_help, R.id.menu_settings, R.id.menu_share, R.id.menu_update)
        val drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList)
        if (drawerOpen) {
            menu.findItem(R.id.menu_add_currency).isVisible = true
            for (i in disabledWhenNavDrawerIsOpen) menu.findItem(i).isVisible = false
        } else {
            menu.findItem(R.id.menu_add_currency).isVisible = false
            for (i in disabledWhenNavDrawerIsOpen) menu.findItem(i).isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // If the user pressed the app icon, and if the drawer is closed, change the preference
        // to avoid opening automatically the nav drawer on next launch
        if (item.itemId == android.R.id.home && !mDrawerLayout.isDrawerOpen(mDrawerList)) preferences.setIsNavDrawerNew(false)
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.menu_settings -> {
                startActivityForResult(Intent(this, PreferencesActivity::class.java),
                        RequestCode.SETTINGS.ordinal)
            }
            R.id.menu_add_currency -> {
                startActivityForResult(Intent(this, AddCurrency::class.java),
                        RequestCode.ADD_CURRENCY.ordinal)
            }
            R.id.menu_update -> viewModel.refreshRates(true)
            R.id.menu_help -> startActivity(Intent(this, HelpActivity::class.java))
            R.id.menu_about -> startActivity(Intent(this, About::class.java))
            R.id.menu_share -> startActivity(getUpdatedShareIntent())
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Used to refresh NavDrawer list after a currency is deleted
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val adapter = mDrawerList.adapter as ChosenCurrenciesAdapter
        adapter.updateCurrenciesList()
    }

}
