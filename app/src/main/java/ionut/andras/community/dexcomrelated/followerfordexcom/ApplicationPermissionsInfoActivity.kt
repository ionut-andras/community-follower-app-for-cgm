package ionut.andras.community.dexcomrelated.followerfordexcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ApplicationPermissionsInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_permissions_info)

        enablePermissionsInfoActivityListeners()
    }

    private fun enablePermissionsInfoActivityListeners() {
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