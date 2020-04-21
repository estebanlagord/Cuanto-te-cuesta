package com.smartpocket.cuantoteroban.repository

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class CurrencyRemoteRepository {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private val googleDownloader by lazy { CurrencyDownloaderGoogle() }
    private val xeDownloader by lazy { CurrencyDownloaderXE() }
    private val brlDownloader by lazy { CurrencyDownloaderDolarHoyBRL() }
    private val eurDownloader by lazy { CurrencyDownloaderDolarHoyEUR() }
    private val usdDownloader by lazy { CurrencyDownloaderDolarHoyUSD() }
    private val uyuDownloader by lazy { CurrencyDownloaderDolarHoyUYU() }
    val preferences: PreferencesManager by lazy { PreferencesManager.getInstance() }

    suspend fun getCurrencyExchange(
            currencyFrom: Currency,
            currencyTo: String,
            amount: Double
    ): CurrencyResult {
        val downloader = when (currencyFrom.code.toUpperCase(Locale.ROOT)) {
            CurrencyManager.BRL -> brlDownloader
            CurrencyManager.EUR -> eurDownloader
            CurrencyManager.USD -> usdDownloader
            CurrencyManager.UYU -> uyuDownloader
            else -> googleDownloader
//            else -> xeDownloader
        }
        val result = downloader.getExchangeRateFor1(currencyFrom.code, currencyTo)

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