package com.smartpocket.cuantoteroban.chosencurrencies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.R


private const val TypeHeader = 1
private const val TypeNormal = 2
private const val TypeFooter = 3

class ChosenCurrenciesRecyclerAdapter(val listener: ChosenCurrenciesListener) : RecyclerView.Adapter<ChosenCurrenciesVH>() {
    private var items = mutableListOf<ChosenCurrenciesItem>()

    init {
        updateCurrenciesList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChosenCurrenciesVH {
        val inflater = LayoutInflater.from(parent.context)
        val rowView: View = when (viewType) {
            TypeNormal -> inflater.inflate(R.layout.choose_currency_row, parent, false)
            TypeHeader -> inflater.inflate(R.layout.choose_currency_header, parent, false)
            TypeFooter -> inflater.inflate(R.layout.choose_currency_footer, parent, false)
            else -> throw IllegalStateException("Invalid view type")
        }
        return ChosenCurrenciesVH(rowView, viewType == TypeNormal)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ChosenCurrenciesItem.Header -> TypeHeader
        is ChosenCurrenciesItem.CurrencyItem -> TypeNormal
        is ChosenCurrenciesItem.Footer -> TypeFooter
    }

    /*= when (position) {
        0 -> TypeHeader
        in 1 until itemCount -> TypeNormal
        else -> TypeFooter
    }*/

    override fun onBindViewHolder(holder: ChosenCurrenciesVH, position: Int) {
        val item = items[position]

        if (item is ChosenCurrenciesItem.CurrencyItem) {
            holder.bindTo(item.curr, listener)
        }
    }

    /**
     * Used to refresh the list of courses in the adapter
     */
    fun updateCurrenciesList() {
        val prevList = items.toList()
        items.clear()
        items.add(ChosenCurrenciesItem.Header())

        CurrencyManager.getInstance().userCurrencies.forEach {
            items.add(ChosenCurrenciesItem.CurrencyItem(it))
        }

        items.add(ChosenCurrenciesItem.Footer())

        if (prevList.size > items.size) {
            // something was removed, find its position
            val removed = prevList.filterNot {
                it is ChosenCurrenciesItem.Header || it is ChosenCurrenciesItem.Footer || it in items
            }
            if (removed.size == 1) {
                notifyItemRemoved(prevList.indexOf(removed.first()))
            } else {
                notifyDataSetChanged()
            }
        } else {
            // something was added, find its position
            val added = items.filterNot {
                it is ChosenCurrenciesItem.Header || it is ChosenCurrenciesItem.Footer || it in prevList
            }
            if (added.size == 1) {
                notifyItemInserted(items.indexOf(added.first()))
            } else {
                notifyDataSetChanged()
            }
        }
    }


/*    fun getItem(position: Int): Currency {
        return items[position]
    }*/
}