package ionut.andras.community.dexcomrelated.followerfordexcom.alarms

import android.util.Log
import ionut.andras.community.dexcomrelated.followerfordexcom.R
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
    fun getNotificationAlarmSound(glucoseNotificationData: GlucoseNotificationData): Int {
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
                if (
                    DexcomTrendsConversionMap.DOUBLE_DOWN == DexcomTrendsConversionMap.convert[trend]
                    || DexcomTrendsConversionMap.DOUBLE_UP == DexcomTrendsConversionMap.convert[trend]
                ) {
                    triggerRiseDropFastAlarm()
                } else {
                    triggerNoAlarm()
                }
            }
        }

        return returnValue
    }

    private fun triggerUrgentLowAlarm(): Int {
        Log.i("DexcomAlarmManager", "triggerUrgentLowAlarm")
        return R.raw.glucose_urgent_low
    }

    private fun triggerLowAlarm(): Int {
        Log.i("DexcomAlarmManager", "triggerLowAlarm")
        return R.raw.glucose_low
    }

    private fun triggerHighAlarm(): Int {
        Log.i("DexcomAlarmManager", "triggerHighAlarm")
        return R.raw.glucose_high
    }

    private fun triggerRiseDropFastAlarm(): Int {
        Log.i("DexcomAlarmManager", "triggerRiseDropFastAlarm")
        return R.raw.glucose_risedrop_fast
    }

    private fun triggerNoAlarm(): Int {
        Log.i("DexcomAlarmManager", "triggerNoAlarm")
        return 0
    }
}