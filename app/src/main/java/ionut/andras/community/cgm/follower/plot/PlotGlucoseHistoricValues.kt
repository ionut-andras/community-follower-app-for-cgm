package ionut.andras.community.cgm.follower.plot

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.common.GlucoseValueColorRange
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import ionut.andras.community.cgm.follower.utils.DateTimeConversion
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneOffset


class PlotGlucoseHistoricValues(private var configuration: Configuration, var data: JSONArray) {
    private var entriesArray: ArrayList<Entry> = ArrayList()
    private var entriesArrayColor: ArrayList<Int> = ArrayList()
    private var timeZoneOffsetSeconds: Double = 0.0
    private val graphicXOffsetMinutes = 1
    private var entriesIntervalSpanMultipleDays = false
    private var canRunNormalization = true

    init {
        entriesIntervalSpanMultipleDays = isDataIntervalSpanningMultipleDays(data)
        canRunNormalization = true

        val preparedData = prepareChartValues(data)
        Log.i("preparedData", preparedData.toString())

        val listOfKeys = arrayListOf<Float>()
        for (i in 0 until preparedData.length()){
            val obj: JSONObject = preparedData[i] as JSONObject
            // var hour = (obj.get("date") as PlotEntryDataModel).hour.toFloat()
            // var minute = (obj.get("date") as PlotEntryDataModel).minute.toFloat()
            val value = obj.getDouble("value").toFloat()

            timeZoneOffsetSeconds = (obj.get("date") as PlotEntryDataModel).timeZoneOffset.totalSeconds.toDouble()

            // var keyForValue = hour * 60 + minute
            val rawKeyForValue = DexcomChartValuesFormatter().formatTimeForChartDisplay((obj.get("date") as PlotEntryDataModel).timestamp + timeZoneOffsetSeconds.toLong())
            val keyForValue = normalizeTimestamp(listOfKeys, rawKeyForValue)
            listOfKeys.add(rawKeyForValue)

            // Prepare entries
            entriesArray.add(Entry(keyForValue, value))
            // Prepare entries color
            entriesArrayColor.add(GlucoseValueColorRange(configuration).getGlucoseColorValue(value.toInt()))
        }
        Log.i("entriesArray: ", entriesArray.toString())

    }

    fun start(applicationContext: Context, glucoseHistoricChart: LineChart, glucosePlotMarkerViewLayout: Int) {
        glucoseHistoricChart.highlightValue(null)
        // The list of measured values comes in the revers order, so they need to be reversed. If not reversed, somehow the graphic module fails
        val lineDataSet = LineDataSet(entriesArray.reversed(), "")
        val lowValuesAreaLimit = LimitLine(configuration.glucoseLowThreshold.toFloat())
        val normalValuesAreaLimit = LimitLine(configuration.glucoseHighThreshold.toFloat())

        lowValuesAreaLimit.lineColor = ContextCompat.getColor(applicationContext, R.color.red)
        lowValuesAreaLimit.lineWidth *= 1.5F
        normalValuesAreaLimit.lineColor = ContextCompat.getColor(applicationContext, R.color.orange)
        normalValuesAreaLimit.lineWidth *= 1.5F

        with(lineDataSet) {
            color = TRANSPARENT
            valueTextColor = TRANSPARENT
            // Reverse the colors to match reversed entries
            circleColors = entriesArrayColor.reversed()
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
        val xAxisMinimum =  + DexcomChartValuesFormatter().formatTimeForChartDisplay(
            entriesArray.reversed()[0].x
        )

        var xAxisMaximum = DexcomChartValuesFormatter().formatTimeForChartDisplay(
            DateTimeConversion().getCurrentTimestamp() + timeZoneOffsetSeconds.toLong()
        )
        // Seems the graphics engine does not actually get values from dataset in order to plot the
        // labels, but instead makes even slices from min and max values
        // For this reason, max value must always be greater than min value after trimming
        if (entriesIntervalSpanMultipleDays) {
            xAxisMaximum += DexcomConstants().maxDisplayIntervalSeconds
        }

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

            axisMinimum = (-graphicXOffsetMinutes * 60) + xAxisMinimum
            axisMaximum = (graphicXOffsetMinutes * 60) + xAxisMaximum
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

        glucoseHistoricChart.invalidate()
    }

    private fun isDataIntervalSpanningMultipleDays(glucoseData: JSONArray): Boolean {
        val firstElement = glucoseData.getJSONObject(0)
        val lastElement = glucoseData.getJSONObject(glucoseData.length() - 1)
        val firstDatePrepared = processDexcomMetricDate(firstElement.getString("DT"))
        val lastDatePrepared = processDexcomMetricDate(lastElement.getString("DT"))

        val firstDate = DexcomChartValuesFormatter().formatTimeForChartDisplay(
            firstDatePrepared.timestamp + firstDatePrepared.timeZoneOffset.totalSeconds
        )
        val lastDate = DexcomChartValuesFormatter().formatTimeForChartDisplay(
            lastDatePrepared.timestamp + lastDatePrepared.timeZoneOffset.totalSeconds
        )
        // Log.i("isDataIntervalSpanningMultipleDays", "$firstDate<$lastDate")
        return (firstDate < lastDate)
    }

    private fun prepareChartValues(glucoseData: JSONArray): JSONArray {
        val processedData = JSONArray()
        // Glucose chart values
        for (i in 0 until glucoseData.length()) {
            // Get current measured value
            val glucoseDetails = glucoseData.getJSONObject(i)
            // Log.i("glucoseDetails", glucoseDetails.toString())

            // Process the date for the current metric
            val date = processDexcomMetricDate(glucoseDetails.getString("DT"))

            // Prepare the simplified object that will replace this in the chart
            val processedGlucoseDetails = JSONObject()
            processedGlucoseDetails.put("date", date)
            processedGlucoseDetails.put("value", glucoseDetails.getString("Value"))

            // Add the corresponding value in the processed data array
            processedData.put(processedGlucoseDetails)
        }

        // Log.i("processedData", processedData.toString())

        return processedData
    }

    private fun processDexcomMetricDate(unformattedDateTime: String): PlotEntryDataModel {
        val returnValue = PlotEntryDataModel()

        val (unixTimestamp, gmtOffset) = unformattedDateTime.split("(")[1].trim(')').split("+")
        // Log.i("unixTimestamp", unixTimestamp)
        // Log.i("gmtOffset", gmtOffset)

        val zoneOffset: ZoneOffset = ZoneOffset.ofHoursMinutes(
            Integer.parseInt(gmtOffset.substring(IntRange(0,1))),
            Integer.parseInt(gmtOffset.substring(IntRange(2,3)))
        )
        val epochSeconds = unixTimestamp.toLong() / 1000
        val localDateTime = LocalDateTime.ofEpochSecond(epochSeconds, 0, zoneOffset)
        // Log.i("localDateTime", localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

        returnValue.dateTime = localDateTime.toString()
        returnValue.timestamp = epochSeconds //unixTimestamp.toLong()
        returnValue.hour = localDateTime.hour.toString()
        returnValue.minute = localDateTime.minute.toString()
        returnValue.timeZoneOffset = zoneOffset

        return returnValue
    }

    private fun normalizeTimestamp(listOfKeys: ArrayList<Float>, currentKey: Float): Float {
        // Because we float is not able to hold timestamps correctly, we need to convert it inti smaller numbers
        // In order to do that, we MOD by 1 day (86400 seconds) and we work with Jan 1st 1970.
        // However, when we are reaching end of the day, the MODed timestamps in precedent day are bigger than the
        // MODed timestamps in the current day, causing display errors.
        // In order to avoid that we normalize the smaller timestamp by adding 1 day in seconds
        val prevKey: Float
        var ret = currentKey
        if (canRunNormalization) {
            if (0 < listOfKeys.size) {
                prevKey = listOfKeys[listOfKeys.lastIndex]
                // NOTE: The values are coming in reverse order,
                // so the first one is the biggest and the next ones are lower timestamps
                if (entriesIntervalSpanMultipleDays && (prevKey > currentKey)) { // If value is from the current day
                    with (DexcomConstants()) {
                        // Make it greater than the previous day
                        // - Subtract 1 day in seconds and add 5 minutes as this the last step is not yet completed
                        ret += maxDisplayIntervalSeconds.toFloat() - stepDisplayIntervalSeconds.toFloat()
                    }
                } else if (prevKey < currentKey) {
                    canRunNormalization = false
                }
            } else {
                if (entriesIntervalSpanMultipleDays) {
                    with(DexcomConstants()) {
                        // Make it greater than the previous day
                        // - Subtract 1 day in seconds and add 5 minutes as this the last step is not yet completed
                        ret += maxDisplayIntervalSeconds.toFloat() - stepDisplayIntervalSeconds.toFloat()
                    }
                }
            }
            // Log.i("normalizeTimestamp", "prevKey: $prevKey -- currentKey: $currentKey -- ret: $ret")
        }
        return ret
    }
}