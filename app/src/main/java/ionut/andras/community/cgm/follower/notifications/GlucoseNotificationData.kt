package ionut.andras.community.cgm.follower.notifications

import android.content.Context
import android.graphics.Bitmap
import android.text.Html
import androidx.core.graphics.drawable.IconCompat
import ionut.andras.community.dexcomrelated.followerfordexcom.R
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.utils.BitmapConversion
import ionut.andras.community.cgm.follower.utils.DexcomDateTimeConversion

data class GlucoseNotificationData (var glucoseValue: String, var glucoseValueTrend: String, var timeOffset: String, var glucoseRecentHistory: ArrayList<Int> = arrayListOf()) {
    fun toIcon(): IconCompat {
        // Convert text to bitmap
        val trendAsHtmlSymbol = Html.fromHtml(glucoseValueTrend, Html.FROM_HTML_MODE_COMPACT).toString()
        val bitmap: Bitmap = BitmapConversion().createBitmapFromString(trendAsHtmlSymbol)
        val icon: IconCompat = IconCompat.createWithBitmap(bitmap)
        return icon
    }

    fun toNotificationTitle(): String {
        val trendAsHtmlSymbol = Html.fromHtml(glucoseValueTrend, Html.FROM_HTML_MODE_COMPACT).toString()
        return "$glucoseValue$trendAsHtmlSymbol $timeOffset (" + DexcomDateTimeConversion().getLocalHourMinuteOfCurrentTime() + ")"
    }

    fun toNotificationMessage(appContext: Context, configuration: Configuration): String {
        var notificationMessage: String = appContext.getString(R.string.normalGlucoseValue)
        if (configuration.glucoseHighThreshold < glucoseValue.toInt()) {
            notificationMessage = appContext.getString(R.string.highGlucoseValue)
        } else if (configuration.glucoseLowThreshold > glucoseValue.toInt()) {
            notificationMessage = appContext.getString(R.string.lowGlucoseValue)
        }

        if (0 < glucoseRecentHistory.size) {
            notificationMessage += "\n("
            for ((i, value) in glucoseRecentHistory.withIndex()) {
                if (i > 0) {
                    notificationMessage += ", "
                }
                notificationMessage += "$value"
            }
            notificationMessage += ")"
        }

        return notificationMessage
    }
}