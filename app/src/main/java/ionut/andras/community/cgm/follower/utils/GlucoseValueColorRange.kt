package ionut.andras.community.cgm.follower.utils

import android.graphics.Color
import ionut.andras.community.cgm.follower.configuration.Configuration

class GlucoseValueColorRange(var configuration: Configuration) {

    /**
     * Get glucose color which represents the grade of glucose value
     * - Normal
     * - High
     * - Low
     */
    fun getGlucoseColorValue (glucoseValue: Int): Int {

        val color = if (glucoseValue > configuration.glucoseHighThreshold) {
            // High value
            Color.rgb(255, 165, 0)
        } else if (glucoseValue < configuration.glucoseLowThreshold) {
            // Low value
            Color.RED
        } else {
            // Normal Value
            Color.GREEN
        }

        return color
    }
}