package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class Role {
    ADMINISTRATOR,
    SUPERVISOR,
    CASHIER
}

fun Role.toSpanishLabel(): String = when (this) {
    Role.ADMINISTRATOR -> "Administrador"
    Role.SUPERVISOR -> "Supervisor"
    Role.CASHIER -> "Cajero"
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val pin: String,
    val role: Role = Role.CASHIER,
    val isActive: Boolean = true,
)

