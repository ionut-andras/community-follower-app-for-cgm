package ionut.andras.community.cgm.follower.utils

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DateTimeConversionTest {
    private lateinit var SUT: DateTimeConversion

    @BeforeEach
    fun setUp() {
        SUT = DateTimeConversion()
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getCurrentTimestamp() {
        val result = SUT.getCurrentTimestamp()
        assert(result == (System.currentTimeMillis() / 1000))
    }
}