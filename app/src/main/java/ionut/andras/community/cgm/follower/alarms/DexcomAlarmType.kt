package ionut.andras.community.cgm.follower.alarms

import androidx.annotation.Keep
import ionut.andras.community.cgm.follower.constants.DexcomTrendsConversionMap
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

        // Dummy conversion to make the companion variables usable in the class and do not allow code optimization on them when minifying
        private val convert: MutableMap<String, String> = mutableMapOf(
            URGENT_LOW to URGENT_LOW,
            LOW to LOW,
            HIGH to HIGH,
            RISING_FAST to RISING_FAST,
            DROPPING_FAST to DROPPING_FAST,
            NORMAL to NORMAL
        )

        fun getValues() : List<String> {
            return convert.map {
                it.value
            }
        }
        fun getNormalizedValues() : List<String> {
            return convert.map {
                normalizeValue(it.value)
            }
        }

        fun normalizeValue(alarmType: String): String {
            return alarmType.lowercase().replace(" ", "_")
        }

        fun isAlarmType(alarmType: String): Boolean {
            return convert.any {
                it.value == alarmType
            }
        }
    }
}