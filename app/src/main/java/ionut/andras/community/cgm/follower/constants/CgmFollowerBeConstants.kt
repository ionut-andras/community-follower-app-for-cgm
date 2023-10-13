package ionut.andras.community.cgm.follower.constants

class CgmFollowerBeConstants {
    var baseUrl = "https://cgm-follower-be.cgmtools.com"
    var sessionManagementEndpoint = "/session"

    var httpHeadersArray = arrayOf(
        "User-Agent: CgmFollower Base/1.0.0 Kotlin App",
        "Accept: application/json",
        "Content-Type: application/json"
    )
}