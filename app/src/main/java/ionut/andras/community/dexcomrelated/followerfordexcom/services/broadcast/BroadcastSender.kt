package ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast

import android.content.Context
import android.content.Intent
import android.util.Log

class BroadcastSender(private var applicationContext: Context, private var broadcastAction: String) {

    fun broadcast(variableName: String, dataString: String) {
        Log.i("GlucoseDataUpdateBroadcastSender: ", ">>>>>>>>>>>>>> Broadcasting to MainActivity >>>>>>>>>>>>>>")
        val intent = Intent(broadcastAction)
        intent.putExtra(variableName, dataString)
        applicationContext.sendBroadcast(intent)
    }
}