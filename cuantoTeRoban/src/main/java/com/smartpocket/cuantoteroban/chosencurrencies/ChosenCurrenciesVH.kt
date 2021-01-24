package com.smartpocket.cuantoteroban.chosencurrencies

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.R
import com.smartpocket.cuantoteroban.databinding.ChooseCurrencyRowBinding

class ChosenCurrenciesVH(val view: View, private val isItem: Boolean) : RecyclerView.ViewHolder(view) {

    private lateinit var flag: ImageView
    lateinit var name: TextView
    private lateinit var originalBackground: Drawable
    private lateinit var binding: ChooseCurrencyRowBinding

    init {
        if (isItem) {
            binding = ChooseCurrencyRowBinding.bind(view)
            flag = binding.chooseCurrencyFlag
            name = binding.chooseCurrencyName
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