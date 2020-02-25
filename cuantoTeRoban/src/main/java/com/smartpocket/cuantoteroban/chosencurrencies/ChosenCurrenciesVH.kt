package com.smartpocket.cuantoteroban.chosencurrencies

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.R
import kotlinx.android.synthetic.main.choose_currency_row.view.*

class ChosenCurrenciesVH(val view: View, val isItem: Boolean) : RecyclerView.ViewHolder(view) {

    lateinit var flag: ImageView
    lateinit var name: TextView
    lateinit var originalBackground: Drawable

    init {
        if (isItem) {
            flag = view.chooseCurrencyFlag
            name = view.chooseCurrencyName
            originalBackground = view.background
        }
    }

    fun bindTo(currency: Currency, listener: ChosenCurrenciesListener, selected: Boolean) {
        if (isItem) {
            flag.setImageResource(currency.flagIdentifier)
            name.text = currency.name

            if (selected) {
                view.setBackgroundResource(R.color.navDrawerSelected)
                name.setTypeface(null, Typeface.BOLD)
            } else {
                view.background = originalBackground
                name.setTypeface(null, Typeface.NORMAL)
            }

            view.setOnClickListener { listener.onChosenCurrencyClick(currency) }
            view.setOnLongClickListener {
                listener.onChosenCurrencyLongClick(currency)
                true
            }
        }
    }

}