package com.smartpocket.cuantoteroban.repository.graph

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.awaitUnit
import com.smartpocket.cuantoteroban.Currency
import java.io.File
import java.lang.Exception
import java.util.*

class GraphRemoteRepository {

    // URL example:
    // https://query1.finance.yahoo.com/v7/finance/download/USDARS=X?period1=994809600&period2=1586563200&interval=1d  // since 2001

    suspend fun downloadGraphData(currencyFrom: Currency, currencyTo: String, file: File) {
        println("downloading new CSV file for ${currencyFrom.code} to $currencyTo")
        val currStr = currencyFrom.code + currencyTo + "=X"
        val period1 = "period1" to "0" // since 1970
        val period2 = "period2" to Date().time.toString() // until today
        val interval = "interval" to "1d"
        val params = listOf(period1, period2, interval)

        try {
            Fuel.download(
                    "https://query1.finance.yahoo.com/v7/finance/download/$currStr", parameters = params)

                    .fileDestination { _, _ -> file }
/*                .progress { readBytes, totalBytes ->
                    val progress = readBytes.toFloat() / totalBytes.toFloat() * 100
                    println("Bytes downloaded $readBytes / $totalBytes ($progress %)")
                }*/
                    .awaitUnit()
            println("Download completed")
        } catch (e: Exception) {
            println("Download error: $e")
            // delete the file which was downloaded with an error message
            file.delete()
            throw e
        }
    }
}