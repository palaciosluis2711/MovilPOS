package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.PurchaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: PurchaseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = PurchaseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun purchases_initialDataIsLoaded() = runTest {
        val purchases = viewModel.allPurchases.value
        assertEquals(1, purchases.size)
        assertEquals("Distribuidora Central S.A.", purchases[0].supplierName)
    }

    @Test
    fun addProductToDraft_updatesDraftItemsAndTotalCost() = runTest {
        val product = repository.products.value.first()
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val draftItems = viewModel.draftItems.value
        assertEquals(1, draftItems.size)
        assertEquals(product.id, draftItems[0].productId)
        assertEquals(1, draftItems[0].quantity)

        // Add same product again -> quantity should increment to 2
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedDraftItems = viewModel.draftItems.value
        assertEquals(1, updatedDraftItems.size)
        assertEquals(2, updatedDraftItems[0].quantity)
    }

    @Test
    fun updateDraftItemQuantityAndUnitCost_calculatesTotalCostCorrectly() = runTest {
        val product = repository.products.value.first()
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDraftItemQuantity(product.id, 5)
        viewModel.updateDraftItemUnitCost(product.id, 12.50)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(62.50, viewModel.totalCost.value, 0.001)
    }

    @Test
    fun savePurchase_savesRecordUpdatesInventoryAndResetsForm() = runTest {
        val initialProduct = repository.products.value.first()
        val initialStock = initialProduct.stock
        val supplier = repository.suppliers.value.first()

        viewModel.setSupplier(supplier)
        viewModel.addProductToDraft(initialProduct)
        viewModel.updateDraftItemQuantity(initialProduct.id, 10)
        viewModel.updateDraftItemUnitCost(initialProduct.id, 1.80)
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = viewModel.savePurchase()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        // Form should be reset
        assertNull(viewModel.selectedSupplier.value)
        assertTrue(viewModel.draftItems.value.isEmpty())

        // Repository should have new purchase
        val allPurchases = viewModel.allPurchases.value
        assertEquals(2, allPurchases.size)

        // Inventory should be updated: stock increased by 10 and cost set to 1.80
        val updatedProduct = repository.products.value.find { it.id == initialProduct.id }
        assertNotNull(updatedProduct)
        assertEquals(initialStock + 10, updatedProduct?.stock)
        assertEquals(1.80, updatedProduct?.cost ?: 0.0, 0.001)
    }

    @Test
    fun savePurchase_failsIfNoSupplierOrItems() = runTest {
        assertFalse(viewModel.savePurchase())

        val supplier = repository.suppliers.value.first()
        viewModel.setSupplier(supplier)
        assertFalse(viewModel.savePurchase())
    }

    @Test
    fun searchQuery_filtersPurchasesBySupplierOrProduct() = runTest {
        viewModel.onSearchQueryChanged("Central")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Central", viewModel.searchQuery.value)
    }

    @Test
    fun filteredPurchases_filtersBySearchQuerySupplierIdAndDate() = runTest {
        val supplier = repository.suppliers.value.first()
        val initialPurchase = viewModel.allPurchases.value.first()

        // 1. Text Search Filter
        viewModel.onSearchQueryChanged("Central")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredPurchases.value.size)

        viewModel.onSearchQueryChanged("Inexistente")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.filteredPurchases.value.size)

        viewModel.onSearchQueryChanged("")
        testDispatcher.scheduler.advanceUntilIdle()

        // 2. Supplier Filter
        viewModel.setSelectedSupplierIdFilter(supplier.id)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredPurchases.value.size)

        viewModel.setSelectedSupplierIdFilter("non_existent_supplier_id")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.filteredPurchases.value.size)

        viewModel.setSelectedSupplierIdFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Date Filter
        viewModel.setSelectedDateFilterMillis(initialPurchase.dateMillis)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredPurchases.value.size)

        // 4. Reset Filters
        viewModel.onSearchQueryChanged("Central")
        viewModel.setSelectedSupplierIdFilter(supplier.id)
        viewModel.resetFilters()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedSupplierIdFilter.value)
        assertNull(viewModel.selectedDateFilterMillis.value)
        assertEquals(1, viewModel.filteredPurchases.value.size)
    }

    @Test
    fun deletePurchase_removesPurchaseFromRepository() = runTest {
        val existingPurchase = viewModel.allPurchases.value.first()
        viewModel.deletePurchase(existingPurchase.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val purchases = viewModel.allPurchases.value
        assertEquals(0, purchases.size)
        assertNull(viewModel.getPurchaseById(existingPurchase.id))
    }

    @Test
    fun getSupplierById_returnsCorrectSupplier() = runTest {
        val supplier = repository.suppliers.value.first()
        val foundSupplier = viewModel.getSupplierById(supplier.id)
        assertNotNull(foundSupplier)
        assertEquals(supplier.name, foundSupplier?.name)
        assertNull(viewModel.getSupplierById(null))
        assertNull(viewModel.getSupplierById("non_existent_id"))
    }

    @Test
    fun filteredPurchases_isSortedByDateDescending() = runTest {
        val supplier = repository.suppliers.value.first()
        val now = System.currentTimeMillis()
        val purchaseOld = Purchase(
            supplierId = supplier.id,
            supplierName = supplier.name,
            dateMillis = now - 100000,
            items = emptyList(),
            totalCost = 10.0
        )
        val purchaseNew = Purchase(
            supplierId = supplier.id,
            supplierName = supplier.name,
            dateMillis = now,
            items = emptyList(),
            totalCost = 20.0
        )
        repository.addPurchase(purchaseOld)
        repository.addPurchase(purchaseNew)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = viewModel.filteredPurchases.value
        val newIndex = list.indexOfFirst { it.dateMillis == now }
        val oldIndex = list.indexOfFirst { it.dateMillis == now - 100000 }
        assertTrue(newIndex != -1 && oldIndex != -1)
        assertTrue(newIndex < oldIndex)
    }
}
