package com.lopezapp.movilpos.data.model

import java.util.UUID

data class UnitOfMeasure(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val abbreviation: String? = null,
    val isPackageOrBox: Boolean = false
)
