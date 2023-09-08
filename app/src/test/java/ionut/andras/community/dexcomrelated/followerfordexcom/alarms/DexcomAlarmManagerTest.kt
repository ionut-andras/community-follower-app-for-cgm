package ionut.andras.community.dexcomrelated.followerfordexcom.alarms

import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.Configuration
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomTrendsConversionMap
import ionut.andras.community.dexcomrelated.followerfordexcom.notifications.GlucoseNotificationData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DexcomAlarmManagerTest{
    private lateinit var SUT: DexcomAlarmManager

    @BeforeEach
    fun setUp() {
        SUT = DexcomAlarmManager(Configuration())
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getNotificationAlarmType() {
        var glucoseValue = 60
        var glucoseTrend = DexcomTrendsConversionMap.FLAT
        var trendSign = DexcomTrendsConversionMap.convert[glucoseTrend]
        var glucoseNotificationData = GlucoseNotificationData(glucoseValue.toString(), trendSign!!.toString(), "0")
        assert(DexcomAlarmType.URGENT_LOW === SUT.getNotificationAlarmType(glucoseNotificationData))

        glucoseValue = 80
        glucoseNotificationData = GlucoseNotificationData(glucoseValue.toString(), trendSign.toString(), "0")
        assert(DexcomAlarmType.LOW === SUT.getNotificationAlarmType(glucoseNotificationData))

        glucoseValue = 100
        glucoseNotificationData = GlucoseNotificationData(glucoseValue.toString(), trendSign.toString(), "0")
        assert(DexcomAlarmType.NORMAL === SUT.getNotificationAlarmType(glucoseNotificationData))

        glucoseValue = 190
        glucoseNotificationData = GlucoseNotificationData(glucoseValue.toString(), trendSign.toString(), "0")
        assert(DexcomAlarmType.HIGH === SUT.getNotificationAlarmType(glucoseNotificationData))

        glucoseValue = 120
        glucoseTrend = DexcomTrendsConversionMap.DOUBLE_DOWN
        trendSign = DexcomTrendsConversionMap.convert[glucoseTrend]
        glucoseNotificationData = GlucoseNotificationData(glucoseValue.toString(), trendSign!!.toString(), "0")
        assert(DexcomAlarmType.DROPPING_FAST === SUT.getNotificationAlarmType(glucoseNotificationData))

        glucoseValue = 200
        glucoseTrend = DexcomTrendsConversionMap.DOUBLE_UP
        trendSign = DexcomTrendsConversionMap.convert[glucoseTrend]
        glucoseNotificationData = GlucoseNotificationData(glucoseValue.toString(), trendSign!!.toString(), "0")
        assert(DexcomAlarmType.RISING_FAST === SUT.getNotificationAlarmType(glucoseNotificationData))

    }
}