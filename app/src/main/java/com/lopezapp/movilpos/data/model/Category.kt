package com.lopezapp.movilpos.data.model

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null
)
