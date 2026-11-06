package com.example.moneymate

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

/**
 * Unit tests for MoneyMate's pure money helpers (run on the computer,
 * no phone needed): Build -> Run Tests, or open this file and press
 * the green arrow next to the class name.
 */
class MoneyMateTest {

    @Test
    fun randsFormatsCentsIntoRandText() {
        assertEquals("R 48 650.00", rands(4_865_000))
        assertEquals("R 1 234.56", rands(123_456))
        assertEquals("R 0.00", rands(0))
    }

    @Test
    fun plainRandsHasNoCurrencySymbol() {
        assertEquals("4000.00", plainRands(400_000))
        assertEquals("2500.50", plainRands(250_050))
    }

    @Test
    fun parseRandReadsWhatPeopleType() {
        assertEquals(2500.0, parseRand("2500") as Double, 0.001)
        assertEquals(2500.0, parseRand("2 500") as Double, 0.001)
        assertEquals(2499.5, parseRand("2499,50") as Double, 0.001)
    }

    @Test
    fun parseRandRejectsJunk() {
        assertNull(parseRand("abc"))
        assertNull(parseRand(""))
        assertNull(parseRand("0"))
        assertNull(parseRand("-5"))
        assertNull(parseRand("99999999"))
    }
}