package ionut.andras.community.cgm.follower

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper

class AddNewUserEventActivity : AppCompatActivityWrapper(R.menu.add_new_user_event_menu) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_user_event)

        // Set Action bar
        setSupportActionBar(findViewById(R.id.addUserEventActivityActionToolbar))

        enableAddEventActivityListeners()
    }

    private fun enableAddEventActivityListeners() {
        val logoutButton = findViewById<Button>(R.id.btnAddEventSave)
        logoutButton.setOnClickListener{
            btnAddEventSaveOnClick()
        }
    }

    private fun btnAddEventSaveOnClick() {
        // Save data locally
        // @TODO

        // Redirect to main view
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}