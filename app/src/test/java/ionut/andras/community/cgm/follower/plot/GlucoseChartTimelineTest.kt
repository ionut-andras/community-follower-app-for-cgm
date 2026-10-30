package ionut.andras.community.cgm.follower.plot

import ionut.andras.community.cgm.follower.utils.DexcomDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class GlucoseChartTimelineTest {
    private val formatter = DexcomChartValuesFormatter()

    private fun epoch(local: String, offset: ZoneOffset): Long =
        LocalDateTime.parse(local).toEpochSecond(offset)

    /** Readings every 5 minutes from [oldestLocal] up to [count] readings, newest first like Dexcom. */
    private fun readings(oldestLocal: String, count: Int, offset: ZoneOffset): List<DexcomDate> {
        val oldest = epoch(oldestLocal, offset)
        return (0 until count).map { DexcomDate(oldest + it * 300L, offset.totalSeconds) }.reversed()
    }

    private fun label(x: Float) = formatter.convertTimestampInAxisLabel(x)

    private fun assertIncreasingOldestFirst(timeline: GlucoseChartTimeline) {
        val oldestFirst = timeline.xValues.reversed()
        oldestFirst.zipWithNext().forEach { (previous, next) ->
            assertTrue(next > previous, "X values must keep increasing: $previous then $next")
        }
    }

    @Test
    fun nowAfterMidnightWithLastReadingBeforeMidnight() {
        // The case that drew an axis from 21:02 to 18:02 with a single dot
        val offset = ZoneOffset.ofHours(2)
        val data = readings("2026-10-30T21:04:00", 36, offset) // 21:04 .. 23:59
        val timeline = GlucoseChartTimeline(data, epoch("2026-10-31T00:02:00", offset))

        assertTrue(timeline.axisMinimum < timeline.axisMaximum)
        assertEquals("21:03", label(timeline.axisMinimum))
        assertEquals("00:03", label(timeline.axisMaximum))
        assertTrue(timeline.axisMaximum - timeline.axisMinimum < 4 * 3600)
        assertEquals("23:59", label(timeline.xValues.first()))
        assertIncreasingOldestFirst(timeline)
        timeline.xValues.forEach { assertTrue(it in timeline.axisMinimum..timeline.axisMaximum) }
    }

    @Test
    fun readingsAcrossMidnightKeepTheirTimeAndOrder() {
        // Before, readings after midnight were shifted 5 minutes back onto the previous day's points
        val offset = ZoneOffset.ofHours(2)
        val data = readings("2026-10-30T21:40:00", 36, offset) // 21:40 .. 00:35
        val timeline = GlucoseChartTimeline(data, epoch("2026-10-31T00:37:00", offset))

        assertIncreasingOldestFirst(timeline)
        val labels = timeline.xValues.reversed().map { label(it) }
        assertEquals("23:55", labels[27])
        assertEquals("00:00", labels[28])
        assertEquals("00:05", labels[29])
        assertEquals("00:35", labels.last())
        assertEquals("00:38", label(timeline.axisMaximum))
    }

    @Test
    fun midnightReadingExactlyAtZero() {
        val offset = ZoneOffset.ofHours(3)
        val data = readings("2026-07-01T23:50:00", 3, offset) // 23:50, 23:55, 00:00
        val timeline = GlucoseChartTimeline(data, epoch("2026-07-02T00:01:00", offset))

        assertIncreasingOldestFirst(timeline)
        assertEquals("00:00", label(timeline.xValues.first()))
    }

    @Test
    fun fullDayWindow() {
        val offset = ZoneOffset.ofHours(2)
        val data = readings("2026-10-29T22:40:00", 288, offset) // 24 hours
        val timeline = GlucoseChartTimeline(data, epoch("2026-10-30T22:37:00", offset))

        assertIncreasingOldestFirst(timeline)
        assertEquals("22:35", label(timeline.xValues.first()))
        assertTrue(timeline.axisMaximum - timeline.axisMinimum <= 24 * 3600 + 120)
    }

    @Test
    fun daylightSavingChangeDoesNotFoldTheAxis() {
        // Bucharest leaves summer time on 2026-10-25 at 04:00 (+03:00) -> 03:00 (+02:00)
        val summer = ZoneOffset.ofHours(3)
        val winter = ZoneOffset.ofHours(2)
        val change = epoch("2026-10-25T04:00:00", summer)
        val data = (-12 until 12).map { i ->
            val t = change + i * 300L
            DexcomDate(t, if (t < change) summer.totalSeconds else winter.totalSeconds)
        }.reversed()
        val timeline = GlucoseChartTimeline(data, change + 12 * 300L)

        assertIncreasingOldestFirst(timeline)
        // Everything is shown on the current (winter) clock
        assertEquals("03:55", label(timeline.xValues.first()))
    }

    @Test
    fun negativeOffsetAcrossMidnight() {
        val offset = ZoneOffset.ofHours(-5)
        val data = readings("2026-10-30T23:00:00", 24, offset) // 23:00 .. 00:55
        val timeline = GlucoseChartTimeline(data, epoch("2026-10-31T00:57:00", offset))

        assertIncreasingOldestFirst(timeline)
        assertEquals("23:00", label(timeline.xValues.last()))
        assertEquals("00:55", label(timeline.xValues.first()))
    }

    @Test
    fun slowDeviceClockStillShowsAllReadings() {
        val offset = ZoneOffset.ofHours(2)
        val data = readings("2026-10-30T21:00:00", 12, offset)
        val timeline = GlucoseChartTimeline(data, epoch("2026-10-30T20:00:00", offset))

        timeline.xValues.forEach { assertTrue(it in timeline.axisMinimum..timeline.axisMaximum) }
    }
}
