package ionut.andras.community.cgm.follower

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper

class ApplicationPermissionsInfoActivity : AppCompatActivityWrapper() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_permissions_info)

        enableActivityListeners()
    }

    private fun enableActivityListeners() {
        val logoutButton = findViewById<Button>(R.id.btnPermissionsInfoOk)
        logoutButton.setOnClickListener{
            btnPermissionsInfoOkOnClick()
        }

    }

    private fun btnPermissionsInfoOkOnClick() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}