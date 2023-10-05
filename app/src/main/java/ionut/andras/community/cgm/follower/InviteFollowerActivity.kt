package ionut.andras.community.cgm.follower

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.sms.SmsAuthenticationWrapper
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class InviteFollowerActivity : AppCompatActivityWrapper(R.menu.invite_followers_menu) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_follower)

        // Set Action bar
        setSupportActionBar(findViewById(R.id.inviteFollowerActivityActionToolbar))

        initializeDefaultValues()

        enableActivityListeners()
    }

    private fun initializeDefaultValues () {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val senderPhoneNumber = sharedPreferences.getString(UserPreferences.senderPhoneNo, "")
        if (!senderPhoneNumber.isNullOrEmpty()) {
            findViewById<EditText>(R.id.personalPhoneEditText).setText(senderPhoneNumber)
        }
    }

    private fun enableActivityListeners() {
        val sendFollowerInvite = findViewById<Button>(R.id.btnSendInviteToFollower)
        sendFollowerInvite.setOnClickListener{
            btnSendInviteToFollowerOnClick(it)
        }

    }

    private fun btnSendInviteToFollowerOnClick(button: View) {
        val receiverPhoneNumber = findViewById<EditText>(R.id.sendInviteToFollowerPhoneEditText).text.toString()
        val senderPhoneNumber = findViewById<EditText>(R.id.personalPhoneEditText).text.toString()

        if (receiverPhoneNumber.isNotEmpty() && senderPhoneNumber.isNotEmpty()) {
            if (sendInviteToFollow(receiverPhoneNumber, senderPhoneNumber)) {
                // Hide the button
                button.visibility = View.INVISIBLE

                ToastWrapper(applicationContext).displayMessageToast(
                    button,
                    getString(R.string.textInvitationSent)
                )

                val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
                sharedPreferences.edit()
                    .putString(UserPreferences.senderPhoneNo, senderPhoneNumber)
                    .apply()
            }
        } else {
            ToastWrapper(applicationContext).displayInfoToast(getString(R.string.textPhoneNumberNullOrEmpty))
        }


//        val intent = Intent(applicationContext, MainActivity::class.java)
//        startActivity(intent)

    }

    // TODO: Implement a proper way to see if SMS has been successfully sent

    private fun sendInviteToFollow(receiverPhoneNo: String, senderPhoneNo: String): Boolean {
        var inviteSent = false
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val dexcomSessionId = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)

        if (!dexcomSessionId.isNullOrEmpty()) {
            SmsAuthenticationWrapper(applicationContext).sendAuthenticationSms(receiverPhoneNo, senderPhoneNo)
            inviteSent = true
        }

        return inviteSent
    }
}