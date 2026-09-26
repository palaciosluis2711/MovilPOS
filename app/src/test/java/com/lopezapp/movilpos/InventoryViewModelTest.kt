package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = InventoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addProduct_withImageUri_addsProductSuccessfully() = runTest {
        viewModel.addProduct(
            name = "Test Product",
            price = 10.0,
            imageUri = "content://media/external/images/media/123"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val products = repository.products.value
        val addedProduct = products.find { it.name == "Test Product" }

        assertEquals("Test Product", addedProduct?.name)
        assertEquals(10.0, addedProduct?.price ?: 0.0, 0.001)
        assertEquals("content://media/external/images/media/123", addedProduct?.imageUri)
    }

    @Test
    fun updateProduct_withImageUri_updatesProductSuccessfully() = runTest {
        val initialProduct = repository.products.value.first()
        assertNull(initialProduct.imageUri)

        val updatedProduct = initialProduct.copy(imageUri = "content://media/external/images/media/456")
        viewModel.updateProduct(updatedProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val productInRepo = repository.products.value.find { it.id == initialProduct.id }
        assertEquals("content://media/external/images/media/456", productInRepo?.imageUri)
    }

    @Test
    fun removeProduct_removesProductSuccessfully() = runTest {
        val initialProduct = repository.products.value.first()
        viewModel.removeProduct(initialProduct.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val productInRepo = repository.products.value.find { it.id == initialProduct.id }
        assertNull(productInRepo)
    }
}
