package com.lopezapp.movilpos.data.repository

import android.content.Context
import android.content.ContextWrapper
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID

class LocalDiskPersistenceTest {

    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "movilpos_test_${UUID.randomUUID()}")
        tempDir.mkdirs()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun createTestContext(): Context {
        return object : ContextWrapper(null) {
            override fun getFilesDir() = tempDir
            override fun getApplicationContext() = this
        }
    }

    @Test
    fun testSaveAndLoadDatabaseState() {
        val testContext = createTestContext()

        val initialState = LocalDatabaseState(
            products = listOf(
                Product(id = "p1", name = "Test Product 1", price = 10.0, stock = 20)
            ),
            customers = listOf(
                Customer(id = "c1", name = "Juan Perez", documentType = DocumentType.DUI, documentNumber = "12345678-9", department = "San Salvador", municipality = "San Salvador Centro", district = "San Salvador")
            ),
            sales = listOf(
                Sale(
                    id = "s1",
                    customerId = "c1",
                    customerName = "Juan Perez",
                    paymentMethodId = "pm1",
                    paymentMethodName = "Efectivo",
                    items = listOf(SaleItem(productId = "p1", productName = "Test Product 1", quantity = 2, unitPrice = 10.0, subtotal = 20.0)),
                    totalAmount = 20.0
                )
            ),
            businessInfo = BusinessInfo(name = "Mi Tienda Test", nit = "0614-010190-101-5")
        )

        LocalDiskPersistence.saveToDiskSync(testContext, initialState)

        assertTrue(LocalDiskPersistence.hasSavedDatabase(testContext))

        val loadedState = LocalDiskPersistence.loadFromDiskSync(testContext)
        assertNotNull(loadedState)
        assertEquals(1, loadedState!!.products.size)
        assertEquals("Test Product 1", loadedState.products[0].name)
        assertEquals(1, loadedState.customers.size)
        assertEquals("Juan Perez", loadedState.customers[0].name)
        assertEquals(1, loadedState.sales.size)
        assertEquals(20.0, loadedState.sales[0].totalAmount, 0.001)
        assertEquals("Mi Tienda Test", loadedState.businessInfo.name)
    }

    @Test
    fun testAppRepositoryLocalPersistenceIntegration() {
        val testContext = createTestContext()
        val repo1 = AppRepository()
        repo1.initializeLocalDiskPersistence(testContext)

        // Modify repository data
        val newProduct = Product(id = "p_persistence", name = "Sofa Cama", price = 150.0, stock = 5)
        repo1.addProduct(newProduct)

        val newCustomer = Customer(id = "c_persistence", name = "Maria Lopez", documentType = DocumentType.DUI, documentNumber = "87654321-0", department = "San Salvador", municipality = "San Salvador Centro", district = "San Salvador")
        repo1.addCustomer(newCustomer)

        repo1.updateBusinessInfo(BusinessInfo(name = "Supermercado Lopez", phone = "7777-8888"))

        // Save repository state explicitly sync
        LocalDiskPersistence.saveToDiskSync(testContext, repo1.getCurrentState())

        // Verify the file was created
        assertTrue(LocalDiskPersistence.hasSavedDatabase(testContext))

        // Create a brand new AppRepository and initialize persistence
        val repo2 = AppRepository()
        repo2.initializeLocalDiskPersistence(testContext)

        // Verify data was 100% restored in the new repository instance
        val restoredProducts = repo2.products.value
        assertTrue(restoredProducts.any { it.id == "p_persistence" && it.name == "Sofa Cama" })

        val restoredCustomers = repo2.customers.value
        assertTrue(restoredCustomers.any { it.id == "c_persistence" && it.name == "Maria Lopez" })

        val restoredBusinessInfo = repo2.businessInfo.value
        assertEquals("Supermercado Lopez", restoredBusinessInfo.name)
        assertEquals("7777-8888", restoredBusinessInfo.phone)
    }
}
