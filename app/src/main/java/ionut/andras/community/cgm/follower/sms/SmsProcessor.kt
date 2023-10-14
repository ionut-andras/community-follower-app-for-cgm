package ionut.andras.community.cgm.follower.sms

import android.content.Context
import ionut.andras.community.cgm.follower.R

class SmsProcessor(applicationContext: Context) {
    private var smsWakeupMessageRegex: Regex = Regex(applicationContext.getString(R.string.regexSmsWakeupMessage))

    fun getWakeupMessageElements(smsWakeUpMessage: String): List<String>? {
        val match = smsWakeupMessageRegex.find(smsWakeUpMessage)
        return match?.groupValues
    }
}