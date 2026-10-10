package com.lopezapp.movilpos.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

fun formatNrc(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(7)
    return if (digits.length > 6) {
        "${digits.substring(0, 6)}-${digits.substring(6)}"
    } else {
        digits
    }
}

class NrcVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(7)
        val formatted = formatNrc(raw)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                val transformed = if (clamped <= 6) clamped else clamped + 1
                return transformed.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                val original = if (clamped <= 6) clamped else clamped - 1
                return original.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
