package ionut.andras.community.dexcomrelated.followerfordexcom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.LineChart
import ionut.andras.community.dexcomrelated.followerfordexcom.common.GlucoseValueColorRange
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.Configuration
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.UserPreferences
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomConstants
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomTrendsConversionMap
import ionut.andras.community.dexcomrelated.followerfordexcom.core.AppCompatActivityWrapper
import ionut.andras.community.dexcomrelated.followerfordexcom.notifications.GlucoseNotificationData
import ionut.andras.community.dexcomrelated.followerfordexcom.permissions.PermissionHandler
import ionut.andras.community.dexcomrelated.followerfordexcom.plot.PlotGlucoseHistoricValues
import ionut.andras.community.dexcomrelated.followerfordexcom.services.GlucoseValuesUpdateService
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastActions
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastSender
import ionut.andras.community.dexcomrelated.followerfordexcom.toast.ToastWrapper
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DateTimeConversion
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DexcomDateTimeConversion
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.SharedPreferencesFactory
import org.json.JSONArray


class MainActivity : AppCompatActivityWrapper() {

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

        // Setup design elements
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.mainActivityActionToolbar))

        // Check application minimum requirements
        checkApplicationMinimumRequirements()

        // Initialize application settings
        initApplicationSettings()

        if (!displayLoginFormNeeded()) {
            Log.i("MainActivity onCreate", "Display form not needed. Continue...")

            // Display some default values before showing a loading screen
            viewGlucoseValue = findViewById(R.id.glucoseValue)
            viewGlucoseValue.text = DexcomConstants().glucoseInitialValue

            // Get chart to fill in later
            glucoseHistoricChart = findViewById(R.id.lineChart)

            // getAndProcessGlucoseData()

            startServiceGetAndProcessGlucoseData()

            enableSwipeToRefresh()

            enableMainActivityListeners()

            registerBroadcastReceivers()
        } else {
            Log.i("MainActivity onCreate", "Login needed. Display login form...")
            displayLoginForm()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity onResume", "Resuming main activity. Resume from background: " + resumeFromBackground.toString())

        registerBroadcastReceivers(resumeFromBackground)
        resumeFromBackground = false
    }

    override fun onStop() {
        super.onStop()

        unregisterBroadcastReceivers()
    }

    override fun onPause() {
        Log.i("MainActivity onPause", "Starting...")

        super.onPause()
        resumeFromBackground = true
        // unregisterBroadcastReceivers()
    }

    override fun onDestroy() {
        Log.i("MainActivity onDestroy", "Starting...")

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

        appConfiguration.username = sharedPreferences.getString(UserPreferences.loginEmail, null).toString()
        appConfiguration.password = sharedPreferences.getString(UserPreferences.loginPassword, null).toString()
        appConfiguration.dexcomSessionID = sharedPreferences.getString(UserPreferences.dexcomSessionId, null).toString()
        lastToastDisplayTimestamp = sharedPreferences.getLong(UserPreferences.lastToastDisplayTimestamp, 0)
    }

    private fun checkApplicationMinimumRequirements() {
        if (!PermissionHandler(applicationContext).checkPermissions()){
            PermissionHandler(applicationContext).requestPermissions(this)
        }
    }

    // Handle the permission result in onRequestPermissionsResult()
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Configuration.REQUEST_CODE_PERMISSION_NOTIFICATIONS) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The user granted the permission.
                // You can now send notifications.
                Log.i("onRequestPermissionsResult", "Notifications permissions granted.")
            } else {
                // The user denied the permission.
                // You cannot send notifications.
                Log.i("onRequestPermissionsResult", "Notifications permissions denied.")
            }
        }
    }

    private fun displayLoginFormNeeded(): Boolean {
        // sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferences = SharedPreferencesFactory(applicationContext).getInstance()

        val email = sharedPreferences.getString(UserPreferences.loginEmail, null)
        val password = sharedPreferences.getString(UserPreferences.loginPassword, null)
        val dexcomSessionID = sharedPreferences.getString(UserPreferences.dexcomSessionId, null)
        Log.i("displayLoginFormNeeded > email", email.toString())
        Log.i("displayLoginFormNeeded > password", password.toString())
        Log.i("displayLoginFormNeeded > dexcomSessionID", dexcomSessionID.toString())
        return (dexcomSessionID.isNullOrEmpty() && (email.isNullOrEmpty() || password.isNullOrEmpty()))
    }

    private fun displayLoginForm(message: String? = null) {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.putExtra(
            getString(R.string.variableNameLoginFormMessage),
            message
        )
        startActivity(intent)
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
        Log.i("mainActivity > plotGlucoseData", "Starting")
        val plotGlucoseHistoricValues = PlotGlucoseHistoricValues(appConfiguration, JSONArray(chartJsonDataAsString))
        plotGlucoseHistoricValues.start(applicationContext, glucoseHistoricChart, R.layout.glucose_plot_marker_view)
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

    private fun enableMainActivityListeners() {
       // Placeholder
    }

    private fun registerBroadcastReceivers(forceRegister: Boolean = false) {
        Log.i("mainActivity > registerBroadcastReceivers", "Starting...")

        if ((null == broadcastReceiver) || forceRegister) {
            Log.i("mainActivity > registerBroadcastReceivers", "Registering broadcast receiver...")
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Log.i("mainActivity > registerBroadcastReceivers", "Received broadcast: " + intent.action)
                    // Identify broadcast operation
                    when (intent.action) {
                        BroadcastActions.AUTHENTICATION_FAILED -> displayLoginForm(getString(R.string.messageLoginFailed))
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
            Log.i("mainActivity > registerBroadcastReceivers", "Skip broadcast receiver registration...")
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
                toastText
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

    private fun iconInfoOnClick() {
        val intent = Intent(applicationContext, ApplicationPermissionsInfoActivity::class.java)
        startActivity(intent)
    }

    fun btn3hOnClick(view: View) {
        appConfiguration.glucoseHistoryNumberOfMetrics = 3 * 12
        forceRefreshGlucoseData()
    }
    fun btn6hOnClick(view: View) {
        appConfiguration.glucoseHistoryNumberOfMetrics = 6 * 12
        forceRefreshGlucoseData()
    }
    fun btn12hOnClick(view: View) {
        appConfiguration.glucoseHistoryNumberOfMetrics = 12 * 12
        forceRefreshGlucoseData()
    }
    fun btn24hOnClick(view: View) {
        appConfiguration.glucoseHistoryNumberOfMetrics = 24 * 12
        forceRefreshGlucoseData()
    }
}