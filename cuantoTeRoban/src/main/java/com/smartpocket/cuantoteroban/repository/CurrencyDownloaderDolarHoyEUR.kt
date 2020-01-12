package com.smartpocket.cuantoteroban.repository

class CurrencyDownloaderDolarHoyEUR : CurrencyDownloaderDolarHoyOthers() {

    override fun getSubUrl() = "/cotizacion-euro"

}