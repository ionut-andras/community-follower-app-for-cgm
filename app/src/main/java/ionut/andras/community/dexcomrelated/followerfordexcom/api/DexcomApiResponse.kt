package ionut.andras.community.dexcomrelated.followerfordexcom.api

class DexcomApiResponse() {
    var data: String? = null
    var error: String? = null
    var exception: String? = null

    fun isSuccess(): Boolean {
        return (null != data)
    }

    fun errorOccurred(): Boolean {
        return ((null != error) && (null != exception))
    }

    fun exceptionOccurred(): Boolean{
        return (null != exception)
    }

    override fun toString(): String {
        return "{data: $data, error: $error, exception: $exception}"
    }
}