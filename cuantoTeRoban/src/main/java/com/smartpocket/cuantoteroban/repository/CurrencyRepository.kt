package com.smartpocket.cuantoteroban.repository

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class CurrencyRepository(val preferences: PreferencesManager) {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private val localRepos by lazy { CurrencyLocalRepository(preferences) }
    private val remoteRepos by lazy { CurrencyRemoteRepository(preferences) }

    suspend fun getCurrencyExchange(
            currencyFrom: Currency,
            currencyTo: String,
            amount: Double,
            isForce: Boolean = false
    ): CurrencyResult {
        // decide if we should use local or remote
        val result: CurrencyResult
        val noKnownRate = preferences.internetExchangeRate <= 0.0

        result = if (isForce || noKnownRate || needsUpdate(currencyFrom)) {
            remoteRepos.getCurrencyExchange(currencyFrom, currencyTo, amount)
        } else {
            localRepos.getCurrencyExchange(currencyFrom, currencyTo, amount)
        }


        logger.log(Level.INFO, "$amount $currencyFrom = $result $currencyTo")
        return result
    }

    private fun needsUpdate(currencyFrom: Currency): Boolean {
        val lastUpdate = preferences.getLastUpdateDate(currencyFrom)
        val now = Date()
        val timeSinceUpdate = now.time - lastUpdate.time
        val updateFreqMs = TimeUnit.HOURS.toMillis(preferences.updateFrequencyInHours.toLong())
        return timeSinceUpdate >= updateFreqMs
    }
}