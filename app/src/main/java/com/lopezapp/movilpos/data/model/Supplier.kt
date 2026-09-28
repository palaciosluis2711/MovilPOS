package com.lopezapp.movilpos.data.model

import java.util.UUID

data class Supplier(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val logoUri: String? = null,
    val address: String? = null,
    val email: String? = null,
    val phone: String? = null
)
