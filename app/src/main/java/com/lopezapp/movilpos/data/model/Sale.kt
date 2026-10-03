package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class InvoiceType {
    CONSUMIDOR_FINAL,
    CREDITO_FISCAL,
    TICKET
}

data class SaleItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

data class Sale(
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val customerName: String,
    val invoiceType: InvoiceType = InvoiceType.CONSUMIDOR_FINAL,
    val paymentMethodId: String,
    val paymentMethodName: String,
    val items: List<SaleItem>,
    val totalAmount: Double,
    val cashReceived: Double = 0.0,
    val changeAmount: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis(),
    val isDteIssued: Boolean = false,
    val dteGenerationCode: String? = null,
    val dteReceptionSeal: String? = null,
    val dteControlNumber: String? = null,
    val dteType: String? = null
)
