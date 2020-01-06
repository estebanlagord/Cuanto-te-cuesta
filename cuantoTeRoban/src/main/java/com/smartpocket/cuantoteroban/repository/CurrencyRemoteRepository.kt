package com.smartpocket.cuantoteroban.repository

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.currency.CurrencyDownloaderDolarHoyDolar
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class CurrencyRemoteRepository {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private val downloader = CurrencyDownloaderGoogle()
    private val usdDownloader = CurrencyDownloaderDolarHoyDolar()
    val preferences: PreferencesManager by lazy { PreferencesManager.getInstance() }

    suspend fun getCurrencyExchange(
            currencyFrom: Currency,
            currencyTo: String,
            amount: Double
    ): CurrencyResult {
        val result = when (currencyFrom.code.toUpperCase(Locale.ROOT)) {
            CurrencyManager.USD -> {
                val rates = usdDownloader.getExchangeRateFor1(currencyFrom.code, currencyTo)
                DolarResult(rates.official, rates.blue)
            }
            else -> {
                val rate = downloader.getExchangeRateFor1(currencyFrom.code, currencyTo)
                CurrencyResult(rate.official)
            }
        }

        logger.log(Level.INFO, "$amount $currencyFrom = $result $currencyTo")
        saveUpdatedRates(currencyFrom, result)
        return result
    }

    private fun saveUpdatedRates(currency: Currency, currencyResult: CurrencyResult) {
        preferences.setInternetExchangeRate(currency, currencyResult.official)
        preferences.setLastUpdateDate(Date(), currency)
        if (currencyResult is DolarResult)
            preferences.setBlueDollarToArsRate(currencyResult.blue)
    }

}