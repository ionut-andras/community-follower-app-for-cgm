package ionut.andras.community.cgm.follower

import android.os.Bundle
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper

class InviteFollowerActivity : AppCompatActivityWrapper(R.menu.invite_followers_menu) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_follower)

        // Set Action bar
        setSupportActionBar(findViewById(R.id.inviteFollowerActivityActionToolbar))
        enableActivityListeners()
    }

    private fun enableActivityListeners() {
        /*val logoutButton = findViewById<Button>(R.id.btnPermissionsInfoOk)
        logoutButton.setOnClickListener{
            btnPermissionsInfoOkOnClick()
        }*/

    }

    private fun btnSendInviteToFollowerOnClick() {
        /*val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)*/
    }
}