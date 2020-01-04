package com.smartpocket.cuantoteroban.currency

import android.content.res.Resources
import com.smartpocket.cuantoteroban.repository.CurrencyDownloader
import com.smartpocket.cuantoteroban.repository.CurrencyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.logging.Level

class CurrencyDownloaderDolarHoyOthers : CurrencyDownloader() {

    override suspend fun getExchangeRateFor1(currencyFrom: String, currencyTo: String) =
            withContext(Dispatchers.IO) {
                logger.log(Level.INFO, "Downloading exchange rate for ${currencyFrom}x$currencyTo")
                val doc: Document = Jsoup.connect("http://www.dolarhoy.com").get()

                logger.log(Level.INFO, "Parsing result...")
                doc.select("a").forEach { link ->
                    if (link.text().contains("dólar oficial", true)) {
                        (link.parentNode().parentNode() as Element).getElementsContainingText("VENTA")
                                .forEach {
                                    if (it.text().contains("COMPRA", true).not()) {
                                        val valueStr = it.getElementsByClass("PRICE").text()
                                                .replace("$", "")
                                                .replace(',', '.')
                                        if (valueStr.isNotBlank()) {
                                            val value = valueStr.toDouble()
                                            logger.log(Level.INFO, "Found result: $value")
                                            return@withContext CurrencyResult(value)
                                        }
                                    }
                                }
                    }
                }
                throw Resources.NotFoundException("Conversion rate not found")
            }
}