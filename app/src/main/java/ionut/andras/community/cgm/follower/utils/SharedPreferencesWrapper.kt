package ionut.andras.community.cgm.follower.utils

import android.content.Context
import android.content.SharedPreferences
import ionut.andras.community.cgm.follower.R

class SharedPreferencesFactory(private var applicationContext: Context) {

    fun getInstance(): SharedPreferences {
        return applicationContext.getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
    }
}