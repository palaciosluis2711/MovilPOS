package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class DocumentType {
    DUI,
    NIT
}

data class Customer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val documentType: DocumentType = DocumentType.DUI,
    val documentNumber: String,
    val nrc: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val country: String = "El Salvador",
    val department: String,
    val municipality: String,
    val district: String,
    val address: String? = null,
    val isLargeContributor: Boolean = false,
    val commercialActivity: String? = null,
    val commercialName: String = "",
    val isDefault: Boolean = false,
    val currentDebt: Double = 0.0,
)
