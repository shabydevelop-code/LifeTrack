package com.shabydevelop.eattrack

import org.junit.Test
import org.junit.Assert.*

class WeightPrecisionTest {
    @Test fun exactGramsAndRoundTrip() {
        assertEquals(72501, parseWeightGrams("72,501"))
        assertEquals(72500, parseWeightGrams("72.500"))
        assertEquals("72.501", editableWeightKg(72501))
        for (grams in listOf(1, 1000, 72501, Int.MAX_VALUE))
            assertEquals(grams, parseWeightGrams(editableWeightKg(grams)))
        assertNull(parseWeightGrams("72.5001"))
        assertNull(parseWeightGrams("2147483.648"))
        assertNull(parseWeightGrams("invalid"))
    }
}
