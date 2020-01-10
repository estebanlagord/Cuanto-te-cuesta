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

class MainActivity2 : AppCompatActivity() {

    private lateinit var refreshItem: MenuItem
    private lateinit var rotatingRefreshButtonView: ImageView
    private lateinit var refreshButtonRotation: Animation
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mDrawerList: ListView
    private lateinit var viewModel: MainActivityVM
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

        mSwipeRefreshLayout = activity_main_swipe_refresh_layout
        mSwipeRefreshLayout.setColorSchemeResources(R.color.my_app_green)
        mSwipeRefreshLayout.setOnRefreshListener { viewModel.onForceRefresh() }

        setupViewModel()
        setupClickListeners()
        setupNavDrawer()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainActivityVM::class.java)
        with(viewModel) {
            isLoadingLiveData.observe(this@MainActivity2, Observer {
                setLoadingState(it)
            })
            currencyLiveData.observe(this@MainActivity2, Observer {
                updateFlag(it, true)
                updateBlueVisibility(it)
            })
            amountLiveData.observe(this@MainActivity2, Observer {
                showValue(it, amountEditText)
            })
            discountLiveData.observe(this@MainActivity2, Observer {
                showPercentage(it, discountEditText)
            })
            taxesLiveData.observe(this@MainActivity2, Observer {
                showPercentage(it, taxesEditText)
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
            currencyEditorTypeLiveData.observe(this@MainActivity2, Observer {
                showCurrentEditor(it)
            })
            lastUpdateLiveData.observe(this@MainActivity2, Observer {
                showLastUpdate(it)
            })
        }
    }

    private fun showLastUpdate(date: Date) {
        textLastUpdateValue.text =
                if (date.time > 0) displayDateFormat.format(date)
                else getString(R.string.LastUpdateNever)
    }

    private fun updateBlueVisibility(curr: Currency) {
        tableRowBlue.visibility = if (curr.code == CurrencyManager.USD) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

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
        withSavingsValue.setOnClickListener(OnClickListenerShowCalc(withSavingsValue, resources.getString(R.string.WithSavings), EditorType.SAVINGS))
        withBlueValue.setOnClickListener(OnClickListenerShowCalc(withBlueValue, resources.getString(R.string.WithBlue), EditorType.BLUE))
        deleteDiscount.setOnClickListener { viewModel.onDeleteDiscount() }
        deleteTaxes.setOnClickListener { viewModel.onDeleteTaxes() }
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
                    viewModel.onCalculatorValueChanged(editorType, newValue)
                }
            }
            RequestCode.CHOOSE_CURRENCY.ordinal,
            RequestCode.SETTINGS.ordinal -> {
                viewModel.onSettingsChanged()
            }
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
        // Open nav drawer unless it has been opened manually
        if (PreferencesManager.getInstance().isNavDrawerANewFeature)
            mDrawerLayout.openDrawer(GravityCompat.START)

        mDrawerList.onItemLongClickListener = ChosenCurrencyLongClickListener(this)
        mDrawerList.onItemClickListener = OnItemClickListener { _, _, position, _ -> selectItemFromNavDrawer(position) }
    }

    private fun selectItemFromNavDrawer(position: Int) { // Highlight the selected item
        mDrawerList.setItemChecked(position, true)
        val adapter = mDrawerList.adapter as ChosenCurrenciesAdapter
        val newCurr = mDrawerList.adapter.getItem(position) as Currency
        adapter.selectedItem = newCurr
        PreferencesManager.getInstance().currentCurrency = newCurr
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
        val countryFlagView = findViewById<ImageView>(R.id.countryFlag)
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
        currencyName.text = "en " + currency.name
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
        // testing line
        // Set up ShareActionProvider's default share intent
/*        val shareItem = menu.findItem(R.id.menu_share)
        mShareActionProvider = MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider
        mShareActionProvider.setShareIntent(getUpdatedShareIntent())
        mShareActionProvider.setOnShareTargetSelectedListener(ShareActionProvider.OnShareTargetSelectedListener { arg0, shareIntent ->
            try { // copiar el texto al portapapeles, para poder pegarlo por ejemplo en Facebook
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                //Intent shareIntent = getUpdatedShareIntent();
                val shareContent = shareIntent.extras!![Intent.EXTRA_TEXT].toString()
                clipboard.text = shareContent
                Utilities.showToast("Conversión copiada al portapapeles.\nLa podes pegar en cualquier aplicación.")
            } catch (e: Exception) {
            }
            false
        })*/
        return true
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
        if (item.itemId == android.R.id.home && !mDrawerLayout.isDrawerOpen(mDrawerList)) PreferencesManager.getInstance().setIsNavDrawerNew(false)
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.menu_settings -> {
                //Intent settingsIntent = new Intent(this, PreferencesScreen.class);
                val settingsIntent = Intent(this, PreferencesActivity::class.java)
                startActivityForResult(settingsIntent, RequestCode.SETTINGS.ordinal)
            }
            R.id.menu_update -> {
//                updateExchangeRate(true)
//                recalculateConversionRates()
            }
            R.id.menu_add_currency -> {
                val intent = Intent(this, AddCurrency::class.java)
                startActivityForResult(intent, RequestCode.ADD_CURRENCY.ordinal)
            }
            R.id.menu_help -> {
                val helpIntent = Intent(this, HelpActivity::class.java)
                startActivity(helpIntent)
            }
            R.id.menu_about -> {
                val aboutIntent = Intent(this, About::class.java)
                startActivity(aboutIntent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
