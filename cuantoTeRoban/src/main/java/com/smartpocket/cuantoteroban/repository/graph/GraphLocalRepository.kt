package com.smartpocket.cuantoteroban.repository.graph

import com.smartpocket.cuantoteroban.Currency
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class GraphLocalRepository : GraphRepository {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

    override fun getGraphData(currencyFrom: Currency, currencyTo: String, file: File): List<PastCurrency> {
        if (file.exists()) {
            return parseFile(file)
        } else {
            throw FileNotFoundException()
        }
    }

    fun fileExists(file: File) = file.exists()

    fun fileTimestamp(file: File) = Date(file.lastModified())

    private fun parseFile(file: File): List<PastCurrency> {
        val listOfValues = mutableListOf<PastCurrency>()
        file.forEachLine { line ->
            val tokens = line.split(',')
            if (tokens.size >= 5) {
                try {
                    val date = dateFormatter.parse(tokens[0]) as Date
                    val value = tokens[4].toFloat()
                    listOfValues.add(PastCurrency(date, value))
                } catch (e: Exception) {
                }
            }
        }
        return listOfValues
    }
}