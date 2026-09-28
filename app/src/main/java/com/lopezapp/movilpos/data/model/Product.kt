package com.lopezapp.movilpos.data.model

import java.util.UUID

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String? = null,
    val category: String = "General",
    val brand: String = "",
    val unitOfMeasure: String = "unidad",
    val cost: Double = 0.0,
    val price: Double,
    val alertQuantity: Int = 0,
    val stock: Int,
    val imageUri: String? = null,
    val appliedTaxIds: List<String> = emptyList(),
    val isTaxIncludedInPrice: Boolean = false,
    val isBundle: Boolean = false,
    val bundleItems: List<BundleItem> = emptyList(),
    val isService: Boolean = false
)
