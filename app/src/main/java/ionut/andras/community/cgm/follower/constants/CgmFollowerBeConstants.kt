package ionut.andras.community.cgm.follower.constants

class CgmFollowerBeConstants {
    var baseUrl = "https://cgm-follower-be.cgmtools.com"
    var sessionManagementEndpoint = "/session"

    /**
     * Shared secret expected by the CGM Follower backend (env.APP_HASH on the Cloudflare Worker).
     *
     * The backend answers HTTP 401 to every request whose secret does not match the value
     * configured with `wrangler secret put APP_HASH`, so this constant MUST be kept in sync
     * with the deployed secret.
     */
    var appHash = "1c0232e5dc508b7f79f116f9d4feb8d1fd6209077e5067bcb1fa9ac816fc85ec"

    var httpHeadersArray = arrayOf(
        "User-Agent: CgmFollower Base/1.0.0 Kotlin App",
        "Accept: application/json",
        "Content-Type: application/json",
        // Authorizes both GET and POST calls (GET has no body to carry app_hash in)
        "X-App-Hash: $appHash"
    )
}
