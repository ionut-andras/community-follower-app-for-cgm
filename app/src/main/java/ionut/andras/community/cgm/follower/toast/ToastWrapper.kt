package ionut.andras.community.cgm.follower.toast

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.services.broadcast.BroadcastSender
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory

class ToastWrapper(context: Context?) : Toast(context) {
    private lateinit var appContext: Context

    init {
        setApplicationContext(context)
    }

    private fun setApplicationContext(context: Context?) {
        if (context != null) {
            appContext = context
        }
    }

    fun displayMessageToast(view: View, text: String, action: String? = null) {
        val customToast = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
        customToast.setAction("OK") {
            Log.i("displayMessageToast", "OK button clicked")
            action?.let {
                BroadcastSender(appContext, it)
                    .addInfo(appContext.getString(R.string.variableNameGenericData), "OK")
                    .broadcast()
            }
        }
        customToast.show()
    }

    fun displayInfoToast(text: String) {
        makeText(appContext, text, LENGTH_SHORT).show()
    }

    fun displayDebugToast(text: String) {
        val sharedPreferences = SharedPreferencesFactory(appContext).getInstance()

        val enableDebugMode = sharedPreferences.getBoolean(UserPreferences.enableDebugMode, Configuration().enableDebugMode)
        if (enableDebugMode) {
            makeText(appContext, text, LENGTH_SHORT).show()
        }
    }

    fun showDialog(title: String, text: String, onClickListener: android.content.DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(appContext)
        builder.apply {
            setTitle(title)
            setMessage(text)
            setPositiveButton(context.getString(R.string.textOk), onClickListener)
        }
        builder.create().show()
    }
}