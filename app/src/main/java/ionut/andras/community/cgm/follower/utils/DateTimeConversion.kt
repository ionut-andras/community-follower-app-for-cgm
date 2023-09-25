package ionut.andras.community.cgm.follower.utils

import android.icu.util.Calendar

open class DateTimeConversion (dateTimeObject: DateTimeObject = DateTimeObject()) {
    private val dateTimeObject: DateTimeObject

    init {
        this.dateTimeObject = dateTimeObject
    }

    /**
     * Gets the current timestamp in seconds, from epoch start (1970)
     *
     * @return Long
     */
    fun getCurrentTimestamp(): Long {
        return  (System.currentTimeMillis() / 1000)
    }

    /**
     * Converts a pair of Date / Time into a unix timestamp of your local time zone.
     * @return Long
     */
    fun getLocalTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(dateTimeObject.year, dateTimeObject.month, dateTimeObject.day, dateTimeObject.hour, dateTimeObject.minute, dateTimeObject.second)
        return (calendar.timeInMillis / 1000)
    }
}