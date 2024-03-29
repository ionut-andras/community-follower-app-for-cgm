package ionut.andras.community.cgm.follower.services.broadcast

object BroadcastActions {
    const val STOP_FOREGROUND_SERVICE = "ionut.andras.community.cgm.follower.STOP_FOREGROUND_SERVICE"

    const val AUTHENTICATION_FAILED = "ionut.andras.community.cgm.follower.AUTHENTICATION_FAILED"
    const val INVALID_SESSION = "ionut.andras.community.cgm.follower.INVALID_SESSION"
    const val USER_AUTHENTICATION_KEY_RETRIEVAL_FAILED = "ionut.andras.community.cgm.follower.USER_AUTHENTICATION_KEY_RETRIEVAL_FAILED"
    const val AUTHENTICATION_SESSION_SETUP_FAILED = "ionut.andras.community.cgm.follower.AUTHENTICATION_SESSION_SETUP_FAILED"

    const val GLUCOSE_DATA_CHANGED = "ionut.andras.community.cgm.follower.GLUCOSE_DATA_CHANGED"
    const val TOASTER_OK_GLUCOSE_VALUE = "ionut.andras.community.cgm.follower.TOASTER_OK_GLUCOSE_VALUE"
    const val USER_REQUEST_REFRESH = "ionut.andras.community.cgm.follower.USER_REQUEST_REFRESH"
    const val TEMPORARY_DISABLE_NOTIFICATIONS_SOUND = "ionut.andras.community.cgm.follower.TEMPORARY_DISABLE_NOTIFICATIONS_SOUND"
}