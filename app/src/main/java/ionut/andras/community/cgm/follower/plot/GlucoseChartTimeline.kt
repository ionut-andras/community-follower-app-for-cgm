package ionut.andras.community.cgm.follower.plot

import ionut.andras.community.cgm.follower.utils.DexcomDate

/**
 * Places glucose readings on the chart's X axis.
 *
 * An X value is a local time in seconds, counted from the midnight that starts the oldest
 * reading's day. Values keep growing across midnight (00:05 today lands after 23:55 yesterday),
 * and stay small enough to be exact in a Float. DexcomChartValuesFormatter turns them back into
 * "HH:mm" with hour % 24.
 *
 * Every point uses the newest reading's UTC offset, so a daylight saving change inside the
 * window cannot make the axis run backwards.
 */
class GlucoseChartTimeline(
    readings: List<DexcomDate>,
    nowEpochSeconds: Long,
    marginSeconds: Int = 60,
) {
    /** X value of each reading, in the same order as the readings passed in. */
    val xValues: List<Float>
    val axisMinimum: Float
    val axisMaximum: Float

    init {
        require(readings.isNotEmpty()) { "At least one reading is needed to build the timeline" }

        val offsetSeconds = readings.maxBy { it.epochSeconds }.utcOffsetSeconds
        val oldestLocalSeconds = readings.minOf { it.epochSeconds } + offsetSeconds
        val originLocalSeconds = Math.floorDiv(oldestLocalSeconds, SECONDS_PER_DAY) * SECONDS_PER_DAY

        fun toX(epochSeconds: Long): Float =
            (epochSeconds + offsetSeconds - originLocalSeconds).toFloat()

        xValues = readings.map { toX(it.epochSeconds) }

        // Keep "now" on screen, but never let a slow device clock put the end before the data
        axisMinimum = xValues.min() - marginSeconds
        axisMaximum = maxOf(xValues.max(), toX(nowEpochSeconds)) + marginSeconds
    }

    companion object {
        private const val SECONDS_PER_DAY = 24L * 3600L
    }
}
