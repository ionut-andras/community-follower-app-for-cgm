package ionut.andras.community.cgm.follower.permissions

class PermissionRequestCodes {
    companion object {
        // Mandatory permissions
        const val FOREGROUND_SERVICE = 100
        const val GLUCOSE_VALUE_NOTIFICATION = 101
        const val BATTERY_OPTIMIZATION = 102

        // Temporary permissions
        const val SEND_SMS = 103
        const val RECEIVE_SMS = 104
        const val READ_SMS = 105
        const val READ_PHONE_STATE = 106

    }
}