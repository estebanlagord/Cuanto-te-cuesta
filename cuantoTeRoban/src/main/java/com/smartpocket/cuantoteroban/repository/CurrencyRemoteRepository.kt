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

    private val xeCurrencyList = listOf("VES", "STD", "LVL", "SHP", "GIP", "FKP", "SYP", "LTL",
            "YER", "WST", "MNT", "KPW", "ZMK")
    /*
    List of currencies not supported by Google:
    Bolívar - Venezuela
	Dobra de Santo Tomé - Santo Tomé
	Lats letón - Letonia
	Libra de Santa Helena -  Santa Elena /  Ascensión / Tristán de Acuña
	Libra gibraltareña - Gibraltar
	Libra malvinense - Islas Malvinas
	Libra siria - Siria
	Litas lituana - Lituania
	Riyal de Yemen - Yemen
	Tala de Samoa - Samoa
	Tugrik mongol - Mongolia
	Won norcoreano - Corea del Norte
    */

    suspend fun getCurrencyExchange(
            currencyFrom: Currency,
            currencyTo: String,
            amount: Double
    ): CurrencyResult {
        val from = currencyFrom.code.toUpperCase(Locale.ROOT)
        val downloader = when {
            from == CurrencyManager.BRL -> brlDownloader
            from == CurrencyManager.EUR -> eurDownloader
            from == CurrencyManager.USD -> usdDownloader
            from == CurrencyManager.UYU -> uyuDownloader
            xeCurrencyList.contains(from) -> xeDownloader
            else -> googleDownloader
//            else -> xeDownloader
        }
        val result = downloader.getExchangeRateFor1(currencyFrom.code, currencyTo)

        logger.log(Level.INFO, "$amount ${currencyFrom.code} = $result $currencyTo")
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