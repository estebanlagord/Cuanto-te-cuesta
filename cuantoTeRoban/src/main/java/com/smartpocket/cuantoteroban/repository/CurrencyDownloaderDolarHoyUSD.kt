package com.smartpocket.cuantoteroban.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.logging.Level

class CurrencyDownloaderDolarHoyUSD : CurrencyDownloader() {

    override suspend fun getExchangeRateFor1(currencyFrom: String, currencyTo: String) =
            withContext(Dispatchers.IO) {
                logger.log(Level.INFO, "Downloading exchange rate for ${currencyFrom}x$currencyTo")
                val doc: Document = Jsoup.connect("http://www.dolarhoy.com").get()

                logger.log(Level.INFO, "Parsing result...")
                val allLinks = doc.select("a")
                var official = 0.0
                var blue = 0.0

                for (link in allLinks) {
                    if (official == 0.0)
                        official = findPriceFromLink(link, "/cotizaciondolaroficial")
                    if (blue == 0.0)
                        blue = findPriceFromLink(link, "/cotizaciondolarblue")
                }

                DolarResult(official, blue)

//                throw Resources.NotFoundException("Conversion rate not found")
            }

    private fun findPriceFromLink(link: Element, targetLink: String): Double {
        if (link.attr("href") == targetLink) {
            val valueStr = (link.parentNode() as Element)
                    .getElementsByClass("values")[0]
                    .getElementsByClass("venta")[0]
                    .getElementsByClass("val")
                    .text()
                    .replace("$", "")
                    .replace(',', '.')

            if (valueStr.isNotBlank()) {
                val value = valueStr.toDouble()
                logger.log(Level.INFO, "Found result: $value")
                return value
            }
        }
        return 0.0
    }

    @Deprecated("old version, no longer working")
    fun findPrice(link: Element, currencyName: String): Double {
        if (link.text().contains(currencyName, true)) {
            (link.parentNode()!!.parentNode() as Element).getElementsContainingText("VENTA")
                    .forEach {
                        if (it.text().contains("COMPRA", true).not()) {
                            val valueStr = it.getElementsByClass("PRICE").text()
                                    .replace("$", "")
                                    .replace(',', '.')
                            if (valueStr.isNotBlank()) {
                                val value = valueStr.toDouble()
                                logger.log(Level.INFO, "Found result: $value")
                                return value
                            }
                        }
                    }
        }
        return 0.0
    }
}