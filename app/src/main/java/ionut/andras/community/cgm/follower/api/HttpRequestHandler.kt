package ionut.andras.community.cgm.follower.api

import android.util.Log
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

open class HttpRequestHandler {
    /**
     * Performs a HTTP GET request
     *
     * @param httpHeadersArray Array<String>
     * @param urlString String
     * @param jsonBody String
     * @return DexcomApiResponse
     */
    fun getHttpRequest(httpHeadersArray: Array<String>, urlString: String, jsonBody:JSONObject?) : ApiResponse {
        return httpRequest("GET", httpHeadersArray, urlString, jsonBody)
    }

    /**
     * Performs a HTTP POST request
     *
     * @param httpHeadersArray Array<String>
     * @param urlString String
     * @param jsonBody String
     * @return DexcomApiResponse
     */
    fun postHttpRequest(httpHeadersArray: Array<String>, urlString: String, jsonBody:JSONObject?) : ApiResponse {
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
    private fun httpRequest(method: String = "POST", httpHeadersArray: Array<String>, urlString: String, jsonBody: JSONObject?) : ApiResponse {
        val returnValue = ApiResponse()

        try {
            // Start HTTP Request routine
            val url = URL(urlString)
            Log.i("httpRequest > url", url.toString())
            val connection = url.openConnection() as HttpsURLConnection

            try {
                connection.requestMethod = method
                Log.i("httpRequest > method", connection.requestMethod)

                httpHeadersArray.forEach { header ->
                    // Split on the first ':' only, so header values may themselves contain ':'
                    val (key, value) = header.split(":", limit = 2).map { str -> str.trim() }
                    connection.setRequestProperty(key, value)

                    val loggedHeader = if (key.equals("X-App-Hash", ignoreCase = true)) "$key: <redacted>" else header.trim()
                    Log.i("httpRequest > httpHeadersArray", loggedHeader)
                }
                Log.i("httpRequest > httpHeadersArray", "Host: " + url.host)

                // There is a known issue in the Android Kotlin HttpsURLConnection class
                // where the request method is set to POST even if the connection.requestMethod property is set to GET.
                // This issue was reported in 2019 and is still present in the latest version of the Android SDK (Android 13).
                connection.doOutput = (method == "POST")

                if (null != jsonBody) {
                    Log.i("httpRequest > jsonBody", jsonBody.toString())
                    connection.outputStream.write(jsonBody.toString().toByteArray())
                }

                val responseCode = connection.responseCode
                Log.i("httpRequest > responseCode", responseCode.toString())

                if (HttpsURLConnection.HTTP_OK == responseCode) {
                    // Receive response as inputStream
                    returnValue.data = connection.inputStream.bufferedReader().readText()
                } else {
                    // Prefer the error body (the backend replies with JSON on every error).
                    // Fall back to the status line, which may be empty over HTTP/2.
                    val errorBody = connection.errorStream?.bufferedReader()?.readText()
                    val errorMessage = if (errorBody.isNullOrEmpty()) {
                        "HTTP $responseCode " + (connection.responseMessage ?: "")
                    } else {
                        errorBody
                    }
                    returnValue.setError(errorMessage.trim())
                }

            } catch (exception1: Exception) {
                returnValue.exception = exception1.toString()
                returnValue.noInternetConnection = true
                Log.i("httpRequest > exception1: ", exception1.toString())
            } finally {
                connection.disconnect()
            }
        } catch (exception2: Exception) {
            returnValue.exception = exception2.toString()
            returnValue.noInternetConnection = true
            Log.i("httpRequest > exception2: ", exception2.toString())
        }
        Log.i("httpRequest > returnValue", returnValue.toString())
        return returnValue
    }

}
