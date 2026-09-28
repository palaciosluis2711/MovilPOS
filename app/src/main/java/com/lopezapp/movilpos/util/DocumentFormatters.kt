package com.lopezapp.movilpos.util

fun formatDui(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(9)
    return if (digits.length > 8) {
        "${digits.substring(0, 8)}-${digits.substring(8)}"
    } else {
        digits
    }
}

fun formatNit(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(14)
    return when {
        digits.length <= 4 -> digits
        digits.length <= 10 -> "${digits.substring(0, 4)}-${digits.substring(4)}"
        digits.length <= 13 -> "${digits.substring(0, 4)}-${digits.substring(4, 10)}-${digits.substring(10)}"
        else -> "${digits.substring(0, 4)}-${digits.substring(4, 10)}-${digits.substring(10, 13)}-${digits.substring(13)}"
    }
}

fun formatPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(8)
    return if (digits.length > 4) {
        "${digits.substring(0, 4)}-${digits.substring(4)}"
    } else {
        digits
    }
}
