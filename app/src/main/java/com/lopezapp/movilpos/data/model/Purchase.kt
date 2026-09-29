package com.lopezapp.movilpos.data.model

import java.util.UUID

data class PurchaseItem(
    val productId: String,
    val productName: String,
    val quantity: Int = 1,
    val unitCost: Double = 0.0
)

data class Purchase(
    val id: String = UUID.randomUUID().toString(),
    val supplierId: String,
    val supplierName: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val items: List<PurchaseItem> = emptyList(),
    val totalCost: Double = 0.0
)
