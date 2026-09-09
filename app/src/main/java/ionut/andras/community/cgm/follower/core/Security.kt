package ionut.andras.community.cgm.follower.core

import java.security.MessageDigest

class Security {

    /**
     * SHA-256 hex digest. The CGM Follower backend derives the session userKey with
     * SHA-256(phone_receiver + "-" + phone_sender), so the app must use the same algorithm.
     */
    fun sha256(data: String): String {
        return digest("SHA-256", data)
    }

    private fun digest(algorithm: String, data: String): String {
        val messageDigest = MessageDigest.getInstance(algorithm)
        messageDigest.update(data.toByteArray())
        return messageDigest.digest().joinToString("") { "%02x".format(it) }
    }
}
