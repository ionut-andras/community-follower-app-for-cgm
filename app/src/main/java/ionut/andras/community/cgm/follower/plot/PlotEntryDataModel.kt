package ionut.andras.community.cgm.follower.plot

import java.time.ZoneOffset

data class PlotEntryDataModel (
    var hour: String = "0",
    var minute: String = "0",
    var timestamp: Long = 0,
    var dateTime: String = "",
    var timeZoneOffset: ZoneOffset = ZoneOffset.UTC
)
