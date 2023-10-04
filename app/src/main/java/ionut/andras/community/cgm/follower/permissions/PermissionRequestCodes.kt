package ionut.andras.community.cgm.follower.permissions

class PermissionRequestCodes {
    companion object {
        // Mandatory permissions
        const val FOREGROUND_SERVICE = 100
        const val FOREGROUND_SERVICE_DATA_SYNC = 101
        const val GLUCOSE_VALUE_NOTIFICATION = 102
        const val BATTERY_OPTIMIZATION = 103

        // Temporary permissions
        const val SEND_SMS = 104
        const val READ_PHONE_STATE = 105

    }
}