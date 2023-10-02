package ionut.andras.community.cgm.follower.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SmsBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        /*
        when (intent.action) {
            Telephony.Sms.Intents.SMS_SENT_ACTION -> {
                // The SMS has been sent
            }
            Telephony.Sms.Intents.SMS_DELIVERED_ACTION -> {
                // The SMS has been delivered
            }
            Telephony.Sms.Intents.SMS_FAILED_ACTION -> {
                // The SMS failed to send
            }
        }
        */
        Log.i("SmsBroadcastReceiver", intent.toString())
    }
}