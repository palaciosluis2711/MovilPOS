package com.lopezapp.movilpos.ui.components

import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.util.roundToTwoDecimals
import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftDialogsTest {

    @Test
    fun openShift_userValidation_selectsActiveUser() {
        val users = listOf(
            User(id = "1", name = "Juan Pérez", pin = "1234", role = Role.CASHIER, isActive = false),
            User(id = "2", name = "Maria Gomez", pin = "5678", role = Role.SUPERVISOR, isActive = true)
        )

        val activeUsers = users.filter { it.isActive }
        assertEquals(1, activeUsers.size)
        assertEquals("Maria Gomez", activeUsers.first().name)
    }

    @Test
    fun closeShift_differenceCalculation_sobrante() {
        val shift = CashShift(
            cashierId = "1",
            cashierName = "Carlos",
            initialFloat = 50.0,
            totalCashSales = 100.0,
            totalCardSales = 50.0
        )

        val expectedCash = shift.expectedCash // 50.0 + 100.0 = 150.0
        val actualCounted = 160.0
        val difference = (actualCounted - expectedCash).roundToTwoDecimals()

        assertEquals(150.0, expectedCash, 0.001)
        assertEquals(10.0, difference, 0.001)
    }

    @Test
    fun closeShift_differenceCalculation_faltante() {
        val shift = CashShift(
            cashierId = "1",
            cashierName = "Carlos",
            initialFloat = 50.0,
            totalCashSales = 100.0,
            totalCardSales = 50.0
        )

        val expectedCash = shift.expectedCash // 150.0
        val actualCounted = 142.50
        val difference = (actualCounted - expectedCash).roundToTwoDecimals()

        assertEquals(-7.50, difference, 0.001)
    }

    @Test
    fun closeShift_differenceCalculation_cuadrado() {
        val shift = CashShift(
            cashierId = "1",
            cashierName = "Carlos",
            initialFloat = 50.0,
            totalCashSales = 100.0,
            totalCardSales = 50.0
        )

        val expectedCash = shift.expectedCash // 150.0
        val actualCounted = 150.0
        val difference = (actualCounted - expectedCash).roundToTwoDecimals()

        assertEquals(0.0, difference, 0.001)
    }
}
