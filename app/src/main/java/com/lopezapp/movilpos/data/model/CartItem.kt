package com.lopezapp.movilpos.data.model

import com.lopezapp.movilpos.util.roundToTwoDecimals
import java.util.UUID

data class CartItem(
    val product: Product,
    val quantity: Int,
    val unitPriceOverride: Double? = null,
    val customUnitPrice: Double? = null,
    val isBundleDiscounted: Boolean = false,
    val isRuleDiscounted: Boolean = false,
    val appliedRuleId: String? = null,
    val appliedRuleName: String? = null,
    val linkedGroupId: String? = null,
    val parentProductId: String? = null,
    val id: String = UUID.randomUUID().toString(),
) {
    val calculatedUnitPrice: Double
        get() = unitPriceOverride ?: product.price

    val unitPrice: Double
        get() = customUnitPrice ?: calculatedUnitPrice

    val effectiveUnitPrice: Double
        get() = unitPrice

    val subtotal: Double
        get() = (unitPrice * quantity).roundToTwoDecimals()
}

