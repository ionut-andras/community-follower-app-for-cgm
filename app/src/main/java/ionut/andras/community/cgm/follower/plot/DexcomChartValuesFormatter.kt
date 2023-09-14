package ionut.andras.community.cgm.follower.plot

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import kotlin.math.floor

class DexcomChartValuesFormatter: ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return convertTimestampInAxisLabel(value)
    }

    fun convertTimestampInAxisLabel(value: Float): String {
        // Get timestamp in seconds
        val timestampInSeconds = value // (value / 1000).toFloat()
        // Get the hour
        // Hour = timestamp / 3600 modulus 24
        val hour = (timestampInSeconds/3600).toInt().mod(24)
        // Get minutes of the hour
        val minute = floor((timestampInSeconds/60).toDouble()).mod(60f).toInt()
        // Log.i("value - hour:minute - ", "$timestampInSeconds - $hour:$minute")

        return hour.toString().padStart(2, '0')
            .plus(":")
            .plus(minute.toString().padStart(2, '0'))
    }

    fun formatTimeForChartDisplay(timestampValue: Long): Float {
        return timestampValue.toDouble().mod(DexcomConstants().maxDisplayIntervalSeconds.toDouble()).toFloat()
    }

    fun formatTimeForChartDisplay(timestampValue: Float): Float {
        return timestampValue.mod(DexcomConstants().maxDisplayIntervalSeconds.toDouble()).toFloat()
    }
}