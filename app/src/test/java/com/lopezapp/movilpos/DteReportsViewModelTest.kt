package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.DteReportsViewModel
import com.lopezapp.movilpos.util.DteJsonGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DteReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: DteReportsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = DteReportsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun issuedDtes_filtersOnlySalesWhereIsDteIssuedIsTrue() = runTest {
        val normalSale = Sale(
            customerId = "c1",
            customerName = "Cliente Normal",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 10.0,
            isDteIssued = false
        )
        val dteSale = Sale(
            customerId = "c2",
            customerName = "Cliente DTE",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 25.0,
            isDteIssued = true,
            dteGenerationCode = "ABC-123",
            dteReceptionSeal = "SEAL-999",
            dteControlNumber = "CTRL-001"
        )

        repository.addSale(normalSale)
        repository.addSale(dteSale)
        testDispatcher.scheduler.advanceUntilIdle()

        val issuedList = viewModel.issuedDtes.value
        assertEquals(1, issuedList.size)
        assertEquals("Cliente DTE", issuedList.first().customerName)
        assertEquals("SEAL-999", issuedList.first().dteReceptionSeal)
    }

    @Test
    fun dteJsonGenerator_generatesValidJsonPayload() {
        val sale = Sale(
            id = "test-sale-1",
            customerId = "c1",
            customerName = "Juan Perez",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(
                SaleItem(productId = "p1", productName = "Coffee", quantity = 2, unitPrice = 2.5, subtotal = 5.0)
            ),
            totalAmount = 5.0,
            isDteIssued = true,
            dteGenerationCode = "01234567-89AB-CDEF-0123-456789ABCDEF",
            dteReceptionSeal = "MH-DTE-2025-00000000000001",
            dteControlNumber = "DTE-01-00000001-000000000000001",
            dteType = "01"
        )
        val businessInfo = BusinessInfo(name = "Mi Cafetería S.A.", nit = "0614-010190-101-5", nrc = "123456-7")
        val config = ElectronicBillingConfig(nit = "0614-010190-101-5")

        val jsonString = DteJsonGenerator.generateDteJson(sale, businessInfo, config)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("01234567-89AB-CDEF-0123-456789ABCDEF"))
        assertTrue(jsonString.contains("MH-DTE-2025-00000000000001"))
        assertTrue(jsonString.contains("Juan Perez"))
        assertTrue(jsonString.contains("Coffee"))
    }

    @Test
    fun generateDteInvalidationJson_generatesValidPayload() {
        val sale = Sale(
            id = "test-sale-void",
            customerId = "c1",
            customerName = "Juan Perez",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(
                SaleItem(productId = "p1", productName = "Coffee", quantity = 1, unitPrice = 2.5, subtotal = 2.5)
            ),
            totalAmount = 2.5,
            isDteIssued = true,
            dteGenerationCode = "01234567-89AB-CDEF-0123-456789ABCDEF",
            dteReceptionSeal = "MH-DTE-2025-00000000000001",
            dteControlNumber = "DTE-01-00000001-000000000000001",
            dteType = "01",
            cashierName = "Carlos Cajero"
        )
        val businessInfo = BusinessInfo(name = "Mi Tienda", nit = "0614-010190-101-5", nrc = "123456-7")

        val invalidationJson = DteJsonGenerator.generateDteInvalidationJson(
            sale = sale,
            reason = "Error en precio digitado",
            businessInfo = businessInfo
        )

        assertNotNull(invalidationJson)
        assertTrue(invalidationJson.contains("identificacion"))
        assertTrue(invalidationJson.contains("emisor"))
        assertTrue(invalidationJson.contains("documentoAnulado"))
        assertTrue(invalidationJson.contains("motivo"))
        assertTrue(invalidationJson.contains("01234567-89AB-CDEF-0123-456789ABCDEF"))
        assertTrue(invalidationJson.contains("MH-DTE-2025-00000000000001"))
        assertTrue(invalidationJson.contains("Error en precio digitado"))
        assertTrue(invalidationJson.contains("Carlos Cajero"))
    }

    @Test
    fun voidSaleDte_updatesSaleStateInRepositoryAndViewModel() = runTest {
        val dteSale = Sale(
            id = "sale-to-void-1",
            customerId = "c1",
            customerName = "Cliente Test",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 15.0,
            isDteIssued = true,
            dteGenerationCode = "GEN-123",
            dteReceptionSeal = "SEAL-123"
        )
        repository.addSale(dteSale)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.voidSaleDte("sale-to-void-1", "Devolución de cliente")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedSale = viewModel.getSaleById("sale-to-void-1")
        assertNotNull(updatedSale)
        assertTrue(updatedSale!!.isVoided)
        assertEquals("Devolución de cliente", updatedSale.voidReason)
        assertNotNull(updatedSale.voidedAtMillis)
    }

    @Test
    fun issuedDtes_includesContingencySalesAndExposesContingencyDtes() = runTest {
        val contingencySale = Sale(
            id = "contingency-sale-1",
            customerId = "c1",
            customerName = "Cliente Contingencia",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 50.0,
            isDteIssued = false,
            contingencyMode = true
        )
        repository.addToContingencyQueue(contingencySale)
        testDispatcher.scheduler.advanceUntilIdle()

        val contingencyList = viewModel.contingencyDtes.value
        assertEquals(1, contingencyList.size)
        assertEquals("contingency-sale-1", contingencyList.first().id)

        val issuedList = viewModel.issuedDtes.value
        assertEquals(1, issuedList.size)
        assertTrue(issuedList.first().contingencyMode)
    }

    @Test
    fun retryContingencyTransmissions_retransmitsAndUpdatesViewModelFlows() = runTest {
        val contingencySale = Sale(
            id = "contingency-sale-retry",
            customerId = "c1",
            customerName = "Cliente Contingencia Retry",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 75.0,
            isDteIssued = false
        )
        repository.addToContingencyQueue(contingencySale)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.contingencyDtes.value.size)

        val result = viewModel.retryContingencyTransmissions()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        assertTrue(viewModel.contingencyDtes.value.isEmpty())

        val updatedSale = viewModel.getSaleById("contingency-sale-retry")
        assertNotNull(updatedSale)
        assertTrue(updatedSale!!.isDteIssued)
        Assert.assertFalse(updatedSale.contingencyMode)
    }
}
