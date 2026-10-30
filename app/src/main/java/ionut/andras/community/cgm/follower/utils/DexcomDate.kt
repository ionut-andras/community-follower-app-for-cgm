package ionut.andras.community.cgm.follower.utils

/**
 * A Dexcom Share date such as "Date(1663163157000+0300)" or "Date(1663163157000-0500)":
 * the reading time in epoch milliseconds plus the UTC offset of the device that recorded it.
 */
data class DexcomDate(val epochSeconds: Long, val utcOffsetSeconds: Int) {

    companion object {
        private val pattern = Regex("""\((-?\d+)(?:([+-])(\d{2})(\d{2}))?\)""")

        fun parse(value: String): DexcomDate {
            val match = pattern.find(value)
                ?: throw IllegalArgumentException("Unexpected Dexcom date: $value")
            val (millis, sign, hours, minutes) = match.destructured
            val offsetSeconds = if (sign.isEmpty()) {
                0
            } else {
                val absolute = hours.toInt() * 3600 + minutes.toInt() * 60
                if (sign == "-") -absolute else absolute
            }
            return DexcomDate(millis.toLong() / 1000, offsetSeconds)
        }
    }
}
