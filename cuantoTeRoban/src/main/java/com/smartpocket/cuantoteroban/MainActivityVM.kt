package com.smartpocket.cuantoteroban

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smartpocket.cuantoteroban.editortype.EditorType
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import com.smartpocket.cuantoteroban.repository.CurrencyRepository
import com.smartpocket.cuantoteroban.repository.DolarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.logging.Logger

class MainActivityVM : ViewModel() {

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)
    private val logger = Logger.getLogger(javaClass.simpleName)
    private val repository by lazy { CurrencyRepository() }
    val preferencesManager by lazy { PreferencesManager.getInstance() }
    val currencyManager = CurrencyManager.getInstance()
    val selectedCurrency = currencyManager.findCurrency("USD")

    val targetCurrencyLiveData = MutableLiveData<Currency>()
    val amountLiveData = MutableLiveData<Double>()
    val officialLiveData = MutableLiveData<Double>()
    val cardLiveData = MutableLiveData<Double>()
    val blueLiveData = MutableLiveData<Double>()
    val currencyLiveData = MutableLiveData<Currency>()

    init {
        onSettingsChanged()
    }

    fun onAmountValueChanged(amount: Double) {
        coroutineScope.launch {
            amountLiveData.value = amount
            val currencyResult = repository.getCurrencyExchange(selectedCurrency.code, "ARS", amount)
            officialLiveData.value = currencyResult.official
            cardLiveData.value = currencyResult.official * 1.3
            if (currencyResult is DolarResult)
                blueLiveData.value = currencyResult.blue
        }
    }

    fun onCalculatorValueChanged(editorType: EditorType, newValue: Double) {
        when (editorType) {
            EditorType.AMOUNT -> onAmountValueChanged(newValue)
        }
        if (editorType != EditorType.TAXES && editorType != EditorType.SAVINGS) {
            preferencesManager.lastConversionType = editorType
            preferencesManager.lastConversionValue = newValue
        }
    }

    fun onSettingsChanged() {
        currencyLiveData.value = preferencesManager.currentCurrency
    }
}