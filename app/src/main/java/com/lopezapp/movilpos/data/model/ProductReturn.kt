package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class ReturnReason {
    DEFECTIVE,
    WRONG_ITEM,
    CUSTOMER_REMORSE,
    SIZE_CHANGE,
    OTHER
}

fun ReturnReason.toSpanishLabel(): String {
    return when (this) {
        ReturnReason.DEFECTIVE -> "Defectuoso / Dañado"
        ReturnReason.WRONG_ITEM -> "Artículo Incorrecto"
        ReturnReason.CUSTOMER_REMORSE -> "Arrepentimiento del Cliente"
        ReturnReason.SIZE_CHANGE -> "Cambio de Talla / Modelo"
        ReturnReason.OTHER -> "Otro"
    }
}

data class ReturnItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val refundSubtotal: Double = quantity * unitPrice
)

data class ProductReturn(
    val id: String = UUID.randomUUID().toString(),
    val originalSaleId: String,
    val customerId: String? = null,
    val customerName: String,
    val reason: ReturnReason = ReturnReason.DEFECTIVE,
    val notes: String = "",
    val items: List<ReturnItem> = emptyList(),
    val totalRefundAmount: Double = items.sumOf { it.refundSubtotal },
    val refundMethod: String = "Efectivo",
    val cashierId: String = "",
    val cashierName: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)
