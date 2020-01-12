package com.smartpocket.cuantoteroban.repository

class CurrencyDownloaderDolarHoyBRL : CurrencyDownloaderDolarHoyOthers() {

    override fun getSubUrl() = "/cotizacion-real-brasileno"

}