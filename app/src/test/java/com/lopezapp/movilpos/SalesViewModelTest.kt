package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.SalesViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: SalesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = SalesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sales_initialDataIsLoaded() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val sales = viewModel.allSales.value
        assertTrue(sales.isNotEmpty())
        assertEquals(sales.size, viewModel.filteredSales.value.size)
    }

    @Test
    fun searchQuery_filtersSalesByCustomerNameSaleIdAndProductName() = runTest {
        val sale1 = Sale(
            id = "SALE-1001",
            customerId = "c1",
            customerName = "Juan Pérez",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(
                SaleItem(productId = "p1", productName = "Café Latte", quantity = 2, unitPrice = 3.0, subtotal = 6.0)
            ),
            totalAmount = 6.0
        )
        val sale2 = Sale(
            id = "SALE-2002",
            customerId = "c2",
            customerName = "Maria Gomez",
            invoiceType = InvoiceType.CREDITO_FISCAL,
            paymentMethodId = "pm2",
            paymentMethodName = "Tarjeta",
            items = listOf(
                SaleItem(productId = "p2", productName = "Empanada", quantity = 1, unitPrice = 2.0, subtotal = 2.0)
            ),
            totalAmount = 2.0
        )

        repository.addSale(sale1)
        repository.addSale(sale2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter by Customer Name
        viewModel.onSearchQueryChanged("Juan")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredSales.value.size)
        assertEquals("SALE-1001", viewModel.filteredSales.value.first().id)

        // Filter by Sale ID
        viewModel.onSearchQueryChanged("2002")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredSales.value.size)
        assertEquals("SALE-2002", viewModel.filteredSales.value.first().id)

        // Filter by Product Name
        viewModel.onSearchQueryChanged("Latte")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredSales.value.size)
        assertEquals("Café Latte", viewModel.filteredSales.value.first().items.first().productName)

        // Empty Search
        viewModel.onSearchQueryChanged("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.filteredSales.value.size) // initial dummy + sale1 + sale2
    }

    @Test
    fun customerFilter_filtersSalesByCustomerId() = runTest {
        val sale = Sale(
            id = "SALE-3003",
            customerId = "cust_special",
            customerName = "Empresa X",
            invoiceType = InvoiceType.CREDITO_FISCAL,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 100.0
        )
        repository.addSale(sale)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSelectedCustomerIdFilter("cust_special")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.filteredSales.value.size)
        assertEquals("cust_special", viewModel.filteredSales.value.first().customerId)

        viewModel.setSelectedCustomerIdFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.filteredSales.value.size)
    }

    @Test
    fun invoiceTypeFilter_filtersSalesByInvoiceType() = runTest {
        val saleCf = Sale(
            id = "SALE-CF",
            customerId = "c1",
            customerName = "Cliente CF",
            invoiceType = InvoiceType.CREDITO_FISCAL,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 50.0
        )
        repository.addSale(saleCf)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSelectedInvoiceTypeFilter(InvoiceType.CREDITO_FISCAL)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.filteredSales.value.size)
        assertEquals(InvoiceType.CREDITO_FISCAL, viewModel.filteredSales.value.first().invoiceType)

        viewModel.setSelectedInvoiceTypeFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.filteredSales.value.size)
    }

    @Test
    fun paymentMethodFilter_filtersSalesByPaymentMethodId() = runTest {
        val pmId = repository.paymentMethods.value.last().id
        val saleCard = Sale(
            id = "SALE-PM",
            customerId = "c1",
            customerName = "Cliente",
            invoiceType = InvoiceType.TICKET,
            paymentMethodId = pmId,
            paymentMethodName = "Cheque",
            items = emptyList(),
            totalAmount = 75.0
        )
        repository.addSale(saleCard)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSelectedPaymentMethodIdFilter(pmId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.filteredSales.value.size)
        assertEquals(pmId, viewModel.filteredSales.value.first().paymentMethodId)
    }

    @Test
    fun dateFilter_filtersSalesByDate() = runTest {
        val calToday = Calendar.getInstance()
        val calPast = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -5) }

        val salePast = Sale(
            id = "SALE-PAST",
            customerId = "c1",
            customerName = "Cliente",
            invoiceType = InvoiceType.TICKET,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 25.0,
            dateMillis = calPast.timeInMillis
        )
        repository.addSale(salePast)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSelectedDateFilterMillis(calPast.timeInMillis)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.filteredSales.value.size)
        assertEquals("SALE-PAST", viewModel.filteredSales.value.first().id)

        viewModel.setSelectedDateFilterMillis(calToday.timeInMillis)
        testDispatcher.scheduler.advanceUntilIdle()

        // Today's sales should include the initial dummy sale
        assertTrue(viewModel.filteredSales.value.none { it.id == "SALE-PAST" })
    }

    @Test
    fun resetFilters_clearsAllFilters() = runTest {
        viewModel.onSearchQueryChanged("Test")
        viewModel.setSelectedCustomerIdFilter("c1")
        viewModel.setSelectedInvoiceTypeFilter(InvoiceType.CREDITO_FISCAL)
        viewModel.setSelectedPaymentMethodIdFilter("pm1")
        viewModel.setSelectedDateFilterMillis(System.currentTimeMillis())

        viewModel.resetFilters()

        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedCustomerIdFilter.value)
        assertNull(viewModel.selectedInvoiceTypeFilter.value)
        assertNull(viewModel.selectedPaymentMethodIdFilter.value)
        assertNull(viewModel.selectedDateFilterMillis.value)
    }

    @Test
    fun getSaleById_returnsCorrectSale() = runTest {
        val initialSale = viewModel.allSales.value.first()
        val foundSale = viewModel.getSaleById(initialSale.id)
        assertNotNull(foundSale)
        assertEquals(initialSale.id, foundSale?.id)

        assertNull(viewModel.getSaleById(null))
        assertNull(viewModel.getSaleById("invalid_id"))
    }

    @Test
    fun deleteSale_removesSaleFromRepository() = runTest {
        val initialSale = viewModel.allSales.value.first()
        viewModel.deleteSale(initialSale.id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.getSaleById(initialSale.id))
    }

    @Test
    fun filteredSales_isSortedByDateDescending() = runTest {
        val now = System.currentTimeMillis()
        val saleOld = Sale(
            id = "SALE-OLD",
            customerId = "c1",
            customerName = "Old",
            invoiceType = InvoiceType.TICKET,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 10.0,
            dateMillis = now - 100000
        )
        val saleNew = Sale(
            id = "SALE-NEW",
            customerId = "c1",
            customerName = "New",
            invoiceType = InvoiceType.TICKET,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 20.0,
            dateMillis = now
        )
        repository.addSale(saleOld)
        repository.addSale(saleNew)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = viewModel.filteredSales.value
        val newIndex = list.indexOfFirst { it.id == "SALE-NEW" }
        val oldIndex = list.indexOfFirst { it.id == "SALE-OLD" }
        assertTrue(newIndex != -1 && oldIndex != -1)
        assertTrue(newIndex < oldIndex)
    }

    @Test
    fun voidSaleDte_voidsSaleInViewModel() = runTest {
        val sale = Sale(
            id = "SALE-VOID-TEST",
            customerId = "c1",
            customerName = "Test Client",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 20.0,
            isDteIssued = true
        )
        repository.addSale(sale)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.voidSaleDte("SALE-VOID-TEST", "Anulación por error de digitación")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.getSaleById("SALE-VOID-TEST")
        assertNotNull(updated)
        assertTrue(updated!!.isVoided)
        assertEquals("Anulación por error de digitación", updated.voidReason)
    }
}
