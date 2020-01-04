package com.smartpocket.cuantoteroban.repository

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.currency.CurrencyDownloaderDolarHoyDolar
import java.util.logging.Level
import java.util.logging.Logger

class CurrencyRepository {

    private val logger = Logger.getLogger(javaClass.simpleName)
    val downloader = CurrencyDownloaderGoogle()
    val usdDownloader = CurrencyDownloaderDolarHoyDolar()

    suspend fun getCurrencyExchange(
            currencyFrom: String,
            currencyTo: String,
            amount: Double
    ): CurrencyResult {
        val result = when (currencyFrom.toUpperCase()) {
            "USD" -> {
                val rates = usdDownloader.getExchangeRateFor1(currencyFrom, currencyTo)
                val official = rates.official * amount
                val blue = rates.blue * amount
                DolarResult(official, blue)
            }
            else -> {
                val rate = downloader.getExchangeRateFor1(currencyFrom, currencyTo)
                CurrencyResult(rate.official * amount)
            }
        }

        logger.log(Level.INFO, "$amount $currencyFrom = $result $currencyTo")
        return result
    }

}