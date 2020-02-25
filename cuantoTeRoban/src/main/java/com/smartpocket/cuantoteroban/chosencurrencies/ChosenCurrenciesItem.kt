package com.smartpocket.cuantoteroban.chosencurrencies

import com.smartpocket.cuantoteroban.Currency

sealed class ChosenCurrenciesItem {
    data class CurrencyItem(val curr : Currency) : ChosenCurrenciesItem()
    class Header : ChosenCurrenciesItem()
    class Footer : ChosenCurrenciesItem()
}