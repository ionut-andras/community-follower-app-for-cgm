package ionut.andras.community.cgm.follower.plot

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import ionut.andras.community.dexcomrelated.followerfordexcom.R
import ionut.andras.community.cgm.follower.common.GlucoseValueColorRange
import ionut.andras.community.cgm.follower.configuration.Configuration


class GlucoseMarkerView(context: Context?, layoutId: Int) :
    MarkerView(context, layoutId) {

    private var applicationContext: Context? = null
    private var configuration: Configuration? = null
    private var glucoseValue: TextView? = null
    private var valuesFormatter: DexcomChartValuesFormatter = DexcomChartValuesFormatter()
    private lateinit var glucoseValueColorRange: GlucoseValueColorRange

    init {
        Log.i("GlucoseMarkerView", "Constructor")
        applicationContext = context
        // This marker-view only displays a textview
        glucoseValue = findViewById<View>(R.id.glucosePlotMarkerView) as TextView
    }

    fun setConfigurationObject(configuration: Configuration) {
        this.configuration = configuration
        glucoseValueColorRange = GlucoseValueColorRange(this.configuration!!)
    }

    // Callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        Log.i("GlucoseMarkerView", "refreshContent")
        if (e != null) {
            glucoseValue!!.text = buildString {
                append(valuesFormatter.convertTimestampInAxisLabel(e.x))
                append(" / ")
                append(e.y.toInt())
                append(" ")
                append(applicationContext?.getString(R.string.glucoseMeasureUnit))
            }
            applicationContext?.let {
                val markerViewGlucoseColor = glucoseValueColorRange.getGlucoseColorValue(e.y.toInt())
                glucoseValue!!.setTextColor(markerViewGlucoseColor)
                // glucoseValue!!.setTextColor(ContextCompat.getColor(it, R.color.orange))
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        Log.i("GlucoseMarkerView", "getOffset")
        // This will center the marker-view horizontally
        // and will cause the marker-view to be above the selected value
        super.setOffset((-width / 2).toFloat(), (-height).toFloat())
        return super.getOffset()
    }
}