package ionut.andras.community.dexcomrelated.followerfordexcom.constants

class DexcomTrendsConversionMap {
    companion object {
        const val FLAT: String = "Flat"
        const val FORTY_FIVE_DOWN: String = "FortyFiveDown"
        const val FORTY_FIVE_UP: String = "FortyFiveUp"
        const val SINGLE_DOWN: String = "SingleDown"
        const val SINGLE_UP: String = "SingleUp"
        const val DOUBLE_DOWN: String = "DoubleDown"
        const val DOUBLE_UP: String = "DoubleUp"
        // -------------------------------------------
        const val NONE: String = "None"
        const val NOT_COMPUTABLE: String = "NotComputable"
        const val RATE_OUT_OF_RANGE: String = "RateOutOfRange"

        val convert: MutableMap<String, String> = mutableMapOf(
            FLAT to "&#8594;",                  // OK
            FORTY_FIVE_DOWN to "&#8600;",      // OK
            FORTY_FIVE_UP to "&#8599;",        // OK
            SINGLE_DOWN to "&#8595;",           // OK
            SINGLE_UP to "&#8593;",             // OK
            DOUBLE_DOWN to "&#8595;&#8595;" ,   // OK
            DOUBLE_UP to "&#8593;&#8593;",      // OK
            // -------------------------------------------
            NONE to "",
            NOT_COMPUTABLE to "",
            RATE_OUT_OF_RANGE to ""
        )
    }


}