package ionut.andras.community.cgm.follower.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.toast.ToastWrapper

class PermissionHandler(private val activity: AppCompatActivityWrapper,private val context: Context) {
    fun checkPermission(androidPermissionCode: String, permissionFriendlyName: String, permissionRequestCode: Int){
        when {
            // Case 1: Permissions already granted
            ContextCompat.checkSelfPermission(context, androidPermissionCode) == PackageManager.PERMISSION_GRANTED -> {
                // ToastWrapper(context).displayInfoToast("$permissionFriendlyName " + context.getString(R.string.textPermissionGranted))
            }

            // Case 2:  Permissions should be explained
            ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermissionCode) -> {
                showDialog(androidPermissionCode, permissionFriendlyName, permissionRequestCode)
            }

            //  Case default: Permission should be requested
            else -> {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(androidPermissionCode),
                    permissionRequestCode
                )
            }
        }
    }

    fun onRequestPermissionResult(permissionFriendlyName: String, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // ToastWrapper(context).displayInfoToast("$permissionFriendlyName " + context.getString(R.string.textPermissionDenied))
        } else {
            // ToastWrapper(context).displayInfoToast("$permissionFriendlyName " + context.getString(R.string.textPermissionGranted))
        }
    }

    private fun showDialog(androidPermissionCode: String, permissionFriendlyName: String, permissionRequestCode: Int) {
        val title = context.getString(R.string.textPermissionRequired)
        val text = context.getString(R.string.textPermissionsRequiredToUseInviteFollowerFunction) + ": $permissionFriendlyName"

        ToastWrapper(context).showDialog(title, text) { dialog, which ->
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(androidPermissionCode),
                permissionRequestCode
            )
        }
    }
}
