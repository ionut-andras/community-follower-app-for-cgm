package ionut.andras.community.cgm.follower.notifications

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.alarms.DexcomAlarmSoundMap
import ionut.andras.community.cgm.follower.alarms.DexcomAlarmType
import ionut.andras.community.cgm.follower.constants.DexcomTrendsConversionMap
import ionut.andras.community.cgm.follower.permissions.PermissionHandler
import java.util.concurrent.atomic.AtomicInteger

class NotificationsManager (private var appContext: Context) {
    private lateinit var androidNotificationManager: NotificationManager

    private lateinit var icon: IconCompat

    private var soundUri: Uri = Uri.EMPTY

    private var alarmType: String = DexcomAlarmType.NORMAL

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

    fun setAlarmType(alarmType: String) {
        if (DexcomAlarmType.isAlarmType(alarmType)) {
            this.alarmType = alarmType
        }
    }

    private fun setSoundUrl(soundUrl: String) {
        this.soundUri = Uri.parse(soundUrl)
    }

    private fun clearSound() {
        soundUri = Uri.EMPTY
    }

    fun triggerNotification(title: String, message: String): Int {
        val notificationID = if (autoCancelNotification) {
            // Log.i(">>>>>>>>>>>>>>>", "GetAndIncrement")
            // Notifications that will be automatically cancelled after a time period needs to have different IDs
            notificationIdHandler.getAndIncrement()
        } else {
            // Log.i(">>>>>>>>>>>>>>>", "Get")
            notificationIdHandler.get()
        }

        prepareNotificationChannels()

        val builder = createNotificationBuilder(title, message)
        if (null != builderContentIntent) {
            builder.setContentIntent(builderContentIntent)
        }

        with(NotificationManagerCompat.from(appContext)) {
            // NotificationId is a unique int for each notification that you must define
            // notify(notificationIdHandler.getAndIncrement(), builder.build())

            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return 0
            }
            notify(notificationID, builder.build())
        }
        return notificationID
    }

    fun triggerAutoCancelNotification(title: String, message: String, autoCancelDelayMillis: Long = 1000) {
        val notificationID = triggerNotification(title, message)

        val handler = Handler(Looper.getMainLooper())

        val runnable = Runnable {
            androidNotificationManager.cancel(notificationID)
        }
        handler.postDelayed(runnable, autoCancelDelayMillis)
    }

    fun prepareNotificationChannels(){
        // Create notification channel. This happens only once because, even if the channel is updated,
        // the androidNotificationManager.createNotificationChannel(channel) function will do nothing
        // as the channel is already registered
        // Moreover, a notification channel that is associated with a foreground service is a system channel
        // and cannot be updated (by 3rd party apps)
        // For this reason, when having multiple possible sounds for different notifications,
        // multiple channels must be created

        Log.i("NotificationManager", "Start prepareNotificationChannels")

        // Main channel
        createNotificationChannel(
            appContext.getString(R.string.notificationChannelId),
            appContext.getString(R.string.app_name),
            appContext.getString(R.string.notificationChannelDescription),
            null
        )

        Log.i("NotificationManager > Alarm types: ", DexcomAlarmType.getValues().toString())

        // Alarm channels
        DexcomAlarmType.getValues().map {
            val normalizedValue = DexcomAlarmType.normalizeValue(it)
            Log.i("prepareNotificationChannels", normalizedValue)

            createNotificationChannel(
                appContext.getString(R.string.notificationChannelId) + "_$normalizedValue",
                it,
                appContext.getString(R.string.notificationChannelDescription),
                normalizedValue
            )
        }
    }

    fun createNotificationBuilder(title: String, message: String, isServiceInitial: Boolean = false): NotificationCompat.Builder {
        Log.i("NotificationsManager", "Start createNotificationBuilder")
        var channelId = appContext.getString(R.string.notificationChannelId)
        if (isServiceInitial) {
            alarmType = DexcomAlarmType.NORMAL
        } else {
            channelId += "_${DexcomAlarmType.normalizeValue(alarmType)}"
        }

        Log.i("NotificationsManager", "createNotificationBuilder (Initial: $isServiceInitial) for channel ID: $channelId with alarm type: $alarmType")

        return NotificationCompat.Builder(
            appContext,
            channelId
            )
            //.setChannelId(channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
    }

    private fun createNotificationChannel(channelId: String, channelName: String, channelDescription: String, alarmTypeNormalized: String? = null) {
        Log.i("NotificationsManager", "Start createNotificationChannel for $channelId ($channelName)")
        androidNotificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
            enableVibration(true)
        }

        if (!alarmTypeNormalized.isNullOrEmpty()) {
            val soundResourceId = DexcomAlarmSoundMap.SOUND_MAP[alarmTypeNormalized]

            if (0 < soundResourceId!!) {
                setSoundUrl("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${appContext.packageName}/${soundResourceId}")
                Log.i("NotificationsManager:createNotificationChannel",
                    "Setting sound URI: $soundUri for channel $channelId"
                )
                channel.setSound(soundUri, null)
            } else {
                Log.i("NotificationsManager:createNotificationChannel", "Clear sound URI for channel $channelId")
                clearSound()
            }
        } else {
            Log.i("NotificationsManager:createNotificationChannel", "Alarm type is NULL")
        }

        // Register the channel with the system
        androidNotificationManager.createNotificationChannel(channel)
    }
}