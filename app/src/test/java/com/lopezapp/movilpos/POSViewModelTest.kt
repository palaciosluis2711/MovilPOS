package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.ArithmeticOperator
import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.BundleItem
import com.lopezapp.movilpos.data.model.CartItem
import com.lopezapp.movilpos.data.model.CreditStatus
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DteEnvironment
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.ShiftStatus
import com.lopezapp.movilpos.data.model.User
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
import java.util.Locale

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
        assertFalse(cartItem.isRuleDiscounted)
    }

    @Test
    fun cartItem_effectiveUnitPriceAndSubtotal_withOverrideAndDiscounted() {
        val product = Product(name = "Test Product", price = 10.0, stock = 10)
        val cartItem = CartItem(
            product = product,
            quantity = 3,
            unitPriceOverride = 8.0,
            isBundleDiscounted = true,
            isRuleDiscounted = false
        )

        assertEquals(8.0, cartItem.effectiveUnitPrice, 0.001)
        assertEquals(24.0, cartItem.subtotal, 0.001)
        assertEquals(8.0, cartItem.unitPriceOverride ?: 0.0, 0.001)
        assertTrue(cartItem.isBundleDiscounted)
        assertFalse(cartItem.isRuleDiscounted)
    }

    @Test
    fun setSearchQuery_filtersProductsByNameAndBarcode() = runTest {
        val p1 = Product(name = "Espresso Coffee", barcode = "123456", price = 3.0, stock = 10)
        val p2 = Product(name = "Green Tea", barcode = "789012", price = 2.0, stock = 10)
        repository.addProduct(p1)
        repository.addProduct(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Search by name
        viewModel.setSearchQuery("espresso")
        testDispatcher.scheduler.advanceUntilIdle()
        val nameFiltered = viewModel.uiState.value.products
        assertEquals(1, nameFiltered.size)
        assertEquals("Espresso Coffee", nameFiltered.first().name)

        // Search by barcode
        viewModel.setSearchQuery("789012")
        testDispatcher.scheduler.advanceUntilIdle()
        val barcodeFiltered = viewModel.uiState.value.products
        assertEquals(1, barcodeFiltered.size)
        assertEquals("Green Tea", barcodeFiltered.first().name)

        // Clear search
        viewModel.setSearchQuery("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.products.size >= 5)
    }

    @Test
    fun onSearchQueryChanged_updatesSearchQuery() = runTest {
        viewModel.onSearchQueryChanged("Latte")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Latte", viewModel.searchQuery.value)
    }

    @Test
    fun allProducts_exposesAllRepositoryProducts() = runTest {
        val p1 = Product(name = "AutoAdd Product", barcode = "11223344", price = 5.0, stock = 10)
        repository.addProduct(p1)
        testDispatcher.scheduler.advanceUntilIdle()

        val products = viewModel.allProducts.value
        assertTrue(products.any { it.barcode == "11223344" })
    }

    @Test
    fun selectPriceRule_appliesDiscountToEligibleCartItems() = runTest {
        val p1 = Product(name = "Pastel de Chocolate", price = 10.0, stock = 10, category = "Postres")
        repository.addProduct(p1)

        val priceRule = PriceRule(
            name = "Descuento 20%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.80,
            isActive = true
        )
        repository.addPriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(10.0, viewModel.uiState.value.total, 0.001)
        assertFalse(viewModel.uiState.value.cartItems.first().isRuleDiscounted)

        // Apply price rule
        viewModel.selectPriceRule(priceRule.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(priceRule.id, state.selectedPriceRuleId)
        val cartItem = state.cartItems.first()
        assertTrue(cartItem.isRuleDiscounted)
        assertEquals(8.0, cartItem.effectiveUnitPrice, 0.001)
        assertEquals(8.0, state.total, 0.001)

        // Deselect price rule
        viewModel.selectPriceRule(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateNoRule = viewModel.uiState.value
        assertNull(stateNoRule.selectedPriceRuleId)
        val itemNoRule = stateNoRule.cartItems.first()
        assertFalse(itemNoRule.isRuleDiscounted)
        assertEquals(10.0, itemNoRule.effectiveUnitPrice, 0.001)
        assertEquals(10.0, stateNoRule.total, 0.001)
    }

    @Test
    fun deleteFromCart_removesItemCompletely() = runTest {
        val product = repository.products.value.first()
        viewModel.addToCart(product)
        viewModel.addToCart(product)
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val item = viewModel.uiState.value.cartItems.first()
        assertEquals(3, item.quantity)

        viewModel.deleteFromCart(item)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
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

    @Test
    fun toggleItemSelection_addsAndRemovesItemIdFromSelectedSet() = runTest {
        val product = repository.products.value.first()
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val item = viewModel.uiState.value.cartItems.first()
        assertTrue(viewModel.selectedCartItemIds.value.isEmpty())

        viewModel.toggleItemSelection(item.id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.selectedCartItemIds.value.contains(item.id))
        assertEquals(1, viewModel.selectedCartItemIds.value.size)

        viewModel.toggleItemSelection(item.id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.selectedCartItemIds.value.contains(item.id))
        assertTrue(viewModel.selectedCartItemIds.value.isEmpty())
    }

    @Test
    fun selectAll_selectsAllCartItemIds() = runTest {
        val products = repository.products.value.take(3)
        products.forEach { viewModel.addToCart(it) }
        testDispatcher.scheduler.advanceUntilIdle()

        val cartItems = viewModel.uiState.value.cartItems
        assertEquals(3, cartItems.size)

        viewModel.selectAll()
        testDispatcher.scheduler.advanceUntilIdle()

        val selected = viewModel.selectedCartItemIds.value
        assertEquals(3, selected.size)
        cartItems.forEach { assertTrue(selected.contains(it.id)) }
    }

    @Test
    fun clearSelection_clearsAllSelectedCartItemIds() = runTest {
        val products = repository.products.value.take(2)
        products.forEach { viewModel.addToCart(it) }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectAll()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.selectedCartItemIds.value.size)

        viewModel.clearSelection()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.selectedCartItemIds.value.isEmpty())
    }

    @Test
    fun togglePriceRule_noItemsSelected_targetsAllCartItems_appliesRule() = runTest {
        val p1 = Product(name = "Producto 1", price = 10.0, stock = 10)
        val p2 = Product(name = "Producto 2", price = 20.0, stock = 10)
        repository.addProduct(p1)
        repository.addProduct(p2)

        val priceRule = PriceRule(
            id = "rule1",
            name = "Descuento 50%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.50,
            isActive = true
        )
        repository.addPriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        viewModel.addToCart(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearSelection()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("rule1", state.selectedPriceRuleId)
        assertEquals(2, state.cartItems.size)
        state.cartItems.forEach { item ->
            assertTrue(item.isRuleDiscounted)
            assertEquals("rule1", item.appliedRuleId)
        }
        val item1 = state.cartItems.find { it.product.id == p1.id }!!
        val item2 = state.cartItems.find { it.product.id == p2.id }!!
        assertEquals(5.0, item1.effectiveUnitPrice, 0.001)
        assertEquals(10.0, item2.effectiveUnitPrice, 0.001)
        assertEquals(15.0, state.total, 0.001)
    }

    @Test
    fun togglePriceRule_itemsSelected_targetsOnlySelectedItems() = runTest {
        val p1 = Product(name = "Producto 1", price = 10.0, stock = 10)
        val p2 = Product(name = "Producto 2", price = 20.0, stock = 10)
        repository.addProduct(p1)
        repository.addProduct(p2)

        val priceRule = PriceRule(
            id = "rule1",
            name = "Descuento 50%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.50,
            isActive = true
        )
        repository.addPriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        viewModel.addToCart(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        val item1Before = viewModel.uiState.value.cartItems.find { it.product.id == p1.id }!!
        viewModel.toggleItemSelection(item1Before.id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val item1 = state.cartItems.find { it.product.id == p1.id }!!
        val item2 = state.cartItems.find { it.product.id == p2.id }!!

        assertTrue(item1.isRuleDiscounted)
        assertEquals("rule1", item1.appliedRuleId)
        assertEquals(5.0, item1.effectiveUnitPrice, 0.001)

        assertFalse(item2.isRuleDiscounted)
        assertNull(item2.appliedRuleId)
        assertEquals(20.0, item2.effectiveUnitPrice, 0.001)
    }

    @Test
    fun togglePriceRule_allTargetItemsHaveRule_togglesOff() = runTest {
        val p1 = Product(name = "Producto 1", price = 10.0, stock = 10)
        repository.addProduct(p1)

        val priceRule = PriceRule(
            id = "rule1",
            name = "Descuento 20%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.80,
            isActive = true
        )
        repository.addPriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Toggle ON
        viewModel.togglePriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.cartItems.first().isRuleDiscounted)
        assertEquals("rule1", viewModel.uiState.value.cartItems.first().appliedRuleId)

        // Toggle OFF
        viewModel.togglePriceRule(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.selectedPriceRuleId)
        val item = state.cartItems.first()
        assertFalse(item.isRuleDiscounted)
        assertNull(item.appliedRuleId)
        assertNull(item.unitPriceOverride)
        assertEquals(10.0, item.effectiveUnitPrice, 0.001)
    }

    @Test
    fun togglePriceRule_replacesPreviousRuleOnTargetItems() = runTest {
        val p1 = Product(name = "Producto 1", price = 100.0, stock = 10)
        repository.addProduct(p1)

        val rule1 = PriceRule(
            id = "rule1",
            name = "Descuento 10%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.90,
            isActive = true
        )
        val rule2 = PriceRule(
            id = "rule2",
            name = "Descuento 30%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.70,
            isActive = true
        )
        repository.addPriceRule(rule1)
        repository.addPriceRule(rule2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply rule 1
        viewModel.togglePriceRule(rule1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(90.0, viewModel.uiState.value.cartItems.first().effectiveUnitPrice, 0.001)
        assertEquals("rule1", viewModel.uiState.value.cartItems.first().appliedRuleId)

        // Replace with rule 2 (30% off base price 100.0 -> 70.0)
        viewModel.togglePriceRule(rule2)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("rule2", state.selectedPriceRuleId)
        val item = state.cartItems.first()
        assertTrue(item.isRuleDiscounted)
        assertEquals("rule2", item.appliedRuleId)
        assertEquals("Descuento 30%", item.appliedRuleName)
        assertEquals(70.0, item.effectiveUnitPrice, 0.001)
    }

    @Test
    fun appliedRuleName_carriesRuleNameOnCartItem() = runTest {
        val p1 = Product(name = "Producto 1", price = 100.0, stock = 10)
        repository.addProduct(p1)

        val rule = PriceRule(
            id = "rule1",
            name = "Mayoreo",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.80,
            isActive = true
        )
        repository.addPriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        val item = viewModel.uiState.value.cartItems.first()
        assertTrue(item.isRuleDiscounted)
        assertEquals("rule1", item.appliedRuleId)
        assertEquals("Mayoreo", item.appliedRuleName)
    }

    @Test
    fun resetActiveRule_whenCartIsEmptied() = runTest {
        val p1 = Product(name = "Producto 1", price = 100.0, stock = 10)
        repository.addProduct(p1)

        val rule = PriceRule(
            id = "rule1",
            name = "Mayoreo",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.80,
            isActive = true
        )
        repository.addPriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. removeFromCart empties cart
        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("rule1", viewModel.uiState.value.selectedPriceRuleId)

        val cartItem1 = viewModel.uiState.value.cartItems.first()
        viewModel.removeFromCart(cartItem1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
        assertNull(viewModel.uiState.value.selectedPriceRuleId)

        // 2. deleteFromCart empties cart
        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("rule1", viewModel.uiState.value.selectedPriceRuleId)

        val cartItem2 = viewModel.uiState.value.cartItems.first()
        viewModel.deleteFromCart(cartItem2)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
        assertNull(viewModel.uiState.value.selectedPriceRuleId)

        // 3. clearCart empties cart
        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("rule1", viewModel.uiState.value.selectedPriceRuleId)

        viewModel.clearCart()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
        assertNull(viewModel.uiState.value.selectedPriceRuleId)
    }

    @Test
    fun deleteFromCart_deactivatesPriceRule_whenNoRemainingItemsHaveRuleApplied() = runTest {
        val p1 = Product(name = "Producto 1", price = 100.0, stock = 10)
        val p2 = Product(name = "Producto 2", price = 50.0, stock = 10)
        repository.addProduct(p1)
        repository.addProduct(p2)

        val rule = PriceRule(
            id = "rule1",
            name = "Descuento 10%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.90,
            isActive = true
        )
        repository.addPriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        viewModel.addToCart(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply rule only to p1
        val item1Before = viewModel.uiState.value.cartItems.find { it.product.id == p1.id }!!
        viewModel.toggleItemSelection(item1Before.id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("rule1", viewModel.uiState.value.selectedPriceRuleId)

        // Clear selection and delete p1 from cart
        viewModel.clearSelection()
        testDispatcher.scheduler.advanceUntilIdle()

        val item1WithRule = viewModel.uiState.value.cartItems.find { it.product.id == p1.id }!!
        viewModel.deleteFromCart(item1WithRule)
        testDispatcher.scheduler.advanceUntilIdle()

        // p2 remains in cart without the rule applied
        assertEquals(1, viewModel.uiState.value.cartItems.size)
        assertEquals(p2.id, viewModel.uiState.value.cartItems.first().product.id)
        assertNull(viewModel.uiState.value.selectedPriceRuleId)
    }

    @Test
    fun removeFromCart_deactivatesPriceRule_whenLastItemWithRuleIsRemoved() = runTest {
        val p1 = Product(name = "Producto 1", price = 100.0, stock = 10)
        val p2 = Product(name = "Producto 2", price = 50.0, stock = 10)
        repository.addProduct(p1)
        repository.addProduct(p2)

        val rule = PriceRule(
            id = "rule1",
            name = "Descuento 10%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.90,
            isActive = true
        )
        repository.addPriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        viewModel.addToCart(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply rule to p1
        val item1Before = viewModel.uiState.value.cartItems.find { it.product.id == p1.id }!!
        viewModel.toggleItemSelection(item1Before.id)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("rule1", viewModel.uiState.value.selectedPriceRuleId)

        viewModel.clearSelection()
        testDispatcher.scheduler.advanceUntilIdle()

        // Remove p1 (quantity 1) via removeFromCart
        val item1WithRule = viewModel.uiState.value.cartItems.find { it.product.id == p1.id }!!
        viewModel.removeFromCart(item1WithRule)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.cartItems.size)
        assertNull(viewModel.uiState.value.selectedPriceRuleId)
    }

    @Test
    fun deleteFromCart_keepsPriceRuleActive_ifOtherCartItemsStillHaveRuleApplied() = runTest {
        val p1 = Product(name = "Producto 1", price = 100.0, stock = 10)
        val p2 = Product(name = "Producto 2", price = 50.0, stock = 10)
        repository.addProduct(p1)
        repository.addProduct(p2)

        val rule = PriceRule(
            id = "rule1",
            name = "Descuento 10%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.90,
            isActive = true
        )
        repository.addPriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        viewModel.addToCart(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply rule to all items (p1 and p2)
        viewModel.clearSelection()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("rule1", viewModel.uiState.value.selectedPriceRuleId)

        // Delete p1
        val item1 = viewModel.uiState.value.cartItems.find { it.product.id == p1.id }!!
        viewModel.deleteFromCart(item1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Rule should STILL be active because p2 has rule1 applied
        assertEquals(1, viewModel.uiState.value.cartItems.size)
        assertEquals("rule1", viewModel.uiState.value.selectedPriceRuleId)

        // Delete p2
        val item2 = viewModel.uiState.value.cartItems.find { it.product.id == p2.id }!!
        viewModel.deleteFromCart(item2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Cart is empty and rule deactivated
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
        assertNull(viewModel.uiState.value.selectedPriceRuleId)
    }

    @Test
    fun deleteFromCart_selectedItem_removesIdFromSelectedCartItemIds_andUpdatesCounterInRealTime() = runTest {
        val p1 = Product(name = "Producto 1", price = 100.0, stock = 10)
        val p2 = Product(name = "Producto 2", price = 50.0, stock = 10)
        repository.addProduct(p1)
        repository.addProduct(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        viewModel.addToCart(p2)
        testDispatcher.scheduler.advanceUntilIdle()

        val cartItemsBefore = viewModel.uiState.value.cartItems
        assertEquals(2, cartItemsBefore.size)
        val item1 = cartItemsBefore.find { it.product.id == p1.id }!!
        val item2 = cartItemsBefore.find { it.product.id == p2.id }!!

        // Select item 1
        viewModel.toggleItemSelection(item1.id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.selectedCartItemIds.value.contains(item1.id))
        assertEquals(1, viewModel.selectedCartItemIds.value.size)

        var state = viewModel.uiState.value
        var validSelectedCount = state.cartItems.count { state.selectedCartItemIds.contains(it.id) }
        assertEquals(1, validSelectedCount)
        assertEquals(2, state.cartItems.size)

        // Delete item 1 from cart
        viewModel.deleteFromCart(item1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify item1's ID is removed from selectedCartItemIds state flow and pruned
        assertFalse(viewModel.selectedCartItemIds.value.contains(item1.id))
        assertTrue(viewModel.selectedCartItemIds.value.isEmpty())

        state = viewModel.uiState.value
        validSelectedCount = state.cartItems.count { state.selectedCartItemIds.contains(it.id) }
        // Remaining cart items = 1 (item2), valid selected count = 0
        assertEquals(1, state.cartItems.size)
        assertEquals(item2.id, state.cartItems.first().id)
        assertEquals(0, validSelectedCount)
        assertEquals(0, state.selectedCartItemIds.size)
    }

    @Test
    fun removeFromCart_lastQuantity_prunesSelectedCartItemIds() = runTest {
        val p1 = Product(name = "Producto 1", price = 10.0, stock = 10)
        repository.addProduct(p1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(p1)
        testDispatcher.scheduler.advanceUntilIdle()

        val item1 = viewModel.uiState.value.cartItems.first()
        viewModel.toggleItemSelection(item1.id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.selectedCartItemIds.value.size)

        viewModel.removeFromCart(item1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.selectedCartItemIds.value.isEmpty())
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun defaultCustomerAndPaymentMethod_selectedAutomatically() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val defaultCustomer = repository.customers.value.find { it.isDefault }
        val defaultPaymentMethod = repository.paymentMethods.value.find { it.isDefault }

        assertEquals(defaultCustomer?.id, state.selectedCustomerId)
        assertEquals(defaultPaymentMethod?.id, state.selectedPaymentMethodId)
        assertEquals(InvoiceType.CONSUMIDOR_FINAL, state.selectedInvoiceType)
    }

    @Test
    fun processSale_createsSale_decrementsStock_clearsCart_andCalculatesChange() = runTest {
        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = false))
        viewModel.selectInvoiceType(InvoiceType.TICKET)
        val product = repository.products.value.first { !it.isService } // Coffee stock=100, price=2.5
        val initialStock = product.stock

        viewModel.addToCart(product)
        viewModel.addToCart(product) // quantity = 2, subtotal = 5.0
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setCashReceivedStr("10.00")
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBefore = viewModel.uiState.value
        assertEquals(5.0, stateBefore.total, 0.001)
        assertEquals(5.0, stateBefore.changeAmount, 0.001)

        val initialSalesCount = repository.sales.value.size
        val sale = viewModel.processSale()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(sale != null)
        assertEquals(5.0, sale!!.totalAmount, 0.001)
        assertEquals(10.0, sale.cashReceived, 0.001)
        assertEquals(5.0, sale.changeAmount, 0.001)
        assertEquals(initialSalesCount + 1, repository.sales.value.size)

        // Stock should be decremented by 2
        val updatedProduct = repository.products.value.first { it.id == product.id }
        assertEquals(initialStock - 2, updatedProduct.stock)

        // Cart should be empty and cashReceivedStr reset
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
        assertEquals("", viewModel.cashReceivedStr.value)
    }

    @Test
    fun processSale_doesNotDecrementStockForServices() = runTest {
        val serviceProduct = Product(name = "Consulta General", price = 15.0, stock = 0, isService = true)
        repository.addProduct(serviceProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(serviceProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processSale()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedService = repository.products.value.first { it.id == serviceProduct.id }
        assertEquals(0, updatedService.stock)
    }

    @Test
    fun setCashReceivedStr_prefilledWithPurchaseTotal_calculatesZeroChange() = runTest {
        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = false))
        viewModel.selectInvoiceType(InvoiceType.TICKET)
        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val total = viewModel.uiState.value.total
        val formattedTotal = String.format(Locale.US, "%.2f", total)

        viewModel.setCashReceivedStr(formattedTotal)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(formattedTotal, state.cashReceivedStr)
        assertEquals(0.0, state.changeAmount, 0.001)

        val sale = viewModel.processSale()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(sale != null)
        assertEquals(total, sale!!.totalAmount, 0.001)
        assertEquals(total, sale.cashReceived, 0.001)
        assertEquals(0.0, sale.changeAmount, 0.001)
    }

    @Test
    fun processSale_withElectronicBillingEnabled_issuesDteForConsumidorFinal() = runTest {
        repository.updateElectronicBillingConfig(
            ElectronicBillingConfig(
                isEnabled = true,
                establishmentCode = "0001",
                posCode = "0001"
            )
        )
        viewModel.selectInvoiceType(InvoiceType.CONSUMIDOR_FINAL)

        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        var navigatedSaleId: String? = null
        viewModel.processSale { saleId ->
            navigatedSaleId = saleId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(navigatedSaleId != null)
        val savedSale = repository.sales.value.find { it.id == navigatedSaleId }
        assertTrue(savedSale != null)
        assertTrue(savedSale!!.isDteIssued)
        assertEquals("01", savedSale.dteType)
        assertTrue(savedSale.dteGenerationCode?.isNotBlank() == true)
        assertTrue(savedSale.dteReceptionSeal?.startsWith("MH-DTE-") == true)
        assertTrue(savedSale.dteControlNumber?.startsWith("DTE-01-") == true)
    }

    @Test
    fun processSale_withElectronicBillingEnabled_issuesDteForCreditoFiscal() = runTest {
        repository.updateElectronicBillingConfig(
            ElectronicBillingConfig(
                isEnabled = true,
                establishmentCode = "0002",
                posCode = "0002"
            )
        )
        viewModel.selectInvoiceType(InvoiceType.CREDITO_FISCAL)

        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        var navigatedSaleId: String? = null
        viewModel.processSale { saleId ->
            navigatedSaleId = saleId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(navigatedSaleId != null)
        val savedSale = repository.sales.value.find { it.id == navigatedSaleId }
        assertTrue(savedSale != null)
        assertTrue(savedSale!!.isDteIssued)
        assertEquals("03", savedSale.dteType)
        assertTrue(savedSale.dteGenerationCode?.isNotBlank() == true)
        assertTrue(savedSale.dteReceptionSeal?.startsWith("MH-DTE-") == true)
        assertTrue(savedSale.dteControlNumber?.startsWith("DTE-03-") == true)
    }

    @Test
    fun processSale_whenDteTransmissionFails_savesInContingencyModeAndNavigates() = runTest {
        repository.updateElectronicBillingConfig(
            ElectronicBillingConfig(
                isEnabled = true,
                environment = DteEnvironment.SANDBOX,
                nit = "", // missing credentials -> contingency
                apiToken = "",
                isSimulationMode = false
            )
        )
        viewModel.selectInvoiceType(InvoiceType.CONSUMIDOR_FINAL)

        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        var navigatedSaleId: String? = null
        viewModel.processSale { saleId ->
            navigatedSaleId = saleId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(navigatedSaleId != null)
        val savedSale = repository.sales.value.find { it.id == navigatedSaleId }
        assertTrue(savedSale != null)
        assertTrue(savedSale!!.contingencyMode)
        assertFalse(savedSale.isDteIssued)
        assertEquals(InvoiceType.CONSUMIDOR_FINAL, savedSale.invoiceType)
        assertTrue(repository.contingencyDtes.value.any { it.id == navigatedSaleId })
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun selectInvoiceType_whenElectronicBillingDisabled_fallsBackToTicketForConsumidorFinalAndCreditoFiscal() = runTest {
        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = false))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectInvoiceType(InvoiceType.CONSUMIDOR_FINAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(InvoiceType.TICKET, viewModel.uiState.value.selectedInvoiceType)

        viewModel.selectInvoiceType(InvoiceType.CREDITO_FISCAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(InvoiceType.TICKET, viewModel.uiState.value.selectedInvoiceType)

        viewModel.selectInvoiceType(InvoiceType.TICKET)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(InvoiceType.TICKET, viewModel.uiState.value.selectedInvoiceType)
    }

    @Test
    fun selectInvoiceType_whenElectronicBillingEnabled_allowsConsumidorFinalAndCreditoFiscal() = runTest {
        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectInvoiceType(InvoiceType.CONSUMIDOR_FINAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(InvoiceType.CONSUMIDOR_FINAL, viewModel.uiState.value.selectedInvoiceType)

        viewModel.selectInvoiceType(InvoiceType.CREDITO_FISCAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(InvoiceType.CREDITO_FISCAL, viewModel.uiState.value.selectedInvoiceType)
    }

    @Test
    fun electronicBillingDisabled_autoFallsBackSelectedInvoiceTypeToTicket() = runTest {
        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectInvoiceType(InvoiceType.CREDITO_FISCAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(InvoiceType.CREDITO_FISCAL, viewModel.uiState.value.selectedInvoiceType)

        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = false))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(InvoiceType.TICKET, viewModel.uiState.value.selectedInvoiceType)
    }

    @Test
    fun openShift_validPin_opensShiftAndReturnsTrue() = runTest {
        val cashier = User(id = "user1", name = "Juan Pérez", pin = "1234", role = Role.CASHIER)
        repository.addUser(cashier)
        testDispatcher.scheduler.advanceUntilIdle()

        val success = viewModel.openShift(cashier, "1234", 50.0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(success)
        val active = viewModel.activeShift.value
        assertTrue(active != null)
        assertEquals("Juan Pérez", active?.cashierName)
        assertEquals(50.0, active?.initialFloat ?: 0.0, 0.001)
        assertEquals(ShiftStatus.OPEN, active?.status)
    }

    @Test
    fun openShift_invalidPin_returnsFalse() = runTest {
        val cashier = User(id = "user1", name = "Juan Pérez", pin = "1234", role = Role.CASHIER)
        repository.addUser(cashier)
        testDispatcher.scheduler.advanceUntilIdle()

        val success = viewModel.openShift(cashier, "9999", 50.0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(success)
        assertNull(viewModel.activeShift.value)
    }

    @Test
    fun closeShift_closesActiveShiftAndReturnsClosedShift() = runTest {
        val cashier = User(id = "user1", name = "Juan Pérez", pin = "1234", role = Role.CASHIER)
        viewModel.openShift(cashier, "1234", 100.0)
        testDispatcher.scheduler.advanceUntilIdle()

        val active = viewModel.activeShift.value
        assertTrue(active != null)

        val closed = viewModel.closeShift(120.0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(closed != null)
        assertEquals(120.0, closed?.actualCashCounted ?: 0.0, 0.001)
        assertEquals(20.0, closed?.difference ?: 0.0, 0.001)
        assertEquals(ShiftStatus.CLOSED, closed?.status)
        assertNull(viewModel.activeShift.value)
    }

    @Test
    fun activeShiftAndUsers_exposesRepositoryStateFlows() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val users = viewModel.users.value
        assertTrue(users.isNotEmpty())
        assertEquals("Administrador", users.first().name)
        assertNull(viewModel.activeShift.value)
    }

    @Test
    fun setIsCreditSale_updatesStateFlowAndUiState() = runTest {
        assertFalse(viewModel.isCreditSale.value)
        assertFalse(viewModel.uiState.value.isCreditSale)

        val targetDueDate = System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000L
        viewModel.setIsCreditSale(true)
        viewModel.setCreditDueDateMillis(targetDueDate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isCreditSale.value)
        assertTrue(viewModel.uiState.value.isCreditSale)
        assertEquals(targetDueDate, viewModel.creditDueDateMillis.value)
        assertEquals(targetDueDate, viewModel.uiState.value.creditDueDateMillis)
    }

    @Test
    fun processSale_creditSale_withDefaultCustomer_failsValidationAndReturnsNull() = runTest {
        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = false))
        viewModel.selectInvoiceType(InvoiceType.TICKET)

        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        // Enable credit sale with default customer selected
        val defaultCustomer = repository.customers.value.find { it.isDefault }
        viewModel.selectCustomer(defaultCustomer?.id)
        viewModel.setIsCreditSale(true)
        testDispatcher.scheduler.advanceUntilIdle()

        val sale = viewModel.processSale()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(sale)
        assertFalse(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun processSale_creditSale_withSpecificCustomer_createsCreditSaleWithCorrectFields() = runTest {
        repository.updateElectronicBillingConfig(ElectronicBillingConfig(isEnabled = false))
        viewModel.selectInvoiceType(InvoiceType.TICKET)

        // Add a specific non-default customer
        val specificCustomer = Customer(
            id = "cust_credit_1",
            name = "Empresa X, S.A.",
            documentNumber = "01234567-8",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador",
            isDefault = false
        )
        repository.addCustomer(specificCustomer)
        testDispatcher.scheduler.advanceUntilIdle()

        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedTotal = viewModel.uiState.value.total
        val targetDueDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L

        viewModel.selectCustomer(specificCustomer.id)
        viewModel.setIsCreditSale(true)
        viewModel.setCreditDueDateMillis(targetDueDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val sale = viewModel.processSale()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(sale != null)
        assertEquals(specificCustomer.id, sale!!.customerId)
        assertEquals(specificCustomer.name, sale.customerName)
        assertTrue(sale.isCredit)
        assertEquals(targetDueDate, sale.creditDueDateMillis)
        assertEquals("Crédito / Fiado", sale.paymentMethodName)
        assertEquals(0.0, sale.cashReceived, 0.001)
        assertEquals(0.0, sale.changeAmount, 0.001)
        assertEquals(expectedTotal, sale.remainingBalance, 0.001)
        assertEquals(CreditStatus.UNPAID, sale.creditStatus)

        // After successful sale, cart is cleared and isCreditSale resets to false
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
        assertFalse(viewModel.isCreditSale.value)
    }
}
