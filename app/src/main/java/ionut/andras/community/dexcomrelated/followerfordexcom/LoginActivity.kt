package ionut.andras.community.dexcomrelated.followerfordexcom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.UserPreferences

class LoginActivity : AppCompatActivityWrapper() {
    private lateinit var emailText: TextView
    private lateinit var passwordText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        emailText = findViewById(R.id.loginEmailAddress)
        passwordText = findViewById(R.id.loginPassword)

        val loginButton = findViewById<Button>(R.id.btnLogin)
        loginButton.isEnabled = true

        showToastIfMessageAvailable(intent)
    }

    fun btnLoginOnClick(view: View) {
        val sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(UserPreferences.loginEmail, emailText.text.toString()).apply()
        sharedPreferences.edit().putString(UserPreferences.loginPassword, passwordText.text.toString()).apply()
        view.isEnabled = false

        switchToMainActivity()
    }

    private fun switchToMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showToastIfMessageAvailable(intent: Intent?) {
        if (null != intent) {
            val toastMessage = intent.getStringExtra(getString(R.string.variableNameLoginFormMessage))
            if (!toastMessage.isNullOrEmpty()) {
                val toast = Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }
}