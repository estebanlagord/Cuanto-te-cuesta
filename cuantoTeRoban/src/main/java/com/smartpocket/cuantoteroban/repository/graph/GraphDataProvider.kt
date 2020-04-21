package com.smartpocket.cuantoteroban.repository.graph

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.MyApplication
import java.io.File
import java.util.*
import java.util.logging.Logger

class GraphDataProvider {

    private val localRepository by lazy { GraphLocalRepository() }
    private val remoteRepository by lazy { GraphRemoteRepository() }
    private val logger = Logger.getLogger(javaClass.simpleName)

    suspend fun getGraphData(currencyFrom: Currency, currencyTo: String, dateRange: ClosedRange<Date>)
            : List<PastCurrency> {

        val from = currencyFrom.code
        val to = currencyTo
        val file = File(MyApplication.applicationContext().filesDir, "cotizacion$from-$to.csv")

        if (needsDownload(file)) {
            logger.info("Downloading graph data...")
            remoteRepository.downloadGraphData(currencyFrom, currencyTo, file)
        }

        val pastCurrencies = localRepository.getGraphData(currencyFrom, currencyTo, file)
        return pastCurrencies.filter { dateRange.contains(it.date) }
    }

    private fun needsDownload(file: File): Boolean {
        if (localRepository.fileExists(file).not()) {
            logger.info("Cached file for graph data does not exist")
            return true
        }

        // check if existing file is older than 1 day
        val earliestDate = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val fileDate = localRepository.fileTimestamp(file)
//        logger.info("Cached file for graph data timestamp: $fileDate")
        return fileDate < earliestDate.time
    }
}