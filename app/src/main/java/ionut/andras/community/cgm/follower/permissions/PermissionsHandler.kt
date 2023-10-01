package ionut.andras.community.cgm.follower.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler(private val context: Context) {
    companion object {
        const val REQUEST_CODE = 200
    }

    /**
     * Check if minimum permissions needed by the application are requested from the user.
     */
    fun checkPermissions(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE)
        val result2 = ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        val result3 = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        val result4 = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
        val result5 = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        val result6 = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
        val result7 = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        Log.i("PermissionHandler > checkPermissions", result1.toString())
        Log.i("PermissionHandler > checkPermissions", result2.toString())
        Log.i("PermissionHandler > checkPermissions", result3.toString())
        Log.i("PermissionHandler > checkPermissions", result4.toString())
        Log.i("PermissionHandler > checkPermissions", result5.toString())
        Log.i("PermissionHandler > checkPermissions", result6.toString())
        Log.i("PermissionHandler > checkPermissions", result7.toString())
        return result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED
                && result3 == PackageManager.PERMISSION_GRANTED
                && result4 == PackageManager.PERMISSION_GRANTED
                && result5 == PackageManager.PERMISSION_GRANTED
                && result6 == PackageManager.PERMISSION_GRANTED
                && result7 == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE
            ),
            REQUEST_CODE
        )
    }
//
//    fun handleBatteryOptimizations() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
//            intent.data = Uri.parse("package:" + context.packageName)
//            context.startActivity(intent)
//        }
//    }
}
