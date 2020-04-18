package com.smartpocket.cuantoteroban.graphic

import com.github.mikephil.charting.formatter.ValueFormatter
import com.smartpocket.cuantoteroban.Utilities
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateValueFormatter : ValueFormatter() {
    private val msInADay = TimeUnit.DAYS.toMillis(1)
    private val dateFormatter = Utilities.getDateFormat()

    override fun getFormattedValue(value: Float): String {
        val date = Date(value.toLong() * msInADay)
        return dateFormatter.format(date)
    }
}