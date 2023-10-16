package ionut.andras.community.cgm.follower.core

import android.content.Context
import android.content.Intent
import android.util.Log
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.api.cgmfollowerbe.CgmFollowerBeApiRequestHandler
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.ApplicationRunMode
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.ApplicationRunModesHelper
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                "geo": "us"
                "session": "8dfe387c-a322-430f-9b82-c23965d427b8",
                "notifications_enabled_flag": "0",
                "phone_sender": "+40111111111",
                "phone_receiver": "+40222222222",
                "app_hash": "5de6329f620f38f0eaddb58cbafce54f"
            }*/
            apiResponse.data?.let {
                val userSession = JSONObject(it)

                // Setup base url based on geolocation
                val geolocation: String? = userSession.getString("geo")
                var baseUrl = DexcomConstants().baseUrlUsa
                if (geolocation != DexcomConstants().usa) {
                    if (!geolocation.isNullOrEmpty()) {
                        baseUrl = DexcomConstants().baseUrlOutsideUsa
                    }
                }

                // Save session for later use
                sharedPreferences.edit()
                    .putString(UserPreferences.dexcomSessionId, userSession.getString("session"))
                    .putString(DexcomConstants().baseUrlKey, baseUrl)
                    .apply()
                // Update application settings
                ApplicationSettingsWrapper(applicationContext).setNotificationStatus(userSession.getString("notifications_enabled_flag"))
            }
        }
    }

    fun recoverSessionFromSmsKey(receivedMessageComponents: List<String>?) {
        // https://cgmfollower/login?<SMSWAKEUPMESSAGE>=<USERKEY> GOOGLE_PLAY_11_CHARACTERS_HASH

        val userKey:String? = receivedMessageComponents?.get(3)
        ToastWrapper(applicationContext).displayDebugToast("SMS Extracted userKey = $userKey")

        userKey?.let {
            GlobalScope.launch(AsyncDispatcher.default) {
                val authenticationData = CgmFollowerBeApiRequestHandler(applicationContext).getSession(it)
                if (!authenticationData.errorOccurred()) {
                    authenticationData.data?.let {
                        withContext(AsyncDispatcher.main) {
                            /*{
                                "geo": "us"
                                "session": "8dfe387c-a322-430f-9b82-c23965d427b8",
                                "notifications_enabled_flag": "0",
                                "phone_sender": "+40111111111",
                                "phone_receiver": "+40222222222",
                                "app_hash": "5de6329f620f38f0eaddb58cbafce54f"
                            }*/
                            val authenticationJson = JSONObject(it)
                            if (!authenticationJson.isNull("session")) {
                                val geolocation: String? = authenticationJson.getString("geo")
                                val sessionId: String? = authenticationJson.getString("session")
                                val notificationsEnabled: String? =
                                    authenticationJson.getString("notifications_enabled_flag")
                                val senderPhoneNo: String? =
                                    authenticationJson.getString("phone_sender")
                                val receiverPhoneNo: String? =
                                    authenticationJson.getString("phone_receiver")

                                // Extract one-time code from the message and complete verification
                                // by sending the code back to your server.
                                // ToastWrapper(context).displayInfoToast(message)
                                /* @INFO: Only for internal testing...  */
                                ToastWrapper(applicationContext).displayDebugToast("Extracted Geo location = $geolocation")
                                ToastWrapper(applicationContext).displayDebugToast("Extracted Session Id = $sessionId")
                                ToastWrapper(applicationContext).displayDebugToast("Extracted Notifications Flag = $notificationsEnabled")
                                ToastWrapper(applicationContext).displayDebugToast("Extracted senderPhoneNo = $senderPhoneNo")
                                ToastWrapper(applicationContext).displayDebugToast("Extracted receiverPhoneNo = $receiverPhoneNo")


                                val sharedPreferences =
                                    SharedPreferencesFactory(applicationContext).getInstance()
                                val runModeHelper = ApplicationRunModesHelper(applicationContext)

                                // Setup base url based on geolocation
                                var baseUrl = DexcomConstants().baseUrlUsa
                                if (geolocation != DexcomConstants().usa) {
                                    baseUrl = DexcomConstants().baseUrlOutsideUsa
                                }
                                if (!geolocation.isNullOrEmpty()) {
                                    sharedPreferences.edit()
                                        .putString(DexcomConstants().baseUrlKey, baseUrl)
                                        .apply()
                                }

                                // Setup shared session
                                if (!sessionId.isNullOrEmpty()) {
                                    sharedPreferences.edit()
                                        .putString(UserPreferences.dexcomSessionId, sessionId)
                                        .apply()
                                }

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

                                // Save phone numbers from the Follower perspective
                                // - Sender is now the Follower (old receiver)
                                // - Receiver is now the Master (old sender)
                                if (!senderPhoneNo.isNullOrEmpty() && !receiverPhoneNo.isNullOrEmpty()) {
                                    sharedPreferences.edit()
                                        .putString(UserPreferences.receiverPhoneNo, senderPhoneNo)
                                        .putString(UserPreferences.senderPhoneNo, receiverPhoneNo)
                                        .apply()
                                }

                                // Enable Follower Mode
                                Log.i(
                                    "SmsBroadCastReceiver > processWakeUpSmsActionSessionSet",
                                    "Switching application mode to FOLLOWER ..."
                                )
                                runModeHelper.switchRunModeTo(ApplicationRunMode.FOLLOWER)

                                val redirectIntent =
                                    Intent(applicationContext, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                applicationContext.startActivity(redirectIntent)
                            }
                        }
                    }
                }
            }
        }
    }
}