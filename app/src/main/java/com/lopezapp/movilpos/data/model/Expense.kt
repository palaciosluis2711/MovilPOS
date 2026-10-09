package com.lopezapp.movilpos.data.model

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val shiftId: String? = null,
    val cashierId: String = "",
    val cashierName: String = "",
    val category: String,
    val description: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis()
)
