package ionut.andras.community.dexcomrelated.followerfordexcom

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.Configuration
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.UserPreferences
import ionut.andras.community.dexcomrelated.followerfordexcom.core.AppCompatActivityWrapper
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.SharedPreferencesFactory

class ApplicationSettingsActivity : AppCompatActivityWrapper() {
    private lateinit var sharedPreferences: SharedPreferences
    private val appConfiguration: Configuration = Configuration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_application_settings)
        setSupportActionBar(findViewById(R.id.applicationSettingsActivityActionToolbar))

        init()

        enableSettingsActivityListeners()
    }

    private fun init() {
        // sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        val autoCancelNotifications = findViewById<SwitchCompat>(R.id.autoCancelNotifications)
        autoCancelNotifications.isChecked = sharedPreferences.getBoolean(UserPreferences.autoCancelNotifications, appConfiguration.autoCancelNotifications)

        Log.i("Settings: autoCancelNotifications.isChecked", autoCancelNotifications.isChecked.toString())

        findViewById<EditText>(R.id.minDisplayableGlucoseValue).setText(appConfiguration.minDisplayableGlucoseValue.toString())
        findViewById<EditText>(R.id.maxDisplayableGlucoseValue).setText(appConfiguration.maxDisplayableGlucoseValue.toString())

        findViewById<EditText>(R.id.glucoseUrgentLowThresholdValue).setText(appConfiguration.glucoseUrgentLowThreshold.toString())
        findViewById<EditText>(R.id.glucoseLowThresholdValue).setText(appConfiguration.glucoseLowThreshold.toString())
        findViewById<EditText>(R.id.glucoseHighThresholdValue).setText(appConfiguration.glucoseHighThreshold.toString())
    }

    private fun enableSettingsActivityListeners() {
        val autoCancelNotifications = findViewById<SwitchCompat>(R.id.autoCancelNotifications)
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.application_settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.iconSettingsBack -> {
                iconSettingsBackOnClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun iconSettingsBackOnClick() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}