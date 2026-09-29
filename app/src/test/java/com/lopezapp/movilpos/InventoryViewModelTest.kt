package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.BundleItem
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
        viewModel = InventoryViewModel(repository)
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
}
