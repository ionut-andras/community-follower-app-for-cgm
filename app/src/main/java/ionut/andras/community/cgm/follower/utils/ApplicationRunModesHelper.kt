package ionut.andras.community.cgm.follower.utils

import android.content.Context
import ionut.andras.community.cgm.follower.configuration.UserPreferences

class ApplicationRunModesHelper(private var applicationContext: Context) {

    fun switchRunModeTo(runMode: Int) {
        // Enable Follower Mode
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        sharedPreferences.edit()
            .putInt(UserPreferences.runMode, runMode)
            .apply()
    }

    fun getRunMode(runModeDefault: Int): Int {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        return sharedPreferences.getInt(UserPreferences.runMode, runModeDefault)
    }
}