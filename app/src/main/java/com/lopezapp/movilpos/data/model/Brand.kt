package com.lopezapp.movilpos.data.model

import java.util.UUID

data class Brand(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val logoUri: String? = null
)
