package ionut.andras.community.dexcomrelated.followerfordexcom.services

import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import ionut.andras.community.dexcomrelated.followerfordexcom.MainActivity
import ionut.andras.community.dexcomrelated.followerfordexcom.R
import ionut.andras.community.dexcomrelated.followerfordexcom.alarms.DexcomAlarmManager
import ionut.andras.community.dexcomrelated.followerfordexcom.alarms.DexcomAlarmType
import ionut.andras.community.dexcomrelated.followerfordexcom.api.DexcomApiRequestsHandler
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.Configuration
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.UserPreferences
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomTrendsConversionMap
import ionut.andras.community.dexcomrelated.followerfordexcom.notifications.GlucoseNotificationData
import ionut.andras.community.dexcomrelated.followerfordexcom.notifications.NotificationsManager
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastActions
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastSender
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DateTimeConversion
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DexcomDateTimeConversion
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

    companion object{
        // Action
        const val ACTION = "ACTION"

        // Actions list
        const val START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE"
        const val STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE"
        const val USER_REQUEST_REFRESH = "USER_REQUEST_REFRESH"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serviceAction = intent?.getStringExtra(ACTION)

        Log.i("onStartCommand", "Action: $serviceAction")
        appConfiguration = getSerializableExtra(intent, "appConfiguration", Configuration::class.java)
        if (appConfiguration.dexcomSessionID.isNotEmpty()) {
            glucoseRetrievalSession = appConfiguration.dexcomSessionID
        }

        when (serviceAction) {
            START_FOREGROUND_SERVICE -> startServiceInForeground(intent)
            STOP_FOREGROUND_SERVICE -> stopServiceFromForeground(intent)
            USER_REQUEST_REFRESH -> userRequestRefresh()
        }

        return START_STICKY
    }

    private fun startServiceInForeground(intent: Intent?) {
        Log.i("startServiceInForeground", "Starting...")

        // Send the notification needed by OS in order to start a foreground service
        val title = "Starting " + applicationContext.getString(R.string.app_name) + " in background"
        sendInitialNotificationAndStartForegroundService(GlucoseNotificationData(title, "", "now"))

        // Get initial values to fill in the main activity fields
        getAuthenticatedUserGlucoseData()

        // Schedule next reads
        enableContinuousRefresh()

        // stopSelf(startId)
    }

    private fun stopServiceFromForeground(intent: Intent?) {
        Log.i("stopServiceFromForeground", "Stopping...")

        stopService(intent)
        stopSelf()
    }

    private fun userRequestRefresh() {
        Log.i("userRequestRefresh", "Refreshing...")
        getAuthenticatedUserGlucoseData()
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
        Log.i("Get glucose data:", "Running Get Glucose Data routine")

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
                Log.i("getLatestGlucoseValues - Response", glucoseDataString)
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
        Log.i("processGlucoseData:", glucoseDataString)

        val glucoseNotificationData = buildNotificationDataObject(glucoseDataString)

        // Broadcast information to update glucose value and trend in the main screen
        BroadcastSender(applicationContext, BroadcastActions.GLUCOSE_DATA_CHANGED).broadcast(getString(R.string.variableNameGenericData), glucoseDataString)

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

            val alarmType = DexcomAlarmManager(appConfiguration).getNotificationAlarmType(glucoseNotificationData)
            notificationManager.setAlarmType(alarmType)

            val sharedPreferences = applicationContext.getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
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
        val sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(UserPreferences.dexcomAccountId, accountId).apply()
    }

    private fun saveDexcomSession() {
        val sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(UserPreferences.dexcomSessionId, glucoseRetrievalSession.toString()).apply()
    }

    private fun broadcastLoginFailed() {
        // Broadcast command to redirect to login with message
        BroadcastSender(
            applicationContext,
            BroadcastActions.AUTHENTICATION_FAILED
        ).broadcast(getString(R.string.variableNameLoginFailed), "true")
    }

    private fun <T : Serializable?> getSerializableExtra(intent: Intent?, extraParameterName: String, className: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent?.getSerializableExtra(extraParameterName, className)!!
        else
            intent?.getSerializableExtra(extraParameterName) as T
    }

}