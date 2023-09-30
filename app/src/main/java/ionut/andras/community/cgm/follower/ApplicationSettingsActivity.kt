package ionut.andras.community.cgm.follower

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class ApplicationSettingsActivity : AppCompatActivityWrapper(R.menu.application_settings_menu) {
    private lateinit var sharedPreferences: SharedPreferences
    private val appConfiguration: Configuration = Configuration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_application_settings)

        // Set Action bar
        setSupportActionBar(findViewById(R.id.applicationSettingsActivityActionToolbar))

        init()

        enableActivityListeners()
    }

    private fun init() {
        // sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        val autoCancelNotifications = findViewById<SwitchCompat>(R.id.autoCancelNotifications)
        autoCancelNotifications.isChecked = sharedPreferences.getBoolean(UserPreferences.autoCancelNotifications, appConfiguration.autoCancelNotifications)
        Log.i("Settings: autoCancelNotifications.isChecked", autoCancelNotifications.isChecked.toString())

        val disableNotifications = findViewById<SwitchCompat>(R.id.disableNotifications)
        disableNotifications.isChecked = sharedPreferences.getBoolean(UserPreferences.disableNotifications, appConfiguration.disableNotification)
        Log.i("Settings: disableNotifications.isChecked", disableNotifications.isChecked.toString())

        findViewById<EditText>(R.id.minDisplayableGlucoseValue).setText(appConfiguration.minDisplayableGlucoseValue.toString())
        findViewById<EditText>(R.id.maxDisplayableGlucoseValue).setText(appConfiguration.maxDisplayableGlucoseValue.toString())

        findViewById<EditText>(R.id.glucoseUrgentLowThresholdValue).setText(appConfiguration.glucoseUrgentLowThreshold.toString())
        findViewById<EditText>(R.id.glucoseLowThresholdValue).setText(appConfiguration.glucoseLowThreshold.toString())
        findViewById<EditText>(R.id.glucoseHighThresholdValue).setText(appConfiguration.glucoseHighThreshold.toString())
    }

    private fun enableActivityListeners() {
        val autoCancelNotifications = findViewById<SwitchCompat>(R.id.autoCancelNotifications)
        val disableNotifications = findViewById<SwitchCompat>(R.id.disableNotifications)
        // sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        autoCancelNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.i("SettingsActivityListener: UserPreferences.autoCancelNotifications", true.toString())
                sharedPreferences.edit().putBoolean(UserPreferences.autoCancelNotifications, true).apply()
            } else {
                Log.i("SettingsActivityListener: UserPreferences.autoCancelNotifications", false.toString())
                sharedPreferences.edit().putBoolean(UserPreferences.autoCancelNotifications, false).apply()
            }
        }

        disableNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.i("SettingsActivityListener: UserPreferences.disableNotifications", true.toString())
                sharedPreferences.edit().putBoolean(UserPreferences.disableNotifications, true).apply()
            } else {
                Log.i("SettingsActivityListener: UserPreferences.disableNotifications", false.toString())
                sharedPreferences.edit().putBoolean(UserPreferences.disableNotifications, false).apply()
            }
        }

        val logoutButton = findViewById<Button>(R.id.btnSettingsLogout)
        logoutButton.setOnClickListener{
            btnLogoutOnClick()
        }

    }

    private fun btnLogoutOnClick() {
        Log.i("iconLogoutOnClick", "Logging out")

        var email = sharedPreferences.getString(UserPreferences.loginEmail, null)
        var password = sharedPreferences.getString(UserPreferences.loginPassword, null)
        var dexcomSessionID = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        Log.i("displayLoginFormNeeded > email", email.toString())
        Log.i("displayLoginFormNeeded > password", password.toString())
        Log.i("displayLoginFormNeeded > dexcomSessionID", dexcomSessionID.toString())


        SharedPreferencesFactory(applicationContext).getInstance().edit().clear().apply()

        email = sharedPreferences.getString(UserPreferences.loginEmail, null)
        password = sharedPreferences.getString(UserPreferences.loginPassword, null)
        dexcomSessionID = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        Log.i("displayLoginFormNeeded > email", email.toString())
        Log.i("displayLoginFormNeeded > password", password.toString())
        Log.i("displayLoginFormNeeded > dexcomSessionID", dexcomSessionID.toString())


        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }
}