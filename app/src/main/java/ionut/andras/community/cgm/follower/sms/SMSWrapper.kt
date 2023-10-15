package ionut.andras.community.cgm.follower.sms

import android.content.Context
import android.content.Intent
import android.net.Uri


class SMSWrapper(private val applicationContext: Context) {
    fun sendTextSms(phoneNumber: String, message: String) {
        // Create an Intent to start the default SMS app.
        // Set the phone number and message text.
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("sms:$phoneNumber")
            putExtra(Intent.EXTRA_TEXT, message)
        }
       // Start the default SMS app.
        applicationContext.startActivity(intent)
    }
}