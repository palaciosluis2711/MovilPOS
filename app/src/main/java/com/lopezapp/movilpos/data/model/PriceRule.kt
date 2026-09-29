package com.lopezapp.movilpos.data.model

import java.util.UUID

enum class BaseVariable {
    COST, PRICE
}

enum class ArithmeticOperator {
    ADD, SUBTRACT, MULTIPLY, DIVIDE
}

data class PriceRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseVariable: BaseVariable,
    val operator: ArithmeticOperator,
    val value: Double,
    val applyToAllCategories: Boolean = true,
    val categoryNames: List<String> = emptyList(),
    val applyToAllCustomers: Boolean = true,
    val customerIds: List<String> = emptyList(),
    val applyToBundles: Boolean = false,
    val applyToServices: Boolean = false,
    val applyToAlreadyDiscounted: Boolean = false,
    val isActive: Boolean = true
) {
    fun calculatePrice(basePrice: Double, cost: Double): Double {
        val base = if (baseVariable == BaseVariable.COST) cost else basePrice
        return when (operator) {
            ArithmeticOperator.ADD -> base + value
            ArithmeticOperator.SUBTRACT -> base - value
            ArithmeticOperator.MULTIPLY -> base * value
            ArithmeticOperator.DIVIDE -> if (value != 0.0) base / value else base
        }
    }

    fun formulaRepresentation(): String {
        val baseStr = when (baseVariable) {
            BaseVariable.COST -> "[Costo]"
            BaseVariable.PRICE -> "[Precio]"
        }
        val opStr = when (operator) {
            ArithmeticOperator.ADD -> "+"
            ArithmeticOperator.SUBTRACT -> "-"
            ArithmeticOperator.MULTIPLY -> "×"
            ArithmeticOperator.DIVIDE -> "÷"
        }
        val valStr = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
        return "$baseStr $opStr $valStr"
    }
}
