package com.smartpocket.cuantoteroban.graphic

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.smartpocket.cuantoteroban.R
import com.smartpocket.cuantoteroban.Utilities
import com.smartpocket.cuantoteroban.databinding.DisplayGraphicFragmentBinding
import kotlinx.android.synthetic.main.display_graphic_fragment.*

private const val ANIMATION_DURATION_MS = 400
private const val CHOSEN_PERIOD_KEY = "Chosen Period Key"

class DisplayGraphicFragment : Fragment() {

    private lateinit var binding: DisplayGraphicFragmentBinding
    private lateinit var viewModel: DisplayGraphicViewModel
    private val currencyFormatter = Utilities.getCurrencyFormat()
    private var textColorDayNight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DisplayGraphicFragmentBinding.inflate(inflater)
        val toolbar: Toolbar = binding.root.findViewById(R.id.my_awesome_toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        textColorDayNight = requireContext().getColorResCompat(android.R.attr.textColorPrimary)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar as ActionBar
        actionBar.title = "Gráfico histórico"
        actionBar.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState != null) {
            toggleGroup.check(savedInstanceState.getInt(CHOSEN_PERIOD_KEY))
        }

        viewModel = ViewModelProvider(this)[DisplayGraphicViewModel::class.java]
        configGraph()
        viewModel.entriesLD.observe(viewLifecycleOwner, Observer {
            updateEntries(it)
        })
        viewModel.statusLD.observe(viewLifecycleOwner, Observer {
            updateStatus(it)
        })

        button7D.setOnClickListener { viewModel.on7DaysClicked() }
        button1M.setOnClickListener { viewModel.on1MonthClicked() }
        button1A.setOnClickListener { viewModel.on1YearClicked() }
        button5A.setOnClickListener { viewModel.on5YearsClicked() }
        buttonMax.setOnClickListener { viewModel.onMaxDaysClicked() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CHOSEN_PERIOD_KEY, toggleGroup.checkedButtonId)
    }

    private fun updateStatus(it: GraphicStatus) {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        return when (it) {
            is GraphicStatus.Loading -> {
                showViews(progressBar)
                hideViews(tvErrorMsg, chart, toggleGroup)
            }
            is GraphicStatus.Error -> {
                val viewsToShow = mutableListOf<View>(tvErrorMsg)
                val viewsToHide = mutableListOf<View>(progressBar, chart)

                if (it.showPeriodButtons) viewsToShow.add(toggleGroup)
                else viewsToHide.add(toggleGroup)

                showViews(*viewsToShow.toTypedArray())
                hideViews(*viewsToHide.toTypedArray())
                tvErrorMsg.text = it.errorMsg
            }
            is GraphicStatus.ShowingData -> {
                showViews(chart, toggleGroup)
                hideViews(progressBar, tvErrorMsg)
            }
        }
    }

    private fun showViews(vararg view: View) = view.forEach { it.visibility = View.VISIBLE }
    private fun hideViews(vararg view: View) = view.forEach { it.visibility = View.GONE }

    private fun updateEntries(it: List<Entry>) {

        val curr = viewModel.preferences.currentCurrency.code
        val dataSet = LineDataSet(it, "Valor de $1 $curr en Pesos argentinos").apply {
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            lineWidth = resources.getDimension(R.dimen.chart_line_width)
            color = ContextCompat.getColor(requireContext(), R.color.color_primary)
            setDrawHighlightIndicators(false)
            setCircleColor(color)
            setDrawCircles(false)
            valueTextColor = textColorDayNight
        }
        val lineData = LineData(dataSet).apply {
            setValueTextSize(resources.getDimension(R.dimen.chart_entry_value_text_size))
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return currencyFormatter.format(value)
                }
            })
        }
        with(chart) {
            data = lineData
            highlightValues(null)
            fitScreen()
            animateX(ANIMATION_DURATION_MS)
        }
    }

    private fun configGraph() {
        with(chart) {
            isAutoScaleMinMaxEnabled = true
            xAxis.setDrawGridLines(false)
            marker = CustomMarkerView(requireContext(), R.layout.chart_marker_view)
            xAxis.valueFormatter = DateValueFormatter()
            xAxis.labelRotationAngle = resources.getInteger(R.integer.graph_label_rotation_angle).toFloat()
            xAxis.isGranularityEnabled = false
            xAxis.textColor = textColorDayNight
            axisLeft.textColor = textColorDayNight
            axisRight.textColor = textColorDayNight
            val currName = viewModel.preferences.currentCurrency.name
            val desc = Description().apply {
                text = "Datos históricos para $currName"
                textColor = textColorDayNight
            }
            description = desc
            legend.textColor = textColorDayNight
//            xAxis.granularity = TimeUnit.DAYS.toMillis(1).toFloat()
//            axisRight.granularity = 1f
        }
    }

    @ColorInt
    fun Context.getColorResCompat(@AttrRes id: Int): Int {
        val resolvedAttr = TypedValue()
        theme.resolveAttribute(id, resolvedAttr, true)
        return ContextCompat.getColor(requireContext(), resolvedAttr.resourceId)
    }
}
