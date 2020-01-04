package com.smartpocket.cuantoteroban

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
    private val repository = CurrencyRepository()
    val currencyManager = CurrencyManager.getInstance()
    val selectedCurrency = currencyManager.findCurrency("USD")
    var amount = 1.0

    val targetCurrencyLiveData = MutableLiveData<Currency>()
    val amountLiveData = MutableLiveData<Double>()
    val officialLiveData = MutableLiveData<Double>()
    val cardLiveData = MutableLiveData<Double>()
    val blueLiveData = MutableLiveData<Double>()

    fun onAmountValueChanged(amount: Double) {
        coroutineScope.launch {
            val currencyResult = repository.getCurrencyExchange(selectedCurrency.code, "ARS", amount)
            officialLiveData.value = currencyResult.official
            cardLiveData.value = currencyResult.official * 1.3
            if (currencyResult is DolarResult)
                blueLiveData.value = currencyResult.blue
        }
    }
}