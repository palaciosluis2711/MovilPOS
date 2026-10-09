package com.lopezapp.movilpos.data.model

import com.lopezapp.movilpos.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExpenseTest {

    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        repository = AppRepository()
    }

    // ------------------------------------------------------------------
    // 1. Expense Model Creation, Default Values, and Properties
    // ------------------------------------------------------------------

    @Test
    fun expense_creationWithCustomValues_setsAllPropertiesCorrectly() {
        val customDate = 1700000000000L
        val expense = Expense(
            id = "exp_custom_1",
            shiftId = "shift_100",
            cashierId = "cashier_1",
            cashierName = "Carlos Gomez",
            category = "Servicios",
            description = "Pago de luz mensual",
            amount = 45.50,
            dateMillis = customDate
        )

        assertEquals("exp_custom_1", expense.id)
        assertEquals("shift_100", expense.shiftId)
        assertEquals("cashier_1", expense.cashierId)
        assertEquals("Carlos Gomez", expense.cashierName)
        assertEquals("Servicios", expense.category)
        assertEquals("Pago de luz mensual", expense.description)
        assertEquals(45.50, expense.amount, 0.001)
        assertEquals(customDate, expense.dateMillis)
    }

    @Test
    fun expense_defaultValues_areSetCorrectly() {
        val beforeCreation = System.currentTimeMillis()
        val expense = Expense(
            category = "Insumos",
            description = "Papel para impresora de recibos",
            amount = 12.00
        )
        val afterCreation = System.currentTimeMillis()

        assertNotNull(expense.id)
        assertTrue(expense.id.isNotBlank())
        assertNull(expense.shiftId)
        assertEquals("", expense.cashierId)
        assertEquals("", expense.cashierName)
        assertEquals("Insumos", expense.category)
        assertEquals("Papel para impresora de recibos", expense.description)
        assertEquals(12.00, expense.amount, 0.001)
        assertTrue(expense.dateMillis in beforeCreation..afterCreation)
    }

    @Test
    fun expense_copyAndEquality() {
        val original = Expense(
            id = "exp_1",
            category = "Mantenimiento",
            description = "Reparación de puerta",
            amount = 25.00
        )

        val updatedAmount = original.copy(amount = 35.00)
        assertEquals("exp_1", updatedAmount.id)
        assertEquals("Mantenimiento", updatedAmount.category)
        assertEquals("Reparación de puerta", updatedAmount.description)
        assertEquals(35.00, updatedAmount.amount, 0.001)

        val duplicate = Expense(
            id = "exp_1",
            shiftId = original.shiftId,
            cashierId = original.cashierId,
            cashierName = original.cashierName,
            category = "Mantenimiento",
            description = "Reparación de puerta",
            amount = 25.00,
            dateMillis = original.dateMillis
        )
        assertEquals(original, duplicate)
        assertEquals(original.hashCode(), duplicate.hashCode())
    }

    // ------------------------------------------------------------------
    // 2. CashShift Calculation with totalExpenses Deduction
    // ------------------------------------------------------------------

    @Test
    fun cashShift_defaultTotalExpenses_isZero() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Ana Martinez",
            initialFloat = 100.0
        )

        assertEquals(0.0, shift.totalExpenses, 0.001)
        assertEquals(100.0, shift.expectedCash, 0.001)
    }

    @Test
    fun cashShift_expectedCash_deductsTotalExpensesCorrectly() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Ana Martinez",
            initialFloat = 100.0,
            totalCashSales = 150.0,
            totalExpenses = 40.0
        )

        // expectedCash = maxOf(0.0, (100.0 + 150.0) - 40.0) = 210.0
        assertEquals(210.0, shift.expectedCash, 0.001)
    }

    @Test
    fun cashShift_expectedCash_ignoresNonCashSalesForCashDeduction() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Ana Martinez",
            initialFloat = 100.0,
            totalCashSales = 50.0,
            totalCardSales = 200.0,
            totalOtherSales = 75.0,
            totalExpenses = 30.0
        )

        // expectedCash = maxOf(0.0, (100.0 + 50.0) - 30.0) = 120.0
        assertEquals(120.0, shift.expectedCash, 0.001)
    }

    @Test
    fun cashShift_expectedCash_cappedAtZeroWhenExpensesExceedCash() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Ana Martinez",
            initialFloat = 50.0,
            totalCashSales = 20.0,
            totalExpenses = 100.0
        )

        // 70.0 - 100.0 = -30.0 -> maxOf(0.0, -30.0) = 0.0
        assertEquals(0.0, shift.expectedCash, 0.001)
    }

    @Test
    fun cashShift_difference_calculatesCorrectlyWithTotalExpenses() {
        val shift = CashShift(
            cashierId = "cashier_1",
            cashierName = "Ana Martinez",
            initialFloat = 100.0,
            totalCashSales = 100.0,
            totalExpenses = 50.0
        )
        // expectedCash = 150.0

        val shiftCountedShort = shift.copy(actualCashCounted = 140.0)
        assertEquals(-10.0, shiftCountedShort.difference ?: 0.0, 0.001)

        val shiftCountedOver = shift.copy(actualCashCounted = 165.0)
        assertEquals(15.0, shiftCountedOver.difference ?: 0.0, 0.001)

        assertNull(shift.difference)
    }

    // ------------------------------------------------------------------
    // 3. AppRepository addExpense Balance Deduction on Active Shift
    // ------------------------------------------------------------------

    @Test
    fun addExpense_withActiveShift_deductsBalanceAndUpdatesTotalExpenses() {
        val cashier = repository.users.value.first()
        val activeShift = repository.openShift(cashier, initialFloat = 100.0)

        // Add a cash sale to the active shift
        repository.updateActiveShiftSales(cashAmount = 50.0, cardAmount = 0.0, otherAmount = 0.0)

        val shiftBeforeExpense = repository.activeShift.value
        assertNotNull(shiftBeforeExpense)
        assertEquals(0.0, shiftBeforeExpense?.totalExpenses ?: 0.0, 0.001)
        assertEquals(150.0, shiftBeforeExpense?.expectedCash ?: 0.0, 0.001)

        // Add expense 1
        val expense1 = Expense(
            category = "Servicios",
            description = "Pago de agua potable",
            amount = 35.0
        )
        repository.addExpense(expense1)

        val shiftAfterExpense1 = repository.activeShift.value
        assertNotNull(shiftAfterExpense1)
        assertEquals(35.0, shiftAfterExpense1?.totalExpenses ?: 0.0, 0.001)
        assertEquals(115.0, shiftAfterExpense1?.expectedCash ?: 0.0, 0.001)

        // Verify expense details saved in repository
        val savedExpense1 = repository.expenses.value.find { it.description == "Pago de agua potable" }
        assertNotNull(savedExpense1)
        assertEquals(activeShift.id, savedExpense1?.shiftId)
        assertEquals(cashier.id, savedExpense1?.cashierId)
        assertEquals(cashier.name, savedExpense1?.cashierName)

        // Add expense 2
        val expense2 = Expense(
            category = "Limpieza",
            description = "Compra de desinfectante",
            amount = 15.0
        )
        repository.addExpense(expense2)

        val shiftAfterExpense2 = repository.activeShift.value
        assertNotNull(shiftAfterExpense2)
        assertEquals(50.0, shiftAfterExpense2?.totalExpenses ?: 0.0, 0.001)
        assertEquals(100.0, shiftAfterExpense2?.expectedCash ?: 0.0, 0.001)

        // Verify shift history updated
        val shiftInHistory = repository.shiftHistory.value.find { it.id == activeShift.id }
        assertNotNull(shiftInHistory)
        assertEquals(50.0, shiftInHistory?.totalExpenses ?: 0.0, 0.001)
        assertEquals(100.0, shiftInHistory?.expectedCash ?: 0.0, 0.001)
    }

    @Test
    fun addExpense_withActiveShift_preservesCustomCashierInfoIfProvided() {
        val cashier = repository.users.value.first()
        repository.openShift(cashier, initialFloat = 100.0)

        val customExpense = Expense(
            shiftId = "custom_shift_id",
            cashierId = "custom_cashier_id",
            cashierName = "Custom Cashier Name",
            category = "Flete",
            description = "Transporte de carga",
            amount = 20.0
        )

        repository.addExpense(customExpense)

        val savedExpense = repository.expenses.value.find { it.description == "Transporte de carga" }
        assertNotNull(savedExpense)
        assertEquals("custom_shift_id", savedExpense?.shiftId)
        assertEquals("custom_cashier_id", savedExpense?.cashierId)
        assertEquals("Custom Cashier Name", savedExpense?.cashierName)

        // Shift totalExpenses is still updated
        assertEquals(20.0, repository.activeShift.value?.totalExpenses ?: 0.0, 0.001)
        assertEquals(80.0, repository.activeShift.value?.expectedCash ?: 0.0, 0.001)
    }

    @Test
    fun addExpense_withoutActiveShift_addsExpenseWithoutUpdatingShift() {
        assertNull(repository.activeShift.value)

        val expense = Expense(
            category = "Otros",
            description = "Gasto administrativo general",
            amount = 25.0
        )

        repository.addExpense(expense)

        val savedExpense = repository.expenses.value.find { it.description == "Gasto administrativo general" }
        assertNotNull(savedExpense)
        assertEquals("Otros", savedExpense?.category)
        assertEquals(25.0, savedExpense?.amount ?: 0.0, 0.001)
        assertNull(repository.activeShift.value)
    }
}
