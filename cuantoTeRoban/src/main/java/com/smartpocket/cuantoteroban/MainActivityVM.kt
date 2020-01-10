package com.smartpocket.cuantoteroban

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smartpocket.cuantoteroban.editortype.EditorType
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import com.smartpocket.cuantoteroban.repository.CurrencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import java.util.logging.Logger

class MainActivityVM : ViewModel() {

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)
    private val logger = Logger.getLogger(javaClass.simpleName)
    private val repository by lazy { CurrencyRepository() }
    val preferences by lazy { PreferencesManager.getInstance() }
    val currencyManager = CurrencyManager.getInstance()

    private var bankExchangeRate = 0.0
    private var invertBankExchangeRate = false
    private var bankExchangeRatePercentage = 0.0
    private var agencyExchangeRate = 0.0
    private var invertAgencyExchangeRate = false
    private var savingsPercentage = 0.0
    private var afipPercentage = 0.0
    private var payPalPercentage = 0.0
    private var discount = 0.0
    private var taxes = 0.0

    val amountLiveData = MutableLiveData<Double>(0.0)
    val discountLiveData = MutableLiveData<Double>(0.0)
    val taxesLiveData = MutableLiveData<Double>(0.0)
    val totalLiveData = MutableLiveData<Double>(0.0)
    val pesosLiveData = MutableLiveData<Double>(0.0)
    val creditCardLiveData = MutableLiveData<Double>(0.0)
    val blueLiveData = MutableLiveData<Double>(0.0)
    val exchangeAgencyLiveData = MutableLiveData<Double>(0.0)

    val currencyLiveData = MutableLiveData<Currency>(preferences.currentCurrency)
    val currencyEditorTypeLiveData = MutableLiveData<EditorType>()
    val isLoadingLiveData = MutableLiveData<Boolean>(false)
    val lastUpdateLiveData = MutableLiveData<Date>(Date(0))

    init {
        onSettingsChanged()
    }

    fun onAmountValueChanged(amount: Double) {
//        coroutineScope.launch {
        val total = amountToTotal(amount)
        amountLiveData.value = amount
        updateTotal(total)
        updatePesos(total)
        updateCreditCard(total)
        updateAgency(total)
        updateBlue(total)

/*            val currency = preferences.currentCurrency
            amountLiveData.value = amount
            val currencyResult = repository.getCurrencyExchange(currency, CurrencyManager.ARS, amount)
            pesosLiveData.value = currencyResult.official * amount
            creditCardLiveData.value = currencyResult.official * amount * 1.3
            if (currencyResult is DolarResult)
                blueLiveData.value = currencyResult.blue * amount*/
//        }
    }

    private fun updateAmount(total: Double) {
        amountLiveData.value = totalToAmount(total)
    }

    private fun updateDiscount() {
        discountLiveData.value = discount
    }

    private fun updateTaxes() {
        taxesLiveData.value = taxes
    }

    private fun updateTotal(total: Double) {
        totalLiveData.value = total
    }

    private fun updatePesos(total: Double) {
        pesosLiveData.value = totalToPesos(total)
    }

    private fun updateCreditCard(total: Double) {
        creditCardLiveData.value = totalToCreditCard(total)
    }

    private fun updateBlue(total: Double) {
        blueLiveData.value = totalToBlue(total)
    }

    private fun updateAgency(total: Double) {
        exchangeAgencyLiveData.value = totalToAgency(total)
    }

    fun onCalculatorValueChanged(editorType: EditorType, newValue: Double) {
        when (editorType) {
            EditorType.AMOUNT -> onAmountValueChanged(newValue)
            EditorType.PESOS -> onPesosValueChanged(newValue)
            EditorType.CREDIT_CARD -> onCreditCardValueChanged(newValue)
            EditorType.EXCHANGE_AGENCY -> onAgencyValueChanged(newValue)
            EditorType.PAYPAL -> {
            }
            EditorType.DISCOUNT -> onDiscountValueChanged(newValue)
            EditorType.TAXES -> onTaxesValueChanged(newValue)
            EditorType.TOTAL -> onTotalValueChanged(newValue)
            EditorType.SAVINGS -> {
            }
            EditorType.BLUE -> onBlueValueChanged(newValue)
        }
        if (editorType != EditorType.TAXES && editorType != EditorType.SAVINGS) {
            preferences.lastConversionType = editorType
            preferences.lastConversionValue = newValue
        }
        currencyEditorTypeLiveData.value = editorType
    }

    private fun onBlueValueChanged(newValue: Double) {
        blueLiveData.value = newValue
        val total = blueToTotal(newValue)
        updateAmount(total)
        updateTotal(total)
        updatePesos(total)
        updateCreditCard(total)
        updateAgency(total)
    }

    private fun onTotalValueChanged(total: Double) {
        totalLiveData.value = total
        updateAmount(total)
        updatePesos(total)
        updateCreditCard(total)
        updateAgency(total)
        updateBlue(total)
    }

    private fun onTaxesValueChanged(newValue: Double) {
        this.taxes = newValue
        preferences.taxes = newValue
        taxesLiveData.value = newValue
        onAmountValueChanged(amountLiveData.value ?: 0.0)
    }

    private fun onDiscountValueChanged(newValue: Double) {
        this.discount = newValue
        preferences.discount = newValue
        discountLiveData.value = newValue
        onAmountValueChanged(amountLiveData.value ?: 0.0)
    }

    private fun onAgencyValueChanged(newValue: Double) {
        exchangeAgencyLiveData.value = newValue
        val total = agencyToTotal(newValue)
        updateAmount(total)
        updateTotal(total)
        updatePesos(total)
        updateCreditCard(total)
        updateBlue(total)
    }

    private fun onCreditCardValueChanged(newValue: Double) {
        creditCardLiveData.value = newValue
        val total = creditCardToTotal(newValue)
        updateAmount(total)
        updateTotal(total)
        updatePesos(total)
        updateAgency(total)
        updateBlue(total)
    }

    private fun onPesosValueChanged(newValue: Double) {
        pesosLiveData.value = newValue
        val total = pesosToTotal(newValue)
        updateAmount(total)
        updateTotal(total)
        updateCreditCard(total)
        updateAgency(total)
        updateBlue(total)
    }

    private fun totalToAmount(total: Double): Double {
        val totalWithoutTaxes = total / (1 + taxes / 100)
        //double totalWithoutTaxesOrDiscounts = totalWithoutTaxes / (1 - discount / 100); //THIS IS SOMETIMES  DIVIDING BY 0
        var totalWithoutTaxesOrDiscounts = 0.0
        if (discount != 100.0) totalWithoutTaxesOrDiscounts = totalWithoutTaxes / (1 - discount / 100)
        return totalWithoutTaxesOrDiscounts
    }

    private fun amountToTotal(amount: Double): Double {
        val amountWithDiscount = amount - discount * amount / 100
        return amountWithDiscount + taxes * amountWithDiscount / 100
    }

    private fun totalToPesos(total: Double): Double {
        var pesos = total * bankExchangeRate
        pesos += bankExchangeRatePercentage * pesos / 100 // suma porcentaje de correccion de la cotizacion
        return pesos
    }

    private fun pesosToTotal(pesos: Double): Double {
        if (bankExchangeRate == 0.0) return 0.0
        var total = pesos - bankExchangeRatePercentage * pesos / 100 // resta la correccion
        total /= bankExchangeRate
        return total
    }

    private fun totalToCreditCard(total: Double): Double {
        val pesos = totalToPesos(total)
        return pesos + afipPercentage * pesos / 100
    }

    private fun creditCardToTotal(creditCard: Double): Double {
        val pesos = creditCard / (1 + afipPercentage / 100)
        return pesosToTotal(pesos)
    }

    private fun totalToBlue(total: Double): Double {
        val blueRate = PreferencesManager.getInstance().blueDollarToARSRate
        return total * blueRate
    }

    private fun blueToTotal(blue: Double): Double {
        val blueRate = PreferencesManager.getInstance().blueDollarToARSRate
        return blue / blueRate
    }

    private fun totalToAgency(total: Double): Double {
        return total * agencyExchangeRate
    }

    private fun agencyToTotal(agency: Double): Double {
        return if (agencyExchangeRate == 0.0) 0.0 else agency / agencyExchangeRate
    }

    fun onSettingsChanged() {
        bankExchangeRate = if (preferences.isUseInternetBankExchangeRateEnabled) {
            preferences.internetExchangeRate
        } else {
            preferences.bankExchangeRate
        }
        bankExchangeRatePercentage = preferences.bankCorrectionPercentage
        agencyExchangeRate = preferences.agencyExchangeRate
        afipPercentage = preferences.afipPercentage
        payPalPercentage = preferences.payPalPercentage
        invertBankExchangeRate = preferences.isBankExchangeRateInverted
        invertAgencyExchangeRate = preferences.isAgencyExchangeRateInverted
        discount = preferences.discount
        taxes = preferences.taxes
        savingsPercentage = preferences.savingsPercentage

        if (invertBankExchangeRate) {
            if (bankExchangeRate != 0.0) bankExchangeRate = 1 / bankExchangeRate
        }

        if (invertAgencyExchangeRate) {
            if (agencyExchangeRate != 0.0) agencyExchangeRate = 1 / agencyExchangeRate
        }
        val currentCurrency = preferences.currentCurrency
        currencyLiveData.value = currentCurrency
        lastUpdateLiveData.value = preferences.getLastUpdateDate(currentCurrency)
        retoreLastConversion()
    }

    private fun retoreLastConversion() {
        val lastConversionType = preferences.lastConversionType
        if (lastConversionType != null) {
            onCalculatorValueChanged(lastConversionType, preferences.lastConversionValue)
        }
    }

    fun onForceRefresh() {
        isLoadingLiveData.value = true
        val currency = preferences.currentCurrency
        val now = Date()
        coroutineScope.launch {
            repository.getCurrencyExchange(currency, CurrencyManager.ARS, 0.0, true)
            preferences.setLastUpdateDate(now, currency)
            onSettingsChanged()
            isLoadingLiveData.value = false
        }
    }

    fun onDeleteDiscount() {
        onDiscountValueChanged(0.0)
    }

    fun onDeleteTaxes() {
        onTaxesValueChanged(0.0)
    }
}