package com.lopezapp.movilpos.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Sanitizes input string for decimal fields:
 * - Filters out all characters that are not digits ('0'..'9') or '.'.
 * - Rejects commas ',' and any other non-digit symbols.
 * - Allows at most one '.' in the string (drops any subsequent '.' characters).
 * - If the sanitized result starts with '.', automatically prefixes it with '0' (e.g., "." -> "0.", ".5" -> "0.5").
 */
fun sanitizeDecimalInput(input: String): String {
    var hasDot = false
    val builder = StringBuilder()
    for (char in input) {
        if (char.isDigit()) {
            builder.append(char)
        } else if ((char == '.') && (!hasDot)) {
            hasDot = true
            builder.append(char)
        }
    }
    val sanitized = builder.toString()
    return if (sanitized.startsWith(".")) {
        "0$sanitized"
    } else {
        sanitized
    }
}

/**
 * Sanitizes input for decimal text fields while properly preserving or adjusting
 * the cursor position (selection).
 *
 * Specifically:
 * - When a field is empty and the user types '.', the field text becomes "0." AND the cursor/selection
 *   is explicitly placed AFTER the dot (selection = TextRange(2) or cursor index at end of "0.").
 * - When leading '.' is auto-prefixed with '0' (e.g. ".5" -> "0.5"), cursor index is shifted
 *   so that typing '.' followed by '5' results in "0.5" with cursor after '5'.
 */
fun sanitizeDecimalTextFieldValue(
    newValue: TextFieldValue,
    oldValue: TextFieldValue? = null,
): TextFieldValue {
    val newText = newValue.text
    val isDeletion = (oldValue != null) && (oldValue.text.length > newValue.text.length)

    if (newText == ".") {
        return if (isDeletion) {
            TextFieldValue(text = "", selection = TextRange(0))
        } else {
            TextFieldValue(text = "0.", selection = TextRange(2))
        }
    }

    if (newText.startsWith(".")) {
        return if (isDeletion) {
            val withoutDot = sanitizeDecimalInput(newText.drop(1))
            val maxPos = withoutDot.length
            val newStart = newValue.selection.start.coerceIn(0, maxPos)
            val newEnd = newValue.selection.end.coerceIn(0, maxPos)
            TextFieldValue(
                text = withoutDot,
                selection = TextRange(newStart, newEnd)
            )
        } else {
            val sanitized = sanitizeDecimalInput(newText)
            val cursorOffset = 1
            val newStart = (newValue.selection.start + cursorOffset).coerceIn(0, sanitized.length)
            val newEnd = (newValue.selection.end + cursorOffset).coerceIn(0, sanitized.length)
            TextFieldValue(
                text = sanitized,
                selection = TextRange(newStart, newEnd)
            )
        }
    }

    val sanitized = sanitizeDecimalInput(newText)

    if (sanitized == newText) {
        return newValue.copy(text = sanitized)
    }

    if (oldValue != null && sanitized == oldValue.text) {
        val maxPos = sanitized.length
        val oldStart = oldValue.selection.start.coerceIn(0, maxPos)
        val oldEnd = oldValue.selection.end.coerceIn(0, maxPos)
        return TextFieldValue(
            text = sanitized,
            selection = TextRange(oldStart, oldEnd)
        )
    }

    val maxPos = sanitized.length
    val newStart = newValue.selection.start.coerceIn(0, maxPos)
    val newEnd = newValue.selection.end.coerceIn(0, maxPos)
    return TextFieldValue(
        text = sanitized,
        selection = TextRange(newStart, newEnd)
    )
}
