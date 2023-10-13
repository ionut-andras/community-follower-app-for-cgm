package ionut.andras.community.cgm.follower.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.api.cgmfollowerbe.CgmFollowerBeApiRequestHandler
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.ApplicationRunMode
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.ApplicationRunModesHelper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
class SmsBroadcastReceiver: BroadcastReceiver() {
    private lateinit var smsWakeupMessageRegex: Regex
    private lateinit var applicationContext: Context

    private var defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
    private var mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    override fun onReceive(context: Context, intent: Intent) {
        applicationContext = context
        // ToastWrapper(context).displayInfoToast("SMS Received")
        Log.i("SmsBroadcastReceiver", "SMS received: ${intent}")

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
                    val runMode = ApplicationRunModesHelper(applicationContext).getRunMode(ApplicationRunMode.UNDEFINED)

                    Log.i("SmsBroadcastReceiver", "Processing $action ...")
                    when (action) {
                        Configuration().smsWakeupTriggerString -> {
                            Log.i("SmsBroadcastReceiver", "Checking run mode $runMode ...")
                            if (runMode != ApplicationRunMode.OWNER) {
                                Log.i("SmsBroadcastReceiver", "Start processing SMS  ...")
                                processWakeUpSmsActionSessionSet(receivedMessageComponents)
                            }
                        }

                        Configuration().smsRefreshAuthenticationString -> {
                            Log.i("SmsBroadcastReceiver", "Checking run mode $runMode ...")
                            if (runMode == ApplicationRunMode.OWNER) {
                                val receiverPhoneNo: String = receivedMessageComponents[5]
                                val senderPhoneNo: String = receivedMessageComponents[4]
                                Log.i("SmsBroadcastReceiver", "Sending SMS from $senderPhoneNo to $receiverPhoneNo ...")

                                // Now the sender becomes receiver and vice-versa
                                SmsAuthenticationWrapper(applicationContext).sendAuthenticationSms(
                                    senderPhoneNo,
                                    receiverPhoneNo
                                )
                            }
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

    private fun processWakeUpSmsActionSessionSet(receivedMessageComponents: List<String>?) {
        // <SMSWAKEUPMESSAGE>:<USERKEY> GOOGLE_PLAY_11_CHARACTERS_HASH

        val userKey:String? = receivedMessageComponents?.get(2)
        ToastWrapper(applicationContext).displayInfoToast("SMS Extracted userKey = $userKey")

        userKey?.let {
            GlobalScope.launch(defaultDispatcher) {
                val authenticationData = CgmFollowerBeApiRequestHandler(applicationContext).getSession(it)
                if (!authenticationData.errorOccurred()) {
                    authenticationData.data?.let {
                        withContext(mainDispatcher) {
                            /*{
                                "session": "8dfe387c-a322-430f-9b82-c23965d427b8",
                                "notifications_enabled_flag": "0",
                                "phone_sender": "+40111111111",
                                "phone_receiver": "+40222222222",
                                "app_hash": "5de6329f620f38f0eaddb58cbafce54f"
                            }*/
                            val authenticationJson = JSONObject(it)
                            if (!authenticationJson.isNull("session")) {
                                val sessionId: String? = authenticationJson.getString("session")
                                val notificationsEnabled: String? =
                                    authenticationJson.getString("notifications_enabled_flag")
                                val senderPhoneNo: String? =
                                    authenticationJson.getString("phone_sender")
                                val receiverPhoneNo: String? =
                                    authenticationJson.getString("phone_receiver")

                                // Extract one-time code from the message and complete verification
                                // by sending the code back to your server.
                                // ToastWrapper(context).displayInfoToast(message)
                                ToastWrapper(applicationContext).displayInfoToast("Extracted Session Id = $sessionId")
                                ToastWrapper(applicationContext).displayInfoToast("Extracted Notifications Flag = $notificationsEnabled")
                                ToastWrapper(applicationContext).displayInfoToast("Extracted senderPhoneNo = $senderPhoneNo")
                                ToastWrapper(applicationContext).displayInfoToast("Extracted receiverPhoneNo = $receiverPhoneNo")

                                val sharedPreferences =
                                    SharedPreferencesFactory(applicationContext).getInstance()
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
                                Log.i(
                                    "SmsBroadCastReceiver > processWakeUpSmsActionSessionSet",
                                    "Switching application mode to FOLLOWER ..."
                                )
                                runModeHelper.switchRunModeTo(ApplicationRunMode.FOLLOWER)

                                val redirectIntent =
                                    Intent(applicationContext, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                applicationContext.startActivity(redirectIntent)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getWakeupMessageElements(smsWakeUpMessage: String): List<String>? {
        val match = smsWakeupMessageRegex.find(smsWakeUpMessage)
        return match?.groupValues
    }
}