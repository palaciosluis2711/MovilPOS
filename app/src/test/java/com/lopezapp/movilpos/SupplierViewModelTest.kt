package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.SupplierViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupplierViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: SupplierViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = SupplierViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun suppliers_initialDataIsLoaded() = runTest {
        val initialSuppliers = viewModel.allSuppliers.value
        assertEquals(2, initialSuppliers.size)
        assertEquals("Distribuidora Central S.A.", initialSuppliers[0].name)
    }

    @Test
    fun addSupplier_addsNewSupplierToRepository() = runTest {
        viewModel.addSupplier(
            name = "Nuevo Proveedor S.A.",
            address = "Calle 10",
            email = "info@nuevo.com",
            phone = "12345678",
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val suppliers = viewModel.allSuppliers.value
        assertEquals(3, suppliers.size)
        assertNotNull(suppliers.find { it.name == "Nuevo Proveedor S.A." })
    }

    @Test
    fun addSupplierObject_addsSupplierToRepository() = runTest {
        val newSupplier = Supplier(
            name = "Proveedor Objeto S.A.",
            address = "Av. Sol 50",
            email = "sol@objeto.com",
            phone = "987654321",
        )
        viewModel.addSupplier(newSupplier)
        testDispatcher.scheduler.advanceUntilIdle()

        val suppliers = viewModel.allSuppliers.value
        assertEquals(3, suppliers.size)
        assertNotNull(suppliers.find { it.name == "Proveedor Objeto S.A." })
    }

    @Test
    fun updateSupplier_updatesExistingSupplier() = runTest {
        val existingSupplier = viewModel.allSuppliers.value.first()
        val updatedSupplier = existingSupplier.copy(name = "Distribuidora Central Editada")

        viewModel.updateSupplier(updatedSupplier)
        testDispatcher.scheduler.advanceUntilIdle()

        val supplier = viewModel.getSupplierById(existingSupplier.id)
        assertNotNull(supplier)
        assertEquals("Distribuidora Central Editada", supplier?.name)
    }

    @Test
    fun deleteSupplier_removesSupplierFromRepository() = runTest {
        val existingSupplier = viewModel.allSuppliers.value.first()

        viewModel.deleteSupplier(existingSupplier.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val suppliers = viewModel.allSuppliers.value
        assertEquals(1, suppliers.size)
        assertNull(viewModel.getSupplierById(existingSupplier.id))
    }

    @Test
    fun searchQuery_filtersSuppliersByName() = runTest {
        viewModel.onSearchQueryChanged("del Norte")
        testDispatcher.scheduler.advanceUntilIdle()

        // Test search query state
        assertEquals("del Norte", viewModel.searchQuery.value)
    }
}
