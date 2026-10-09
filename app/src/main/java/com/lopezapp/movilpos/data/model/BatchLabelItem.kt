package com.lopezapp.movilpos.data.model

data class BatchLabelItem(
    val productId: String,
    val productName: String,
    val barcode: String,
    val price: Double,
    val category: String,
    val brand: String,
    val quantity: Int = 1
)
