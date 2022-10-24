package ionut.andras.community.dexcomrelated.followerfordexcom.common

import android.graphics.Color
import ionut.andras.community.dexcomrelated.followerfordexcom.configuration.Configuration
import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DateTimeConversion
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GlucoseValueColorRangeTest {
    private lateinit var SUT: GlucoseValueColorRange
    private lateinit var configuration: Configuration

    @BeforeEach
    fun setUp() {
        configuration = Configuration()
        SUT = GlucoseValueColorRange(configuration)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getGlucoseColorValue() {
        assert(Color.rgb(255, 165, 0) == SUT.getGlucoseColorValue(configuration.glucoseHighThreshold + 1))
        assert(Color.RED == SUT.getGlucoseColorValue(configuration.glucoseLowThreshold - 1))
        assert(Color.GREEN == SUT.getGlucoseColorValue(configuration.glucoseHighThreshold - 1))
        assert(Color.GREEN == SUT.getGlucoseColorValue(configuration.glucoseLowThreshold + 1))
    }
}