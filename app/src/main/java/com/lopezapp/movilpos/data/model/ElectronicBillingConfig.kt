package com.lopezapp.movilpos.data.model

enum class DteEnvironment {
    SANDBOX, PRODUCTION
}

data class ElectronicBillingConfig(
    val isEnabled: Boolean = true,
    val environment: DteEnvironment = DteEnvironment.SANDBOX,
    val nit: String = "",
    val apiToken: String = "",
    val establishmentCode: String = "0001",
    val posCode: String = "0001",
    val economicActivity: String = "",
    val certificatePassword: String = "",
    val certificateUri: String? = null,
    val certificateFileName: String? = null,
    val isSimulationMode: Boolean = true
)
