package ionut.andras.community.dexcomrelated.followerfordexcom.api

import ionut.andras.community.dexcomrelated.followerfordexcom.utils.DateTimeConversion
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DexcomApiResponseTest {
    private lateinit var SUT: DexcomApiResponse

    @BeforeEach
    fun setUp() {
        SUT = DexcomApiResponse()
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun isSuccess() {
        assert(false === SUT.isSuccess())
        SUT.data = "{}"
        assert(true === SUT.isSuccess())
    }

    @Test
    fun errorOccurred() {
        assert(false === SUT.errorOccurred())
        SUT.error = "0000"
        assert(true === SUT.errorOccurred())
    }

    @Test
    fun exceptionOccurred() {
        assert(false === SUT.exceptionOccurred())
        SUT.exception = ""
        assert(true === SUT.exceptionOccurred())
    }
}