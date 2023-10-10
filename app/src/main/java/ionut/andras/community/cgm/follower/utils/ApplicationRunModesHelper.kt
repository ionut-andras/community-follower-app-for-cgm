package ionut.andras.community.cgm.follower.utils

import android.content.Context
import android.util.Log
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.ApplicationRunMode

class ApplicationRunModesHelper(private var applicationContext: Context) {

    fun switchRunModeTo(runMode: Int) {
        // Enable Follower Mode
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        Log.i("ApplicationRunModesHelper", "Switching application mode to " + ApplicationRunMode.convert[runMode])
        sharedPreferences.edit()
            .putInt(UserPreferences.runMode, runMode)
            .apply()
    }

    fun getRunMode(runModeDefault: Int): Int {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        return sharedPreferences.getInt(UserPreferences.runMode, runModeDefault)
    }
}