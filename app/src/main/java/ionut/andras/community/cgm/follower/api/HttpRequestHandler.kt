package ionut.andras.community.cgm.follower.api

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

open class HttpRequestHandler {
    private var defaultDispatcher: CoroutineDispatcher = Dispatchers.IO

    /**
     * Performs a HTTP POST request
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
     * Performs a HTTP POST request
     *
     * @param httpHeadersArray Array<String>
     * @param urlString String
     * @param jsonBody String
     * @return DexcomApiResponse
     */
    fun postHttpAsyncRequest(httpHeadersArray: Array<String>, urlString: String, jsonBody:JSONObject?) : ApiResponse {
        var apiCallResponse = ApiResponse()
        GlobalScope.launch(defaultDispatcher) {
            apiCallResponse = httpRequest("POST", httpHeadersArray, urlString, jsonBody)
        }
        return apiCallResponse
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

                if (connection.responseCode == HttpsURLConnection.HTTP_OK) {
                    // Receive response as inputStream
                    returnValue.data = connection.inputStream.bufferedReader().readText()
                } else if (connection.responseCode == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                    // Receive response as inputStream
                    returnValue.error = connection.errorStream.bufferedReader().readText()
                } else {
                    returnValue.error = connection.responseMessage
                }

            } catch (exception1: Exception) {
                returnValue.exception = exception1.toString()
                returnValue.noInternetConnection = true
            } finally {
                connection.disconnect()
            }
        } catch (exception2: Exception) {
            returnValue.exception = exception2.toString()
            returnValue.noInternetConnection = true
        }
        Log.i("httpRequest > returnValue", returnValue.toString())
        return returnValue
    }

}