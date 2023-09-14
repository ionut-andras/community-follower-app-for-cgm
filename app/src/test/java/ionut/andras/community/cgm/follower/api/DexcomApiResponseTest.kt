package ionut.andras.community.cgm.follower.api

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
        assert(!SUT.isSuccess())
        SUT.data = "{}"
        assert(SUT.isSuccess())
    }

    @Test
    fun errorOccurred() {
        assert(!SUT.errorOccurred())
        SUT.error = "0000"
        assert(SUT.errorOccurred())
    }

    @Test
    fun exceptionOccurred() {
        assert(!SUT.exceptionOccurred())
        SUT.exception = ""
        assert(SUT.exceptionOccurred())
    }
}