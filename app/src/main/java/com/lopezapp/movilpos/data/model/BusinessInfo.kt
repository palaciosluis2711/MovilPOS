package com.lopezapp.movilpos.data.model

data class BusinessInfo(
    val name: String = "Mi Negocio",
    val nit: String = "",
    val nrc: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val socialMedia: String = "",
    val commercialName: String = "",
    val logoUri: String? = null
)
