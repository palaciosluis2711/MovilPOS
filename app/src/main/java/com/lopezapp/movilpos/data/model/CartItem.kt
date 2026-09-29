package com.lopezapp.movilpos.data.model

import com.lopezapp.movilpos.util.roundToTwoDecimals
import java.util.UUID

data class CartItem(
    val product: Product,
    val quantity: Int,
    val unitPriceOverride: Double? = null,
    val isBundleDiscounted: Boolean = false,
    val isRuleDiscounted: Boolean = false,
    val appliedRuleId: String? = null,
    val appliedRuleName: String? = null,
    val id: String = UUID.randomUUID().toString(),
) {
    val effectiveUnitPrice: Double
        get() = unitPriceOverride ?: product.price

    val subtotal: Double
        get() = (effectiveUnitPrice * quantity).roundToTwoDecimals()
}

