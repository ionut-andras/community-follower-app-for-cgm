package ionut.andras.community.cgm.follower.common

import android.graphics.Color
import ionut.andras.community.cgm.follower.configuration.Configuration

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