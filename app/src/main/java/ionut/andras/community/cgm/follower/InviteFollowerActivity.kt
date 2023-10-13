package ionut.andras.community.cgm.follower

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import ionut.andras.community.cgm.follower.api.cgmfollowerbe.CgmFollowerBeApiRequestHandler
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.sms.SmsAuthenticationWrapper
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import org.json.JSONObject

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
            } else {
                ToastWrapper(applicationContext).displayMessageToast(
                    button,
                    getString(R.string.textInvitationNotSent)
                )
            }

            // Save phone number
            val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
            sharedPreferences.edit()
                .putString(UserPreferences.senderPhoneNo, senderPhoneNumber)
                .apply()
        } else {
            ToastWrapper(applicationContext).displayInfoToast(getString(R.string.textPhoneNumberNullEmptyInvalid))
        }


//        val intent = Intent(applicationContext, MainActivity::class.java)
//        startActivity(intent)

    }

    // TODO: Implement a proper way to see if SMS has been successfully sent

    private fun sendInviteToFollow(receiverPhoneNo: String, senderPhoneNo: String): Boolean{
        var inviteSent = false
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val dexcomSessionId = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)

        if (!dexcomSessionId.isNullOrEmpty()) {
            val apiCallResponse = CgmFollowerBeApiRequestHandler(applicationContext).setSession(
                receiverPhoneNo,
                senderPhoneNo
            )
            Log.i("apiCallResponse", apiCallResponse.toString())

            if (!apiCallResponse.errorOccurred()) {
                // {"response": {"code": 0, "message": {"userKey": "a7bf19cbe19a149e8a52b5a0042bd690"}}}
                apiCallResponse.data?.let {
                    val responseData = JSONObject(it)
                    Log.i("responseData", responseData.toString())
                    val userKey =
                        responseData.getJSONObject("response").getJSONObject("message")
                            .getString("userKey")

                    SmsAuthenticationWrapper(applicationContext).sendAuthenticationSms(
                        receiverPhoneNo,
                        userKey
                    )
                    inviteSent = true
                }
            }
        }
        return inviteSent
    }
}