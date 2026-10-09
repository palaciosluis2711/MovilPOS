package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class PrintMode {
    THERMAL_ROLL,
    LETTER_SHEET
}

data class LabelSize(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val widthMm: Double,
    val heightMm: Double,
    val isFavorite: Boolean = false
)
