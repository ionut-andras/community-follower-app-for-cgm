package ionut.andras.community.dexcomrelated.followerfordexcom.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import ionut.andras.community.dexcomrelated.followerfordexcom.R
import java.util.concurrent.atomic.AtomicInteger

class NotificationsManager (private var appContext: Context) {
    private lateinit var androidNotificationManager: NotificationManager

    private lateinit var icon: IconCompat

    private var builderContentIntent: PendingIntent? = null

    private var notificationIdHandler: AtomicInteger = AtomicInteger(2)

    private var autoCancelNotification: Boolean = false

    fun setNotificationIcon(icon: IconCompat) {
        this.icon = icon
    }

    fun setBuilderContentIntent(intent: PendingIntent) {
        this.builderContentIntent = intent
    }

    fun setAutoCancelNotificationFlag(autoCancelFlag: Boolean= false) {
        this.autoCancelNotification = autoCancelFlag
    }

    fun triggerNotification(title: String, message: String): Int {
        var notificationID = 0
        if (autoCancelNotification) {
            // Log.i(">>>>>>>>>>>>>>>", "GetAndIncrement")
            // Notifications that will be automatically cancelled after a time period needs to have different IDs
            notificationID = notificationIdHandler.getAndIncrement()
        } else {
            // Log.i(">>>>>>>>>>>>>>>", "Get")
            notificationID = notificationIdHandler.get()
        }

        prepareNotificationChannel()

        var  builder = createNotificationBuilder(title, message)
        if (null != builderContentIntent) {
            builder.setContentIntent(builderContentIntent)
        }

        with(NotificationManagerCompat.from(appContext)) {
            // NotificationId is a unique int for each notification that you must define
            // notify(notificationIdHandler.getAndIncrement(), builder.build())
            notify(notificationID, builder.build())
        }
        return notificationID
    }

    fun triggerAutoCancelNotification(title: String, message: String, autoCancelDelayMillis: Long = 1000) {
        var notificationID = triggerNotification(title, message)

        var handler = Handler(Looper.getMainLooper())

        var runnable = Runnable {
            androidNotificationManager.cancel(notificationID)
        }
        handler.postDelayed(runnable, autoCancelDelayMillis)
    }

    private fun prepareNotificationChannel(){
        createNotificationChannel(
            appContext.getString(R.string.notificationChannelId),
            appContext.getString(R.string.notificationChannelName),
            appContext.getString(R.string.notificationChannelDescription)
        )
    }

    fun createNotificationBuilder(title: String, message: String): NotificationCompat.Builder {
        // Prepare notification channel
        prepareNotificationChannel()
        return NotificationCompat.Builder(appContext, appContext.getString(R.string.notificationChannelId))
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // .setAutoCancel(false)
    }

    private fun createNotificationChannel(channelId: String, channelName: String, channelDescription: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
            enableVibration(true)
        }

        // Register the channel with the system
        androidNotificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        androidNotificationManager.createNotificationChannel(channel)
    }
}