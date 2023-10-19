package ionut.andras.community.cgm.follower.api

import org.json.JSONObject

class ApiResponse {
    var data: String? = null
    private var error: String? = null
    private var errorData: JSONObject? = null
    var exception: String? = null
    var noInternetConnection: Boolean = false

    fun setError(errorString: String) {
        error = errorString
        errorData = try {
            if (errorString.startsWith("{")) {
                JSONObject(errorString)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getError(): String? {
        return error
    }

    fun getErrorData(): JSONObject? {
        return errorData
    }

    fun isSuccess(): Boolean {
        return (null != data)
    }

    fun errorOccurred(): Boolean {
        return ((null != error) || (null != exception) || (noInternetConnection))
    }

    fun exceptionOccurred(): Boolean{
        return (null != exception)
    }

    fun noInternetConnectionError(): Boolean {
        return noInternetConnection
    }

    override fun toString(): String {
        return "{data: $data, error: $error, errorData: $errorData, exception: $exception, noInternetConnection: $noInternetConnection}"
    }
}