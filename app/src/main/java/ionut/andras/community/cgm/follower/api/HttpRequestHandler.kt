package ionut.andras.community.cgm.follower.api

import android.util.Log
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

open class HttpRequestHandler {
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

                httpHeadersArray.map { header ->
                    val (key, value) = header.split(":").map { str -> str.trim() }
                    connection.setRequestProperty(key, value)
                }
                httpHeadersArray[0] = "Host: " + url.host

                httpHeadersArray.map {
                    Log.i("httpRequest > httpHeadersArray", it.trim())
                }

                // There is a known issue in the Android Kotlin HttpsURLConnection class
                // where the request method is set to POST even if the connection.requestMethod property is set to GET.
                // This issue was reported in 2019 and is still present in the latest version of the Android SDK (Android 13).
                connection.doOutput = (method == "POST")

                if (null != jsonBody) {
                    Log.i("httpRequest > jsonBody", jsonBody.toString())
                    connection.outputStream.write(jsonBody.toString().toByteArray())
                }

                Log.i("httpRequest > responseCode", connection.responseCode.toString())

                when (connection.responseCode) {
                    HttpsURLConnection.HTTP_OK -> {
                        // Receive response as inputStream
                        returnValue.data = connection.inputStream.bufferedReader().readText()
                    }
                    HttpsURLConnection.HTTP_INTERNAL_ERROR -> {
                        // Receive response as inputStream
                        returnValue.setError(connection.errorStream.bufferedReader().readText())
                    }
                    else -> {
                        returnValue.setError(connection.responseMessage)
                    }
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