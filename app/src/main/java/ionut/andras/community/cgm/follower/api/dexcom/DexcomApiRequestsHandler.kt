package ionut.andras.community.cgm.follower.api.dexcom

import ionut.andras.community.cgm.follower.api.ApiResponse
import ionut.andras.community.cgm.follower.api.HttpRequestHandler
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import org.json.JSONObject

class DexcomApiRequestsHandler : HttpRequestHandler() {
    private var dexcomConstants: DexcomConstants = DexcomConstants()

    /**
     * Authenticates with username and password in order to get the Account ID
     *
     * @return DexcomApiResponse
     */
    fun authenticateWithUsernamePassword(username: String, password: String) : ApiResponse {
        // Build the complete URL
        val urlString = dexcomConstants.baseUrl + dexcomConstants.authenticationEndpoint

        // Build JSON nody
        val jsonBody = JSONObject()
        jsonBody.put("accountName", username)
        jsonBody.put("password", password)
        jsonBody.put("applicationId", dexcomConstants.applicationId)
        return postHttpRequest(dexcomConstants.httpHeadersArrayLogin, urlString, jsonBody)
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
            val urlString = dexcomConstants.baseUrl + dexcomConstants.loginByAccountId

            // Build JSON nody
            val jsonBody = JSONObject()
            jsonBody.put("accountId", accountId)
            jsonBody.put("password", password)
            jsonBody.put("applicationId", dexcomConstants.applicationId)

            returnValue = postHttpRequest(dexcomConstants.httpHeadersArrayLogin, urlString, jsonBody)
        } else {
            returnValue.error = dexcomConstants.messageInvalidAccountId
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
            val urlString = dexcomConstants.baseUrl + dexcomConstants.getGlucoseValueUrl + "?sessionId=${sessionId}&minutes=${minutes}&maxCount=${count}"

           returnValue = postHttpRequest(dexcomConstants.httpHeadersArrayResources, urlString, null)
        } else {
            returnValue.error = dexcomConstants.messageInvalidSessionId
        }
        return returnValue
    }
}