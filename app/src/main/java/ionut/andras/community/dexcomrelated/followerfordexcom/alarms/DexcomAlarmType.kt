package ionut.andras.community.dexcomrelated.followerfordexcom.alarms

import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberProperties

open class DexcomAlarmType {
    companion object {
        const val URGENT_LOW: String = "Urgent Low"
        const val LOW: String = "Low"
        const val HIGH: String = "High"
        const val RISING_FAST: String = "Rising fast"
        const val DROPPING_FAST: String = "Dropping fast"
        const val NORMAL: String = "Normal"

        fun getValues() : List<String> {
            return DexcomAlarmType::class.companionObject?.memberProperties?.map {
                it.getter.call(null) as String
            } ?: mutableListOf()
        }
        fun getNormalizedValues() : List<String> {
            return DexcomAlarmType::class.companionObject?.memberProperties?.map {
                normalizeValue(it.getter.call(null) as String)
            } ?: mutableListOf()
        }

        fun normalizeValue(alarmType: String): String {
            return alarmType.lowercase().replace(" ", "_")
        }

        fun isAlarmType(alarmType: String): Boolean {
            return DexcomAlarmType::class.companionObject?.memberProperties?.any {
                it.getter.call(null) == alarmType
            } ?: false
        }
    }
}