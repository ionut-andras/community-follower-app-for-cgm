package ionut.andras.community.cgm.follower.services.broadcast

import android.content.Context
import android.content.Intent
import android.util.Log
import ionut.andras.community.cgm.follower.configuration.Configuration

class BroadcastSender(private var applicationContext: Context, private var broadcastAction: String) {
    private val intent = Intent()

    init {
        intent.action = broadcastAction
    }

    fun addInfo(variableName: String, data: String?): BroadcastSender {
        intent.putExtra(variableName, data)
        return this
    }

    fun addInfo(variableName: String, data: Configuration?): BroadcastSender {
        intent.putExtra(variableName, data)
        return this
    }

    fun broadcast() {
        Log.i("GlucoseDataUpdateBroadcastSender: ", ">>>>>>>>>>>>>> Broadcasting >>>>>>>>>>>>>>")
        Log.i("GlucoseDataUpdateBroadcastSender > Action: ", broadcastAction)
        Log.i("GlucoseDataUpdateBroadcastSender > Extra: ", intent.extras.toString())
        applicationContext.sendBroadcast(intent)
    }
}