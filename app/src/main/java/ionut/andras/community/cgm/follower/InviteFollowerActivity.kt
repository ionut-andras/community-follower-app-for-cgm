package ionut.andras.community.cgm.follower

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.sms.SMSWrapper
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class InviteFollowerActivity : AppCompatActivityWrapper(R.menu.invite_followers_menu) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_follower)

        // Set Action bar
        setSupportActionBar(findViewById(R.id.inviteFollowerActivityActionToolbar))

        enableActivityListeners()
    }

    private fun enableActivityListeners() {
        val sendFollowerInvite = findViewById<Button>(R.id.btnSendInviteToFollower)
        sendFollowerInvite.setOnClickListener{
            btnSendInviteToFollowerOnClick(it)
        }

    }

    private fun btnSendInviteToFollowerOnClick(button: View) {
        val phoneNumber = findViewById<EditText>(R.id.sendInviteToFollowerPhoneEditText).text.toString()
        if (phoneNumber.isNotEmpty()) {
            if (sendInviteToFollow(phoneNumber)) {
                // Hide the button
                button.visibility = View.INVISIBLE

                ToastWrapper(applicationContext).displayMessageToast(
                    button,
                    getString(R.string.textInvitationSent)
                )
            }
        } else {
            ToastWrapper(applicationContext).displayInfoToast(getString(R.string.textPhoneNumberNullOrEmpty))
        }
//        val intent = Intent(applicationContext, MainActivity::class.java)
//        startActivity(intent)

    }

    // TODO: Implement a proper way to see if SMS has been successfully sent

    private fun sendInviteToFollow(phoneNo: String): Boolean {
        var inviteSent = false
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val dexcomSessionId = sharedPreferences.getString(UserPreferences.dexcomSessionId, null).toString()

        if (dexcomSessionId.isNotEmpty()) {
            // SMSWAKEUPMESSAGE:DexcomSessionId-EnableDisableNotificationsOnFollower GOOGLE_PLAY_11_CHARACTERS_HASH
            val binarySMS = Configuration().smsWakeupTriggerString + ":$dexcomSessionId-N1-P$phoneNo " + Configuration().smsGooglePlayVerificationHash
            // ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "SMS: $binarySMS")
            //ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "Phone No: $phoneNo")
            SMSWrapper(applicationContext).sendBinarySms(phoneNo, binarySMS)
            inviteSent = true
        }

        return inviteSent
    }
}