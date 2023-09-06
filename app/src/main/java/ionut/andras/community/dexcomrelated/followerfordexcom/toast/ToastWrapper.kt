package ionut.andras.community.dexcomrelated.followerfordexcom.toast

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import ionut.andras.community.dexcomrelated.followerfordexcom.R
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastActions
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastSender

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

    fun displayMessageToast(view: View, text: String) {
        val customToast = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
        customToast.setAction("OK") {
            Log.i("displayMessageToast", "OK button clicked")
            BroadcastSender(appContext, BroadcastActions.TOASTER_OK_GLUCOSE_VALUE)
                .addInfo(appContext.getString(R.string.variableNameGenericData), "OK")
                .broadcast()
        }
        customToast.show()
    }
}