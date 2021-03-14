package com.smartpocket.cuantoteroban.repository

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class CurrencyLocalRepository(private val preferences: PreferencesManager) {

    private val logger = Logger.getLogger(javaClass.simpleName)

    fun getCurrencyExchange(
            currencyFrom: Currency,
            currencyTo: String,
            amount: Double
    ): CurrencyResult {
        val official = preferences.internetExchangeRate
        val result = when (currencyFrom.code.toUpperCase(Locale.ROOT)) {
            CurrencyManager.USD -> {
                DolarResult(official, preferences.blueDollarToARSRate)
            }
            else -> {
                CurrencyResult(official)
            }
        }

        logger.log(Level.INFO, "$amount $currencyFrom = $result $currencyTo")
        return result
    }
}