package com.smartpocket.cuantoteroban

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

    val amountLiveData = MutableLiveData<Double>()
    val officialLiveData = MutableLiveData<Double>()
    val cardLiveData = MutableLiveData<Double>()
    val blueLiveData = MutableLiveData<Double>()
    val currencyLiveData = MutableLiveData<Currency>(preferencesManager.currentCurrency)
    val currentEditorType = MutableLiveData<EditorType>()

    init {
        onSettingsChanged()
    }

    fun onAmountValueChanged(amount: Double) {
        coroutineScope.launch {
            val currency = preferencesManager.currentCurrency
            amountLiveData.value = amount
            val currencyResult = repository.getCurrencyExchange(currency, CurrencyManager.ARS, amount)
            officialLiveData.value = currencyResult.official * amount
            cardLiveData.value = currencyResult.official * amount * 1.3
            if (currencyResult is DolarResult)
                blueLiveData.value = currencyResult.blue * amount
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
        currentEditorType.value = editorType
    }

    fun onSettingsChanged() {
        currencyLiveData.value = preferencesManager.currentCurrency
        retoreLastConversion()
    }

    private fun retoreLastConversion() {
        val lastConversionType = preferencesManager.lastConversionType
        if (lastConversionType != null) {
            onCalculatorValueChanged(lastConversionType, preferencesManager.lastConversionValue)
        }
    }
}