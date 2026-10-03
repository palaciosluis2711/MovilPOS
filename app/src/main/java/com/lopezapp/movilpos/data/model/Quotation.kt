package com.lopezapp.movilpos.data.model

import java.util.UUID

data class QuotationItem(
    val productId: String,
    val productName: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val isDiscounted: Boolean = false,
    val appliedRuleName: String? = null
)

data class Quotation(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String,
    val customerEmail: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val expirationDateMillis: Long = System.currentTimeMillis() + (15L * 24 * 3600 * 1000),
    val items: List<QuotationItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val showTaxBreakdown: Boolean = false,
    val taxAmount: Double = 0.0,
    val showSignatureBlock: Boolean = false,
    val showStampBlock: Boolean = false,
    val showContactBlock: Boolean = false
)
