package com.smartpocket.cuantoteroban.repository

class CurrencyDownloaderDolarHoyUYU : CurrencyDownloaderDolarHoyOthers() {

    override fun getSubUrl() = "/cotizacion-peso-uruguayo"

}