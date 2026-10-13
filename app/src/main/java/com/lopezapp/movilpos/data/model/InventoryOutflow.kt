package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class OutflowReason {
    CONSUMO_INTERNO,
    MERMA_DANADO,
    VENCIDO,
    MUESTRA_PROMOCION,
    AJUSTE_INVENTARIO
}

fun OutflowReason.toSpanishLabel(): String {
    return when (this) {
        OutflowReason.CONSUMO_INTERNO -> "Consumo Propio / Uso Interno"
        OutflowReason.MERMA_DANADO -> "Merma / Producto Dañado"
        OutflowReason.VENCIDO -> "Producto Vencido"
        OutflowReason.MUESTRA_PROMOCION -> "Muestra Gratis / Promoción"
        OutflowReason.AJUSTE_INVENTARIO -> "Ajuste de Conteo"
    }
}

data class OutflowItem(
    val productId: String,
    val productName: String,
    val quantity: Int = 1,
    val unitCost: Double = 0.0,
    val totalCost: Double = quantity * unitCost
)

data class InventoryOutflow(
    val id: String = UUID.randomUUID().toString(),
    val reason: OutflowReason = OutflowReason.CONSUMO_INTERNO,
    val registeredByUserId: String = "",
    val registeredByUserName: String = "",
    val notes: String = "",
    val items: List<OutflowItem> = emptyList(),
    val totalCost: Double = items.sumOf { it.totalCost },
    val dateMillis: Long = System.currentTimeMillis()
)
