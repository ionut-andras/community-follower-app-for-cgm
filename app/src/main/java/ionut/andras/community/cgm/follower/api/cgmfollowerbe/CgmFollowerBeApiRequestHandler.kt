package ionut.andras.community.cgm.follower.api.cgmfollowerbe

import android.content.Context
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.api.ApiResponse
import ionut.andras.community.cgm.follower.api.HttpRequestHandler
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.CgmFollowerBeConstants
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import org.json.JSONObject

class CgmFollowerBeApiRequestHandler(private val applicationContext: Context): HttpRequestHandler() {
    private val cgmFollowerBeConstants = CgmFollowerBeConstants()

    /**
     * Sets the session on the CGM Follower Backend.
     *
     * @param receiverPhoneNo: String
     * @param senderPhoneNo: String
     * @return ApiResponse
     */
    fun setSession(receiverPhoneNo: String = "", senderPhoneNo: String = "") : ApiResponse {
        var returnValue = ApiResponse()
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
        val sessionId = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        var senderPhoneNumber = senderPhoneNo
        // If no senderPhoneNumber specified, try to get it from SharedPreferences
        if (senderPhoneNo.isEmpty()) {
            senderPhoneNumber =
                sharedPreferences.getString(UserPreferences.senderPhoneNo, "").toString()
        }
        val baseUrl = sharedPreferences.getString(DexcomConstants().baseUrlKey, null)

        if (receiverPhoneNo.isNotEmpty() && receiverPhoneNo.isNotEmpty()) {
            /*{
                "geo": "us"
                "session": "8dfe387c-a322-430f-9b82-c23965d427b8",
                "notifications_enabled_flag": "0",
                "phone_sender": "+40111111111",
                "phone_receiver": "+40222222222",
                "app_hash": "5de6329f620f38f0eaddb58cbafce54f"
            }*/

            // Detect geolocation
            var geo = DexcomConstants().usa
            if (baseUrl != DexcomConstants().baseUrlUsa) {
                geo = DexcomConstants().outsideUsa
            }

            // Build the JSON object
            val jsonBody = JSONObject()

            jsonBody.put("geo", geo)
            jsonBody.put("session", sessionId)
            jsonBody.put("notifications_enabled_flag", 0)
            jsonBody.put("phone_sender", senderPhoneNumber)
            jsonBody.put("phone_receiver", receiverPhoneNo)
            jsonBody.put("app_hash", "5de6329f620f38f0eaddb58cbafce54f")

            // Build the complete URL
            val urlString = cgmFollowerBeConstants.baseUrl + cgmFollowerBeConstants.sessionManagementEndpoint
            // Perform the HTTP call
            returnValue = postHttpRequest(cgmFollowerBeConstants.httpHeadersArray, urlString, jsonBody)
        } else {
            returnValue.error = applicationContext.getString(R.string.textPhoneNumberNullEmptyInvalid)
        }
        return returnValue
    }

    /**
     * Get new / renewed session from the CGM Follower Backend.
     *
     * @param userKey String
     * @return ApiResponse
     */
    fun getSession(userKey: String) : ApiResponse {
        var returnValue = ApiResponse()

        if (userKey.isNotEmpty()) {
            // Build the complete URL
            val urlString = cgmFollowerBeConstants.baseUrl + cgmFollowerBeConstants.sessionManagementEndpoint + "?userKey=$userKey"
            // Perform the HTTP cal
            returnValue = getHttpRequest(cgmFollowerBeConstants.httpHeadersArray, urlString, null)
        } else {
            returnValue.error = applicationContext.getString(R.string.messageInvalidUserKey)
        }
        return returnValue
    }
}