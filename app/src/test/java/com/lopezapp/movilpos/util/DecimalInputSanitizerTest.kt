package com.lopezapp.movilpos.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class DecimalInputSanitizerTest {

    @Test
    fun sanitizeDecimalInput_singleDotPrefixesZero() {
        assertEquals("0.", sanitizeDecimalInput("."))
    }

    @Test
    fun sanitizeDecimalInput_dotWithDecimalsPrefixesZero() {
        assertEquals("0.50", sanitizeDecimalInput(".50"))
        assertEquals("0.5", sanitizeDecimalInput(".5"))
    }

    @Test
    fun sanitizeDecimalInput_rejectsCommas() {
        assertEquals("125", sanitizeDecimalInput("12,5"))
        assertEquals("1250", sanitizeDecimalInput("12,50"))
        assertEquals("", sanitizeDecimalInput(","))
    }

    @Test
    fun sanitizeDecimalInput_rejectsLettersAndSpecialCharacters() {
        assertEquals("12.50", sanitizeDecimalInput("12" + '$' + "a.50"))
        assertEquals("100.99", sanitizeDecimalInput("abc100@#.99xyz"))
    }

    @Test
    fun sanitizeDecimalInput_allowsOnlyOneDot() {
        assertEquals("12.34", sanitizeDecimalInput("12.3.4"))
        assertEquals("0.50", sanitizeDecimalInput(".5.0"))
        assertEquals("0.", sanitizeDecimalInput(".."))
    }

    @Test
    fun sanitizeDecimalInput_validDecimalNumbersUnchanged() {
        assertEquals("0", sanitizeDecimalInput("0"))
        assertEquals("12", sanitizeDecimalInput("12"))
        assertEquals("12.5", sanitizeDecimalInput("12.5"))
        assertEquals("12.50", sanitizeDecimalInput("12.50"))
    }

    @Test
    fun sanitizeDecimalInput_emptyString() {
        assertEquals("", sanitizeDecimalInput(""))
    }

    @Test
    fun sanitizeDecimalTextFieldValue_dotInEmptyFieldPlacesCursorAfterDot() {
        val input = TextFieldValue(".", selection = TextRange(1))
        val result = sanitizeDecimalTextFieldValue(input)
        assertEquals("0.", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun sanitizeDecimalTextFieldValue_subsequentDigitAppendedAfterDot() {
        val input = TextFieldValue("0.5", selection = TextRange(3))
        val result = sanitizeDecimalTextFieldValue(input)
        assertEquals("0.5", result.text)
        assertEquals(TextRange(3), result.selection)
    }

    @Test
    fun sanitizeDecimalTextFieldValue_dotFivePrefixesZeroAndShiftsCursor() {
        val input = TextFieldValue(".5", selection = TextRange(2))
        val result = sanitizeDecimalTextFieldValue(input)
        assertEquals("0.5", result.text)
        assertEquals(TextRange(3), result.selection)
    }

    @Test
    fun sanitizeDecimalTextFieldValue_normalNumberPreservesSelection() {
        val input = TextFieldValue("12.50", selection = TextRange(5))
        val result = sanitizeDecimalTextFieldValue(input)
        assertEquals("12.50", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun sanitizeDecimalTextFieldValue_emptyString() {
        val input = TextFieldValue("", selection = TextRange(0))
        val result = sanitizeDecimalTextFieldValue(input)
        assertEquals("", result.text)
        assertEquals(TextRange(0), result.selection)
    }
}
