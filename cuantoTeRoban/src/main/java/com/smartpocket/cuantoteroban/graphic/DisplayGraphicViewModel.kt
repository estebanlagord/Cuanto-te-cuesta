package com.smartpocket.cuantoteroban.graphic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import com.smartpocket.cuantoteroban.repository.graph.GraphDataProvider
import com.smartpocket.cuantoteroban.repository.graph.PastCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class DisplayGraphicViewModel : ViewModel() {
    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + parentJob)
    private val preferences: PreferencesManager by lazy { PreferencesManager.getInstance() }
    private val graphDataProvider by lazy { GraphDataProvider() }
    val entriesLD = MutableLiveData<List<Entry>>()

    enum class DateRangeOption {
        DAYS_7,
        MONTHS_1,
        YEARS_1,
        YEARS_5,
        MAX
    }

    init {
        updateGraph(DateRangeOption.MAX)
    }

    private fun updateGraph(dateRange: DateRangeOption) {
        coroutineScope.launch {
            val data = graphDataProvider.getGraphData(preferences.currentCurrency, CurrencyManager.ARS, getDateRange(dateRange))
            val entries = convertValuesToEntries(data)
            entriesLD.postValue(entries)
        }
    }

    private fun convertValuesToEntries(values: List<PastCurrency>): List<Entry> {
        val entries = mutableListOf<Entry>()
        values.forEach {
            val entry = Entry(it.getXforDate(), it.value)
            entry.data = it
            entries.add(entry)
        }
        return entries
    }

    private fun getDateRange(range: DateRangeOption): ClosedRange<Date> {
        val fromDate = Calendar.getInstance()
        val toDate = Date()
        when (range) {
            DateRangeOption.DAYS_7 -> fromDate.add(Calendar.DATE, -7)
            DateRangeOption.MONTHS_1 -> fromDate.add(Calendar.MONTH, -1)
            DateRangeOption.YEARS_1 -> fromDate.add(Calendar.YEAR, -1)
            DateRangeOption.YEARS_5 -> fromDate.add(Calendar.YEAR, -5)
            DateRangeOption.MAX -> fromDate.timeInMillis = 0L
        }
        return fromDate.time..toDate
    }

    fun on7DaysClicked() = updateGraph(DateRangeOption.DAYS_7)

    fun on1MonthClicked() = updateGraph(DateRangeOption.MONTHS_1)

    fun on1YearClicked() = updateGraph(DateRangeOption.YEARS_1)

    fun on5YearsClicked() = updateGraph(DateRangeOption.YEARS_5)

    fun onMaxDaysClicked() = updateGraph(DateRangeOption.MAX)
}
