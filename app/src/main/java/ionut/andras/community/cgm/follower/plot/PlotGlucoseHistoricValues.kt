package ionut.andras.community.cgm.follower.plot

import android.content.Context
import android.graphics.Color.TRANSPARENT
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.utils.GlucoseValueColorRange
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.utils.DateTimeConversion
import ionut.andras.community.cgm.follower.utils.DexcomDate
import org.json.JSONArray


class PlotGlucoseHistoricValues(
    private var configuration: Configuration,
    var data: JSONArray,
    nowEpochSeconds: Long = DateTimeConversion().getCurrentTimestamp(),
) {
    private val timeline: GlucoseChartTimeline
    // The chart library needs the entries sorted by X (oldest first)
    private val entries: ArrayList<Entry>
    private val entriesColors: ArrayList<Int>

    init {
        val readings = (0 until data.length()).map { data.getJSONObject(it) }
        timeline = GlucoseChartTimeline(
            readings.map { DexcomDate.parse(it.getString("DT")) },
            nowEpochSeconds,
        )

        // Dexcom lists the newest reading first
        entries = ArrayList(
            readings.indices
                .map { i -> Entry(timeline.xValues[i], readings[i].getDouble("Value").toFloat()) }
                .sortedBy { it.x }
        )
        entriesColors = ArrayList(
            entries.map { GlucoseValueColorRange(configuration).getGlucoseColorValue(it.y.toInt()) }
        )
    }

    fun start(applicationContext: Context, glucoseHistoricChart: LineChart, glucosePlotMarkerViewLayout: Int) {
        glucoseHistoricChart.highlightValue(null)
        val lineDataSet = LineDataSet(entries, "")
        val lowValuesAreaLimit = LimitLine(configuration.glucoseLowThreshold.toFloat())
        val normalValuesAreaLimit = LimitLine(configuration.glucoseHighThreshold.toFloat())

        lowValuesAreaLimit.lineColor = ContextCompat.getColor(applicationContext, R.color.red)
        lowValuesAreaLimit.lineWidth *= 1.5F
        normalValuesAreaLimit.lineColor = ContextCompat.getColor(applicationContext, R.color.orange)
        normalValuesAreaLimit.lineWidth *= 1.5F

        with(lineDataSet) {
            color = TRANSPARENT
            valueTextColor = TRANSPARENT
            circleColors = entriesColors
            setDrawCircleHole(false)
            setDrawFilled(false)
            //fillColor = ContextCompat.getColor(applicationContext, R.color.green)
        }

        with(glucoseHistoricChart) {
            data = LineData(lineDataSet)
            description.text = ""
            setNoDataText("No data yet!")

            setDrawGridBackground(true)
            setGridBackgroundColor(TRANSPARENT)

            marker = GlucoseMarkerView(applicationContext, glucosePlotMarkerViewLayout)
            (marker as GlucoseMarkerView).setConfigurationObject(configuration)
        }

        // X-Axis
        with (glucoseHistoricChart.xAxis) {
            position = XAxis.XAxisPosition.BOTTOM
            setAvoidFirstLastClipping(true)
            setDrawAxisLine(true)
            setDrawGridLines(false)
            setDrawGridLinesBehindData(true)
            setDrawLabels(true)
            setLabelCount(configuration.plotLabelsCount, true)
            textColor = ContextCompat.getColor(applicationContext, R.color.orange)
            valueFormatter = DexcomChartValuesFormatter()

            axisMinimum = timeline.axisMinimum
            axisMaximum = timeline.axisMaximum
        }

        // Y-Axis - Left
        with (glucoseHistoricChart.axisLeft) {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawZeroLine(true)
            setDrawGridLines(false)
            setDrawGridLinesBehindData(true)
            setDrawLimitLinesBehindData(true)
            axisMinimum =
                configuration.minDisplayableGlucoseValue.toFloat()
            axisMaximum =
                configuration.maxDisplayableGlucoseValue.toFloat()
        }
        // Y-Axis - Right
        with (glucoseHistoricChart.axisRight) {
            axisMinimum =
                configuration.minDisplayableGlucoseValue.toFloat()
            axisMaximum =
                configuration.maxDisplayableGlucoseValue.toFloat()
            setDrawAxisLine(true)
            setDrawGridLines(false)
            setDrawGridLinesBehindData(true)
            setDrawLimitLinesBehindData(true)
            textColor = ContextCompat.getColor(applicationContext, R.color.orange)

            removeAllLimitLines()
            addLimitLine(lowValuesAreaLimit)
            addLimitLine(normalValuesAreaLimit)
        }

        // The axis limits changed after setting the data: recompute the value-to-pixel mapping,
        // otherwise the chart is drawn with the previous refresh's limits.
        glucoseHistoricChart.notifyDataSetChanged()
        glucoseHistoricChart.invalidate()
    }
}
