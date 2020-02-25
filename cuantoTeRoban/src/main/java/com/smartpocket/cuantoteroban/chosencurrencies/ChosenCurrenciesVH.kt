package com.smartpocket.cuantoteroban.chosencurrencies

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartpocket.cuantoteroban.Currency
import kotlinx.android.synthetic.main.choose_currency_row.view.*

class ChosenCurrenciesVH(val view: View, val isItem: Boolean) : RecyclerView.ViewHolder(view) {

    lateinit var flag: ImageView
    lateinit var name: TextView

    init {
        if (isItem) {
            flag = view.chooseCurrencyFlag
            name = view.chooseCurrencyName
        }
    }

    fun bindTo(currency: Currency, listener: ChosenCurrenciesListener) {
        if (isItem) {
            flag.setImageResource(currency.flagIdentifier)
            name.text = currency.name

            view.setOnClickListener { listener.onChosenCurrencyClick(currency) }
            view.setOnLongClickListener {
                listener.onChosenCurrencyLongClick(currency)
                true
            }
        }
    }

}