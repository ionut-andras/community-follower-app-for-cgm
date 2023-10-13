package ionut.andras.community.cgm.follower.core

import android.content.Context
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class ApplicationSettingsWrapper(private val applicationContext: Context) {
    fun setNotificationStatus(notificationsEnabled: String = "0") {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        // Enable / Disable notifications
        if ("1" == notificationsEnabled) {
            sharedPreferences.edit()
                .putBoolean(UserPreferences.disableNotifications, false)
                .apply()
        } else {
            sharedPreferences.edit()
                .putBoolean(UserPreferences.disableNotifications, true)
                .apply()
        }
    }
}