package com.smartpocket.cuantoteroban.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.R
import kotlinx.android.synthetic.main.add_currency_row.view.*


class CurrencyListAdapter(private val listener: OnCurrencyItemClickListener)
    : RecyclerView.Adapter<CurrencyListAdapter.MyViewHolder>() {

    private var myDataset = mutableListOf<Currency>()

    class MyViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private val flag: ImageView = view.addCurrencyFlag
        val name: TextView = view.addCurrencyName
        val code: TextView = view.addCurrencyCode

        fun bind(item: Currency, listener: OnCurrencyItemClickListener) {
            flag.setImageResource(item.flagIdentifier)
            name.text = item.name
            code.text = item.code

            itemView.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_currency_row, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = myDataset.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currency = myDataset[position]
        holder.bind(currency, listener)
    }

    fun updateList(newItems : List<Currency>) {
        myDataset.clear()
        myDataset.addAll(newItems)
        notifyDataSetChanged()
    }
}


