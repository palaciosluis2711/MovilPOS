package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.BundleItem
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.InventoryViewModel
import com.lopezapp.movilpos.ui.viewmodel.ProductSortOption
import com.lopezapp.movilpos.ui.viewmodel.ProductTypeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = InventoryViewModel(repository, defaultDispatcher = UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun categories_exposesRepositoryCategories() = runTest {
        val categories = viewModel.categories.value
        assertEquals(4, categories.size)
        assertEquals("Bebidas", categories[0].name)
    }

    @Test
    fun brands_exposesRepositoryBrands() = runTest {
        val brands = viewModel.brands.value
        assertEquals(4, brands.size)
        assertEquals("Sin marca", brands[0].name)
    }

    @Test
    fun unitsOfMeasure_exposesRepositoryUnitsOfMeasure() = runTest {
        val units = viewModel.unitsOfMeasure.value
        assertEquals(5, units.size)
        assertEquals("Unidad", units[0].name)
    }

    @Test
    fun taxes_exposesRepositoryTaxes() = runTest {
        val taxes = viewModel.taxes.value
        assertEquals(1, taxes.size)
        assertEquals("IVA", taxes[0].name)
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
    fun addProduct_withTaxes_addsProductWithTaxesSuccessfully() = runTest {
        val taxId = repository.taxes.value.first().id
        viewModel.addProduct(
            name = "Product With Tax",
            price = 20.0,
            appliedTaxIds = listOf(taxId),
            isTaxIncludedInPrice = true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val products = repository.products.value
        val addedProduct = products.find { it.name == "Product With Tax" }

        assertEquals("Product With Tax", addedProduct?.name)
        assertEquals(listOf(taxId), addedProduct?.appliedTaxIds)
        assertTrue(addedProduct?.isTaxIncludedInPrice == true)
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
    fun updateProduct_withNewStock_updatesProductStockSuccessfully() = runTest {
        val initialProduct = repository.products.value.first()
        val updatedProduct = initialProduct.copy(stock = 25)
        viewModel.updateProduct(updatedProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val productInRepo = repository.products.value.find { it.id == initialProduct.id }
        assertEquals(25, productInRepo?.stock)
    }

    @Test
    fun removeProduct_removesProductSuccessfully() = runTest {
        val initialProduct = repository.products.value.first()
        viewModel.removeProduct(initialProduct.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val productInRepo = repository.products.value.find { it.id == initialProduct.id }
        assertNull(productInRepo)
    }

    @Test
    fun addProduct_bundleProduct_addsBundleSuccessfully() = runTest {
        val child1 = repository.products.value[0]
        val child2 = repository.products.value[1]
        val bundleItems = listOf(
            BundleItem(productId = child1.id, quantity = 2),
            BundleItem(productId = child2.id, quantity = 1)
        )
        viewModel.addProduct(
            name = "Combo Breakfast",
            price = 6.5,
            cost = 3.5,
            unitOfMeasure = "Caja",
            isBundle = true,
            bundleItems = bundleItems
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val products = repository.products.value
        val addedBundle = products.find { it.name == "Combo Breakfast" }

        assertTrue(addedBundle?.isBundle == true)
        assertEquals(2, addedBundle?.bundleItems?.size)
        assertEquals(child1.id, addedBundle?.bundleItems?.get(0)?.productId)
        assertEquals(2, addedBundle?.bundleItems?.get(0)?.quantity)
        assertEquals("Caja", addedBundle?.unitOfMeasure)
    }

    @Test
    fun updateProduct_toBundle_updatesProductSuccessfully() = runTest {
        val initialProduct = repository.products.value.first()
        val otherProduct = repository.products.value.last()
        val bundleItems = listOf(BundleItem(productId = otherProduct.id, quantity = 3))

        val updatedProduct = initialProduct.copy(
            isBundle = true,
            bundleItems = bundleItems,
            unitOfMeasure = "Paquete"
        )
        viewModel.updateProduct(updatedProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val productInRepo = repository.products.value.find { it.id == initialProduct.id }
        assertTrue(productInRepo?.isBundle == true)
        assertEquals(1, productInRepo?.bundleItems?.size)
        assertEquals(3, productInRepo?.bundleItems?.first()?.quantity)
    }

    @Test
    fun addProduct_serviceProduct_addsServiceSuccessfully() = runTest {
        val child = repository.products.value.first()
        val requiredItems = listOf(BundleItem(productId = child.id, quantity = 1))

        viewModel.addProduct(
            name = "Mantenimiento General",
            price = 50.0,
            cost = 10.0,
            unitOfMeasure = "Servicio",
            isService = true,
            bundleItems = requiredItems
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val products = repository.products.value
        val addedService = products.find { it.name == "Mantenimiento General" }

        assertTrue(addedService?.isService == true)
        assertEquals("Servicio", addedService?.unitOfMeasure)
        assertEquals(0, addedService?.alertQuantity)
        assertEquals(1, addedService?.bundleItems?.size)
        assertEquals(child.id, addedService?.bundleItems?.first()?.productId)
    }

    @Test
    fun addProduct_serviceProductWithoutBundle_addsServiceSuccessfully() = runTest {
        viewModel.addProduct(
            name = "Corte de Cabello",
            price = 15.0,
            cost = 2.0,
            unitOfMeasure = "Servicio",
            isService = true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val products = repository.products.value
        val addedService = products.find { it.name == "Corte de Cabello" }

        assertTrue(addedService?.isService == true)
        assertEquals("Servicio", addedService?.unitOfMeasure)
        assertEquals(0, addedService?.alertQuantity)
        assertTrue(addedService?.bundleItems.isNullOrEmpty())
    }

    @Test
    fun updateProduct_toService_updatesProductSuccessfully() = runTest {
        val initialProduct = repository.products.value.first()
        val updatedProduct = initialProduct.copy(
            isService = true,
            unitOfMeasure = "Servicio",
            alertQuantity = 0
        )
        viewModel.updateProduct(updatedProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val productInRepo = repository.products.value.find { it.id == initialProduct.id }
        assertTrue(productInRepo?.isService == true)
        assertEquals("Servicio", productInRepo?.unitOfMeasure)
        assertEquals(0, productInRepo?.alertQuantity)
    }

    @Test
    fun categoryFilter_filtersProductsByCategory() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        viewModel.setSelectedCategoryFilter("Bebidas")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.category == "Bebidas" })
    }

    @Test
    fun brandFilter_filtersProductsByBrand() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        viewModel.setSelectedBrandFilter("Nestlé")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertEquals(1, filtered.size)
        assertEquals("Coffee", filtered.first().name)
    }

    @Test
    fun typeFilter_filtersProductsByProductType() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        viewModel.addProduct(
            name = "Combo Breakfast",
            price = 6.5,
            isBundle = true
        )
        viewModel.addProduct(
            name = "Corte de Cabello",
            price = 15.0,
            isService = true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter Normal
        viewModel.setSelectedTypeFilter(ProductTypeFilter.NORMAL)
        testDispatcher.scheduler.advanceUntilIdle()
        val normalProducts = viewModel.filteredProducts.value
        assertTrue(normalProducts.all { !it.isBundle && !it.isService })

        // Filter Bundle
        viewModel.setSelectedTypeFilter(ProductTypeFilter.BUNDLE)
        testDispatcher.scheduler.advanceUntilIdle()
        val bundleProducts = viewModel.filteredProducts.value
        assertEquals(1, bundleProducts.size)
        assertEquals("Combo Breakfast", bundleProducts.first().name)

        // Filter Service
        viewModel.setSelectedTypeFilter(ProductTypeFilter.SERVICE)
        testDispatcher.scheduler.advanceUntilIdle()
        val serviceProducts = viewModel.filteredProducts.value
        assertEquals(1, serviceProducts.size)
        assertEquals("Corte de Cabello", serviceProducts.first().name)
    }

    @Test
    fun lowStockFilter_filtersProductsWithLowStock() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        viewModel.addProduct(
            name = "Low Stock Item",
            price = 5.0,
            stock = 2,
            alertQuantity = 5
        )
        viewModel.addProduct(
            name = "High Stock Item",
            price = 5.0,
            stock = 20,
            alertQuantity = 5
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setLowStockOnlyFilter(true)
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertEquals(1, filtered.size)
        assertEquals("Low Stock Item", filtered.first().name)
    }

    @Test
    fun resetFilters_clearsAllActiveFilters() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        viewModel.onSearchQueryChanged("Coffee")
        viewModel.setSelectedCategoryFilter("Bebidas")
        viewModel.setSelectedBrandFilter("Nestlé")
        viewModel.setSelectedTypeFilter(ProductTypeFilter.NORMAL)
        viewModel.setLowStockOnlyFilter(true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetFilters()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedCategoryFilter.value)
        assertNull(viewModel.selectedBrandFilter.value)
        assertNull(viewModel.selectedTypeFilter.value)
        assertFalse(viewModel.lowStockOnlyFilter.value)
    }

    @Test
    fun addProduct_withTagsAndDifferentiator_addsProductSuccessfully() = runTest {
        viewModel.addProduct(
            name = "Página Bond",
            price = 5.0,
            tags = listOf("Oferta", "Marzo 2025"),
            differentiator = "Lote $0.01"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val products = repository.products.value
        val addedProduct = products.find { it.name == "Página Bond" }

        assertEquals("Página Bond", addedProduct?.name)
        assertEquals("Lote $0.01", addedProduct?.differentiator)
        assertEquals(listOf("Oferta", "Marzo 2025"), addedProduct?.tags)
        assertEquals("Página Bond [Lote $0.01]", addedProduct?.displayName)
    }

    @Test
    fun searchQuery_filtersProductsByTags() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        viewModel.addProduct(
            name = "Cuaderno 1",
            price = 2.0,
            tags = listOf("estante3", "promoción")
        )
        viewModel.addProduct(
            name = "Cuaderno 2",
            price = 2.0,
            tags = listOf("estante1")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("estante3")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertEquals(1, filtered.size)
        assertEquals("Cuaderno 1", filtered.first().name)
    }

    @Test
    fun searchQuery_filtersProductsByDifferentiator() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        viewModel.addProduct(
            name = "Página Bond",
            price = 0.05,
            differentiator = "Lote $0.01"
        )
        viewModel.addProduct(
            name = "Página Bond",
            price = 0.06,
            differentiator = "Lote $0.02"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Lote $0.01")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertEquals(1, filtered.size)
        assertEquals("Lote $0.01", filtered.first().differentiator)
    }

    @Test
    fun removeProducts_bulkRemovesProducts() = runTest {
        val initialProducts = viewModel.allProducts.value
        val idsToDelete = initialProducts.map { it.id }.toSet()

        viewModel.removeProducts(idsToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        val products = viewModel.allProducts.value
        assertEquals(0, products.size)
    }

    @Test
    fun updateProduct_preservesHasIvaAndPriceIncludesTax() = runTest {
        val taxId = repository.taxes.value.first().id
        viewModel.addProduct(
            name = "Taxed Item",
            price = 15.0,
            appliedTaxIds = listOf(taxId),
            isTaxIncludedInPrice = true,
            hasIva = true,
            priceIncludesTax = true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val savedProduct = repository.products.value.find { it.name == "Taxed Item" }!!
        assertTrue(savedProduct.hasIva)
        assertTrue(savedProduct.priceIncludesTax)
        assertTrue(savedProduct.isTaxIncludedInPrice)

        val updatedProduct = savedProduct.copy(
            price = 18.0,
            hasIva = true,
            priceIncludesTax = true
        )
        viewModel.updateProduct(updatedProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        val reloadedProduct = repository.products.value.find { it.id == savedProduct.id }!!
        assertEquals(18.0, reloadedProduct.price, 0.001)
        assertTrue(reloadedProduct.hasIva)
        assertTrue(reloadedProduct.priceIncludesTax)
        assertTrue(reloadedProduct.isTaxIncludedInPrice)
        assertEquals(listOf(taxId), reloadedProduct.appliedTaxIds)
    }

    @Test
    fun setSelectedSortOption_sortsProductsCorrectly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        val initialIds = repository.products.value.map { it.id }
        viewModel.removeProducts(initialIds)
        testDispatcher.scheduler.advanceUntilIdle()

        val p1 = Product(id = "p1", name = "Banana", price = 1.0, stock = 10, salesCount = 5)
        val p2 = Product(id = "p2", name = "Apple", price = 2.0, stock = 10, salesCount = 50)
        val p3 = Product(id = "p3", name = "Cherry", price = 3.0, stock = 10, salesCount = 20)

        repository.addProduct(p1)
        repository.addProduct(p2)
        repository.addProduct(p3)
        testDispatcher.scheduler.advanceUntilIdle()

        // Test NAME_ASC (A-Z)
        viewModel.setSelectedSortOption(ProductSortOption.NAME_ASC)
        testDispatcher.scheduler.advanceUntilIdle()
        var list = viewModel.filteredProducts.value
        assertEquals(listOf("Apple", "Banana", "Cherry"), list.map { it.name })

        // Test NAME_DESC (Z-A)
        viewModel.setSelectedSortOption(ProductSortOption.NAME_DESC)
        testDispatcher.scheduler.advanceUntilIdle()
        list = viewModel.filteredProducts.value
        assertEquals(listOf("Cherry", "Banana", "Apple"), list.map { it.name })

        // Test BEST_SELLERS (salesCount desc)
        viewModel.setSelectedSortOption(ProductSortOption.BEST_SELLERS)
        testDispatcher.scheduler.advanceUntilIdle()
        list = viewModel.filteredProducts.value
        assertEquals(listOf("Apple", "Cherry", "Banana"), list.map { it.name })
    }

    @Test
    fun setSelectedTypeFilter_filtersProductsByDropdownSelection() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filteredProducts.collect() }
        val initialIds = repository.products.value.map { it.id }
        viewModel.removeProducts(initialIds)
        testDispatcher.scheduler.advanceUntilIdle()

        val p1 = Product(id = "p1", name = "Normal Product", price = 10.0, stock = 10, isService = false, isBundle = false)
        val p2 = Product(id = "p2", name = "Service Product", price = 15.0, stock = 0, isService = true, isBundle = false)
        val p3 = Product(id = "p3", name = "Bundle Product", price = 20.0, stock = 5, isService = false, isBundle = true)

        repository.addProduct(p1)
        repository.addProduct(p2)
        repository.addProduct(p3)
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter NORMAL
        viewModel.setSelectedTypeFilter(ProductTypeFilter.NORMAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredProducts.value.size)
        assertEquals("Normal Product", viewModel.filteredProducts.value.first().name)

        // Filter SERVICE
        viewModel.setSelectedTypeFilter(ProductTypeFilter.SERVICE)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredProducts.value.size)
        assertEquals("Service Product", viewModel.filteredProducts.value.first().name)

        // Filter BUNDLE
        viewModel.setSelectedTypeFilter(ProductTypeFilter.BUNDLE)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredProducts.value.size)
        assertEquals("Bundle Product", viewModel.filteredProducts.value.first().name)

        // Reset (ALL)
        viewModel.setSelectedTypeFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.filteredProducts.value.size)
    }

    @Test
    fun updateProduct_togglesIsPinnedInPos() = runTest {
        val prod = Product(id = "pin_test", name = "Pin Test Product", price = 5.0, stock = 10, isPinnedInPos = false)
        repository.addProduct(prod)
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = repository.products.value.find { it.id == "pin_test" }!!
        assertFalse(saved.isPinnedInPos)

        viewModel.updateProduct(saved.copy(isPinnedInPos = true))
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = repository.products.value.find { it.id == "pin_test" }!!
        assertTrue(updated.isPinnedInPos)
    }
}
