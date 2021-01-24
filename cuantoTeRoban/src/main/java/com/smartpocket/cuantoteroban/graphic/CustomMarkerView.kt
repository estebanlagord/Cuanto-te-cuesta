package com.smartpocket.cuantoteroban.graphic

import android.content.Context
import androidx.annotation.LayoutRes
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.smartpocket.cuantoteroban.R
import com.smartpocket.cuantoteroban.Utilities
import com.smartpocket.cuantoteroban.databinding.ChartMarkerViewBinding
import com.smartpocket.cuantoteroban.repository.graph.PastCurrency

class CustomMarkerView(context: Context, @LayoutRes layoutResource: Int) : MarkerView(context, layoutResource) {
    private val currencyFormatter = Utilities.getCurrencyFormat()
    private val dateFormatter = Utilities.getDateFormat()
    private val vOffset = context.resources.getDimension(R.dimen.chart_marker_vertical_offset)
    private lateinit var binding: ChartMarkerViewBinding

    init {
        val xOffset = -width / 2f
        val yOffset = -height.toFloat() - vOffset
        setOffset(xOffset, yOffset)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = ChartMarkerViewBinding.bind(this)
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val entryData = e.data as PastCurrency
        binding.tvValue.text = dateFormatter.format(entryData.date) + "\n" + currencyFormatter.format(entryData.value)
        super.refreshContent(e, highlight)
    }

/*    fun getXOffset(xpos: Float): Int {
        // this will center the marker-view horizontally
        return -(width / 2)
    }

    fun getYOffset(ypos: Float): Int {
        // this will cause the marker-view to be above the selected value
        return -height
    }*/
}