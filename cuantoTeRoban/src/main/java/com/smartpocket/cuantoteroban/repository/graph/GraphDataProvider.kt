package com.smartpocket.cuantoteroban.repository.graph

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.MyApplication
import java.io.File
import java.util.*

class GraphDataProvider {

    private val localRepository by lazy { GraphLocalRepository() }
    private val remoteRepository by lazy { GraphRemoteRepository() }

    suspend fun getGraphData(currencyFrom: Currency, currencyTo: String, dateRange: ClosedRange<Date>)
            : List<PastCurrency> {

        val from = currencyFrom.code
        val to = currencyTo
        val file = File(MyApplication.applicationContext().filesDir, "cotizacion$from-$to.csv")

        if (needsDownload(file)) {
            remoteRepository.downloadGraphData(currencyFrom, currencyTo, file)
        }

        val pastCurrencies = localRepository.getGraphData(currencyFrom, currencyTo, file)
        return pastCurrencies.filter { dateRange.contains(it.date) }
    }

    private fun needsDownload(file: File): Boolean {
//        return true
        return localRepository.fileExists(file).not() //TODO return true if the file is old or something
    }
}