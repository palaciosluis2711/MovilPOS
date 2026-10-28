package com.lopezapp.movilpos.ui.viewmodel

import com.lopezapp.movilpos.data.model.InventoryOutflow
import com.lopezapp.movilpos.data.model.OutflowItem
import com.lopezapp.movilpos.data.model.OutflowReason
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryOutflowViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: InventoryOutflowViewModel
    private lateinit var collectJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = InventoryOutflowViewModel(repository, defaultDispatcher = UnconfinedTestDispatcher(testDispatcher.scheduler))
        collectJob = CoroutineScope(testDispatcher).launch {
            viewModel.outflows.collect {}
        }
    }

    @After
    fun tearDown() {
        collectJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun addDraftItem_addsNewProductToDraft() = runTest {
        val product = Product(id = "p1", name = "Soda 500ml", cost = 0.50, price = 0.75, stock = 10)
        viewModel.addDraftItem(product, 2)
        testDispatcher.scheduler.advanceUntilIdle()

        val draft = viewModel.draftItems.value
        assertEquals(1, draft.size)
        assertEquals("p1", draft.first().productId)
        assertEquals(2, draft.first().quantity)
        assertEquals(1.00, viewModel.totalOutflowCost.value, 0.001)
    }

    @Test
    fun addDraftItem_existingProduct_incrementsQuantity() = runTest {
        val product = Product(id = "p1", name = "Soda 500ml", cost = 0.50, price = 0.75, stock = 10)
        viewModel.addDraftItem(product, 2)
        viewModel.addDraftItem(product, 3)
        testDispatcher.scheduler.advanceUntilIdle()

        val draft = viewModel.draftItems.value
        assertEquals(1, draft.size)
        assertEquals(5, draft.first().quantity)
        assertEquals(2.50, viewModel.totalOutflowCost.value, 0.001)
    }

    @Test
    fun updateDraftItemQuantity_updatesQuantityAndTotalCost() = runTest {
        val product = Product(id = "p1", name = "Pan", cost = 0.20, price = 0.35, stock = 50)
        viewModel.addDraftItem(product, 5)

        viewModel.updateDraftItemQuantity("p1", 10)
        testDispatcher.scheduler.advanceUntilIdle()

        val draft = viewModel.draftItems.value
        assertEquals(1, draft.size)
        assertEquals(10, draft.first().quantity)
        assertEquals(2.00, viewModel.totalOutflowCost.value, 0.001)
    }

    @Test
    fun updateDraftItemQuantity_zeroOrNegative_removesItem() = runTest {
        val product = Product(id = "p1", name = "Pan", cost = 0.20, price = 0.35, stock = 50)
        viewModel.addDraftItem(product, 5)

        viewModel.updateDraftItemQuantity("p1", 0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.draftItems.value.isEmpty())
        assertEquals(0.00, viewModel.totalOutflowCost.value, 0.001)
    }

    @Test
    fun removeDraftItem_removesProductFromDraft() = runTest {
        val p1 = Product(id = "p1", name = "Pan", cost = 0.20, price = 0.35, stock = 50)
        val p2 = Product(id = "p2", name = "Leche", cost = 1.20, price = 1.50, stock = 10)
        viewModel.addDraftItem(p1, 2)
        viewModel.addDraftItem(p2, 1)

        viewModel.removeDraftItem("p1")
        testDispatcher.scheduler.advanceUntilIdle()

        val draft = viewModel.draftItems.value
        assertEquals(1, draft.size)
        assertEquals("p2", draft.first().productId)
        assertEquals(1.20, viewModel.totalOutflowCost.value, 0.001)
    }

    @Test
    fun registerOutflow_invalidState_returnsFalse() = runTest {
        // Empty items and blank notes
        assertFalse(viewModel.registerOutflow())

        // Items present but blank notes
        val product = Product(id = "p1", name = "Pan", cost = 0.20, price = 0.35, stock = 50)
        viewModel.addDraftItem(product, 1)
        viewModel.setDraftNotes("   ")
        assertFalse(viewModel.registerOutflow())
    }

    @Test
    fun registerOutflow_validState_savesOutflowAndDeductsStock() = runTest {
        val product = Product(id = "prod_1", name = "Galletas", cost = 0.75, price = 1.00, stock = 20)
        repository.addProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addDraftItem(product, 4)
        viewModel.setDraftReason(OutflowReason.MERMA_DANADO)
        viewModel.setDraftNotes("Empaque roto durante descarga")

        val result = viewModel.registerOutflow()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        // Check form was reset
        assertTrue(viewModel.draftItems.value.isEmpty())
        assertEquals("", viewModel.draftNotes.value)

        // Check outflow was saved in repository
        val outflows = viewModel.outflows.value
        assertEquals(1, outflows.size)
        val savedOutflow = outflows.first()
        assertEquals(OutflowReason.MERMA_DANADO, savedOutflow.reason)
        assertEquals("Empaque roto durante descarga", savedOutflow.notes)
        assertEquals(3.00, savedOutflow.totalCost, 0.001)

        // Check stock was deducted in repository
        val updatedProduct = repository.products.value.find { it.id == "prod_1" }
        assertNotNull(updatedProduct)
        assertEquals(16, updatedProduct?.stock)
    }

    @Test
    fun searchQueryAndReasonFilter_filtersOutflowsListCorrectly() = runTest {
        val outflow1 = InventoryOutflow(
            id = "out_1",
            reason = OutflowReason.CONSUMO_INTERNO,
            notes = "Uso en cocina",
            items = listOf(OutflowItem("p1", "Aceite", 1, 2.50))
        )
        val outflow2 = InventoryOutflow(
            id = "out_2",
            reason = OutflowReason.VENCIDO,
            notes = "Lote de yogur caducado",
            items = listOf(OutflowItem("p2", "Yogur", 5, 0.80))
        )
        repository.addInventoryOutflow(outflow1)
        repository.addInventoryOutflow(outflow2)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.outflows.value.size)

        // Filter by reason
        viewModel.setReasonFilter(OutflowReason.VENCIDO)
        testDispatcher.scheduler.advanceUntilIdle()
        val filteredReason = viewModel.outflows.value
        assertEquals(1, filteredReason.size)
        assertEquals("out_2", filteredReason.first().id)

        // Filter by search query
        viewModel.setReasonFilter(null)
        viewModel.onSearchQueryChanged("cocina")
        testDispatcher.scheduler.advanceUntilIdle()
        val filteredQuery = viewModel.outflows.value
        assertEquals(1, filteredQuery.size)
        assertEquals("out_1", filteredQuery.first().id)
    }
}
