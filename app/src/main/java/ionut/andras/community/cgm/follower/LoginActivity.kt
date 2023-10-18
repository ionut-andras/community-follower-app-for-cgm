package ionut.andras.community.cgm.follower

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.core.SessionManager
import ionut.andras.community.cgm.follower.permissions.PermissionHandler
import ionut.andras.community.cgm.follower.permissions.PermissionRequestCodes
import ionut.andras.community.cgm.follower.services.broadcast.BroadcastActions
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class LoginActivity : AppCompatActivityWrapper() {
    private lateinit var emailText: TextView
    private lateinit var passwordText: TextView
    private lateinit var loginButton: Button
    private val configuration: Configuration = Configuration()
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        initializeView()

        showToastIfMessageAvailable(intent)

        checkApplicationOptionalRequirements()

        registerBroadcastReceivers()

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
                ToastWrapper(applicationContext).displayInfoToast(toastMessage)
            }
        }
    }

    private fun checkApplicationOptionalRequirements() {
        /**
         * Check if minimum permissions needed by the application are requested from the user.
         */
    }

    private fun registerBroadcastReceivers() {
        Log.i("LoginActivity > registerBroadcastReceivers", "Starting...")

        if ((null == broadcastReceiver)) {
            Log.i("LoginActivity > registerBroadcastReceivers", "Registering broadcast receiver...")
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Log.i("MainActivity > registerBroadcastReceivers", "Received broadcast: " + intent.action)
                    // Identify broadcast operation
                    when (intent.action) {
                        BroadcastActions.USER_AUTHENTICATION_KEY_RETRIEVAL_FAILED -> handleFailedUserAuthenticationKeyRetrieval()
                        BroadcastActions.AUTHENTICATION_SESSION_SETUP_FAILED -> handleFailedAuthenticationSessionSetup()
                        else -> {}
                    }
                }
            }

            registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.USER_AUTHENTICATION_KEY_RETRIEVAL_FAILED), RECEIVER_NOT_EXPORTED)
            registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.AUTHENTICATION_SESSION_SETUP_FAILED), RECEIVER_NOT_EXPORTED)
        } else {
            Log.i("LoginActivity > registerBroadcastReceivers", "Skip broadcast receiver registration...")
        }
    }

    private fun checkStartedByIntent() {
        // val action: String? = intent?.action
        val data: Uri? = intent?.data

        data?.let {
            Log.i("checkStartedByIntent", "Application started by clicking Authentication link in SMS")

            val userKey = it.getQueryParameter(Configuration().smsWakeupTriggerString)
            userKey?.let {
                Log.i("checkStartedByIntent", "User Key: $userKey")
                val url = getString(R.string.autologinUrlPrefix) +
                        "?" +
                        configuration.smsWakeupTriggerString +
                        "=" +
                        userKey.toString() +
                        " " +
                        configuration.smsGooglePlayVerificationHash
                val receivedMessageComponents = mutableListOf(
                    url,
                    "login",
                    configuration.smsWakeupTriggerString,
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

    private fun handleFailedUserAuthenticationKeyRetrieval() {
        ToastWrapper(applicationContext).displayInfoToast(getString(R.string.messageUserKeyUnavailable))
    }

    private fun handleFailedAuthenticationSessionSetup() {

    }
}