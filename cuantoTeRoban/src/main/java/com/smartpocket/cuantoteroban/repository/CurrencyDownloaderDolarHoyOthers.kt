package com.smartpocket.cuantoteroban.repository

import android.content.res.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.logging.Level

abstract class CurrencyDownloaderDolarHoyOthers : CurrencyDownloader() {


    protected abstract fun getSubUrl(): String

    override suspend fun getExchangeRateFor1(currencyFrom: String, currencyTo: String) =
            withContext(Dispatchers.IO) {
                logger.log(Level.INFO, "Downloading exchange rate for ${currencyFrom}x$currencyTo")
                val doc: Document = Jsoup.connect("http://www.dolarhoy.com${getSubUrl()}").get()

                logger.log(Level.INFO, "Parsing result...")
                val rateStr = doc
                        .getElementsByClass("tile cotizacion_value")[0]
                        .getElementsByClass("tile is-parent is-8")[0]
                        .getElementsByIndexEquals(1)[3]
                        .text().trim()
                        .replace("$", "")
                        .replace(',', '.')
                val value = rateStr.toDouble()
                if (value > 0) {
                    return@withContext CurrencyResult(value)
                } else {
                    throw Resources.NotFoundException("Conversion rate not found")
                }
            }
}