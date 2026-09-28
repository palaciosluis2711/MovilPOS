package com.lopezapp.movilpos.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Rounds a Double value to 2 decimal places using HALF_UP rounding.
 */
fun roundToTwoDecimals(value: Double): Double {
    if (value.isNaN() || value.isInfinite()) return 0.0
    return BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
}

@JvmName("roundToTwoDecimalsExt")
fun Double.roundToTwoDecimals(): Double = roundToTwoDecimals(this)

/**
 * Formats monetary amounts according to user formatting preferences:
 * - Default decimal places (2, 3, 4, etc.)
 * - Custom currency symbol (e.g., "$", "€", "MXN$", "USD$")
 * - Option to allow extra decimals when explicitly specified
 */
fun formatCurrency(
    amount: Double?,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
): String {
    val decimals = defaultDecimalPlaces.coerceIn(0, 10)
    val defaultZeros = if (decimals > 0) "0".repeat(decimals) else ""
    val zeroResult = if (decimals > 0) "${currencySymbol}0.$defaultZeros" else "${currencySymbol}0"

    if (amount == null || amount.isNaN() || amount.isInfinite()) {
        return zeroResult
    }

    var valueToFormat = amount
    var bd = BigDecimal.valueOf(valueToFormat).stripTrailingZeros()
    var scale = bd.scale().coerceAtLeast(0)

    if (scale > 6) {
        valueToFormat = BigDecimal.valueOf(valueToFormat)
            .setScale(decimals, RoundingMode.HALF_UP)
            .toDouble()
        bd = BigDecimal.valueOf(valueToFormat).stripTrailingZeros()
        scale = bd.scale().coerceAtLeast(0)
    }

    val symbols = DecimalFormatSymbols(Locale.US).apply {
        this.currencySymbol = currencySymbol
    }

    val pattern = if (decimals > 0) "\u00A4#,##0." + "0".repeat(decimals) else "\u00A4#,##0"

    val formatter = DecimalFormat(pattern, symbols).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = if (allowExtraDecimals && scale > decimals) scale else decimals
        roundingMode = RoundingMode.HALF_UP
    }

    return formatter.format(valueToFormat)
}

fun formatCurrency(
    amount: Double,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
): String = formatCurrency(amount as Double?, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)

fun formatCurrency(
    amount: Number?,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
): String = formatCurrency(amount?.toDouble(), currencySymbol, defaultDecimalPlaces, allowExtraDecimals)
