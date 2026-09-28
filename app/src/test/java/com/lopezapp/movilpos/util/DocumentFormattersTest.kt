package com.lopezapp.movilpos.util

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentFormattersTest {

    @Test
    fun testFormatDui() {
        assertEquals("", formatDui(""))
        assertEquals("0821", formatDui("0821"))
        assertEquals("08215678", formatDui("08215678"))
        assertEquals("08215678-9", formatDui("082156789"))
        assertEquals("08215678-9", formatDui("0821567890123"))
        assertEquals("08215678-9", formatDui("0821-5678-9"))
    }

    @Test
    fun testFormatNit() {
        assertEquals("", formatNit(""))
        assertEquals("0821", formatNit("0821"))
        assertEquals("0821-15", formatNit("082115"))
        assertEquals("0821-150820", formatNit("0821150820"))
        assertEquals("0821-150820-101", formatNit("0821150820101"))
        assertEquals("0821-150820-101-2", formatNit("08211508201012"))
        assertEquals("0821-150820-101-2", formatNit("08211508201012999"))
        assertEquals("0821-150820-101-2", formatNit("0821-150820-101-2"))
    }

    @Test
    fun testFormatPhone() {
        assertEquals("", formatPhone(""))
        assertEquals("7777", formatPhone("7777"))
        assertEquals("7777-8888", formatPhone("77778888"))
        assertEquals("7777-8888", formatPhone("7777888899"))
        assertEquals("7777-8888", formatPhone("7777-8888"))
    }

    @Test
    fun testDuiVisualTransformation() {
        val transformation = DuiVisualTransformation()

        val input = AnnotatedString("082156789")
        val transformed = transformation.filter(input)

        assertEquals("08215678-9", transformed.text.text)

        val offsetMapping = transformed.offsetMapping

        // originalToTransformed
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(4, offsetMapping.originalToTransformed(4))
        assertEquals(8, offsetMapping.originalToTransformed(8))
        assertEquals(10, offsetMapping.originalToTransformed(9))

        // transformedToOriginal
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        assertEquals(4, offsetMapping.transformedToOriginal(4))
        assertEquals(8, offsetMapping.transformedToOriginal(8))
        assertEquals(8, offsetMapping.transformedToOriginal(9)) // Right after dash
        assertEquals(9, offsetMapping.transformedToOriginal(10))
    }

    @Test
    fun testNitVisualTransformation() {
        val transformation = NitVisualTransformation()

        val input = AnnotatedString("08211508201012")
        val transformed = transformation.filter(input)

        assertEquals("0821-150820-101-2", transformed.text.text)

        val offsetMapping = transformed.offsetMapping

        // originalToTransformed
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(4, offsetMapping.originalToTransformed(4))
        assertEquals(6, offsetMapping.originalToTransformed(5))
        assertEquals(11, offsetMapping.originalToTransformed(10))
        assertEquals(13, offsetMapping.originalToTransformed(11))
        assertEquals(15, offsetMapping.originalToTransformed(13))
        assertEquals(17, offsetMapping.originalToTransformed(14))

        // transformedToOriginal
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        assertEquals(4, offsetMapping.transformedToOriginal(4))
        assertEquals(4, offsetMapping.transformedToOriginal(5)) // Right after 1st dash
        assertEquals(5, offsetMapping.transformedToOriginal(6))
        assertEquals(10, offsetMapping.transformedToOriginal(11))
        assertEquals(10, offsetMapping.transformedToOriginal(12)) // Right after 2nd dash
        assertEquals(11, offsetMapping.transformedToOriginal(13))
        assertEquals(13, offsetMapping.transformedToOriginal(15))
        assertEquals(13, offsetMapping.transformedToOriginal(16)) // Right after 3rd dash
        assertEquals(14, offsetMapping.transformedToOriginal(17))
    }

    @Test
    fun testPhoneVisualTransformation() {
        val transformation = PhoneVisualTransformation()

        val input = AnnotatedString("77778888")
        val transformed = transformation.filter(input)

        assertEquals("7777-8888", transformed.text.text)

        val offsetMapping = transformed.offsetMapping

        // originalToTransformed
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(4, offsetMapping.originalToTransformed(4))
        assertEquals(6, offsetMapping.originalToTransformed(5))
        assertEquals(9, offsetMapping.originalToTransformed(8))

        // transformedToOriginal
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        assertEquals(4, offsetMapping.transformedToOriginal(4))
        assertEquals(4, offsetMapping.transformedToOriginal(5)) // Right after dash
        assertEquals(5, offsetMapping.transformedToOriginal(6))
        assertEquals(8, offsetMapping.transformedToOriginal(9))
    }
}
