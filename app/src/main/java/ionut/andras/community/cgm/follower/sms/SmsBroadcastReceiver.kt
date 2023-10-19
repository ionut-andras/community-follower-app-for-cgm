package ionut.andras.community.cgm.follower.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.constants.ApplicationRunMode
import ionut.andras.community.cgm.follower.core.SessionManager
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.ApplicationRunModesHelper

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
class SmsBroadcastReceiver: BroadcastReceiver() {
    private lateinit var applicationContext: Context

    override fun onReceive(context: Context, intent: Intent) {
        applicationContext = context
        ToastWrapper(applicationContext).displayDebugToast("SMS Received")
        Log.i("SmsBroadcastReceiver", "SMS received: $intent")
        Log.i("SmsBroadcastReceiver", "Intent action: ${intent.action}")

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message contents
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                    
                    // https://cgmfollower/login?<SMSWAKEUPMESSAGE>=<userKey> GOOGLE_PLAY_11_CHARACTERS_HASH
                    val receivedMessageComponents = SmsProcessor(applicationContext).getWakeupMessageElements(message)
                    val action:String? = receivedMessageComponents?.get(2)
                    val runMode = ApplicationRunModesHelper(applicationContext).getRunMode(ApplicationRunMode.UNDEFINED)

                    Log.i("SmsBroadcastReceiver", "Processing $action ...")
                    when (action) {
                        Configuration().smsWakeupTriggerString -> {
                            Log.i("SmsBroadcastReceiver", "Checking run mode $runMode ...")
                            if (runMode != ApplicationRunMode.OWNER) {
                                Log.i("SmsBroadcastReceiver", "Start processing SMS  ...")
                                SessionManager(applicationContext).recoverSessionFromSmsKey(receivedMessageComponents)
                            }
                        }

                        /*Configuration().smsRefreshAuthenticationString -> {
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
                        }*/

                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Waiting for SMS timed out (5 minutes)
                    ToastWrapper(context).displayDebugToast("SMS Timeout")
                }
            }
        }
    }
}