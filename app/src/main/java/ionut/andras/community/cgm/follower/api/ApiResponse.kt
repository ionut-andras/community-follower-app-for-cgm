package ionut.andras.community.cgm.follower.api

class ApiResponse() {
    var data: String? = null
    var error: String? = null
    var exception: String? = null
    var noInternetConnection: Boolean = false

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
        return "{data: $data, error: $error, exception: $exception, noInternetConnection: $noInternetConnection}"
    }
}