package ionut.andras.community.cgm.follower

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.LineChart
import ionut.andras.community.cgm.follower.configuration.Configuration
import ionut.andras.community.cgm.follower.configuration.UserPreferences
import ionut.andras.community.cgm.follower.constants.ApplicationRunMode
import ionut.andras.community.cgm.follower.constants.DexcomConstants
import ionut.andras.community.cgm.follower.constants.DexcomTrendsConversionMap
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.notifications.GlucoseNotificationData
import ionut.andras.community.cgm.follower.permissions.PermissionHandler
import ionut.andras.community.cgm.follower.permissions.PermissionRequestCodes
import ionut.andras.community.cgm.follower.plot.PlotGlucoseHistoricValues
import ionut.andras.community.cgm.follower.services.GlucoseValuesUpdateService
import ionut.andras.community.cgm.follower.services.broadcast.BroadcastActions
import ionut.andras.community.cgm.follower.services.broadcast.BroadcastSender
import ionut.andras.community.cgm.follower.sms.SmsAuthenticationWrapper
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.ApplicationRunModesHelper
import ionut.andras.community.cgm.follower.utils.DateTimeConversion
import ionut.andras.community.cgm.follower.utils.DexcomDateTimeConversion
import ionut.andras.community.cgm.follower.utils.GlucoseValueColorRange
import ionut.andras.community.cgm.follower.utils.SharedPreferencesFactory
import org.json.JSONArray


class MainActivity : AppCompatActivityWrapper(R.menu.main_menu) {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var viewGlucoseValue: TextView
    private lateinit var glucoseHistoricChart: LineChart

    private val appConfiguration: Configuration = Configuration()

    // Application settings
    private lateinit var sharedPreferences: SharedPreferences

    private var broadcastReceiver: BroadcastReceiver? = null

    private var lastToastDisplayTimestamp: Long = 0

    private var resumeFromBackground: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check application minimum requirements
        checkApplicationMinimumRequirements()

        // Initialize application settings
        initApplicationSettings()

        // Start the Glucose monitoring service
        startServiceGetAndProcessGlucoseData()

        if (!displayLoginFormNeeded()) {
            Log.i("MainActivity > onCreate", "Display form not needed. Continue...")

            // Setup design elements
            setContentView(R.layout.activity_main)

            // Set Action bar
            setSupportActionBar(findViewById(R.id.mainActivityActionToolbar))

            // Enable Main Application mode
            ApplicationRunModesHelper(applicationContext).switchRunModeTo(ApplicationRunMode.MAIN_APPLICATION)

            // Display some default values before showing a loading screen
            viewGlucoseValue = findViewById(R.id.glucoseValue)
            viewGlucoseValue.text = DexcomConstants().glucoseInitialValue

            // Get chart to fill in later
            glucoseHistoricChart = findViewById(R.id.lineChart)

            enableSwipeToRefresh()

            enableActivityListeners()

            registerBroadcastReceivers()
        } else {
            Log.i("MainActivity > onCreate", "Login needed. Display login form...")
            displayLoginForm()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity > onResume", "Resuming main activity. Resume from background: $resumeFromBackground")

        registerBroadcastReceivers(resumeFromBackground)
        resumeFromBackground = false
    }

    override fun onStop() {
        super.onStop()

        unregisterBroadcastReceivers()
    }

    override fun onPause() {
        Log.i("MainActivity > onPause", "Starting...")

        super.onPause()
        resumeFromBackground = true
        // unregisterBroadcastReceivers()
    }

    override fun onDestroy() {
        Log.i("MainActivity > onDestroy", "Starting...")

        super.onDestroy()

        unregisterBroadcastReceivers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.iconSettings -> {
                iconSettingsOnClick()
                true
            }
            R.id.iconInviteFollower -> {
                iconInviteFollowerOnClick()
                true
            }
            R.id.iconInfo -> {
                iconInfoOnClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initApplicationSettings() {
        // sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        appConfiguration.username = sharedPreferences.getString(UserPreferences.loginEmail, null)
        appConfiguration.password = sharedPreferences.getString(UserPreferences.loginPassword, null)
        appConfiguration.dexcomSessionID = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        lastToastDisplayTimestamp = sharedPreferences.getLong(UserPreferences.lastToastDisplayTimestamp, 0)
    }

    private fun checkApplicationMinimumRequirements() {
        /**
         * Check if minimum permissions needed by the application are requested from the user.
         */
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.FOREGROUND_SERVICE, getString(R.string.permissionFriendlyNameForegroundService), PermissionRequestCodes.FOREGROUND_SERVICE)
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC, getString(R.string.permissionFriendlyNameForegroundService), PermissionRequestCodes.FOREGROUND_SERVICE_DATA_SYNC)
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.POST_NOTIFICATIONS, getString(R.string.permissionFriendlyNamePostNotifications), PermissionRequestCodes.GLUCOSE_VALUE_NOTIFICATION)
        PermissionHandler(this, applicationContext)
            .checkPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, getString(R.string.permissionFriendlyNameDisableBatteryOptimization), PermissionRequestCodes.BATTERY_OPTIMIZATION)
    }

    // Handle the permission result in onRequestPermissionsResult()
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun displayLoginFormNeeded(): Boolean {
        // sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        val email = sharedPreferences.getString(UserPreferences.loginEmail, null)
        val password = sharedPreferences.getString(UserPreferences.loginPassword, null)
        val dexcomSessionID = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        Log.i("displayLoginFormNeeded", "email: ${email.toString()}")
        Log.i("displayLoginFormNeeded", "password: ${password.toString()}")
        Log.i("displayLoginFormNeeded", "dexcomSessionID: ${dexcomSessionID.toString()}")
        return (dexcomSessionID.isNullOrEmpty() && (email.isNullOrEmpty() || password.isNullOrEmpty()))
    }

    private fun startServiceGetAndProcessGlucoseData() {
        val intent = Intent(applicationContext, GlucoseValuesUpdateService::class.java)
        intent.putExtra("appConfiguration", appConfiguration)
        intent.putExtra(GlucoseValuesUpdateService.ACTION, GlucoseValuesUpdateService.START_FOREGROUND_SERVICE)
        applicationContext.startForegroundService(intent)
    }

    private fun updateGlucoseInformation(glucoseDataString: String): GlucoseNotificationData {
        val glucoseData = JSONArray(glucoseDataString)
        // Log.i("getLatestGlucoseValues", glucoseData.toString());

        // Glucose Value
        val viewGlucoseValue = findViewById<TextView>(R.id.glucoseValue)
        val glucoseValue = glucoseData.getJSONObject(0).getString("Value")
        viewGlucoseValue.text = glucoseValue
        viewGlucoseValue.setTextColor(GlucoseValueColorRange(appConfiguration).getGlucoseColorValue(glucoseValue.toInt()))

        // Glucose Date and Time
        val viewGlucoseValueTime = findViewById<TextView>(R.id.glucoseValueTime)
        val timeOffsetFromCurrent = DexcomDateTimeConversion().getTimeOffsetFromCurrentDate(glucoseData.getJSONObject(0).getString("DT").toString())
        viewGlucoseValueTime.text = timeOffsetFromCurrent

        // Glucose Trend
        val viewGlucoseTrend = findViewById<TextView>(R.id.glucoseTrend)

        val glucoseTrend = glucoseData.getJSONObject(0).getString("Trend").toString()
        // glucoseTrend = DexcomTrendsConversionMap.FLAT
        // glucoseTrend = DexcomTrendsConversionMap.FORTY_FIVE_DOWN
        // glucoseTrend = DexcomTrendsConversionMap.FORTY_FIVE_UP
        // glucoseTrend = DexcomTrendsConversionMap.SINGLE_DOWN
        // glucoseTrend = DexcomTrendsConversionMap.SINGLE_UP
        // glucoseTrend = DexcomTrendsConversionMap.DOUBLE_DOWN
        // glucoseTrend = DexcomTrendsConversionMap.DOUBLE_UP

        val trendSign = DexcomTrendsConversionMap.convert[glucoseTrend]

        viewGlucoseTrend.text = Html.fromHtml(trendSign, Html.FROM_HTML_MODE_COMPACT)

        return GlucoseNotificationData(glucoseValue, trendSign.toString(), timeOffsetFromCurrent)
    }

    private fun plotGlucoseData(chartJsonDataAsString: String) {
        Log.i("MainActivity > plotGlucoseData", "Starting")
        val plotGlucoseHistoricValues = PlotGlucoseHistoricValues(appConfiguration, JSONArray(chartJsonDataAsString))
        plotGlucoseHistoricValues.start(applicationContext, glucoseHistoricChart,
            R.layout.glucose_plot_marker_view
        )
    }

    private fun forceRefreshGlucoseData() {
        BroadcastSender(applicationContext, BroadcastActions.USER_REQUEST_REFRESH)
            .addInfo("appConfiguration", appConfiguration)
            .broadcast()
    }

    private fun enableSwipeToRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)

        swipeRefreshLayout.setOnRefreshListener {
            // ::: Only Main window refresh must be done here. No communication with the service

            forceRefreshGlucoseData()

            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun enableActivityListeners() {
       // Placeholder
    }

    private fun registerBroadcastReceivers(forceRegister: Boolean = false) {
        Log.i("MainActivity > registerBroadcastReceivers", "Starting...")

        if ((null == broadcastReceiver) || forceRegister) {
            Log.i("MainActivity > registerBroadcastReceivers", "Registering broadcast receiver...")
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Log.i("MainActivity > registerBroadcastReceivers", "Received broadcast: " + intent.action)
                    // Identify broadcast operation
                    when (intent.action) {
                        BroadcastActions.AUTHENTICATION_FAILED -> handleFailedAuthentication()
                        BroadcastActions.GLUCOSE_DATA_CHANGED -> performGlucoseDataChange(intent)
                        BroadcastActions.TOASTER_OK_GLUCOSE_VALUE -> disableNotificationSoundForSeconds(intent, appConfiguration.disableNotificationSoundSeconds)
                        else -> {}
                    }
                }
            }

            registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.AUTHENTICATION_FAILED), RECEIVER_NOT_EXPORTED)
            registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.GLUCOSE_DATA_CHANGED), RECEIVER_NOT_EXPORTED)
            registerReceiver(broadcastReceiver, IntentFilter(BroadcastActions.TOASTER_OK_GLUCOSE_VALUE), RECEIVER_NOT_EXPORTED)
        } else {
            Log.i("MainActivity > registerBroadcastReceivers", "Skip broadcast receiver registration...")
        }
    }

    private fun handleFailedAuthentication() {
        val runModeHelper = ApplicationRunModesHelper(applicationContext)

        if (runModeHelper.getRunMode(ApplicationRunMode.MAIN_APPLICATION) == ApplicationRunMode.MAIN_APPLICATION) {
            // Main Application mode
            displayLoginForm(getString(R.string.messageLoginFailed))
        } else {
            SmsAuthenticationWrapper(applicationContext).sendRequestAuthenticationRenewSms()
        }
    }

    private fun performGlucoseDataChange(intent: Intent) {
        val glucoseDataString = intent.getStringExtra(getString(R.string.variableNameGenericData)).toString()
        // Log.i("Updating MainActivity views with", glucoseDataString)
        Log.i("performGlucoseDataChange", "Updating MainActivity view")

        // Update glucose value and trend in the main screen
        val glucoseNotificationData = updateGlucoseInformation(glucoseDataString)

        // Draw metrics history
        plotGlucoseData(glucoseDataString)

        // Display the informational toast
        displayToastGlucoseValue(glucoseNotificationData)
    }

    fun disableNotificationSoundForSeconds(intent: Intent, seconds: Int) {
        val broadcastValue = intent.getStringExtra(getString(R.string.variableNameGenericData)).toString()
        Log.i("disableNotificationSoundForSeconds", "Function start")

        if ("OK" == broadcastValue) {
            BroadcastSender(applicationContext, BroadcastActions.TEMPORARY_DISABLE_NOTIFICATIONS_SOUND)
                .addInfo(getString(R.string.variableNameGenericData), seconds.toString())
                .broadcast()
        }
    }

    private fun displayToastGlucoseValue(glucoseNotificationData: GlucoseNotificationData) {
        val currentTimestamp = DateTimeConversion().getCurrentTimestamp()
        Log.i("displayToastGlucoseValue", "Current TS: $currentTimestamp / Last Toast Display TS: $lastToastDisplayTimestamp")
        Log.i("displayToastGlucoseValue", "Notification interval (sec): ${appConfiguration.glucoseValueNotificationIntervalSeconds}")
        Log.i("displayToastGlucoseValue", "Delta (sec): ${currentTimestamp - lastToastDisplayTimestamp}")
        if (currentTimestamp - lastToastDisplayTimestamp > appConfiguration.glucoseValueNotificationIntervalSeconds) {
            Log.i("displayToastGlucoseValue", "Displaying toast...")
            // Display the informational toast
            val toastText =
                glucoseNotificationData.toNotificationMessage(applicationContext, appConfiguration)
            ToastWrapper(applicationContext).displayMessageToast(
                findViewById(R.id.glucoseValue),
                toastText,
                BroadcastActions.TOASTER_OK_GLUCOSE_VALUE
            )
            lastToastDisplayTimestamp = DateTimeConversion().getCurrentTimestamp()
            sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()
            sharedPreferences.edit().putLong(UserPreferences.lastToastDisplayTimestamp, lastToastDisplayTimestamp).apply()
        } else {
            Log.i("displayToastGlucoseValue", "Conditions for displaying toast not meet.")
        }
    }

    private fun unregisterBroadcastReceivers() {
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (_: Exception) {
        } finally {
        }
    }

    private fun iconSettingsOnClick() {
        val intent = Intent(applicationContext, ApplicationSettingsActivity::class.java)
        startActivity(intent)
    }

    private fun iconInviteFollowerOnClick() {
        val intent = Intent(applicationContext, InviteFollowerActivity::class.java)
        startActivity(intent)
    }

    private fun iconInfoOnClick() {
        val intent = Intent(applicationContext, ApplicationPermissionsInfoActivity::class.java)
        startActivity(intent)
    }

    fun btn3hOnClick(view: View) {
        Log.i("btn3hOnClick", "Interval selected: " + view.id)
        appConfiguration.glucoseHistoryNumberOfMetrics = 3 * 12
        forceRefreshGlucoseData()
    }
    fun btn6hOnClick(view: View) {
        Log.i("btn6hOnClick", "Interval selected: " + view.id)
        appConfiguration.glucoseHistoryNumberOfMetrics = 6 * 12
        forceRefreshGlucoseData()
    }
    fun btn12hOnClick(view: View) {
        Log.i("btn12hOnClick", "Interval selected: " + view.id)
        appConfiguration.glucoseHistoryNumberOfMetrics = 12 * 12
        forceRefreshGlucoseData()
    }
    fun btn24hOnClick(view: View) {
        Log.i("btn24hOnClick", "Interval selected: " + view.id)
        appConfiguration.glucoseHistoryNumberOfMetrics = 24 * 12
        forceRefreshGlucoseData()
    }

    fun btnAddEventOnClick(view: View) {
        Log.i("btnAddEventOnClick", "View selected: " + view.id)
        val intent = Intent(applicationContext, AddNewUserEventActivity::class.java)
        startActivity(intent)
    }
}