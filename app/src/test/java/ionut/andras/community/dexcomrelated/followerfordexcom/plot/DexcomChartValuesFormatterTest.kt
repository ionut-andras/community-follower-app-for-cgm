package ionut.andras.community.dexcomrelated.followerfordexcom.plot

import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DateTimeConversion
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class DexcomChartValuesFormatterTest {
    private lateinit var SUT: DexcomChartValuesFormatter

    @BeforeEach
    fun setUp() {
        SUT = DexcomChartValuesFormatter()
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun convertTimestampInAxisLabel() {
        assert("17:20".compareTo(SUT.convertTimestampInAxisLabel(62442F)) == 0)
    }
}