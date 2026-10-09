package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BarcodeScannerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: POSViewModel
    private lateinit var collectJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = POSViewModel(repository)
        collectJob = CoroutineScope(testDispatcher).launch {
            viewModel.uiState.collect {}
        }
    }

    @After
    fun tearDown() {
        collectJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun scanAndAddToCart_validBarcode_addsToCartAndReturnsTrue() = runTest {
        val product = Product(name = "Scanner Product", barcode = "BARCODE123", price = 10.0, stock = 10)
        repository.addProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.scanAndAddToCart("BARCODE123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        assertEquals(1, viewModel.uiState.value.cartItems.size)
        assertEquals("Scanner Product", viewModel.uiState.value.cartItems.first().product.name)
        assertEquals(1, viewModel.uiState.value.cartItems.first().quantity)
    }

    @Test
    fun scanAndAddToCart_validBarcodeWithWhitespace_addsToCartAndReturnsTrue() = runTest {
        val product = Product(name = "Whitespace Barcode", barcode = "  BARCODE456  ", price = 15.0, stock = 10)
        repository.addProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.scanAndAddToCart("  BARCODE456  ")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        assertEquals(1, viewModel.uiState.value.cartItems.size)
    }

    @Test
    fun scanAndAddToCart_invalidBarcode_returnsFalseAndDoesNotAddToCart() = runTest {
        val product = Product(name = "Existing Product", barcode = "VALID123", price = 10.0, stock = 10)
        repository.addProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.scanAndAddToCart("INVALID999")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun scanAndAddToCart_emptyOrBlankBarcode_returnsFalse() = runTest {
        val result1 = viewModel.scanAndAddToCart("")
        val result2 = viewModel.scanAndAddToCart("   ")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(result1)
        assertFalse(result2)
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }
}
