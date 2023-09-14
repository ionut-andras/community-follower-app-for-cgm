package ionut.andras.community.cgm.follower.utils

import ionut.andras.community.cgm.follower.constants.DexcomConstants
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DexcomDateTimeConversion: DateTimeConversion() {

    /**
     * Gets the time offset from current date as received from Dexcom
     * E.g: (1663163157000+0300).
     * Function gets the Timestamp in miliseconds and converts in time offset as "now" or "Xm ago"
     *
     * @param unformattedDateTime String
     * @return String
     */
    fun getTimeOffsetFromCurrentDate(unformattedDateTime: String): String {
        val returnValue: String

        // Convert
        // Log.i("unformattedDateTime", unformattedDateTime)
        val (unixTimestamp, _) = unformattedDateTime.split("(")[1].trim(')').split("+")

        val currentTimestamp = getCurrentTimestamp()

        val minutesAgo = ((currentTimestamp - (unixTimestamp.toLong() / 1000)) / 60).toInt()

        returnValue = if (0 >= minutesAgo) {
            DexcomConstants().messageNow
        } else {
            minutesAgo.toString().plus(DexcomConstants().messageMinutesAgo)
        }

        return returnValue
    }

    /**
     * Gets the current time hour and minute in format hh:mm
     *
     * @return String
     */
    fun getLocalHourMinuteOfCurrentTime(): String {
        val currentTimestamp = getCurrentTimestamp()

        val zoneOffset = ZoneOffset.of(
            ZonedDateTime.now(
                ZoneId.systemDefault()
            ).toOffsetDateTime().offset.toString()
        )

        val hour = LocalDateTime.ofEpochSecond(currentTimestamp, 0, zoneOffset).hour.toString().padStart(2, '0')
        val minute = LocalDateTime.ofEpochSecond(currentTimestamp, 0, zoneOffset).minute.toString().padStart(2, '0')

        return "$hour:$minute"
    }
}