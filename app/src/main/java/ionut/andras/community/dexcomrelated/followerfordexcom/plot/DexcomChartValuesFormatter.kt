package ionut.andras.community.dexcomrelated.followerfordexcom.plot

import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomConstants
import kotlin.math.floor

class DexcomChartValuesFormatter: ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return convertTimestampInAxisLabel(value)
    }

    fun convertTimestampInAxisLabel(value: Float): String {
        // Get timestamp in seconds
        var timestampInSeconds = value // (value / 1000).toFloat()
        // Get the hour
        // Hour = timestamp / 3600 modulus 24
        var hour = (timestampInSeconds/3600).toInt().mod(24)
        // Get minutes of the hour
        var minute = floor((timestampInSeconds/60).toDouble()).mod(60f).toInt()
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