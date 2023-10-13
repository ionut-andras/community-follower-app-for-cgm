package ionut.andras.community.cgm.follower.sms

import android.content.Context
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
            // <SMSWAKEUPMESSAGE>:<USERKEY> GOOGLE_PLAY_11_CHARACTERS_HASH
            val smsText =
                Configuration().smsWakeupTriggerString + ":$userKey " + Configuration().smsGooglePlayVerificationHash
            SMSWrapper(applicationContext).sendTextSms(receiverPhoneNo, smsText)
        } else {
            ToastWrapper(applicationContext).displayInfoToast(applicationContext.getString(R.string.messageUserKeyUnavailable))
        }
    }
}