package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.ShiftStatus
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppRepositoryTest {

    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        repository = AppRepository()
    }

    @Test
    fun defaultUsers_containsAdmin() {
        val users = repository.users.value
        assertEquals(1, users.size)
        val admin = users.first()
        assertEquals("admin_1", admin.id)
        assertEquals("Administrador", admin.name)
        assertEquals("1234", admin.pin)
        assertEquals(Role.ADMINISTRATOR, admin.role)
        assertTrue(admin.isActive)
    }

    @Test
    fun userCrud_addsUpdatesAndDeletesUser() {
        val cashier = User(id = "u2", name = "Juan Pérez", pin = "5678", role = Role.CASHIER)
        repository.addUser(cashier)
        assertEquals(2, repository.users.value.size)
        assertTrue(repository.users.value.any { it.id == "u2" })

        val updatedCashier = cashier.copy(name = "Juan Carlos Pérez")
        repository.updateUser(updatedCashier)
        val fetched = repository.users.value.find { it.id == "u2" }
        assertNotNull(fetched)
        assertEquals("Juan Carlos Pérez", fetched?.name)

        repository.deleteUser("u2")
        assertEquals(1, repository.users.value.size)
        assertNull(repository.users.value.find { it.id == "u2" })
    }

    @Test
    fun updateUser_updatesActiveShiftCashierNameWhenActiveShiftMatchesUser() {
        val cashier = User(id = "c1", name = "Cajero Inicial", pin = "1111", role = Role.CASHIER)
        repository.addUser(cashier)
        repository.openShift(cashier, initialFloat = 50.0)

        assertEquals("Cajero Inicial", repository.activeShift.value?.cashierName)

        val updatedCashier = cashier.copy(name = "Cajero Actualizado")
        repository.updateUser(updatedCashier)

        assertEquals("Cajero Actualizado", repository.activeShift.value?.cashierName)
    }

    @Test
    fun validateAdminPin_validatesCorrectRolesAndActiveStatus() {
        // Admin default PIN "1234"
        assertTrue(repository.validateAdminPin("1234"))
        assertFalse(repository.validateAdminPin("9999"))

        // Add Supervisor
        val supervisor = User(id = "sup_1", name = "Supervisor", pin = "4321", role = Role.SUPERVISOR)
        repository.addUser(supervisor)
        assertTrue(repository.validateAdminPin("4321"))

        // Add Cashier
        val cashier = User(id = "cash_1", name = "Cajero", pin = "1111", role = Role.CASHIER)
        repository.addUser(cashier)
        assertFalse(repository.validateAdminPin("1111"))

        // Deactivate Supervisor
        repository.updateUser(supervisor.copy(isActive = false))
        assertFalse(repository.validateAdminPin("4321"))
    }

    @Test
    fun shiftLifecycle_opensAndClosesShift() {
        assertNull(repository.activeShift.value)
        assertTrue(repository.shiftHistory.value.isEmpty())

        val cashier = repository.users.value.first()
        val shift = repository.openShift(cashier, initialFloat = 100.0)

        assertNotNull(repository.activeShift.value)
        assertEquals(shift.id, repository.activeShift.value?.id)
        assertEquals("admin_1", repository.activeShift.value?.cashierId)
        assertEquals(100.0, repository.activeShift.value?.initialFloat ?: 0.0, 0.001)
        assertEquals(ShiftStatus.OPEN, repository.activeShift.value?.status)
        assertEquals(1, repository.shiftHistory.value.size)

        val closed = repository.closeShift(countedCash = 105.0)
        assertNotNull(closed)
        assertEquals(ShiftStatus.CLOSED, closed?.status)
        assertNotNull(closed?.closedAtMillis)
        assertEquals(105.0, closed?.actualCashCounted ?: 0.0, 0.001)
        assertEquals(5.0, closed?.difference ?: 0.0, 0.001)

        assertNull(repository.activeShift.value)
        assertEquals(1, repository.shiftHistory.value.size)
        assertEquals(ShiftStatus.CLOSED, repository.shiftHistory.value.first().status)
    }

    @Test
    fun updateActiveShiftSales_updatesTotalsAndHistory() {
        val cashier = repository.users.value.first()
        repository.openShift(cashier, initialFloat = 50.0)

        repository.updateActiveShiftSales(cashAmount = 20.0, cardAmount = 15.0, otherAmount = 10.0)

        val active = repository.activeShift.value
        assertNotNull(active)
        assertEquals(20.0, active?.totalCashSales ?: 0.0, 0.001)
        assertEquals(15.0, active?.totalCardSales ?: 0.0, 0.001)
        assertEquals(10.0, active?.totalOtherSales ?: 0.0, 0.001)
        assertEquals(70.0, active?.expectedCash ?: 0.0, 0.001)

        val inHistory = repository.shiftHistory.value.first()
        assertEquals(20.0, inHistory.totalCashSales, 0.001)
    }

    @Test
    fun addSale_withActiveShift_assignsShiftAndUpdatesSalesTotals() {
        val cashier = repository.users.value.first()
        val shift = repository.openShift(cashier, initialFloat = 50.0)

        val cashSale = Sale(
            customerId = "c1",
            customerName = "Cliente General",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(SaleItem("p1", "Product 1", 1, 10.0, 10.0)),
            totalAmount = 10.0
        )
        repository.addSale(cashSale)

        val recordedSale = repository.sales.value.last()
        assertEquals(shift.id, recordedSale.shiftId)
        assertEquals(cashier.id, recordedSale.cashierId)
        assertEquals(cashier.name, recordedSale.cashierName)

        val updatedShift = repository.activeShift.value
        assertEquals(10.0, updatedShift?.totalCashSales ?: 0.0, 0.001)
        assertEquals(60.0, updatedShift?.expectedCash ?: 0.0, 0.001)

        val cardSale = Sale(
            customerId = "c1",
            customerName = "Cliente General",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm2",
            paymentMethodName = "Tarjeta de Crédito",
            items = listOf(SaleItem("p2", "Product 2", 1, 25.0, 25.0)),
            totalAmount = 25.0
        )
        repository.addSale(cardSale)

        val finalShift = repository.activeShift.value
        assertEquals(10.0, finalShift?.totalCashSales ?: 0.0, 0.001)
        assertEquals(25.0, finalShift?.totalCardSales ?: 0.0, 0.001)
        assertEquals(60.0, finalShift?.expectedCash ?: 0.0, 0.001)
    }

    @Test
    fun voidSaleDte_voidsSaleCorrectly() {
        val sale = Sale(
            id = "sale_123",
            customerId = "c1",
            customerName = "Cliente",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 50.0,
            isDteIssued = true,
            dteGenerationCode = "GEN-123"
        )
        repository.addSale(sale)

        val result = repository.voidSaleDte("sale_123", "Error en cobro")
        assertTrue(result.isSuccess)

        val voidedSale = repository.sales.value.find { it.id == "sale_123" }
        assertNotNull(voidedSale)
        assertTrue(voidedSale!!.isVoided)
        assertEquals("Error en cobro", voidedSale.voidReason)
        assertNotNull(voidedSale.voidedAtMillis)
    }

    @Test
    fun addToContingencyQueue_setsContingencyModeTrueAndAddsToQueue() {
        val sale = Sale(
            id = "sale_contingency_1",
            customerId = "c1",
            customerName = "Cliente Contingencia",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 30.0,
            isDteIssued = false
        )
        repository.addToContingencyQueue(sale)

        val contingencyList = repository.contingencyDtes.value
        assertEquals(1, contingencyList.size)
        assertTrue(contingencyList.first().contingencyMode)

        val savedSale = repository.sales.value.find { it.id == "sale_contingency_1" }
        assertNotNull(savedSale)
        assertTrue(savedSale!!.contingencyMode)
    }

    @Test
    fun retryContingencyTransmissions_retransmitsAndClearsQueue() {
        val sale = Sale(
            id = "sale_contingency_2",
            customerId = "c1",
            customerName = "Cliente Retry",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 45.0,
            isDteIssued = false
        )
        repository.addToContingencyQueue(sale)
        assertEquals(1, repository.contingencyDtes.value.size)

        val result = repository.retryContingencyTransmissions()
        assertTrue(result.isSuccess)
        val count = result.getOrNull()
        assertEquals(1, count)

        assertTrue(repository.contingencyDtes.value.isEmpty())
        val updatedSale = repository.sales.value.find { it.id == "sale_contingency_2" }
        assertNotNull(updatedSale)
        assertTrue(updatedSale!!.isDteIssued)
        assertFalse(updatedSale.contingencyMode)
        assertNotNull(updatedSale.dteReceptionSeal)
    }
}
