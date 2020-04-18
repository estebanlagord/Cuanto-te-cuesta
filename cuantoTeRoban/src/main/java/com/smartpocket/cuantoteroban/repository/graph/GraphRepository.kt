package com.smartpocket.cuantoteroban.repository.graph

import com.smartpocket.cuantoteroban.Currency
import java.io.File

interface GraphRepository {

    fun getGraphData(currencyFrom: Currency, currencyTo: String, file: File): List<PastCurrency>
}