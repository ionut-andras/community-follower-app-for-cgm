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

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("SMSWrapper > smsReceiver", intent.toString())
            /*
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>
                val messages = pdus.map { SmsMessage.createFromPdu(it as ByteArray) }
                for (message in messages) {
                    val binaryMessage = message.messageBody.toByteArray(Charsets.UTF_8)
                    // Process the binary message here
                }
            }
            */
        }
    }

    fun registerSmsBroadcastReceiver() {
        val filter = IntentFilter()
        filter.addAction("android.provider.Telephony.SMS_RECEIVED")
        applicationContext.registerReceiver(smsReceiver, filter)
    }

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

        val binaryMessage = message.toByteArray(Charsets.UTF_8)

        smsManager.sendDataMessage(
            phoneNumber,
            null,  // Service Center Address (use null for default)
            0,     // Port (use 0 for default)
            binaryMessage,
            null,
            null
        )

        return
    }
}