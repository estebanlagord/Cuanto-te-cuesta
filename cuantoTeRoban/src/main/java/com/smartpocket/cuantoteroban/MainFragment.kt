package com.smartpocket.cuantoteroban

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.TransitionManager
import com.smartpocket.cuantoteroban.MainActivity.FRACTION_DIGITS
import com.smartpocket.cuantoteroban.MainActivity.RequestCode
import com.smartpocket.cuantoteroban.calc.CalculatorFragment
import com.smartpocket.cuantoteroban.chosencurrencies.ChosenCurrenciesListener
import com.smartpocket.cuantoteroban.chosencurrencies.ChosenCurrenciesRecyclerAdapter
import com.smartpocket.cuantoteroban.editortype.EditorType
import com.smartpocket.cuantoteroban.editortype.EditorTypeHelper
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.container_main.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment(), DeleteCurrencyDialogListener, ChosenCurrenciesListener {

    private lateinit var refreshItem: MenuItem
    private lateinit var rotatingRefreshButtonView: ImageView
    private lateinit var refreshButtonRotation: Animation
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mDrawerList: RecyclerView
    private val chosenCurrencyLongClickListener = ChosenCurrencyLongClickListener(this)
    private lateinit var viewModel: MainFragmentVM
    private lateinit var singleActivityVM: SingleActivityVM
    private lateinit var totalViews: List<View>
    private lateinit var pesosViews: List<View>
    private lateinit var withCardViews: List<View>
    private lateinit var blueViews: List<View>
    private lateinit var exchangeAgencyViews: List<View>
    private var currentCurr: Currency? = null
    private val preferences by lazy { PreferencesManager.getInstance() }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.container_main, container, false)
        val toolbar = view.findViewById(R.id.my_awesome_toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        totalViews = listOf(view.totalTextView, view.totalEditText)
        pesosViews = listOf(view.textViewInPesos, view.pesosBill, view.inPesosValue)
        withCardViews = listOf(view.textViewWithCard, view.ivCreditCard, view.withCreditCardValue)
        blueViews = listOf(view.textViewBlue, view.ivDolarBlue, view.withBlueValue)
        exchangeAgencyViews = listOf(view.textViewAgency, view.exchangeAgencyValue)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        currentCurr = null
        mSwipeRefreshLayout = activity_main_swipe_refresh_layout
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_primary_dark)
        mSwipeRefreshLayout.setOnRefreshListener { viewModel.refreshRates(true) }

        viewModel = ViewModelProvider(this)[MainFragmentVM::class.java]
        singleActivityVM = ViewModelProvider(requireActivity())[SingleActivityVM::class.java]

        setupNavDrawer()
        setupClickListeners()
        setupViewModelObservers()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
        discountTIL.visibility = if (preferences.isShowDiscount) View.VISIBLE else View.GONE
        val pesosVisibility = if (preferences.isShowPesos) View.VISIBLE else View.GONE
        pesosViews.forEach { it.visibility = pesosVisibility }

        val creditCardVisibility = if (preferences.isShowCreditCard) View.VISIBLE else View.GONE
        withCardViews.forEach { it.visibility = creditCardVisibility }

        val exchangeAgencyVisibility = if (preferences.isShowExchangeAgency) View.VISIBLE else View.GONE
        exchangeAgencyViews.forEach { it.visibility = exchangeAgencyVisibility }
        updateBlueVisibility(currentCurr)
    }

    private fun setupViewModelObservers() {
        with(viewModel) {
            isLoadingLiveData.observe(viewLifecycleOwner, Observer {
                setLoadingState(it)
            })
            currencyLiveData.observe(viewLifecycleOwner, Observer {
                if (currentCurr != it) {
                    updateFlag(it, true)
                    updateBlueVisibility(it)
                    currentCurr = it
                }
            })
            amountLiveData.observe(viewLifecycleOwner, Observer {
                showValue(it, amountEditText)
            })
            discountLiveData.observe(viewLifecycleOwner, Observer {
                showPercentage(it, discountEditText)
                updateTotalVisibility(it, taxesLiveData.value)
                discountTIL.isEndIconVisible = it != 0.0
            })
            taxesLiveData.observe(viewLifecycleOwner, Observer {
                showPercentage(it, taxesEditText)
                updateTotalVisibility(discountLiveData.value, it)
                taxesTIL.isEndIconVisible = it != 0.0
            })
            totalLiveData.observe(viewLifecycleOwner, Observer {
                showValue(it, totalEditText)
            })
            pesosLiveData.observe(viewLifecycleOwner, Observer {
                showValue(it, inPesosValue)
            })
            creditCardLiveData.observe(viewLifecycleOwner, Observer {
                showValue(it, withCreditCardValue)
            })
            blueLiveData.observe(viewLifecycleOwner, Observer {
                showValue(it, withBlueValue)
            })
            exchangeAgencyLiveData.observe(viewLifecycleOwner, Observer {
                showValue(it, exchangeAgencyValue)
            })
            currencyEditorTypeLiveData.observe(viewLifecycleOwner, Observer {
                showCurrentEditor(it)
            })
            lastUpdateLiveData.observe(viewLifecycleOwner, Observer {
                showLastUpdate(it)
            })
            errorLiveData.observe(viewLifecycleOwner, Observer {
                showErrorMsg(it)
            })
        }

        singleActivityVM.calculatorResultLD.observe(viewLifecycleOwner, Observer {
            viewModel.onCalculatorValueChanged(it.editorType, Utilities.round(it.amount, FRACTION_DIGITS))
        })
        singleActivityVM.addedCurrencyLD.observe(viewLifecycleOwner, Observer {
            viewModel.chosenCurrenciesAdapter?.updateCurrenciesList()
        })
    }

    private fun updateTotalVisibility(discount: Double?, taxes: Double?) {
        val totalVisibility = if (discount == 0.0 && taxes == 0.0) View.GONE else View.VISIBLE
        totalViews.forEach { it.visibility = totalVisibility }
    }

    private fun showErrorMsg(errorState: MainFragmentVM.ErrorState) {
        val msgRes = when (errorState) {
            MainFragmentVM.ErrorState.NO_INTERNET -> R.string.error_no_internet
            MainFragmentVM.ErrorState.DOWNLOAD_ERROR -> R.string.error_downloading
        }
        singleActivityVM.snackbarLD.value = getString(msgRes)
    }

    private fun showLastUpdate(date: Date) {
        textLastUpdateValue.text =
                if (date.time > 0) displayDateFormat.format(date)
                else getString(R.string.LastUpdateNever)
    }

    private fun updateBlueVisibility(curr: Currency?) {
        val blueVisibility = if (shouldShowBlue(curr)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        blueViews.forEach { it.visibility = blueVisibility }
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
//            EditorType.PAYPAL -> highlightOnly(payPalValue)
            EditorType.DISCOUNT -> highlightOnly(discountEditText)
            EditorType.TAXES -> highlightOnly(taxesEditText)
            EditorType.TOTAL -> highlightOnly(totalEditText)
//            EditorType.SAVINGS -> highlightOnly(withSavingsValue)
            EditorType.BLUE -> highlightOnly(withBlueValue)
        }
    }

    private fun highlightOnly(editText: TextView) {
        listOf<TextView>(amountEditText, discountEditText, taxesEditText, totalEditText, inPesosValue,
                withCreditCardValue, withBlueValue)
                .forEach {
                    it.setTypeface(null, if (it == editText) Typeface.BOLD else Typeface.NORMAL)
                }
    }

    private fun showValue(value: Double, textView: TextView) {
        textView.text = shortNumberFormat.format(value)
    }

    private fun showPercentage(value: Double, textView: TextView) {
        textView.text = if (value == 0.0)
            null
        else
            percentageNumberFormat.format(value)
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
        exchangeAgencyValue.setOnClickListener(OnClickListenerShowCalc(exchangeAgencyValue, resources.getString(R.string.ExchangeAgency), EditorType.EXCHANGE_AGENCY))
        countryFlag.setOnClickListener { mDrawerLayout.openDrawer(GravityCompat.START) }

        discountTIL.setEndIconOnClickListener {
            viewModel.onDeleteDiscount()
            TransitionManager.beginDelayedTransition(scrollView1)
        }
        taxesTIL.setEndIconOnClickListener {
            viewModel.onDeleteTaxes()
            TransitionManager.beginDelayedTransition(scrollView1)
        }
    }

    inner class OnClickListenerShowCalc(private val editText: EditText, editTextName: String, private val editorType: EditorType) : View.OnClickListener {
        private val editTextName: String = editTextName.replace(":", "")
        override fun onClick(v: View) {
//            currentEditTextBeingEdited = editText
            //currentEditTextBeingEdited_Name = editTextName;
//            if (mActionMode != null) mActionMode.finish()

            val action = MainFragmentDirections
                    .actionMainFragmentToCalculator(editText.text.toString(), editTextName, editorType)
            findNavController().navigate(action)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.CALCULATOR.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    val newValue = data!!.extras!!.getDouble(CalculatorFragment.RESULT)
                    val editorTypeName = data.extras!!.getString(CalculatorFragment.RESULT_TYPE)
                    val editorType = EditorTypeHelper.getEditorType(editorTypeName)
                    viewModel.onCalculatorValueChanged(editorType,
                            Utilities.round(newValue, FRACTION_DIGITS))
                }
            }
            RequestCode.CHOOSE_CURRENCY.ordinal,
            RequestCode.SETTINGS.ordinal -> viewModel.onSettingsChanged()
        }
    }

    private fun setupNavDrawer() {
        val actionBar = (activity as AppCompatActivity).supportActionBar as ActionBar
        mDrawerLayout = drawer_layout
        mDrawerToggle = object : ActionBarDrawerToggle(
                requireActivity(),  /* host Activity */
                mDrawerLayout,  /* DrawerLayout object */
                R.string.menu_change,  /* "open drawer" description */
                R.string.menu_change /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                actionBar.setTitle(R.string.app_name)
//                actionBar.setDisplayShowTitleEnabled(false);
                requireActivity().invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                actionBar.setTitle(R.string.title_activity_choose_currency)
//                actionBar.setDisplayShowTitleEnabled(true);
//                updateRefreshProgress()
                requireActivity().invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
//                if (mActionMode != null) mActionMode.finish()
            }
        }
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        mDrawerList = left_drawer
        // Set the adapter for the list view
        mDrawerList.setHasFixedSize(true)

        if (viewModel.chosenCurrenciesAdapter == null) {
            viewModel.chosenCurrenciesAdapter = ChosenCurrenciesRecyclerAdapter(this)
        } else {
            viewModel.chosenCurrenciesAdapter?.updateListener(this)
        }
        mDrawerList.adapter = viewModel.chosenCurrenciesAdapter
        mDrawerToggle.syncState()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    override fun onChosenCurrencyClick(currency: Currency) {
        selectItemFromNavDrawer(currency)
    }

    override fun onChosenCurrencyLongClick(currency: Currency) {
        chosenCurrencyLongClickListener.onItemLongClick(currency)
    }


    private fun selectItemFromNavDrawer(newCurr: Currency) { // Highlight the selected item
        val adapter = mDrawerList.adapter as ChosenCurrenciesRecyclerAdapter
        adapter.selectedItem = newCurr
        preferences.currentCurrency = newCurr
        onActivityResult(RequestCode.CHOOSE_CURRENCY.ordinal, Activity.RESULT_OK, null)
        mDrawerLayout.closeDrawer(mDrawerList)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

/*    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }*/

    private fun updateFlag(currency: Currency, fadeFlag: Boolean) {
        val countryFlagView = countryFlag
        val newFlagIdentifier = currency.flagIdentifier
        if (fadeFlag) {
            val fadeInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.flag_transition_in)
            val fadeOutAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.flag_transition_out)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.activity_main, menu)
//        refreshItem = menu.findItem(R.id.menu_update)
//        val inflater2 = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        rotatingRefreshButtonView = inflater2.inflate(R.layout.refresh_action_view, null) as ImageView
//        refreshButtonRotation = AnimationUtils.loadAnimation(requireContext(), R.anim.clockwise_refresh)
        // this is necessary because the update begins before onCreateOptionsMenu is called
//        updateRefreshProgress()
//        return true
    }

    // gets the content to share with other apps
    private fun getUpdatedShareIntent(): Intent {
        val currentCurrency = preferences.currentCurrency
        val showDiscount = preferences.isShowDiscount && viewModel.discountLiveData.value != 0.0
        val showTaxes = preferences.isShowDiscount && viewModel.taxesLiveData.value != 0.0
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

    override fun onPrepareOptionsMenu(menu: Menu) { // show/hide buttons depending on whether the nav drawer is open
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
            R.id.menu_update -> viewModel.refreshRates(true)
            R.id.menu_add_currency -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToAddCurrency())
            R.id.menu_settings -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToPreferencesFragment())
            R.id.menu_help -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToHelpActivity())
            R.id.menu_about -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToAbout())
            R.id.menu_share -> startActivity(getUpdatedShareIntent())
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Used to refresh NavDrawer list after a currency is deleted
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val adapter = mDrawerList.adapter as ChosenCurrenciesRecyclerAdapter
        adapter.updateCurrenciesList()
    }

}
