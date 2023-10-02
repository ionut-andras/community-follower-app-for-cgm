package ionut.andras.community.cgm.follower

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.permissions.PermissionHandler
import ionut.andras.community.cgm.follower.permissions.PermissionRequestCodes
import ionut.andras.community.cgm.follower.sms.SmsBroadcastReceiver
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

        //////////////////////////////////////////////////////////////

        Log.i("AppCompatActivityWrapper > onCreate", "Register SMS Receiver 1")
        try {
            registerReceiver(SmsBroadcastReceiver(), IntentFilter("android.provider.Telephony.SMS_RECEIVED"), RECEIVER_EXPORTED)
        } catch (e: Exception) {
            Log.i("AppCompatActivityWrapper > onCreate # Exception", e.toString())
        }
        //////////////////////////////////////////////////////////////
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

    private fun checkApplicationOptionalRequirements() {
        /**
         * Check if minimum permissions needed by the application are requested from the user.
         */
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.RECEIVE_SMS, getString(R.string.permissionFriendlyNameReceiveSms), PermissionRequestCodes.RECEIVE_SMS)
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.READ_SMS, getString(R.string.permissionFriendlyNameReadSms), PermissionRequestCodes.READ_SMS)
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.SEND_SMS, getString(R.string.permissionFriendlyNameSendSms), PermissionRequestCodes.SEND_SMS)
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.READ_PHONE_STATE, getString(R.string.permissionFriendlyNameReadPhoneState), PermissionRequestCodes.READ_PHONE_STATE)
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

            PermissionRequestCodes.RECEIVE_SMS -> {
                PermissionHandler(this,applicationContext)
                    .onRequestPermissionResult(getString(R.string.permissionFriendlyNameReceiveSms), grantResults)
            }

            PermissionRequestCodes.READ_SMS -> {
                PermissionHandler(this,applicationContext)
                    .onRequestPermissionResult(getString(R.string.permissionFriendlyNameReadSms), grantResults)
            }

            PermissionRequestCodes.READ_PHONE_STATE -> {
                PermissionHandler(this,applicationContext)
                    .onRequestPermissionResult(getString(R.string.permissionFriendlyNameReadPhoneState), grantResults)
            }
        }
    }
}