package ionut.andras.community.dexcomrelated.followerfordexcom.api

import android.util.Log
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomConstants
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DexcomApiRequestsHandler : DexcomConstants() {

    /**
     * Authenticates with username and password in order to get the Account ID
     *
     * @return DexcomApiResponse
     */
    fun authenticateWithUsernamePassword(username: String, password: String) : DexcomApiResponse {
        // Build the complete URL
        val urlString = baseUrl + authenticationEndpoint

        // Build JSON nody
        val jsonBody = JSONObject()
        jsonBody.put("accountName", username)
        jsonBody.put("password", password)
        jsonBody.put("applicationId", applicationId)
        return postHttpRequest(httpHeadersArrayLogin, urlString, jsonBody)
    }

    /**
     * Uses Account ID and password to get a valid session
     *
     * @param accountId String
     * @return DexcomApiResponse
     */
    fun loginWithAccountId(accountId: String?, password: String) : DexcomApiResponse {
        var returnValue = DexcomApiResponse()

        if (null != accountId) {

            // Build the complete URL
            val urlString = baseUrl + loginByAccountId

            // Build JSON nody
            val jsonBody = JSONObject()
            jsonBody.put("accountId", accountId)
            jsonBody.put("password", password)
            jsonBody.put("applicationId", applicationId)

            returnValue = postHttpRequest(httpHeadersArrayLogin, urlString, jsonBody)
        } else {
            returnValue.error = messageInvalidAccountId
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
    fun getLatestGlucoseValues(sessionId: String?, minutes: Int = 1440, count: Int = 1) : DexcomApiResponse {
        var returnValue = DexcomApiResponse()

        if (null != sessionId) {

            // Build the complete URL
            val urlString = baseUrl + getGlucoseValueUrl + "?sessionId=${sessionId}&minutes=${minutes}&maxCount=${count}"

           returnValue = postHttpRequest(httpHeadersArrayResources, urlString, null)
        } else {
            returnValue.error = messageInvalidSessionId
        }
        return returnValue
    }

    /**
     * Performs a HTTP POST request
     *
     * @param httpHeadersArray Array<String>
     * @param urlString String
     * @param jsonBody String
     * @return DexcomApiResponse
     */
    private fun postHttpRequest(httpHeadersArray: Array<String>, urlString: String, jsonBody: JSONObject?) : DexcomApiResponse {
        return httpRequest("POST", httpHeadersArray, urlString, jsonBody)
    }

    /**
     * Performs a HTTP request
     *
     * @param method String. Default is POST
     * @param httpHeadersArray Array<String>
     * @param urlString String
     * @param jsonBody String
     * @return DexcomApiResponse
     */
    private fun httpRequest(method: String = "POST", httpHeadersArray: Array<String>, urlString: String, jsonBody: JSONObject?) : DexcomApiResponse {
        val returnValue = DexcomApiResponse()

        try {
            // Start HTTP Request routine
            val url = URL(urlString)
            Log.i("httpRequest > url", url.toString())
            val connection = url.openConnection() as HttpsURLConnection

            try {
                connection.requestMethod = method
                httpHeadersArray.map { header ->
                    val (key, value) = header.split(":").map { str -> str.trim() }
                    connection.setRequestProperty(key, value)
                }

                httpHeadersArray.map {
                    Log.i("httpRequest > httpHeadersArray > $it", it.trim())
                }

                connection.doOutput = true
                Log.i("httpRequest > jsonBody", jsonBody.toString())
                if (null != jsonBody) {
                    connection.outputStream.write(jsonBody.toString().toByteArray())
                }

                Log.i("httpRequest > responseCode", connection.responseCode.toString())

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    // Receive response as inputStream
                    returnValue.data = connection.inputStream.bufferedReader().readText()
                } else if (connection.responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    // Receive response as inputStream
                    returnValue.error = connection.errorStream.bufferedReader().readText()
                } else {
                    returnValue.error = connection.responseMessage
                }

            } catch (ex: Exception) {
                returnValue.exception = ex.localizedMessage
                returnValue.noInternetConnection = true
            } finally {
                connection.disconnect()
            }
        } catch (exception: Exception) {
            returnValue.exception = exception.localizedMessage
            returnValue.noInternetConnection = true
        }
        Log.i("httpRequest > returnValue", returnValue.toString())
        return returnValue
    }

}