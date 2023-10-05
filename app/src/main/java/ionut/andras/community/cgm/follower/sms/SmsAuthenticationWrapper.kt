package ionut.andras.community.cgm.follower.sms

import android.content.Context
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.utils.DateTimeConversion
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class SmsAuthenticationWrapper(context: Context) {
    private var applicationContext: Context

    init{
        applicationContext = context
    }

    fun sendAuthenticationSms(receiverPhoneNo: String = "", senderPhoneNo: String = "") {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val sessionId = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        var senderPhoneNumber = senderPhoneNo
        // If no senderPhoneNumber specified, try to get it from SharedPreferences
        if (senderPhoneNo.isEmpty()) {
            senderPhoneNumber =
                sharedPreferences.getString(UserPreferences.senderPhoneNo, "").toString()
        }

        // Send SMS only if receiverPhoneNo is available
        if (receiverPhoneNo.isNotEmpty()) {
            // <SMSWAKEUPMESSAGE>:<DexcomSessionId>-N<EnableDisableNotificationsOnFollower>-PS<Sender Phone No>-PR<Receiver Phone No> GOOGLE_PLAY_11_CHARACTERS_HASH
            val smsText =
                Configuration().smsWakeupTriggerString + ":$sessionId-N0-PS$senderPhoneNumber-PR$receiverPhoneNo " + Configuration().smsGooglePlayVerificationHash
            // ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "SMS: $binarySMS")
            //ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "Phone No: $phoneNo")
            SMSWrapper(applicationContext).sendTextSms(receiverPhoneNo, smsText)
        }
    }

    fun sendAuthenticationRenewSms() {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val sessionId = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        val senderPhoneNo = sharedPreferences.getString(UserPreferences.senderPhoneNo, "")
        val receiverPhoneNo = sharedPreferences.getString(UserPreferences.receiverPhoneNo, "")
        val nextAllowedSmsRequestTs = sharedPreferences.getLong(UserPreferences.smsSecurityDelayWindow, DateTimeConversion().getLocalTimestamp())

        // Allow a security delay window between 2 SMS
        if (nextAllowedSmsRequestTs <= DateTimeConversion().getLocalTimestamp()) {
            // <SMSWAKEUPMESSAGE>:<DexcomSessionId>-N<EnableDisableNotificationsOnFollower>-PS<Sender Phone No>-PR<Receiver Phone No> GOOGLE_PLAY_11_CHARACTERS_HASH
            val smsText =
                Configuration().smsRefreshAuthenticationString + ":$sessionId-N0-PS$senderPhoneNo-PR$receiverPhoneNo " + Configuration().smsGooglePlayVerificationHash
            // ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "SMS: $binarySMS")
            //ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "Phone No: $phoneNo")
            if (!receiverPhoneNo.isNullOrEmpty()) {
                SMSWrapper(applicationContext).sendTextSms(receiverPhoneNo, smsText)
            }
        }
        sharedPreferences.edit()
            .putLong(UserPreferences.smsSecurityDelayWindow, DateTimeConversion().getLocalTimestamp() + Configuration().smsSecurityDelayWindow)
            .apply()
    }
}