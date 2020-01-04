package com.smartpocket.cuantoteroban.repository

import java.util.logging.Logger

abstract class CurrencyDownloader {

    protected val logger = Logger.getLogger(javaClass.simpleName)

    abstract suspend fun getExchangeRateFor1(currencyFrom: String, currencyTo: String): CurrencyResult
}