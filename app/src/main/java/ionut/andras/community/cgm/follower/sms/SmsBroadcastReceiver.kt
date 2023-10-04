package ionut.andras.community.cgm.follower.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
class SmsBroadcastReceiver: BroadcastReceiver() {
    private lateinit var smsWakeupMessageRegex: Regex

    override fun onReceive(context: Context, intent: Intent) {
        // ToastWrapper(context).displayInfoToast("SMS Received")

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    smsWakeupMessageRegex = Regex(context.getString(R.string.regexSmsWakeupMessage))

                    // Get SMS message contents
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return

                    // SMSWAKEUPMESSAGE:DexcomSessionId-EnableDisableNotificationsOnFollower GOOGLE_PLAY_11_CHARACTERS_HASH
                    val sessionId:String? = getWakeupMessageElements(message)?.get(1)

                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                    // ToastWrapper(context).displayInfoToast(message)
                    // ToastWrapper(context).displayInfoToast("Extracted Session Id = $sessionId")

                    if (!sessionId.isNullOrEmpty()) {
                        val sharedPreferences = SharedPreferencesFactory(context).getInstance()
                        sharedPreferences.edit()
                            .putString(UserPreferences.dexcomSessionId, sessionId.toString())
                            .apply()
                    }

                    val redirectIntent = Intent(context, MainActivity::class.java)
                    context.startActivity(redirectIntent)
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Waiting for SMS timed out (5 minutes)
                    // ToastWrapper(context).displayInfoToast("SMS Timeout")
                }
            }
        }
    }

    private fun getWakeupMessageElements(smsWakeUpMessage: String): List<String>? {
        val match = smsWakeupMessageRegex.find(smsWakeUpMessage)
        return match?.groupValues
    }
}