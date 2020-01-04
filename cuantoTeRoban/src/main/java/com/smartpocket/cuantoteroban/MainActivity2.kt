package com.smartpocket.cuantoteroban

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
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
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.container_main.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.DecimalFormat

class MainActivity2 : AppCompatActivity() {

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mDrawerList: ListView
    private lateinit var viewModel: MainActivityVM
    private val shortNumberFormat = (DecimalFormat.getInstance() as DecimalFormat).apply {
        minimumFractionDigits = FRACTION_DIGITS
        maximumFractionDigits = FRACTION_DIGITS
        positivePrefix = "$ "
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_main)

        setSupportActionBar(my_awesome_toolbar)

        mSwipeRefreshLayout = activity_main_swipe_refresh_layout
        mSwipeRefreshLayout.setColorSchemeResources(R.color.my_app_green)

        setupViewModel()
        setupClickListeners()
        setupNavDrawer()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainActivityVM::class.java)
        viewModel.currencyLiveData.observe(this, Observer {
            updateFlag(it, true)
        })
        viewModel.amountLiveData.observe(this, Observer {
            showValue(it, amountEditText)
        })
        viewModel.officialLiveData.observe(this, Observer {
            it
            showValue(it, inPesosValue)
        })
        viewModel.cardLiveData.observe(this, Observer {
            showValue(it, withCreditCardValue)
        })
        viewModel.blueLiveData.observe(this, Observer {
            showValue(it, withBlueValue)
        })
    }

    private fun showValue(value: Double, textView: TextView) {
        textView.text = shortNumberFormat.format(value)
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
//                updateEditTextBackgrounds(targetEditText)
                }
            }
            RequestCode.CHOOSE_CURRENCY.ordinal,
            RequestCode.SETTINGS.ordinal -> {
                viewModel.onSettingsChanged()
            }
        }
    }

    private fun setupNavDrawer() {
        val actionBar = supportActionBar
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
                actionBar?.setTitle(R.string.app_name)
                //actionBar.setDisplayShowTitleEnabled(false);
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                actionBar?.setTitle(R.string.title_activity_choose_currency)
                //actionBar.setDisplayShowTitleEnabled(true);
//                updateRefreshProgress()
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
//                if (mActionMode != null) mActionMode.finish()
            }
        }
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
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
}
