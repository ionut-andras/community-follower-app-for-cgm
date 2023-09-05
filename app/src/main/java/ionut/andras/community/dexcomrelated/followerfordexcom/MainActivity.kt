package ionut.andras.community.dexcomrelated.followerfordexcom

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.LineChart
import ionut.andras.community.dexcomrelated.followerfordexcom.common.GlucoseValueColorRange
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.Configuration
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.UserPreferences
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomConstants
import ionut.andras.community.dexcomrelated.followerfordexcom.constants.DexcomTrendsConversionMap
import ionut.andras.community.dexcomrelated.followerfordexcom.notifications.GlucoseNotificationData
import ionut.andras.community.dexcomrelated.followerfordexcom.plot.PlotGlucoseHistoricValues
import ionut.andras.community.dexcomrelated.followerfordexcom.services.GlucoseValuesUpdateService
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastActions
import ionut.andras.community.dexcomrelated.followerfordexcom.services.broadcast.BroadcastSender
import ionut.andras.community.dexcomrelated.followerfordexcom.toast.ToastWrapper
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DateTimeConversion
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DexcomDateTimeConversion
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

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
        Log.i("MainActivity onResume", "Resuming main activity...")
        registerBroadcastReceivers()
    }

    override fun onStop() {
        super.onStop()

        unregisterBroadcastReceivers()
    }

    override fun onPause() {
        super.onPause()

        unregisterBroadcastReceivers()
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterBroadcastReceivers()
    }

    private fun initApplicationSettings() {
        sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)

        appConfiguration.username = sharedPreferences.getString(UserPreferences.loginEmail, null).toString()
        appConfiguration.password = sharedPreferences.getString(UserPreferences.loginPassword, null).toString()
        appConfiguration.dexcomSessionID = sharedPreferences.getString(UserPreferences.dexcomSessionId, null).toString()
    }

    private fun checkApplicationMinimumRequirements() {
        checkRequiredPermissions()
    }

    private fun checkRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    Configuration.REQUEST_CODE_PERMISSION_NOTIFICATIONS)
            }
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
            } else {
                // The user denied the permission.
                // You cannot send notifications.
            }
        }
    }

    private fun displayLoginFormNeeded(): Boolean {
        sharedPreferences = getSharedPreferences(applicationContext.getString(R.string.app_name), Context.MODE_PRIVATE)
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

        var glucoseTrend = glucoseData.getJSONObject(0).getString("Trend").toString()
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
        val plotGlucoseHistoricValues = PlotGlucoseHistoricValues(appConfiguration, JSONArray(chartJsonDataAsString))
        plotGlucoseHistoricValues.start(applicationContext, glucoseHistoricChart, R.layout.glucose_plot_marker_view)
    }

    private fun forceRefreshGlucoseData() {
        BroadcastSender(applicationContext, BroadcastActions.USER_REQUEST_REFRESH).broadcast()
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

    private fun registerBroadcastReceivers() {
        if (null == broadcastReceiver) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    // Identify broadcast operation
                    when (intent.action) {
                        BroadcastActions.AUTHENTICATION_FAILED -> displayLoginForm(getString(R.string.messageLoginFailed))
                        BroadcastActions.GLUCOSE_DATA_CHANGED -> performGlucoseDataChange(intent)
                        BroadcastActions.TOASTER_OK_GLUCOSE_VALUE -> disableNotificationSoundForSeconds(intent, appConfiguration.disableNotificationSoundSeconds)
                    }
                }
            }

            val intentFilter = IntentFilter()
            intentFilter.addAction(BroadcastActions.AUTHENTICATION_FAILED)
            intentFilter.addAction(BroadcastActions.GLUCOSE_DATA_CHANGED)
            intentFilter.addAction(BroadcastActions.TOASTER_OK_GLUCOSE_VALUE)
            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    private fun performGlucoseDataChange(intent: Intent) {
        val glucoseDataString = intent.getStringExtra(getString(R.string.variableNameGenericData)).toString()
        Log.i("Updating MainActivity views with", glucoseDataString)

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
            BroadcastSender(applicationContext, BroadcastActions.TEMPORARY_DISABLE_NOTIFICATIONS_SOUND).broadcast(getString(R.string.variableNameGenericData), seconds.toString())
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

    fun btnSettingsOnClick(view: View) {
        val intent = Intent(applicationContext, ApplicationSettingsActivity::class.java)
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