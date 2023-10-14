package ionut.andras.community.cgm.follower

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.core.SessionManager
import ionut.andras.community.cgm.follower.permissions.PermissionHandler
import ionut.andras.community.cgm.follower.permissions.PermissionRequestCodes
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class LoginActivity : AppCompatActivityWrapper() {
    private lateinit var emailText: TextView
    private lateinit var passwordText: TextView
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        initializeView()

        showToastIfMessageAvailable(intent)

        checkApplicationOptionalRequirements()

        checkStartedByIntent()

        // Trigger SMS Received event manually for debug purpose only
        /*// Create a new Intent object and specify the action.
        val intent = Intent(SmsRetriever.SMS_RETRIEVED_ACTION)
        intent.putExtra(SmsRetriever.EXTRA_SMS_MESSAGE, "https://cgmfollower/login?ST=a7bf19cbe19a149e8a52b5a0042bd690 IeIw2DQg0Io")
        val status = Status(CommonStatusCodes.SUCCESS)
        // CommonStatusCodes.SUCCESS as Status)
        intent.putExtra(SmsRetriever.EXTRA_STATUS,status)
        // Send the broadcast.
        applicationContext.sendBroadcast(intent)*/

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

    private fun showToastIfMessageAvailable(intent: Intent?) {
        if (null != intent) {
            val toastMessage = intent.getStringExtra(getString(R.string.variableNameLoginFormMessage))
            if (!toastMessage.isNullOrEmpty()) {
                val toast = Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }

    private fun checkApplicationOptionalRequirements() {
        /**
         * Check if minimum permissions needed by the application are requested from the user.
         */
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.SEND_SMS, getString(R.string.permissionFriendlyNameSendSms), PermissionRequestCodes.SEND_SMS)
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.READ_PHONE_STATE, getString(R.string.permissionFriendlyNameReadPhoneState), PermissionRequestCodes.READ_PHONE_STATE)
    }

    private fun checkStartedByIntent() {
        // val action: String? = intent?.action
        val data: Uri? = intent?.data

        data?.let {
            val userKey = it.getQueryParameter(Configuration().smsWakeupTriggerString)
            userKey?.let {
                Log.i("checkStartedByIntent", "$userKey")
                val receivedMessageComponents = mutableListOf(
                    "https://cgmfollower/login?ST=${userKey.toString()}} IeIw2DQg0Io",
                    "login",
                    "ST",
                    userKey.toString()
                )
                SessionManager(applicationContext).recoverSessionFromSmsKey(
                    receivedMessageComponents
                )
            }
        }
    }

    // Handle the permission result in onRequestPermissionsResult()
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionRequestCodes.SEND_SMS -> {
                PermissionHandler(this,applicationContext)
                    .onRequestPermissionResult(getString(R.string.permissionFriendlyNameSendSms), grantResults)
            }

            PermissionRequestCodes.READ_PHONE_STATE -> {
                PermissionHandler(this,applicationContext)
                    .onRequestPermissionResult(getString(R.string.permissionFriendlyNameReadPhoneState), grantResults)
            }
        }
    }
}