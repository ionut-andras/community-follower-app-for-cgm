package ionut.andras.community.cgm.follower.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class DexcomDateTest {

    @Test
    fun parsesPositiveOffset() {
        assertEquals(DexcomDate(1663163157, 3 * 3600), DexcomDate.parse("Date(1663163157000+0300)"))
    }

    @Test
    fun parsesNegativeOffset() {
        assertEquals(DexcomDate(1663163157, -(5 * 3600 + 30 * 60)), DexcomDate.parse("Date(1663163157000-0530)"))
    }

    @Test
    fun parsesDateWithoutOffset() {
        assertEquals(DexcomDate(1663163157, 0), DexcomDate.parse("/Date(1663163157000)/"))
    }

    @Test
    fun rejectsUnknownFormat() {
        assertThrows(IllegalArgumentException::class.java) { DexcomDate.parse("2022-09-14T13:45:57") }
    }
}
