package ionut.andras.community.cgm.follower.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log

class SMSWrapper(private val applicationContext: Context) {
    fun sendBinarySms(phoneNumber: String, message: String) {
        val telephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
            // Device cannot send SMS messages
            return
        }

        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            applicationContext.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        /*
        val binaryMessage = message.toByteArray(Charsets.UTF_8)

        smsManager.sendDataMessage(
            phoneNumber,
            null,  // Service Center Address (use null for default)
            0,     // Port (use 0 for default)
            binaryMessage,
            null,
            null
        )
        */

        smsManager.sendTextMessage(
            phoneNumber,
            null,
            message,
            null,
            null
        )

        return
    }
}