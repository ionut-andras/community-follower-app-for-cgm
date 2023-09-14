package ionut.andras.community.cgm.follower.services

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.R
import ionut.andras.community.cgm.follower.alarms.DexcomAlarmManager
import ionut.andras.community.cgm.follower.alarms.DexcomAlarmType
import ionut.andras.community.cgm.follower.api.DexcomApiRequestsHandler
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.DexcomTrendsConversionMap
import ionut.andras.community.cgm.follower.notifications.GlucoseNotificationData
import ionut.andras.community.cgm.follower.notifications.NotificationsManager
import ionut.andras.community.cgm.follower.services.broadcast.BroadcastActions
import ionut.andras.community.cgm.follower.services.broadcast.BroadcastSender
import ionut.andras.community.cgm.follower.utils.DateTimeConversion
import ionut.andras.community.cgm.follower.utils.DexcomDateTimeConversion
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.Serializable
import java.lang.Runnable

class GlucoseValuesUpdateService : Service() {
    private lateinit var appConfiguration: Configuration

    private var defaultDispatcher: CoroutineDispatcher = Dispatchers.IO

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val dexcomHandler = DexcomApiRequestsHandler()

    private var glucoseRetrievalSession: String? = null

    private var notificationManager = NotificationsManager(this)

    private var lastNotificationTimestamp: Long = 0
    private var lastNotificationValue: Int = 0
    private var temporaryDisableNotificationTimestamp: Long = 0

    private lateinit var broadcastReceiver: BroadcastReceiver

    private var isServiceRunning = false

    companion object{
        // Action
        const val ACTION = "ACTION"

        // Actions list
        const val START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("GlucoseValuesUpdateService > BroadcastReceiver", "Received broadcast message for intent action" + intent.action)

                // Identify broadcast operation
                when (intent.action) {
                    BroadcastActions.USER_REQUEST_REFRESH -> userRequestRefresh(intent)
                    BroadcastActions.TEMPORARY_DISABLE_NOTIFICATIONS_SOUND -> temporaryDisableNotificationSound(intent)
                    BroadcastActions.STOP_FOREGROUND_SERVICE -> stopServiceFromForeground(intent)
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.STOP_FOREGROUND_SERVICE), RECEIVER_NOT_EXPORTED)
        registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.USER_REQUEST_REFRESH), RECEIVER_NOT_EXPORTED)
        registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.TEMPORARY_DISABLE_NOTIFICATIONS_SOUND), RECEIVER_NOT_EXPORTED)
    }

    private fun parseBroadcastExtraInfo(intent: Intent?) {
        appConfiguration = getSerializableExtra(intent, "appConfiguration", Configuration::class.java)
        if (appConfiguration.dexcomSessionID.isNotEmpty()) {
            glucoseRetrievalSession = appConfiguration.dexcomSessionID
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serviceAction = intent?.getStringExtra(ACTION)

        Log.i("onStartCommand", "Action: $serviceAction")
        /*appConfiguration = getSerializableExtra(intent, "appConfiguration", Configuration::class.java)
        if (appConfiguration.dexcomSessionID.isNotEmpty()) {
            glucoseRetrievalSession = appConfiguration.dexcomSessionID
        }*/

        parseBroadcastExtraInfo(intent)

        when (serviceAction) {
            START_FOREGROUND_SERVICE -> startServiceInForeground(intent)
        }

        return START_STICKY
    }

    private fun startServiceInForeground(intent: Intent?) {
        if (!isServiceRunning) {
            Log.i("startServiceInForeground", "Starting...")

            // Send the notification needed by OS in order to start a foreground service
            val title = "Starting " + applicationContext.getString(R.string.app_name) + " in background"
            sendInitialNotificationAndStartForegroundService(GlucoseNotificationData(title, "", "now"))

            // Get initial values to fill in the main activity fields
            getAuthenticatedUserGlucoseData()

            // Schedule next reads
            enableContinuousRefresh()

            // stopSelf(startId)

            isServiceRunning = true
        } else {
            Log.i("startServiceInForeground", "Service already running. Skipping initialization...")

            // Get initial values to fill in the main activity fields
            getAuthenticatedUserGlucoseData()
        }
    }

    private fun stopServiceFromForeground(intent: Intent?) {
        Log.i("stopServiceFromForeground", "Stopping...")
        stopService(intent)
        stopSelf()
    }

    private fun userRequestRefresh(intent: Intent?) {
        Log.i("userRequestRefresh", "Refreshing...")
        parseBroadcastExtraInfo(intent)
        getAuthenticatedUserGlucoseData()
    }

    private fun temporaryDisableNotificationSound(intent: Intent?) {
        Log.i("temporaryDisableNotificationSound", "Starting...")
        temporaryDisableNotificationTimestamp = DateTimeConversion().getCurrentTimestamp()
    }

    /**
     * Tries to get the glucose data in an optimized way.
     *
     * If a session is already available, the function will try to skip authentication and retrieve the glucose data directly.
     * In case this fails, the full flow, including authentication and authorization will be used in order to get a new valid session before getting glucose data.
     */
    private fun getAuthenticatedUserGlucoseData() {
        var glucoseDataString: String?
        // Check if we have a valid session
        if (null != glucoseRetrievalSession) {

            // If a session valid, skip authentication and authorization
            GlobalScope.launch (defaultDispatcher) {
                glucoseDataString = getGlucoseData(glucoseRetrievalSession!!)
                if (null == glucoseDataString) {

                    // If the retrieval failed, try to get glucose data by running a full flow
                    getAndProcessUserGlucoseData()
                } else {
                    withContext(defaultDispatcher) {

                        // If glucose data received, process directly
                        processGlucoseData(glucoseDataString!!)
                    }
                }
            }
        } else {
            getAndProcessUserGlucoseData()
        }
    }

    // TODO: Use DI for processGlucoseData function
    /**
     * The full flow, including authentication and authorization will be used in order to get a new valid session before getting glucose data.
     *
     * Once the data is received, a function that process the data is called
     */
    private fun getAndProcessUserGlucoseData() {
        // Run full flow: authentication, authorization, get glucose data
        GlobalScope.launch (defaultDispatcher) {

            // Authenticate
            val accountId: String? = authenticate()
            if (null != accountId) {
                saveDexcomAccountId(accountId)
            }
            if (null != accountId) {
                GlobalScope.launch (defaultDispatcher) {
                    // Authorize
                    glucoseRetrievalSession = authorize(accountId)
                    saveDexcomSession()
                    if (null != glucoseRetrievalSession) {
                        GlobalScope.launch(defaultDispatcher) {
                            // Get glucose data
                            val glucoseDataString = getGlucoseData(glucoseRetrievalSession!!)
                            if (null != glucoseDataString) {
                                withContext(defaultDispatcher) {
                                    // Process glucose data when all the async operations closed
                                    processGlucoseData(glucoseDataString)
                                }
                            } else {
                                Log.i("getGlucoseData: ", "Glucose data string is null")
                            }
                            // As the calls to authenticate and authorize are made on separate threads
                            // we need to clear credentials only when thread finishes,
                            // and not at the end of getAndProcessUserGlucoseData
                            clearStoredCredentials()
                        }
                    } else {
                        Log.i("Authorize: ", "Glucose value retrieval bypassed")
                        // As the calls to authenticate and authorize are made on separate threads
                        // we need to clear credentials only when thread finishes,
                        // and not at the end of getAndProcessUserGlucoseData
                        clearStoredCredentials()
                        broadcastLoginFailed()
                    }
                }
            } else {
                Log.i("Authenticate: ", "Authorization bypassed")
                // As the calls to authenticate and authorize are made on separate threads
                // we need to clear credentials only when thread finishes,
                // and not at the end of getAndProcessUserGlucoseData
                clearStoredCredentials()
                broadcastLoginFailed()
            }
        }
    }

    /**
     * Authenticates the user and returns the Account ID
     *
     * @return String?
     */
    private fun authenticate(): String? {
        Log.i("Authenticate", "Running Authenticate routine")
        var accountId: String? = null
        if (appConfiguration.username.isNotEmpty() && appConfiguration.password.isNotEmpty()) {
            val apiResponse = dexcomHandler.authenticateWithUsernamePassword(
                appConfiguration.username,
                appConfiguration.password
            )
            if (apiResponse.isSuccess()) {
                accountId = apiResponse.data.toString().trim('"')
                Log.i("authenticateWithUsernamePassword - Response: ", accountId)
            } else {
                Log.i("authenticateWithUsernamePassword - Failure: ", "Clean authentication data")
                if (!apiResponse.noInternetConnectionError()) {
                    SharedPreferencesFactory(applicationContext).getInstance().edit().clear().apply()
                }
            }
        }
        return accountId
    }

    /**
     * Uses the Account Id - obtained from a call to authenticate() - to authorize the user and get a valid Session Id.
     *
     * @return String?
     */
    private fun authorize(accountId: String): String? {
        Log.i("Authorize", "Running Authorize routine")
        var sessionId: String? = null
        if(accountId.isNotEmpty() && appConfiguration.password.isNotEmpty()) {
            val apiResponse = dexcomHandler.loginWithAccountId(
                accountId,
                appConfiguration.password
            )
            if (apiResponse.isSuccess()) {
                sessionId = apiResponse.data.toString().trim('"')
                Log.i("loginWithAccountId - Response: ", sessionId.toString())
            }
        }
        return sessionId
    }

    /**
     * Uses the Session Id - obtained from a call to authorize() - to get the glucose data.
     */
    private fun getGlucoseData(sessionId: String): String? {
        Log.i("getGlucoseData", "Running Get Glucose Data routine")

        var glucoseDataString: String? = null
        if (sessionId.isNotEmpty()) {
            val apiResponse = dexcomHandler.getLatestGlucoseValues(
                sessionId,
                appConfiguration.glucoseHistoryMinutesBack,
                appConfiguration.glucoseHistoryNumberOfMetrics
            )
            if (apiResponse.isSuccess()) {
                //  Extract JSON with the list of metrics
                glucoseDataString = apiResponse.data.toString()
                Log.i("getLatestGlucoseValues", "Response received")
            }
        }
        return glucoseDataString
    }

    /**
     * Uses the glucose data - obtained from a call to getGlucoseData() - to update the parent activity.
     *
     * @param glucoseDataString String
     */
    private fun processGlucoseData(glucoseDataString: String) {
        Log.i("processGlucoseData:", "Starting...") //glucoseDataString)

        val glucoseNotificationData = buildNotificationDataObject(glucoseDataString)

        // Broadcast information to update glucose value and trend in the main screen
        BroadcastSender(applicationContext, BroadcastActions.GLUCOSE_DATA_CHANGED)
            .addInfo(getString(R.string.variableNameGenericData), glucoseDataString)
            .broadcast()

        // Sends the notification of the service
        triggerNotification(glucoseNotificationData)
        // sendInitialNotification(glucoseNotificationData)
    }

    /**
     * Uses glucose data string to build an object that can be used to update main activity
     * and also as source of information for notifications.
     *
     * @param glucoseDataString String
     * @return GlucoseNotificationData
     */
    private fun buildNotificationDataObject(glucoseDataString: String): GlucoseNotificationData {
        val glucoseData = JSONArray(glucoseDataString)
        // Log.i("getLatestGlucoseValues", glucoseData.toString());

        // Glucose Value
        val glucoseValue = glucoseData.getJSONObject(0).getString("Value")

        // Glucose Date and Time
        val timeOffsetFromCurrent = DexcomDateTimeConversion().getTimeOffsetFromCurrentDate(glucoseData.getJSONObject(0).getString("DT").toString())

        // Glucose Trend
        val glucoseTrend = glucoseData.getJSONObject(0).getString("Trend").toString()
        val trendSign = DexcomTrendsConversionMap.convert[glucoseTrend]

        val glucoseRecentHistory: ArrayList<Int> = arrayListOf()

        var prevValue = glucoseValue.toInt()
        for (i in 1..appConfiguration.glucoseNotificationHistoryValues) {
            glucoseRecentHistory.add(prevValue - glucoseData.getJSONObject(i).getString("Value").toInt())
            prevValue = glucoseData.getJSONObject(i).getString("Value").toInt()
        }

        return GlucoseNotificationData(glucoseValue, trendSign.toString(), timeOffsetFromCurrent, glucoseRecentHistory)
    }

    private fun sendInitialNotificationAndStartForegroundService(glucoseNotificationData: GlucoseNotificationData) {
        // Get notification main thread intent
        val notificationIntent = Intent(this, MainActivity::class.java)

        // Pending intent is the current service intent
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        // Convert text to bitmap
        notificationManager.setNotificationIcon(glucoseNotificationData.toIcon())

        notificationManager.prepareNotificationChannels()

        // Trigger notification
        val builder = notificationManager.createNotificationBuilder(
            glucoseNotificationData.glucoseValue,
            glucoseNotificationData.glucoseValueTrend,
            true
        )
        builder.setContentIntent(pendingIntent)

        startForeground(1, builder.build())
    }


    private fun triggerNotification(glucoseNotificationData: GlucoseNotificationData) {
        if (
            // Just updated
            (glucoseNotificationData.timeOffset.compareTo("now") == 0) ||
            // Value of the data changed
            (lastNotificationValue != glucoseNotificationData.glucoseValue.toInt()) ||
            // More than a period of time happen since last notification
            (DateTimeConversion().getCurrentTimestamp() - lastNotificationTimestamp > appConfiguration.glucoseValueNotificationIntervalSeconds)
        ) {
            // Get notification main thread intent
            val notificationIntent = Intent(this, MainActivity::class.java)

            // Pending intent is the current service intent
            val pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

            notificationManager.setBuilderContentIntent(pendingIntent)

            // Convert text to bitmap
            notificationManager.setNotificationIcon(glucoseNotificationData.toIcon())

            var alarmType = DexcomAlarmManager(appConfiguration).getNotificationAlarmType(glucoseNotificationData)
            if (
                0 != DexcomAlarmType.URGENT_LOW.compareTo(alarmType) &&
                0 != DexcomAlarmType.LOW.compareTo(alarmType)
            ) {
                // If alarm is temporary disabled, ignore the alarm type
                // Feature triggered automatically only if the alarm is not low or urgent low
                if (DateTimeConversion().getCurrentTimestamp() - temporaryDisableNotificationTimestamp < appConfiguration.disableNotificationSoundSeconds) {
                    Log.i("triggerNotification", "Notification sound temporary disabled for ${appConfiguration.disableNotificationSoundSeconds} seconds.")
                    alarmType = DexcomAlarmType.NORMAL
                }
            }

            notificationManager.setAlarmType(alarmType)

            // val sharedPreferences = applicationContext.getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
            val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
            appConfiguration.autoCancelNotifications = sharedPreferences.getBoolean(UserPreferences.autoCancelNotifications, appConfiguration.autoCancelNotifications)

            // Set the flag for NotificationManager
            notificationManager.setAutoCancelNotificationFlag(appConfiguration.autoCancelNotifications)

            // Trigger notification
            if (!appConfiguration.autoCancelNotifications) {
                // Log.i(">>>>>>>>>>>>>>>", "Trigger Notification - No auto cancel")
                notificationManager.triggerNotification(
                    glucoseNotificationData.toNotificationTitle(),
                    glucoseNotificationData.toNotificationMessage(
                        applicationContext,
                        appConfiguration
                    )
                )
            } else {
                // Log.i(">>>>>>>>>>>>>>>", "Trigger Notification - Auto cancel")
                notificationManager.triggerAutoCancelNotification(
                    glucoseNotificationData.toNotificationTitle(),
                    glucoseNotificationData.toNotificationMessage(
                        applicationContext,
                        appConfiguration
                    ),
                    appConfiguration.autoCancelDelayMillis
                )
            }
            // Stamp notification timestamp
            lastNotificationTimestamp = DateTimeConversion().getCurrentTimestamp()
            lastNotificationValue = glucoseNotificationData.glucoseValue.toInt()
        }
    }

    private fun enableContinuousRefresh() {
        handler = Handler(Looper.getMainLooper())

        runnable = Runnable {
            getAuthenticatedUserGlucoseData()
            handler.postDelayed(runnable, appConfiguration.glucoseAutomaticUpdateMiliseconds)
        }

        handler.postDelayed(runnable, appConfiguration.glucoseAutomaticUpdateMiliseconds)
    }

    private fun clearStoredCredentials() {
        // TODO: Find a way to safely encrypt the password in shared preferences
        // val sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        // sharedPreferences.edit().remove(UserPreferences.loginEmail).apply()
        // sharedPreferences.edit().remove(UserPreferences.loginPassword).apply()

        // appConfiguration.username = ""
        // appConfiguration.password = ""
    }

    private fun saveDexcomAccountId(accountId: String) {
        // val sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        sharedPreferences.edit().putString(UserPreferences.dexcomAccountId, accountId).apply()
    }

    private fun saveDexcomSession() {
        // val sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        val sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        sharedPreferences.edit().putString(UserPreferences.dexcomSessionId, glucoseRetrievalSession.toString()).apply()
    }

    private fun broadcastLoginFailed() {
        // Broadcast command to redirect to login with message
        BroadcastSender(applicationContext, BroadcastActions.AUTHENTICATION_FAILED)
            .addInfo(getString(R.string.variableNameLoginFailed), "true")
            .broadcast()
    }

    private fun <T : Serializable?> getSerializableExtra(intent: Intent?, extraParameterName: String, className: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent?.getSerializableExtra(extraParameterName, className)!!
        else
            intent?.getSerializableExtra(extraParameterName) as T
    }

}