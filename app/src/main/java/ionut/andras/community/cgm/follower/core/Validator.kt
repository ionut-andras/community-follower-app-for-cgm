package ionut.andras.community.cgm.follower.core

import org.json.JSONArray

class Validator {
    fun isEmptyGlucoseValueHistory(glucoseValueHistory: String): Boolean{
        val temporaryData = JSONArray(glucoseValueHistory)
        return (temporaryData.length() == 0)
    }
}