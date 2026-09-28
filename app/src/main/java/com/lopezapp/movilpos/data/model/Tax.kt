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
