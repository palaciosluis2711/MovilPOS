package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.ShiftStatus
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.model.toSpanishLabel
import com.lopezapp.movilpos.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserAndShiftTest {

    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        repository = AppRepository()
    }

    @Test
    fun userCreation_andDefaults() {
        val user = User(name = "Maria Lopez", pin = "1122")
        assertNotNull(user.id)
        assertEquals("Maria Lopez", user.name)
        assertEquals("1122", user.pin)
        assertEquals(Role.CASHIER, user.role)
        assertTrue(user.isActive)

        val admin = User(id = "usr_admin", name = "Carlos Admin", pin = "9999", role = Role.ADMINISTRATOR, isActive = false)
        assertEquals("usr_admin", admin.id)
        assertEquals("Carlos Admin", admin.name)
        assertEquals("9999", admin.pin)
        assertEquals(Role.ADMINISTRATOR, admin.role)
        assertFalse(admin.isActive)
    }

    @Test
    fun roleToSpanishLabel_returnsCorrectLabels() {
        assertEquals("Administrador", Role.ADMINISTRATOR.toSpanishLabel())
        assertEquals("Supervisor", Role.SUPERVISOR.toSpanishLabel())
        assertEquals("Cajero", Role.CASHIER.toSpanishLabel())
    }

    @Test
    fun pinValidation_validatesAdminAndSupervisorOnly() {
        // Default admin (pin 1234)
        assertTrue(repository.validateAdminPin("1234"))
        assertFalse(repository.validateAdminPin("0000"))

        // Add Supervisor
        val supervisor = User(id = "sup_1", name = "Supervisora Ana", pin = "2468", role = Role.SUPERVISOR)
        repository.addUser(supervisor)
        assertTrue(repository.validateAdminPin("2468"))

        // Add Cashier
        val cashier = User(id = "cash_1", name = "Cajero Pedro", pin = "1357", role = Role.CASHIER)
        repository.addUser(cashier)
        assertFalse(repository.validateAdminPin("1357"))

        // Deactivate supervisor -> validation fails
        repository.updateUser(supervisor.copy(isActive = false))
        assertFalse(repository.validateAdminPin("2468"))
    }

    @Test
    fun cashShiftOpening_andDefaults() {
        val shift = CashShift(
            cashierId = "cashier_101",
            cashierName = "Lucia Gomez",
            initialFloat = 150.0
        )

        assertNotNull(shift.id)
        assertEquals("cashier_101", shift.cashierId)
        assertEquals("Lucia Gomez", shift.cashierName)
        assertEquals(150.0, shift.initialFloat, 0.001)
        assertEquals(0.0, shift.totalCashSales, 0.001)
        assertEquals(0.0, shift.totalCardSales, 0.001)
        assertEquals(0.0, shift.totalOtherSales, 0.001)
        assertEquals(150.0, shift.expectedCash, 0.001)
        assertNull(shift.actualCashCounted)
        assertNull(shift.difference)
        assertEquals(ShiftStatus.OPEN, shift.status)
        assertNull(shift.closedAtMillis)
    }

    @Test
    fun cashShiftSalesUpdate_andExpectedCashCalculation() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Carlos",
            initialFloat = 100.0
        )

        val updated = shift.copy(
            totalCashSales = shift.totalCashSales + 75.0,
            totalCardSales = shift.totalCardSales + 50.0,
            totalOtherSales = shift.totalOtherSales + 20.0
        )

        assertEquals(75.0, updated.totalCashSales, 0.001)
        assertEquals(50.0, updated.totalCardSales, 0.001)
        assertEquals(20.0, updated.totalOtherSales, 0.001)
        assertEquals(175.0, updated.expectedCash, 0.001) // 100 float + 75 cash sales
    }

    @Test
    fun cashShiftCashCounting_andDifferenceCalculations() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Carlos",
            initialFloat = 100.0,
            totalCashSales = 100.0
        )

        // Case 1: Surplus (Sobrante)
        val countedSurplus = 215.0
        val shiftSurplus = shift.copy(
            actualCashCounted = countedSurplus,
            status = ShiftStatus.CLOSED
        )
        assertEquals(15.0, shiftSurplus.difference!!, 0.001) // +15.0 sobrante

        // Case 2: Deficit (Faltante)
        val countedDeficit = 180.0
        val shiftDeficit = shift.copy(
            actualCashCounted = countedDeficit,
            status = ShiftStatus.CLOSED
        )
        assertEquals(-20.0, shiftDeficit.difference!!, 0.001) // -20.0 faltante

        // Case 3: Exact count
        val countedExact = 200.0
        val shiftExact = shift.copy(
            actualCashCounted = countedExact,
            status = ShiftStatus.CLOSED
        )
        assertEquals(0.0, shiftExact.difference!!, 0.001)
    }

    @Test
    fun cashShiftExpenses_updatesExpectedCash() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Carlos",
            initialFloat = 100.0,
            totalCashSales = 100.0,
            totalExpenses = 30.0
        )

        // 100.0 + 100.0 - 30.0 = 170.0
        assertEquals(170.0, shift.expectedCash, 0.001)

        // If expenses exceed initialFloat + cashSales, expectedCash is maxOf(0.0, ...)
        val highExpensesShift = shift.copy(totalExpenses = 250.0)
        assertEquals(0.0, highExpensesShift.expectedCash, 0.001)
    }

    @Test
    fun appRepository_shiftLifecycle_openAddSaleClose() {
        // Initial state
        assertNull(repository.activeShift.value)
        assertTrue(repository.shiftHistory.value.isEmpty())

        // 1. Open shift
        val cashier = User(id = "user_c1", name = "Laura", pin = "5555", role = Role.CASHIER)
        repository.addUser(cashier)

        val openedShift = repository.openShift(cashier = cashier, initialFloat = 80.0)

        assertNotNull(repository.activeShift.value)
        assertEquals(openedShift.id, repository.activeShift.value?.id)
        assertEquals("user_c1", repository.activeShift.value?.cashierId)
        assertEquals("Laura", repository.activeShift.value?.cashierName)
        assertEquals(80.0, repository.activeShift.value?.initialFloat ?: 0.0, 0.001)
        assertEquals(80.0, repository.activeShift.value?.expectedCash ?: 0.0, 0.001)
        assertEquals(ShiftStatus.OPEN, repository.activeShift.value?.status)
        assertEquals(1, repository.shiftHistory.value.size)

        // 2. Add cash sale -> activeShift totalCashSales and expectedCash updated automatically
        val cashSale = Sale(
            customerId = "c1",
            customerName = "Cliente 1",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm_efectivo",
            paymentMethodName = "Efectivo",
            items = listOf(SaleItem("prod_1", "Café", 2, 2.5, 5.0)),
            totalAmount = 5.0
        )
        repository.addSale(cashSale)

        var active = repository.activeShift.value
        assertNotNull(active)
        assertEquals(5.0, active?.totalCashSales ?: 0.0, 0.001)
        assertEquals(0.0, active?.totalCardSales ?: 0.0, 0.001)
        assertEquals(85.0, active?.expectedCash ?: 0.0, 0.001) // 80 float + 5 cash

        // Verify sale received cashier and shift details
        val recordedCashSale = repository.sales.value.last()
        assertEquals(openedShift.id, recordedCashSale.shiftId)
        assertEquals("user_c1", recordedCashSale.cashierId)
        assertEquals("Laura", recordedCashSale.cashierName)

        // 3. Add card sale -> totalCardSales updated, expectedCash unchanged
        val cardSale = Sale(
            customerId = "c1",
            customerName = "Cliente 1",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm_tarjeta",
            paymentMethodName = "Tarjeta de Crédito",
            items = listOf(SaleItem("prod_2", "Sandwich", 1, 4.0, 4.0)),
            totalAmount = 4.0
        )
        repository.addSale(cardSale)

        active = repository.activeShift.value
        assertEquals(5.0, active?.totalCashSales ?: 0.0, 0.001)
        assertEquals(4.0, active?.totalCardSales ?: 0.0, 0.001)
        assertEquals(0.0, active?.totalOtherSales ?: 0.0, 0.001)
        assertEquals(85.0, active?.expectedCash ?: 0.0, 0.001)

        // 4. Add other sale (e.g. transfer)
        val transferSale = Sale(
            customerId = "c1",
            customerName = "Cliente 1",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm_transf",
            paymentMethodName = "Transferencia Bancaria",
            items = listOf(SaleItem("prod_3", "Jugo", 1, 3.0, 3.0)),
            totalAmount = 3.0
        )
        repository.addSale(transferSale)

        active = repository.activeShift.value
        assertEquals(5.0, active?.totalCashSales ?: 0.0, 0.001)
        assertEquals(4.0, active?.totalCardSales ?: 0.0, 0.001)
        assertEquals(3.0, active?.totalOtherSales ?: 0.0, 0.001)
        assertEquals(85.0, active?.expectedCash ?: 0.0, 0.001)

        // 5. Close shift with cash counted = 90.0 (sobrante 5.0)
        val closedShift = repository.closeShift(countedCash = 90.0)

        assertNotNull(closedShift)
        assertEquals(ShiftStatus.CLOSED, closedShift?.status)
        assertNotNull(closedShift?.closedAtMillis)
        assertEquals(90.0, closedShift?.actualCashCounted ?: 0.0, 0.001)
        assertEquals(5.0, closedShift?.difference ?: 0.0, 0.001)

        // Active shift should be cleared
        assertNull(repository.activeShift.value)

        // Shift in history updated to closed
        assertEquals(1, repository.shiftHistory.value.size)
        val historicalShift = repository.shiftHistory.value.first()
        assertEquals(ShiftStatus.CLOSED, historicalShift.status)
        assertEquals(90.0, historicalShift.actualCashCounted ?: 0.0, 0.001)
        assertEquals(5.0, historicalShift.difference ?: 0.0, 0.001)

        // 6. Attempting to close shift again when no active shift exists returns null
        val closeAgain = repository.closeShift(countedCash = 100.0)
        assertNull(closeAgain)
    }
}
