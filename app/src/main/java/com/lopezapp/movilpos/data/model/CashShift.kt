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
    val totalExpenses: Double = 0.0,
    val actualCashCounted: Double? = null,
    val status: ShiftStatus = ShiftStatus.OPEN,
) {
    val expectedCash: Double get() = maxOf(0.0, (initialFloat + totalCashSales) - totalExpenses)
    val difference: Double? get() = actualCashCounted?.let { it - expectedCash }
}
