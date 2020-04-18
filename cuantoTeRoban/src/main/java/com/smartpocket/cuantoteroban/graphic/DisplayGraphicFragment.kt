package com.smartpocket.cuantoteroban.graphic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.smartpocket.cuantoteroban.R
import com.smartpocket.cuantoteroban.Utilities
import com.smartpocket.cuantoteroban.databinding.DisplayGraphicFragmentBinding
import kotlinx.android.synthetic.main.display_graphic_fragment.*

class DisplayGraphicFragment : Fragment() {

    private lateinit var binding: DisplayGraphicFragmentBinding
    private lateinit var viewModel: DisplayGraphicViewModel
    private val currencyFormatter = Utilities.getCurrencyFormat()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DisplayGraphicFragmentBinding.inflate(inflater)
//        val rootView = inflater.inflate(R.layout.display_graphic_fragment, container, false)
        val toolbar: Toolbar = binding.root.findViewById(R.id.my_awesome_toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar as ActionBar
        actionBar.title = "Gráfico histórico"
        actionBar.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[DisplayGraphicViewModel::class.java]
        configGraph()
        viewModel.entriesLD.observe(viewLifecycleOwner, Observer {
            updateEntries(it)
        })

        button7D.setOnClickListener { viewModel.on7DaysClicked() }
        button1M.setOnClickListener { viewModel.on1MonthClicked() }
        button1A.setOnClickListener { viewModel.on1YearClicked() }
        button5A.setOnClickListener { viewModel.on5YearsClicked() }
        buttonMax.setOnClickListener { viewModel.onMaxDaysClicked() }
    }

    private fun updateEntries(it: List<Entry>?) {
        val dataSet = LineDataSet(it, "Valor de $1 USD en Pesos argentinos").apply {
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            lineWidth = resources.getDimension(R.dimen.chart_line_width)
            color = ContextCompat.getColor(requireContext(), R.color.color_primary)
            setCircleColor(color)
        }
        val lineData = LineData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return currencyFormatter.format(value)
                }
            })
        }
        chart.data = lineData
        chart.invalidate()
    }

    private fun configGraph() {
        with(chart) {
            isAutoScaleMinMaxEnabled = true
            xAxis.setDrawGridLines(false)
//            axisLeft.isEnabled = false
//            axisRight.isEnabled = false
            marker = CustomMarkerView(requireContext(), R.layout.chart_marker_view)
            xAxis.valueFormatter = DateValueFormatter()
            xAxis.labelRotationAngle = 45f
            xAxis.isGranularityEnabled = true
            val desc = Description().apply { text = "Descipcion del grafico USD" }
            description = desc
//            xAxis.granularity = TimeUnit.DAYS.toMillis(1).toFloat()
//            axisRight.granularity = 1f
        }
    }
}
