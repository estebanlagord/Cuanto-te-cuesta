package com.smartpocket.cuantoteroban.chosencurrencies

import com.smartpocket.cuantoteroban.Currency

interface ChosenCurrenciesListener {

    fun onChosenCurrencyClick(currency: Currency)

    fun onChosenCurrencyLongClick(currency: Currency)

}