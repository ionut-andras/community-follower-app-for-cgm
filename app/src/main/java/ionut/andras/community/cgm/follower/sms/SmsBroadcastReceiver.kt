package ionut.andras.community.cgm.follower.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.ApplicationRunMode
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.ApplicationRunModesHelper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
class SmsBroadcastReceiver: BroadcastReceiver() {
    private lateinit var smsWakeupMessageRegex: Regex
    private lateinit var applicationContext: Context

    override fun onReceive(context: Context, intent: Intent) {
        applicationContext = context
        // ToastWrapper(context).displayInfoToast("SMS Received")

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    smsWakeupMessageRegex = Regex(context.getString(R.string.regexSmsWakeupMessage))

                    // Get SMS message contents
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                    
                    // <SMSWAKEUPMESSAGE>:<DexcomSessionId>-N<EnableDisableNotificationsOnFollower>-PS<Sender Phone No>-PR<Receiver Phone No> GOOGLE_PLAY_11_CHARACTERS_HASH
                    val receivedMessageComponents = getWakeupMessageElements(message)
                    val action:String? = receivedMessageComponents?.get(1)

                    when (action) {
                        Configuration().smsWakeupTriggerString -> {
                            if (ApplicationRunModesHelper(applicationContext).getRunMode(ApplicationRunMode.FOLLOWER) == ApplicationRunMode.FOLLOWER) {
                                processWakeUpSmsAction(receivedMessageComponents)
                            }
                        }

                        Configuration().smsRefreshAuthenticationString -> {
                            val receiverPhoneNo: String = receivedMessageComponents[5]
                            val senderPhoneNo: String = receivedMessageComponents[4]
                            SmsAuthenticationWrapper(applicationContext).sendAuthenticationSms(receiverPhoneNo, senderPhoneNo)
                        }

                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Waiting for SMS timed out (5 minutes)
                    // ToastWrapper(context).displayInfoToast("SMS Timeout")
                }
            }
        }
    }

    private fun processWakeUpSmsAction(receivedMessageComponents: List<String>?) {
        val action:String? = receivedMessageComponents?.get(1)
        val sessionId:String? = receivedMessageComponents?.get(2)
        val notificationsEnabled:String? = receivedMessageComponents?.get(3)
        val senderPhoneNo:String? = receivedMessageComponents?.get(4)
        val receiverPhoneNo:String? = receivedMessageComponents?.get(5)

        // Extract one-time code from the message and complete verification
        // by sending the code back to your server.
        // ToastWrapper(context).displayInfoToast(message)
        ToastWrapper(applicationContext).displayInfoToast("Extracted action = $action")
        ToastWrapper(applicationContext).displayInfoToast("Extracted Session Id = $sessionId")
        ToastWrapper(applicationContext).displayInfoToast("Extracted Notifications Flag = $notificationsEnabled")
        ToastWrapper(applicationContext).displayInfoToast("Extracted senderPhoneNo = $senderPhoneNo")
        ToastWrapper(applicationContext).displayInfoToast("Extracted receiverPhoneNo = $receiverPhoneNo")


        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val runModeHelper = ApplicationRunModesHelper(applicationContext)


        // Setup shared session
        if (!sessionId.isNullOrEmpty()) {
            sharedPreferences.edit()
                .putString(UserPreferences.dexcomSessionId, sessionId)
                .apply()
        }

        // Enable / Disable notifications
        if ("1" == notificationsEnabled) {
            sharedPreferences.edit()
                .putBoolean(UserPreferences.disableNotifications, false)
                .apply()
        } else {
            sharedPreferences.edit()
                .putBoolean(UserPreferences.disableNotifications, true)
                .apply()
        }

        // Save phone numbers from the Follower perspective
        // - Sender is now the Follower (old receiver)
        // - Receiver is now the Master (old sender)
        if (!senderPhoneNo.isNullOrEmpty() && !receiverPhoneNo.isNullOrEmpty()) {
            sharedPreferences.edit()
                .putString(UserPreferences.receiverPhoneNo, senderPhoneNo)
                .putString(UserPreferences.senderPhoneNo, receiverPhoneNo)
                .apply()
        }

        // Enable Follower Mode
        runModeHelper.switchRunModeTo(ApplicationRunMode.FOLLOWER)

        val redirectIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        applicationContext.startActivity(redirectIntent)
    }

    private fun getWakeupMessageElements(smsWakeUpMessage: String): List<String>? {
        val match = smsWakeupMessageRegex.find(smsWakeUpMessage)
        return match?.groupValues
    }
}