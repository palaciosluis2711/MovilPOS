package com.lopezapp.movilpos.data.model

import java.util.UUID

data class PaymentMethod(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isDefault: Boolean = false
)
