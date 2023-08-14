package ionut.andras.community.dexcomrelated.followerfordexcom.alarms

import android.util.Log
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.Configuration
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomTrendsConversionMap
import ionut.andras.community.dexcomrelated.followerfordexcom.notifications.GlucoseNotificationData

class DexcomAlarmManager(private var configuration: Configuration) {

    /**
     * Process glucose value and trigger alarms if needed
     * - If glucose value under minimum threshold, trigger low alarm
     * - If glucose value under minimum threshold with 20%, trigger urgent low alarm
     * - If glucose value over maximum threshold trigger high glucose alarm
     * - If glucose value raising fast trigger rising fast alarm
     * - If glucose value dropping fast trigger dropping fast alarm
     */
    fun getNotificationAlarmType(glucoseNotificationData: GlucoseNotificationData): String {
        val glucoseValue = glucoseNotificationData.glucoseValue.toInt()

        val glucoseRecentHistory = glucoseNotificationData.glucoseRecentHistory
        val trend = if (
                (glucoseRecentHistory[0] - glucoseRecentHistory[1] >= configuration.glucoseRisingDroppingHighThreshold)
                && (glucoseRecentHistory[1] - glucoseRecentHistory[2] >= configuration.glucoseRisingDroppingHighThreshold)
            ) {
                DexcomTrendsConversionMap.DOUBLE_UP
            } else if (
                (glucoseRecentHistory[0] - glucoseRecentHistory[1] <= -configuration.glucoseRisingDroppingHighThreshold)
                && (glucoseRecentHistory[1] - glucoseRecentHistory[2] <= -configuration.glucoseRisingDroppingHighThreshold)
            ) {
            DexcomTrendsConversionMap.DOUBLE_DOWN
        } else {
            DexcomTrendsConversionMap.FLAT
        }

        val returnValue = when (glucoseValue) {
            in 0..configuration.glucoseUrgentLowThreshold -> triggerUrgentLowAlarm()
            in  configuration.glucoseUrgentLowThreshold..configuration.glucoseLowThreshold -> triggerLowAlarm()
            in configuration.glucoseHighThreshold..configuration.maxDisplayableGlucoseValue -> triggerHighAlarm()
            else -> {
                if (DexcomTrendsConversionMap.DOUBLE_UP == DexcomTrendsConversionMap.convert[trend]) {
                    triggerRiseFastAlarm()
                } else if (DexcomTrendsConversionMap.DOUBLE_DOWN == DexcomTrendsConversionMap.convert[trend]) {
                    triggerDropFastAlarm()
                } else {
                    triggerNoAlarm()
                }
            }
        }

        return returnValue
    }

    private fun triggerUrgentLowAlarm(): String {
        Log.i("DexcomAlarmManager", "triggerUrgentLowAlarm")
        return DexcomAlarmType.URGENT_LOW
    }

    private fun triggerLowAlarm(): String {
        Log.i("DexcomAlarmManager", "triggerLowAlarm")
        return DexcomAlarmType.LOW
    }

    private fun triggerHighAlarm(): String {
        Log.i("DexcomAlarmManager", "triggerHighAlarm")
        return DexcomAlarmType.HIGH
    }

    private fun triggerRiseFastAlarm(): String {
        Log.i("DexcomAlarmManager", "triggerRiseFastAlarm")
        return DexcomAlarmType.RISING_FAST
    }

    private fun triggerDropFastAlarm(): String {
        Log.i("DexcomAlarmManager", "triggerDropFastAlarm")
        return DexcomAlarmType.DROPPING_FAST
    }

    private fun triggerNoAlarm(): String {
        Log.i("DexcomAlarmManager", "triggerNoAlarm")
        return DexcomAlarmType.NORMAL
    }
}