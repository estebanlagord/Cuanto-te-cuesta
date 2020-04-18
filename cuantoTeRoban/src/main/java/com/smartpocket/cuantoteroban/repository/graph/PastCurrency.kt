package com.smartpocket.cuantoteroban.repository.graph

import java.util.*
import java.util.concurrent.TimeUnit

class PastCurrency(val date: Date, val value: Float) {

    companion object {
        val msInADay = TimeUnit.DAYS.toMillis(1)
    }

    fun getXforDate(): Float {
//        return date.time.toFloat()
        return (date.time / msInADay).toFloat()
    }
}