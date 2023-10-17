package ionut.andras.community.cgm.follower.core

import android.content.Context
import ionut.andras.community.cgm.follower.api.dexcom.EndpointsManagement
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class ApplicationSettingsWrapper(applicationContext: Context) {
    private val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

    fun setNotificationStatus(notificationsEnabled: String = "0") {
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

    /**
     * Sets the application geolocated url and the geolocation itself
     *
     * @param geolocation String
     */
    fun setApplicationBaseUrlGeolocation(geolocation: String) {
        // Make sure the value is valid, if not force it to valid
        var baseUrlGeolocation = DexcomConstants().geolocationUsa
        if (geolocation != DexcomConstants().geolocationUsa) {
            baseUrlGeolocation = DexcomConstants().geolocationOutsideUsa
        }

        // Get base url geolocated based on the geolocation
        val baseUrl = EndpointsManagement().getGeolocationZoneDomain(baseUrlGeolocation)
        sharedPreferences.edit()
            .putString(DexcomConstants().baseUrlKey, baseUrl)
            .putString(DexcomConstants().baseUrlGeolocationKey, baseUrlGeolocation)
            .apply()
    }
}