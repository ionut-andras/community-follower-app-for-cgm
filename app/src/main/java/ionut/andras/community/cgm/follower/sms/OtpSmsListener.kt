package ionut.andras.community.cgm.follower.sms

import android.content.Context
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.tasks.Task
import ionut.andras.community.cgm.follower.toast.ToastWrapper

class OtpSmsListener(context: Context) {
    // Get an instance of SmsRetrieverClient, used to start listening for a matching
    // SMS message.
    private var client: SmsRetrieverClient

    // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
    // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
    // action SmsRetriever#SMS_RETRIEVED_ACTION.
    private var task: Task<Void>

    init {
        client = SmsRetriever.getClient(context)
        task = client.startSmsRetriever()

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener {
            // Successfully started retriever, expect broadcast intent
            ToastWrapper(context).displayDebugToast("Successfully started SMS retriever service")
        }
        task.addOnFailureListener {
            // Failed to start retriever, inspect Exception for more details
            ToastWrapper(context).displayDebugToast("Failed to start SMS retriever service")
        }
    }
}