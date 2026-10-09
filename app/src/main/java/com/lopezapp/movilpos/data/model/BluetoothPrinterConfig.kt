package com.lopezapp.movilpos.data.model

data class BluetoothPrinterConfig(
    val macAddress: String? = null,
    val deviceName: String? = null,
    val paperWidthMm: Int = 80,
    val isConnected: Boolean = false,
    val autoPrintSales: Boolean = true,
)
