package ionut.andras.community.cgm.follower.api.dexcom

import ionut.andras.community.cgm.follower.constants.DexcomConstants

class EndpointsManagement() {
    /**
     * Gets the gelocalication of the current user Account based on base url.
     *
     * @param baseUrl: String?
     * @return String
     */
    fun getDomainGeolocationZone(baseUrl: String?): String{
        // Detect geolocation
        var geo = DexcomConstants().geolocationUsa
        if ((null != baseUrl) && (baseUrl != DexcomConstants().baseUrlUsa)) {
            geo = DexcomConstants().geolocationOutsideUsa
        }
        return geo
    }

    /**
     * Gets the base url of the current user Account based on Account geolocalization.
     *
     * @param geolocation: String?
     * @return String
     */
    fun getGeolocationZoneDomain(geolocation: String): String {
        // Setup base url based on geolocation
        var baseUrl = DexcomConstants().baseUrlUsa
        if (geolocation != DexcomConstants().geolocationUsa) {
            baseUrl = DexcomConstants().baseUrlOutsideUsa
        }
        return baseUrl
    }
}