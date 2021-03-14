package com.smartpocket.cuantoteroban.graphic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.core.FuelError
import com.github.mikephil.charting.data.Entry
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.MyApplication
import com.smartpocket.cuantoteroban.R
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import com.smartpocket.cuantoteroban.repository.graph.GraphDataProvider
import com.smartpocket.cuantoteroban.repository.graph.PastCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DisplayGraphicViewModel @Inject constructor(application: Application)
    : AndroidViewModel(application) {

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + parentJob)
    private val graphDataProvider by lazy { GraphDataProvider() }

    @Inject
    lateinit var preferences: PreferencesManager
    val entriesLD = MutableLiveData<List<Entry>>()
    val statusLD = MutableLiveData<GraphicStatus>(GraphicStatus.Loading())

    enum class DateRangeOption {
        DAYS_7,
        MONTHS_1,
        YEARS_1,
        YEARS_5,
        MAX
    }

    init {
        updateGraph(DateRangeOption.YEARS_1)
    }

    private fun updateGraph(dateRange: DateRangeOption) {
        coroutineScope.launch {
            try {
                val data = graphDataProvider.getGraphData(preferences.currentCurrency, CurrencyManager.ARS, getDateRange(dateRange))
                val entries = convertValuesToEntries(data)
                if (entries.isEmpty()) {
                    statusLD.postValue(
                            GraphicStatus.Error(getApplication<MyApplication>().getString(R.string.error_chart_no_data_for_period, preferences.currentCurrency.name),
                                    true))
                } else {
                    statusLD.postValue(GraphicStatus.ShowingData())
                    entriesLD.postValue(entries)
                }
            } catch (e: Exception) {
                var msg = getApplication<MyApplication>().getString(R.string.error_chart_generic_error, preferences.currentCurrency.name)
                if (e is FuelError) {
                    if (e.exception is UnknownHostException)
                        msg = getApplication<MyApplication>().getString(R.string.error_no_internet)
                    else if (e.response.statusCode == 404)
                        msg = getApplication<MyApplication>().getString(R.string.error_chart_no_data_found, preferences.currentCurrency.name)
                }
                statusLD.postValue(GraphicStatus.Error(msg))
            }
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
