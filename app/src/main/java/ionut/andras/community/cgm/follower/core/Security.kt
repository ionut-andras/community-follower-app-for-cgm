package ionut.andras.community.cgm.follower.core

import java.security.MessageDigest

class Security {
    fun md5(data: String): String {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(data.toByteArray())
        val md5Hash = md5.digest()
        return md5Hash.joinToString("") { "%02x".format(it) }
    }
}