package ionut.andras.community.cgm.follower.constants

open class DexcomConstants () {
    var baseUrl = "https://share2.dexcom.com"
    var authenticationEndpoint = "/ShareWebServices/Services/General/AuthenticatePublisherAccount"
    var loginByAccountId = "/ShareWebServices/Services/General/LoginPublisherAccountById"
    var getGlucoseValueUrl = "/ShareWebServices/Services/Publisher/ReadPublisherLatestGlucoseValues"

    var httpHeadersArrayLogin = arrayOf(
        "User-Agent: Dexcom Share/3.0.2.11 CFNetwork/672.0.2 Darwin/14.0.0",
        "Accept: application/json",
        "Content-Type: application/json"
    )

    var httpHeadersArrayResources = arrayOf(
        "User-Agent: Dexcom Share/3.0.2.11 CFNetwork/672.0.2 Darwin/14.0.0",
        "Accept: application/json"
    )

    var applicationId = "d8665ade-9673-4e27-9ff6-92db4ce13d13"

    var glucoseInitialValue = "..."

    var messageInvalidAccountId = "Invalid account id"
    var messageInvalidSessionId = "Invalid session id"

    var messageNow = "Now"
    var messageMinutesAgo = "m ago"

    val stepDisplayIntervalSeconds = 5 * 60
    val maxDisplayIntervalSeconds = 24 * 3600
}
