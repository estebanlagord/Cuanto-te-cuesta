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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.transition.TransitionManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.smartpocket.cuantoteroban.R
import com.smartpocket.cuantoteroban.Utilities
import com.smartpocket.cuantoteroban.databinding.DisplayGraphicFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

private const val ANIMATION_DURATION_MS = 400
private const val CHOSEN_PERIOD_KEY = "Chosen Period Key"

@AndroidEntryPoint
class DisplayGraphicFragment : Fragment() {

    private var _binding: DisplayGraphicFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: DisplayGraphicViewModel by viewModels()
    private val currencyFormatter = Utilities.getCurrencyFormat()
    private var textColorDayNight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = DisplayGraphicFragmentBinding.inflate(inflater)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.myAwesomeToolbar.myAwesomeToolbar)
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
            binding.toggleGroup.check(savedInstanceState.getInt(CHOSEN_PERIOD_KEY))
        }

        configGraph()
        viewModel.entriesLD.observe(viewLifecycleOwner, Observer {
            updateEntries(it)
        })
        viewModel.statusLD.observe(viewLifecycleOwner, Observer {
            updateStatus(it)
        })

        with(binding) {
            button7D.setOnClickListener { viewModel.on7DaysClicked() }
            button1M.setOnClickListener { viewModel.on1MonthClicked() }
            button1A.setOnClickListener { viewModel.on1YearClicked() }
            button5A.setOnClickListener { viewModel.on5YearsClicked() }
            buttonMax.setOnClickListener { viewModel.onMaxDaysClicked() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CHOSEN_PERIOD_KEY, binding.toggleGroup.checkedButtonId)
    }

    private fun updateStatus(it: GraphicStatus) {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        return when (it) {
            is GraphicStatus.Loading -> {
                showViews(binding.progressBar)
                hideViews(binding.tvErrorMsg, binding.chart, binding.toggleGroup)
            }
            is GraphicStatus.Error -> {
                val viewsToShow = mutableListOf<View>(binding.tvErrorMsg)
                val viewsToHide = mutableListOf<View>(binding.progressBar, binding.chart)

                if (it.showPeriodButtons) viewsToShow.add(binding.toggleGroup)
                else viewsToHide.add(binding.toggleGroup)

                showViews(*viewsToShow.toTypedArray())
                hideViews(*viewsToHide.toTypedArray())
                binding.tvErrorMsg.text = it.errorMsg
            }
            is GraphicStatus.ShowingData -> {
                showViews(binding.chart, binding.toggleGroup)
                hideViews(binding.progressBar, binding.tvErrorMsg)
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
        with(binding.chart) {
            data = lineData
            highlightValues(null)
            fitScreen()
            animateX(ANIMATION_DURATION_MS)
        }
    }

    private fun configGraph() {
        with(binding.chart) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
