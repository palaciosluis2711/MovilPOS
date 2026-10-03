package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class TaxValueType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class Tax(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val valueType: TaxValueType,
    val value: Double
)

fun Tax.getFormattedLabel(): String {
    if (name.contains("%")) return name
    val isInteger = (value % 1.0) == 0.0
    val valueStr = if (valueType == TaxValueType.PERCENTAGE) {
        "${if (isInteger) value.toInt().toString() else value.toString()}%"
    } else {
        if (isInteger) value.toInt().toString() else value.toString()
    }
    return "$name ($valueStr)"
}

fun List<Tax>.getFormattedTaxLabel(): String {
    if (isEmpty()) return "Impuestos (Desglose)"
    return joinToString(", ") { it.getFormattedLabel() }
}

fun calculatePriceWithoutTax(priceWithTax: Double, taxes: List<Tax>): Double {
    val totalPercentage = taxes.filter { it.valueType == TaxValueType.PERCENTAGE }.sumOf { it.value }
    val totalFixed = taxes.filter { it.valueType == TaxValueType.FIXED_AMOUNT }.sumOf { it.value }
    return (priceWithTax - totalFixed) / (1.0 + totalPercentage / 100.0)
}
