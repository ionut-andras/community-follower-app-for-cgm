package ionut.andras.community.cgm.follower

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.sms.SMSWrapper
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
            btnSendInviteToFollowerOnClick()
        }

    }

    private fun btnSendInviteToFollowerOnClick() {
        val phoneNumber = findViewById<EditText>(R.id.sendInviteToFollowerPhoneEditText).text.toString()
        sendInviteToFollow(phoneNumber)

//        val intent = Intent(applicationContext, MainActivity::class.java)
//        startActivity(intent)
    }

    private fun sendInviteToFollow(phoneNo: String) {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val dexcomSessionId = sharedPreferences.getString(UserPreferences.dexcomSessionId, "")

        if (!dexcomSessionId.isNullOrEmpty()) {
            // DexcomSessionId-EnableDisableNotificationsOnFollower
            val binarySMS = "$dexcomSessionId-N1"
            // ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "SMS: $binarySMS")
            //ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnSendInviteToFollower), "Phone No: $phoneNo")
            SMSWrapper(applicationContext).sendBinarySms(phoneNo, binarySMS)
        }
    }
}