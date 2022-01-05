package com.smartpocket.cuantoteroban.repository

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class CurrencyRemoteRepository(private val preferences: PreferencesManager) {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private val googleDownloader by lazy { CurrencyDownloaderGoogle() }
    private val xeDownloader by lazy { CurrencyDownloaderXE() }
//    private val brlDownloader by lazy { CurrencyDownloaderDolarHoyBRL() }
    private val eurDownloader by lazy { CurrencyDownloaderDolarHoyEUR() }
    private val usdDownloader by lazy { CurrencyDownloaderDolarHoyUSD() }
    private val uyuDownloader by lazy { CurrencyDownloaderDolarHoyUYU() }

    private val xeCurrencyList = listOf("VES", "STD", "LVL", "SHP", "GIP", "FKP", "SYP", "LTL",
            "YER", "WST", "MNT", "KPW", "ZMK", "ETB", "FJD", "PGK", "PEN", "KHR", "BYR", "DJF")
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
	Birr etíope - Etiopia
    Dólar fiyiano - Fiyi
    Kina de Papúa Nueva Guinea - Papúa Nueva Guinea
    Nuevo sol peruano - Perú
    Riel camboyano - Camboya
    Rublo bielorruso - Bielorrusia
    */

    suspend fun getCurrencyExchange(
            currencyFrom: Currency,
            currencyTo: String,
            amount: Double
    ): CurrencyResult {
        val from = currencyFrom.code.uppercase()
        val downloader = when {
//            from == CurrencyManager.BRL -> brlDownloader // Switching to Google for accuracy
            from == CurrencyManager.EUR -> eurDownloader
            from == CurrencyManager.USD -> usdDownloader
            from == CurrencyManager.UYU -> uyuDownloader
            xeCurrencyList.contains(from) -> xeDownloader
            else -> googleDownloader
//            else -> xeDownloader
        }
        val result = downloader.getExchangeRateFor1(currencyFrom.code, currencyTo)

        if (result.official == 0.0) throw IllegalStateException("Value cannot be 0")

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