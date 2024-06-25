package ionut.andras.community.cgm.follower.api.dexcom

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ionut.andras.community.cgm.follower.api.ApiResponse
import ionut.andras.community.cgm.follower.api.HttpRequestHandler
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import org.json.JSONObject

class DexcomApiRequestsHandler (applicationContext: Context) : HttpRequestHandler() {
    private var dexcomConstants: DexcomConstants = DexcomConstants()
    private var baseUrl: String = dexcomConstants.baseUrlUsa
    private var sharedPreferences: SharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

    init {
        val savedBaseUrl = sharedPreferences.getString(DexcomConstants().baseUrlKey, null)
        if (null != savedBaseUrl) {
            baseUrl = savedBaseUrl
        }
    }

    /**
     * Authenticates with username and password in order to get the Account ID
     *
     * @return DexcomApiResponse
     */
    fun authenticateWithUsernamePassword(username: String, password: String) : ApiResponse {
        // Build the complete URL
        val urlString = baseUrl + dexcomConstants.authenticationEndpoint

        // Build JSON nody
        val jsonBody = JSONObject()
        jsonBody.put("accountName", username)
        jsonBody.put("password", password)
        jsonBody.put("applicationId", dexcomConstants.applicationId)
        var authenticationApiResponse = postHttpRequest(dexcomConstants.httpHeadersArrayLogin, urlString, jsonBody)
        Log.i("authenticateWithUsernamePassword (Attempt 1)", authenticationApiResponse.toString())
        if (authenticationApiResponse.errorOccurred()) {
            authenticationApiResponse.getError()?.let{
                val errorData = JSONObject(it)
                if (
                    (errorData.getString("Code") == "AccountPasswordInvalid") ||
                    (errorData.getString("Code") == "SSO_InternalError")
                    ) {
                    if (baseUrl == dexcomConstants.baseUrlUsa) {
                        baseUrl = dexcomConstants.baseUrlOutsideUsa
                        authenticationApiResponse = authenticateWithUsernamePassword(username, password)
                        Log.i("authenticateWithUsernamePassword (Attempt 2)", authenticationApiResponse.toString())
                    }
                }
            }
        }

        // Upon successful authentication save the user geolocation
        val baseUrlGeolocation = EndpointsManagement().getDomainGeolocationZone(baseUrl)
        sharedPreferences.edit()
            .putString(DexcomConstants().baseUrlKey, baseUrl)
            .putString(DexcomConstants().baseUrlGeolocationKey, baseUrlGeolocation )
            .apply()

        Log.i("authenticateWithUsernamePassword > baseUrl", baseUrl)
        Log.i("authenticateWithUsernamePassword > baseUrlGeolocation", baseUrlGeolocation)

        return authenticationApiResponse
    }

    /**
     * Uses Account ID and password to get a valid session
     *
     * @param accountId String
     * @return DexcomApiResponse
     */
    fun loginWithAccountId(accountId: String?, password: String) : ApiResponse {
        var returnValue = ApiResponse()

        if (null != accountId) {

            // Build the complete URL
            val urlString = baseUrl + dexcomConstants.loginByAccountId

            // Build JSON nody
            val jsonBody = JSONObject()
            jsonBody.put("accountId", accountId)
            jsonBody.put("password", password)
            jsonBody.put("applicationId", dexcomConstants.applicationId)

            returnValue = postHttpRequest(dexcomConstants.httpHeadersArrayLogin, urlString, jsonBody)
        } else {
            returnValue.setError(dexcomConstants.messageInvalidAccountId)
        }

        return returnValue
    }

    /**
     * Get the list of values for the glucose withing a period.
     * Default period is 1 day and the default number of measures is 1.
     *
     * @param sessionId String
     * @param minutes int
     * @param count int
     * @return DexcomApiResponse
     */
    fun getLatestGlucoseValues(sessionId: String?, minutes: Int = 1440, count: Int = 1) : ApiResponse {
        var returnValue = ApiResponse()

        if (null != sessionId) {

            // Build the complete URL
            val urlString = baseUrl + dexcomConstants.getGlucoseValueUrl + "?sessionId=${sessionId}&minutes=${minutes}&maxCount=${count}"

           returnValue = postHttpRequest(dexcomConstants.httpHeadersArrayResources, urlString, null)
        } else {
            returnValue.setError(dexcomConstants.messageInvalidSessionId)
        }
        return returnValue
    }
}