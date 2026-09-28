package com.lopezapp.movilpos.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun formatCurrency_defaultTwoDecimals() {
        assertEquals("$0.00", formatCurrency(0.0))
        assertEquals("$10.00", formatCurrency(10.0))
        assertEquals("$10.50", formatCurrency(10.5))
        assertEquals("$10.12", formatCurrency(10.12))
    }

    @Test
    fun formatCurrency_thousandsSeparator() {
        assertEquals("$1,250.00", formatCurrency(1250.0))
        assertEquals("$1,000,000.00", formatCurrency(1000000.0))
        assertEquals("$1,250.50", formatCurrency(1250.5))
    }

    @Test
    fun formatCurrency_moreThanTwoDecimals() {
        assertEquals("$10.1234", formatCurrency(10.1234))
        assertEquals("$1,250.12345", formatCurrency(1250.12345))
        assertEquals("$0.123456", formatCurrency(0.123456))
    }

    @Test
    fun formatCurrency_nullAndSpecialValues() {
        assertEquals("$0.00", formatCurrency(null as Double?))
        assertEquals("$0.00", formatCurrency(Double.NaN))
        assertEquals("$0.00", formatCurrency(Double.POSITIVE_INFINITY))
    }

    @Test
    fun roundToTwoDecimals_roundsHalfUp() {
        assertEquals(8.33, roundToTwoDecimals(8.333333333333334), 0.0001)
        assertEquals(8.34, roundToTwoDecimals(8.335), 0.0001)
        assertEquals(10.0, roundToTwoDecimals(10.0), 0.0001)
    }

    @Test
    fun formatCurrency_calculatedDivisionRoundsToTwoDecimals() {
        assertEquals("$8.33", formatCurrency(25.0 / 3.0))
        assertEquals("$3.33", formatCurrency(10.0 / 3.0))
    }

    @Test
    fun formatCurrency_customCurrencySymbol() {
        assertEquals("€10.50", formatCurrency(10.5, currencySymbol = "€"))
        assertEquals("MXN$1,250.00", formatCurrency(1250.0, currencySymbol = "MXN$"))
        assertEquals("USD$0.00", formatCurrency(0.0, currencySymbol = "USD$"))
    }

    @Test
    fun formatCurrency_customDecimalPlaces() {
        assertEquals("$10.500", formatCurrency(10.5, defaultDecimalPlaces = 3))
        assertEquals("$10.5000", formatCurrency(10.5, defaultDecimalPlaces = 4))
        assertEquals("€10.1234", formatCurrency(10.1234, currencySymbol = "€", defaultDecimalPlaces = 4))
    }

    @Test
    fun formatCurrency_allowExtraDecimalsFalse() {
        assertEquals("$10.12", formatCurrency(10.1234, defaultDecimalPlaces = 2, allowExtraDecimals = false))
        assertEquals("$10.123", formatCurrency(10.12345, defaultDecimalPlaces = 3, allowExtraDecimals = false))
        assertEquals("MXN$10.12", formatCurrency(10.1234, currencySymbol = "MXN$", defaultDecimalPlaces = 2, allowExtraDecimals = false))
    }
}
