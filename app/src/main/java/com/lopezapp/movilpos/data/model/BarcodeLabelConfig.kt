package com.lopezapp.movilpos.data.model

data class BarcodeLabelConfig(
    val showStoreName: Boolean = true,
    val showProductName: Boolean = true,
    val showBarcodeImage: Boolean = true,
    val showBarcodeNumber: Boolean = true,
    val showPrice: Boolean = true,
    val showCategoryBrand: Boolean = true,
    val printMode: PrintMode = PrintMode.THERMAL_ROLL,
    val hasGap: Boolean = false,
    val selectedSizeId: String = "std_1",
    val customWidthMm: Int = 50,
    val customHeightMm: Int = 25
)
