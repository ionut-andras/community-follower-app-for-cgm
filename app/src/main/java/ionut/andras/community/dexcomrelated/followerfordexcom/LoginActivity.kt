package ionut.andras.community.dexcomrelated.followerfordexcom

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.UserPreferences
import ionut.andras.community.dexcomrelated.followerfordexcom.core.AppCompatActivityWrapper
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.SharedPreferencesFactory

class LoginActivity : AppCompatActivityWrapper() {
    private lateinit var emailText: TextView
    private lateinit var passwordText: TextView
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        initializeView()

        showToastIfMessageAvailable(intent)
    }

    private fun initializeView() {
        // Enable hyperlinks in scrollable text view
        val disclaimerTextView = findViewById<TextView>(R.id.loginDisclaimerTextView)
        disclaimerTextView.movementMethod = LinkMovementMethod.getInstance()

        // Initialize mail components
        emailText = findViewById(R.id.loginEmailAddress)
        passwordText = findViewById(R.id.loginPassword)

        loginButton = findViewById<Button>(R.id.btnLogin)
        loginButton.isEnabled = false
    }

    fun checkboxDisclaimerChanged(view: View) {
        val disclaimerCheckbox = findViewById<CheckBox>(R.id.checkboxDisclaimer)
        loginButton.isEnabled = disclaimerCheckbox.isChecked
    }

    fun btnLoginOnClick(view: View) {
        // val sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

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