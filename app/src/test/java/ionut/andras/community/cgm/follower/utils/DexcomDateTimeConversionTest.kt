package ionut.andras.community.cgm.follower.utils

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal class DexcomDateTimeConversionTest {
    private lateinit var SUT: DexcomDateTimeConversion

    @BeforeEach
    fun setUp() {
        SUT = DexcomDateTimeConversion()
    }

    @AfterEach
    fun tearDown() {
    }
}