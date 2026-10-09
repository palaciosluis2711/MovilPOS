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
    val subtotal: Double,
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
    val dteType: String? = null,
    val shiftId: String? = null,
    val cashierId: String = "",
    val cashierName: String = "",
    val isVoided: Boolean = false,
    val voidReason: String? = null,
    val voidedAtMillis: Long? = null,
    val contingencyMode: Boolean = false,
    val isCredit: Boolean = false,
    val creditDueDateMillis: Long? = null,
    val paidAmount: Double = 0.0,
    val remainingBalance: Double = totalAmount,
    val creditStatus: CreditStatus = CreditStatus.UNPAID,
)
