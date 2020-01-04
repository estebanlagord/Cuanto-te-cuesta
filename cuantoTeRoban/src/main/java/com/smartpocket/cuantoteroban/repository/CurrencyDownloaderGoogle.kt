package com.smartpocket.cuantoteroban.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.logging.Level

class CurrencyDownloaderGoogle : CurrencyDownloader() {

    override suspend fun getExchangeRateFor1(currencyFrom: String, currencyTo: String) =
        withContext(Dispatchers.IO) {
            logger.log(Level.INFO, "Downloading exchange rate for ${currencyFrom}x$currencyTo")
            val doc: Document =
                Jsoup.connect("https://www.google.com/search?q=1+$currencyFrom+in+$currencyTo&num=1")
                    .get()
            val rate = doc.getElementsByAttribute("data-exchange-rate")
                .attr("data-exchange-rate")
                .toDouble()
            logger.log(Level.INFO, "1 $currencyFrom = $rate $currencyTo")
            return@withContext CurrencyResult(rate)
        }
}