package ionut.andras.community.cgm.follower.utils

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DateTimeConversionTest {
    private lateinit var SUT: DateTimeConversion

    @BeforeEach
    fun setUp() {
        SUT = DateTimeConversion(DateTimeObject(2023, 8,22, 16, 18, 0))
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getCurrentTimestamp() {
        val result = SUT.getCurrentTimestamp()
        assert(result == (System.currentTimeMillis() / 1000))
    }

    @Test
    fun getTimestamp() {
        val result = SUT.getLocalTimestamp()
        assert(result == 1695395760L)
    }
}