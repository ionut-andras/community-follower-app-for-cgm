package ionut.andras.community.cgm.follower.core

import android.content.Context
import ionut.andras.community.cgm.follower.api.cgmfollowerbe.CgmFollowerBeApiRequestHandler
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import org.json.JSONObject

class SessionManager (private val applicationContext: Context) {
    fun recoverSessionsFromBackend(requestHandler: CgmFollowerBeApiRequestHandler) {
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val senderPhoneNo = sharedPreferences.getString(UserPreferences.senderPhoneNo, "")
        val receiverPhoneNo = sharedPreferences.getString(UserPreferences.receiverPhoneNo, "")
        val userKey = "$senderPhoneNo-$receiverPhoneNo"
        val apiResponse = requestHandler.getSession(userKey)
        if (!apiResponse.errorOccurred()) {
            /*{
                "session": "8dfe387c-a322-430f-9b82-c23965d427b8",
                "notifications_enabled_flag": "0",
                "phone_sender": "+40111111111",
                "phone_receiver": "+40222222222",
                "app_hash": "5de6329f620f38f0eaddb58cbafce54f"
            }*/
            apiResponse.data?.let {
                val userSession = JSONObject(it)
                // Save session for later use
                sharedPreferences.edit()
                    .putString(UserPreferences.dexcomSessionId, userSession.getString("session"))
                    .apply()
                // Update application settings
                ApplicationSettingsWrapper(applicationContext).setNotificationStatus(userSession.getString("notifications_enabled_flag"))
            }
        }
    }
}