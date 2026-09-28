package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.BundleItem
import com.lopezapp.movilpos.data.model.CartItem
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class POSViewModelTest {

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
    fun cartItem_effectiveUnitPriceAndSubtotal_defaultValues() {
        val product = Product(name = "Test Product", price = 10.0, stock = 10)
        val cartItem = CartItem(product = product, quantity = 2)

        assertEquals(10.0, cartItem.effectiveUnitPrice, 0.001)
        assertEquals(20.0, cartItem.subtotal, 0.001)
        assertNull(cartItem.unitPriceOverride)
        assertFalse(cartItem.isBundleDiscounted)
    }

    @Test
    fun cartItem_effectiveUnitPriceAndSubtotal_withOverrideAndDiscounted() {
        val product = Product(name = "Test Product", price = 10.0, stock = 10)
        val cartItem = CartItem(
            product = product,
            quantity = 3,
            unitPriceOverride = 8.0,
            isBundleDiscounted = true
        )

        assertEquals(8.0, cartItem.effectiveUnitPrice, 0.001)
        assertEquals(24.0, cartItem.subtotal, 0.001)
        assertEquals(8.0, cartItem.unitPriceOverride ?: 0.0, 0.001)
        assertTrue(cartItem.isBundleDiscounted)
    }

    @Test
    fun addToCart_regularProduct_addsToCartWithoutDiscount() = runTest {
        val product = repository.products.value.first() // Coffee $2.5
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals(product.id, state.cartItems[0].product.id)
        assertEquals(1, state.cartItems[0].quantity)
        assertNull(state.cartItems[0].unitPriceOverride)
        assertFalse(state.cartItems[0].isBundleDiscounted)
        assertEquals(2.5, state.total, 0.001)
    }

    @Test
    fun addToCart_bundleWithDiscount_addsIndividualProductsProportionallyDiscounted() = runTest {
        val prod1 = Product(name = "Burger", price = 10.0, stock = 20)
        val prod2 = Product(name = "Soda", price = 5.0, stock = 20)
        repository.addProduct(prod1)
        repository.addProduct(prod2)

        // Bundle: 1 Burger ($10) + 1 Soda ($5) = Original total $15
        // User sets bundle price to $12 (which is < $15)
        val bundleProduct = Product(
            name = "Combo Burger",
            price = 12.0,
            stock = 10,
            isBundle = true,
            bundleItems = listOf(
                BundleItem(productId = prod1.id, quantity = 1),
                BundleItem(productId = prod2.id, quantity = 1)
            )
        )
        repository.addProduct(bundleProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(bundleProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.cartItems.size)

        val burgerItem = state.cartItems.find { it.product.id == prod1.id }
        val sodaItem = state.cartItems.find { it.product.id == prod2.id }

        // ratio = 12 / 15 = 0.8
        // Burger effective price = 10 * 0.8 = 8.0
        // Soda effective price = 5 * 0.8 = 4.0
        assertTrue(burgerItem != null)
        assertTrue(sodaItem != null)

        assertTrue(burgerItem!!.isBundleDiscounted)
        assertEquals(8.0, burgerItem.effectiveUnitPrice, 0.001)
        assertEquals(8.0, burgerItem.subtotal, 0.001)

        assertTrue(sodaItem!!.isBundleDiscounted)
        assertEquals(4.0, sodaItem.effectiveUnitPrice, 0.001)
        assertEquals(4.0, sodaItem.subtotal, 0.001)

        assertEquals(12.0, state.total, 0.001)
    }

    @Test
    fun addToCart_bundleWithoutDiscount_addsIndividualProductsWithoutDiscount() = runTest {
        val prod1 = Product(name = "Burger", price = 10.0, stock = 20)
        val prod2 = Product(name = "Soda", price = 5.0, stock = 20)
        repository.addProduct(prod1)
        repository.addProduct(prod2)

        // Bundle: 1 Burger ($10) + 1 Soda ($5) = Original total $15
        // User sets bundle price to $15 (equal to original total)
        val bundleProduct = Product(
            name = "Combo Burger Regular",
            price = 15.0,
            stock = 10,
            isBundle = true,
            bundleItems = listOf(
                BundleItem(productId = prod1.id, quantity = 1),
                BundleItem(productId = prod2.id, quantity = 1)
            )
        )
        repository.addProduct(bundleProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(bundleProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.cartItems.size)

        val burgerItem = state.cartItems.find { it.product.id == prod1.id }
        val sodaItem = state.cartItems.find { it.product.id == prod2.id }

        assertTrue(burgerItem != null)
        assertTrue(sodaItem != null)

        assertFalse(burgerItem!!.isBundleDiscounted)
        assertNull(burgerItem.unitPriceOverride)
        assertEquals(10.0, burgerItem.subtotal, 0.001)

        assertFalse(sodaItem!!.isBundleDiscounted)
        assertNull(sodaItem.unitPriceOverride)
        assertEquals(5.0, sodaItem.subtotal, 0.001)

        assertEquals(15.0, state.total, 0.001)
    }

    @Test
    fun cartItemRow_incrementAndDecrement() = runTest {
        val product = repository.products.value.first()
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val item = viewModel.uiState.value.cartItems.first()
        viewModel.addToCart(item)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.cartItems.first().quantity)

        viewModel.removeFromCart(item)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.cartItems.first().quantity)

        viewModel.removeFromCart(item)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun addToCart_bundleWithFractionalDiscount_roundsProportionalPricesToTwoDecimals() = runTest {
        val prod1 = Product(name = "Item 1", price = 10.0, stock = 20)
        val prod2 = Product(name = "Item 2", price = 10.0, stock = 20)
        val prod3 = Product(name = "Item 3", price = 10.0, stock = 20)
        repository.addProduct(prod1)
        repository.addProduct(prod2)
        repository.addProduct(prod3)

        val bundleProduct = Product(
            name = "Trio Bundle",
            price = 25.0,
            stock = 10,
            isBundle = true,
            bundleItems = listOf(
                BundleItem(productId = prod1.id, quantity = 1),
                BundleItem(productId = prod2.id, quantity = 1),
                BundleItem(productId = prod3.id, quantity = 1)
            )
        )
        repository.addProduct(bundleProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(bundleProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.cartItems.size)
        state.cartItems.forEach { item ->
            assertEquals(8.33, item.effectiveUnitPrice, 0.001)
            assertEquals(8.33, item.subtotal, 0.001)
        }
        assertEquals(24.99, state.total, 0.001)
    }

    @Test
    fun addToCart_serviceWithIncludedProducts_addsProductsWithNormalPriceAndServiceWithRemainderPrice() = runTest {
        val photoPaper = Product(name = "Papel fotográfico", price = 0.50, stock = 100)
        repository.addProduct(photoPaper)

        val printService = Product(
            name = "Impresión en papel de foto",
            price = 1.25,
            stock = 0,
            isService = true,
            bundleItems = listOf(
                BundleItem(productId = photoPaper.id, quantity = 1)
            )
        )
        repository.addProduct(printService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(printService)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.cartItems.size)

        val paperItem = state.cartItems.find { it.product.id == photoPaper.id }
        val serviceItem = state.cartItems.find { it.product.id == printService.id }

        assertTrue(paperItem != null)
        assertTrue(serviceItem != null)

        assertEquals(1, paperItem!!.quantity)
        assertNull(paperItem.unitPriceOverride)
        assertEquals(0.50, paperItem.effectiveUnitPrice, 0.001)
        assertEquals(0.50, paperItem.subtotal, 0.001)

        assertEquals(1, serviceItem!!.quantity)
        assertEquals(0.75, serviceItem.unitPriceOverride ?: 0.0, 0.001)
        assertEquals(0.75, serviceItem.effectiveUnitPrice, 0.001)
        assertEquals(0.75, serviceItem.subtotal, 0.001)

        assertEquals(1.25, state.total, 0.001)
    }

    @Test
    fun addToCart_serviceWithoutIncludedProducts_addsServiceWithNormalPrice() = runTest {
        val serviceOnly = Product(
            name = "Consulta Técnica",
            price = 20.0,
            stock = 0,
            isService = true
        )
        repository.addProduct(serviceOnly)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(serviceOnly)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)

        val serviceItem = state.cartItems.first()
        assertEquals(serviceOnly.id, serviceItem.product.id)
        assertEquals(1, serviceItem.quantity)
        assertNull(serviceItem.unitPriceOverride)
        assertEquals(20.0, serviceItem.effectiveUnitPrice, 0.001)
        assertEquals(20.0, state.total, 0.001)
    }

    @Test
    fun addToCart_serviceWithIncludedProductsExceedingServicePrice_setsServiceRemainderPriceToZero() = runTest {
        val expensiveFrame = Product(name = "Marco de lujo", price = 15.0, stock = 10)
        repository.addProduct(expensiveFrame)

        val cheapService = Product(
            name = "Enmarcado básico",
            price = 10.0,
            stock = 0,
            isService = true,
            bundleItems = listOf(
                BundleItem(productId = expensiveFrame.id, quantity = 1)
            )
        )
        repository.addProduct(cheapService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(cheapService)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.cartItems.size)

        val serviceItem = state.cartItems.find { it.product.id == cheapService.id }
        assertTrue(serviceItem != null)
        assertEquals(0.0, serviceItem!!.effectiveUnitPrice, 0.001)
        assertEquals(15.0, state.total, 0.001)
    }
}

