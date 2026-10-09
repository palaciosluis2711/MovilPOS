package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class CreditStatus {
    UNPAID,
    PARTIALLY_PAID,
    PAID
}

data class CustomerPayment(
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val customerName: String,
    val saleId: String? = null,
    val amount: Double,
    val paymentMethodName: String = "Efectivo",
    val notes: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
)
