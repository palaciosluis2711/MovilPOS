package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.ExpensesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: ExpensesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = ExpensesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun expenses_initialListContainsSampleExpense() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val initialList = viewModel.allExpenses.value
        assertEquals(1, initialList.size)
        val initialExpense = initialList.first()
        assertEquals("Servicios", initialExpense.category)
        assertEquals("Pago de insumos de oficina", initialExpense.description)
        assertEquals(12.50, initialExpense.amount, 0.001)
    }

    @Test
    fun addExpense_addsExpenseToRepositoryAndUpdatesTotals() = runTest {
        viewModel.addExpense(
            category = "Insumos de Tienda",
            description = "Compra de bolsas plásticas y cinta",
            amount = 15.00
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedList = viewModel.allExpenses.value
        assertEquals(2, updatedList.size)

        val newExpense = updatedList.find { it.description == "Compra de bolsas plásticas y cinta" }
        assertEquals("Insumos de Tienda", newExpense?.category)
        assertEquals(15.00, newExpense?.amount ?: 0.0, 0.001)
    }

    @Test
    fun searchExpenses_filtersByDescriptionCategoryOrCashier() = runTest {
        viewModel.addExpense(
            category = "Flete / Transporte",
            description = "Flete por mercadería enviada",
            amount = 25.00
        )
        viewModel.addExpense(
            category = "Otros",
            description = "Mantenimiento de aire acondicionado",
            amount = 45.00
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter by description
        viewModel.onSearchQueryChanged("Flete")
        testDispatcher.scheduler.advanceUntilIdle()
        val filteredByDesc = viewModel.expenses.value
        assertEquals(1, filteredByDesc.size)
        assertEquals("Flete por mercadería enviada", filteredByDesc.first().description)

        // Filter by category
        viewModel.onSearchQueryChanged("Otros")
        testDispatcher.scheduler.advanceUntilIdle()
        val filteredByCat = viewModel.expenses.value
        assertEquals(1, filteredByCat.size)
        assertEquals("Otros", filteredByCat.first().category)

        // Clear filter
        viewModel.onSearchQueryChanged("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.expenses.value.size)
    }

    @Test
    fun totalExpensesAmount_calculatesSumOfExpenses() = runTest {
        viewModel.addExpense(
            category = "Pago a Proveedor",
            description = "Abono proveedor Panaderia",
            amount = 50.00
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val total = viewModel.totalExpensesAmount.value
        // Initial 12.50 + 50.00 = 62.50
        assertEquals(62.50, total, 0.001)
    }

    @Test
    fun deleteExpense_removesExpenseFromList() = runTest {
        val initialExpenseId = viewModel.allExpenses.value.first().id

        viewModel.deleteExpense(initialExpenseId)
        testDispatcher.scheduler.advanceUntilIdle()

        val currentExpenses = viewModel.allExpenses.value
        assertTrue(currentExpenses.none { it.id == initialExpenseId })
    }
}
