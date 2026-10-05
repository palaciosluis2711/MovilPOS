package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class ShiftStatus {
    OPEN,
    CLOSED
}

data class CashShift(
    val id: String = UUID.randomUUID().toString(),
    val cashierId: String,
    val cashierName: String,
    val openedAtMillis: Long = System.currentTimeMillis(),
    val closedAtMillis: Long? = null,
    val initialFloat: Double = 0.0,
    val totalCashSales: Double = 0.0,
    val totalCardSales: Double = 0.0,
    val totalOtherSales: Double = 0.0,
    val expectedCash: Double = initialFloat + totalCashSales,
    val actualCashCounted: Double? = null,
    val difference: Double? = null,
    val status: ShiftStatus = ShiftStatus.OPEN,
)
