package ionut.andras.community.cgm.follower.sms

import android.content.Context
import android.util.Log
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.toast.ToastWrapper

class SmsAuthenticationWrapper(context: Context) {
    private var applicationContext: Context

    init{
        applicationContext = context
    }

    fun sendAuthenticationSms(receiverPhoneNo: String = "", userKey: String? = "") {
        // Send SMS only if receiverPhoneNo is available
        if (receiverPhoneNo.isNotEmpty() && !userKey.isNullOrEmpty()) {
            // cgmfollower://login?<SMSWAKEUPMESSAGE>=<USERKEY> GOOGLE_PLAY_11_CHARACTERS_HASH

            val smsText =
                applicationContext.getString(R.string.autologinUrlPrefix) +
                "?" +
                Configuration().smsWakeupTriggerString + "=$userKey" +
                " " +
                Configuration().smsGooglePlayVerificationHash
            Log.i("sendAuthenticationSms", smsText)
            SMSWrapper(applicationContext).sendTextSms(receiverPhoneNo, smsText)
        } else {
            ToastWrapper(applicationContext).displayInfoToast(applicationContext.getString(R.string.messageUserKeyUnavailable))
        }
    }
}