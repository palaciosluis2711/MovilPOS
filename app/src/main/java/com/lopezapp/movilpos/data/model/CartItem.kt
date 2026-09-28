package com.lopezapp.movilpos.data.model

import com.lopezapp.movilpos.util.roundToTwoDecimals

data class CartItem(
    val product: Product,
    val quantity: Int,
    val unitPriceOverride: Double? = null,
    val isBundleDiscounted: Boolean = false
) {
    val effectiveUnitPrice: Double
        get() = unitPriceOverride ?: product.price

    val subtotal: Double
        get() = (effectiveUnitPrice * quantity).roundToTwoDecimals()
}

